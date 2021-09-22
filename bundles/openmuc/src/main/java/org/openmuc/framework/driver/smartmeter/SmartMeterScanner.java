/*
 * Copyright 2016-20 ISC Konstanz
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
import org.openmuc.framework.config.ScanException;
import org.openmuc.framework.config.ScanInterruptedException;
import org.openmuc.framework.config.option.annotation.Option;
import org.openmuc.framework.driver.DriverDeviceScanner;
import org.openmuc.framework.driver.smartmeter.sml.SmlListener;
import org.openmuc.framework.driver.spi.DriverDeviceScanListener;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SmartMeterScanner extends DriverDeviceScanner {
    private static final Logger logger = LoggerFactory.getLogger(SmartMeterScanner.class);

    @Option(name = "Protocol mode",
            description = "The protocol mode to use.<br>" + 
                    "IEC 62056-21 mode A, B and C polls data from the meter, while mode D and " + 
                    "SML listens for pushed data by the meter.",
            valueSelection = "SML:Smart Meter Language,ABC:IEC 62056-21 Mode A&C,D:IEC 62056-21 Mode D",
            mandatory = true)
    private ProtocolMode mode;

    @Option(name = "Baud Rate (initial)",
            description = "The baud rate for the serial communication.<br>" + 
                    "Defaults are 300 baud for modes A, B and C, 2400 baud for Mode D and 9600 baud for SML.",
            valueSelection = "-1:Default,300:300,1200:1200,2400:2400,4800:4800,9600:9600,19200:19200," +
                    "38400:38400,57600:57600,115200:115200,230400:230400,460800:460800,921600:921600",
            valueDefault = "-1",
            mandatory = false)
    private int baudRate = -1;

    @Option(name = "Timeout",
            description = "The timeout, after which the blocking call to read from the serial port will be canceled.<br><br>" + 
                    "<i>Only used for IEC 62056-21 mode A, B and C.</i>",
            valueDefault = "2000",
            mandatory = false)
    private int timeout = 2000;

    private volatile boolean interrupt = false;

    @Override
    public void scan(DriverDeviceScanListener listener) 
            throws ArgumentSyntaxException, ScanException, ScanInterruptedException {
        
        interrupt = false;
        
        int progress = 1;
        String[] serialPortNames = SerialPortBuilder.getSerialPortNames();
        for (String serialPortName : serialPortNames) {
            if (interrupt) {
                break;
            }
            logger.debug("Scanning for device at port: {}", serialPortName);
            
            String description = "";
            try {
                switch(mode) {
                case SML:
                    description = scanForSML(serialPortName);
                    break;
                case ABC:
                    description = scanForModeABC(serialPortName);
                    break;
                case D:
                    description = scanForModeD(serialPortName);
                    break;
                }
            } catch (Exception e) {
                logger.debug("No device found at port: {}", serialPortName);
                continue;
            }
            
            StringBuilder deviceAddress = new StringBuilder();
            deviceAddress.append(SmartMeterDevice.SERIAL_PORT).append(':').append(serialPortName);
            
            StringBuilder deviceSettings = new StringBuilder();
            deviceSettings.append(SmartMeterDevice.MODE).append(':').append(mode.name());
            
            if (baudRate > 0) {
                deviceSettings.append(SmartMeterDevice.BAUD_RATE).append(':').append(baudRate);
            }
            if (timeout != SmartMeterDevice.TIMEOUT_DEFAULT) {
                if (deviceSettings.length() > 0) {
                    deviceSettings.append(',');
                }
                deviceSettings.append(SmartMeterDevice.TIMEOUT).append(':').append(timeout);
            }
            listener.deviceFound(new DeviceScanInfo(deviceAddress.toString().trim(), deviceSettings.toString().trim(), description));
            
            listener.scanProgressUpdate((int) Math.round(progress/(double) serialPortName.length()*100));
            progress++;
        }
    }

    @Override
    public void interrupt() throws UnsupportedOperationException {
        interrupt = true;
    }

    public String scanForModeABC(String serialPortName) throws IOException {
        String result = "IEC 62056-21 device";
        
        Iec62056 connection = null;
        try {
            connection = Iec62056Builder.create(serialPortName)
                    .setBaudRate(baudRate)
                    .setTimeout(timeout)
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

    public String scanForModeD(String serialPortName) throws IOException {
        String result = "IEC 62056-21 device";
        
        int baudRate = this.baudRate;
        if (baudRate < 0) {
            baudRate = 2400;
        }
        SerialPortBuilder serialPortBuilder = SerialPortBuilder.newBuilder(serialPortName);
        serialPortBuilder.setBaudRate(baudRate)
            .setDataBits(DataBits.DATABITS_7)
            .setStopBits(StopBits.STOPBITS_1)
            .setParity(Parity.EVEN);
        
        SerialPort serialPort = serialPortBuilder.build();
        serialPort.setSerialPortTimeout(timeout);
        
        DataMessage dataMessage = new ModeDReceiver(null, serialPort).read();
        List<DataSet> dataSets = dataMessage.getDataSets();
        if (dataSets != null) {
            result += " "+dataSets.get(0).getAddress().replaceAll("\\p{Cntrl}", "");
        }
        return result;
    }

    public String scanForSML(String serialPortName) throws IOException {
        String result = "Smart Meter Language device";
        
        int baudRate = this.baudRate;
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

}
