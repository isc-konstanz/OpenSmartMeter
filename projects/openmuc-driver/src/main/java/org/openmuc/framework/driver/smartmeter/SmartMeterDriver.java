/*
 * Copyright 2016-19 ISC Konstanz
 *
 * This file is part of OpenSmartMeter.
 * For more information visit https://github.com/isc-konstanz/OpenSmartMeter.
 *
 * OpenSmartMeter is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * OpenSmartMeter is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with OpenSmartMeter.  If not, see <http://www.gnu.org/licenses/>.
 *
 */
package org.openmuc.framework.driver.smartmeter;

import java.io.IOException;
import java.util.List;

import org.openmuc.framework.config.ArgumentSyntaxException;
import org.openmuc.framework.config.DeviceScanInfo;
import org.openmuc.framework.config.DriverInfo;
import org.openmuc.framework.config.DriverInfoFactory;
import org.openmuc.framework.config.ScanException;
import org.openmuc.framework.config.ScanInterruptedException;
import org.openmuc.framework.driver.smartmeter.iec.ModeAbcConnection;
import org.openmuc.framework.driver.smartmeter.iec.ModeDConnection;
import org.openmuc.framework.driver.smartmeter.settings.DeviceAddress;
import org.openmuc.framework.driver.smartmeter.settings.DeviceScanSettings;
import org.openmuc.framework.driver.smartmeter.settings.DeviceSettings;
import org.openmuc.framework.driver.smartmeter.settings.ProtocolMode;
import org.openmuc.framework.driver.smartmeter.sml.SmlConnection;
import org.openmuc.framework.driver.smartmeter.sml.SmlListener;
import org.openmuc.framework.driver.spi.Connection;
import org.openmuc.framework.driver.spi.ConnectionException;
import org.openmuc.framework.driver.spi.DriverDeviceScanListener;
import org.openmuc.framework.driver.spi.DriverService;
import org.openmuc.iec62056.Iec62056;
import org.openmuc.iec62056.Iec62056Builder;
import org.openmuc.iec62056.ModeDReceiver;
import org.openmuc.iec62056.data.DataMessage;
import org.openmuc.iec62056.data.DataSet;
import org.openmuc.jrxtx.DataBits;
import org.openmuc.jrxtx.FlowControl;
import org.openmuc.jrxtx.Parity;
import org.openmuc.jrxtx.SerialPort;
import org.openmuc.jrxtx.SerialPortBuilder;
import org.openmuc.jrxtx.StopBits;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component
public final class SmartMeterDriver implements DriverService {
    private final static Logger logger = LoggerFactory.getLogger(SmartMeterDriver.class);

    public final static DriverInfo info = DriverInfoFactory.getPreferences(SmartMeterDriver.class);

    private volatile boolean scanForDevicesInterrupted = false;

    public SmartMeterDriver() {
        logger.debug("IEC 62056 part 21 Driver instantiated. Expecting rxtxserial.so in: " + 
                System.getProperty("java.library.path") + " for serial connections.");
    }

    @Override
    public DriverInfo getInfo() {
        return info;
    }

    @Override
    public void scanForDevices(String settingsStr, DriverDeviceScanListener listener)
            throws UnsupportedOperationException, ArgumentSyntaxException, ScanException, ScanInterruptedException {
        scanForDevicesInterrupted = false;
        
        DeviceScanSettings settings = info.parse(settingsStr, DeviceScanSettings.class);
        ProtocolMode mode = settings.getMode();
        
        int progress = 1;
        String[] serialPortNames = SerialPortBuilder.getSerialPortNames();
        for (String serialPortName : serialPortNames) {
            if (scanForDevicesInterrupted) {
                break;
            }
            logger.debug("Scanning for device at port: {}", serialPortName);
            
            String description;
            try {
                switch(mode) {
                case SML:
                    description = scanForSML(serialPortName, settings);
                    break;
                case ABC:
                    description = scanForModeABC(serialPortName, settings);
                    break;
                case D:
                    description = scanForModeD(serialPortName, settings);
                    break;
                default:
                	throw new ScanException("Scan settings invalid: "+settingsStr);
                }
            } catch (Exception e) {
                logger.debug("No device found at port: {}", serialPortName);
                continue;
            }
            
            StringBuilder deviceAddress = new StringBuilder();
            deviceAddress.append(DeviceAddress.SERIAL_PORT_KEY).append(':').append(serialPortName);
            
            StringBuilder deviceSettings = new StringBuilder();
            deviceSettings.append(DeviceSettings.MODE_KEY).append(':').append(mode.name());
            
            if (settings.getBaudRate() > 0) {
                deviceSettings.append(DeviceSettings.BAUD_RATE_KEY).append(':').append(settings.getBaudRate());
            }
            if (settings.getTimeout() != DeviceSettings.TIMEOUT_DEFAULT) {
                if (deviceSettings.length() > 0) {
                    deviceSettings.append(',');
                }
                deviceSettings.append(DeviceSettings.TIMEOUT_KEY).append(':').append(settings.getTimeout());
            }
            listener.deviceFound(new DeviceScanInfo(deviceAddress.toString().trim(), deviceSettings.toString().trim(), description));
            
            listener.scanProgressUpdate((int) Math.round(progress/(double) serialPortName.length()*100));
            progress++;
        }
    }

    public String scanForModeABC(String serialPortName, DeviceScanSettings settings) throws IOException {
    	String result = "IEC 62056-21 device";
    	
        Iec62056 connection = null;
        try {
            connection = Iec62056Builder.create(serialPortName)
                    .setBaudRate(settings.getBaudRate())
                    .setTimeout(settings.getTimeout())
                    .build();
            
            DataMessage dataMessage = connection.read();
            List<DataSet> dataSets = dataMessage.getDataSets();
            if (dataSets != null) {
            	result += " "+dataSets.get(0).getAddress().replaceAll("\\p{Cntrl}", "");
            }
            
        } finally {
            if (connection != null) {
                connection.close();
            }
        }
        return result;
    }

    public String scanForModeD(String serialPortName, DeviceScanSettings settings) throws IOException {
    	String result = "IEC 62056-21 device";
    	
        int baudRate = settings.getBaudRate();
        if (baudRate < 0) {
            baudRate = 2400;
        }
        SerialPortBuilder serialPortBuilder = SerialPortBuilder.newBuilder(serialPortName);
        serialPortBuilder.setBaudRate(baudRate)
	        .setDataBits(DataBits.DATABITS_7)
	        .setStopBits(StopBits.STOPBITS_1)
	        .setParity(Parity.EVEN);
        
        SerialPort serialPort = serialPortBuilder.build();
        serialPort.setSerialPortTimeout(settings.getTimeout());
        
        DataMessage dataMessage = new ModeDReceiver(null, serialPort).read();
        List<DataSet> dataSets = dataMessage.getDataSets();
        if (dataSets != null) {
        	result += " "+dataSets.get(0).getAddress().replaceAll("\\p{Cntrl}", "");
        }
        return result;
    }

    public String scanForSML(String serialPortName, DeviceScanSettings settings) throws IOException {
    	String result = "Smart Meter Language device";
    	
        int baudRate = settings.getBaudRate();
        if (baudRate < 0) {
            baudRate = 9600;
        }
        SmlListener listener = null;
        try {
            SerialPortBuilder serialPortBuilder = SerialPortBuilder.newBuilder(serialPortName);
            serialPortBuilder.setBaudRate(baudRate)
                    .setDataBits(DataBits.DATABITS_8)
                    .setStopBits(StopBits.STOPBITS_1)
                    .setParity(Parity.NONE)
                    .setFlowControl(FlowControl.RTS_CTS);
            
            listener = new SmlListener(null, serialPortBuilder.build(), baudRate);
            listener.readEntries();
            
        } finally {
            if (listener != null) {
                listener.stop();
            }
        }
        return result;
    }

    @Override
    public void interruptDeviceScan() throws UnsupportedOperationException {
        throw new UnsupportedOperationException();
    }

    @Override
    public synchronized Connection connect(String addressStr, String settingsStr)
            throws ArgumentSyntaxException, ConnectionException {
        
        logger.debug("Connect IEC 62056 device address \"{}\": {}", addressStr, settingsStr);
        DeviceAddress address = info.parse(addressStr, DeviceAddress.class);
        DeviceSettings settings = info.parse(settingsStr, DeviceSettings.class);
        try {
            switch(settings.getMode()) {
            case SML:
                return new SmlConnection(address, settings);
            case ABC:
                return new ModeAbcConnection(address, settings);
            case D:
                return new ModeDConnection(address, settings);
            default:
                throw new ConnectionException("Smart Meter Settings invalid: "+settingsStr);
            }
        } catch (Exception e) {
            throw new ConnectionException(e.getMessage());
        }
    }

}
