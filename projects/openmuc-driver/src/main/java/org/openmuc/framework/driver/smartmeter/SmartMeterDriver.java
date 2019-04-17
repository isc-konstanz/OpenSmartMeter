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
import org.openmuc.framework.driver.smartmeter.sml.SmlConnection;
import org.openmuc.framework.driver.spi.Connection;
import org.openmuc.framework.driver.spi.ConnectionException;
import org.openmuc.framework.driver.spi.DriverDeviceScanListener;
import org.openmuc.framework.driver.spi.DriverService;
import org.openmuc.iec62056.Iec62056;
import org.openmuc.iec62056.Iec62056Builder;
import org.openmuc.iec62056.data.DataMessage;
import org.openmuc.iec62056.data.DataSet;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component
public final class SmartMeterDriver implements DriverService {
    private final static Logger logger = LoggerFactory.getLogger(SmartMeterDriver.class);

    public final static DriverInfo info = DriverInfoFactory.getPreferences(SmartMeterDriver.class);

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
    	
        DeviceScanSettings settings = info.parse(settingsStr, DeviceScanSettings.class);
        Iec62056 connection = null;
        try {
            connection = Iec62056Builder.create(settings.getSerialPort())
            		.setBaudRate(settings.getBaudRate())
            		.setTimeout(settings.getTimeout())
            		.build();
            
            DataMessage dataMessage = connection.read();
            List<DataSet> dataSets = dataMessage.getDataSets();
            
            StringBuilder deviceAddress = new StringBuilder();
            deviceAddress.append(DeviceAddress.SERIAL_PORT_KEY).append(':').append(settings.getSerialPort());
            
            StringBuilder deviceSettings = new StringBuilder();
            if (settings.getBaudRate() > 0) {
                deviceSettings.append(DeviceSettings.BAUD_RATE_KEY).append(':').append(settings.getBaudRate());
            }
            if (settings.getTimeout() != DeviceSettings.TIMEOUT_DEFAULT) {
            	if (deviceSettings.length() > 0) {
            		deviceSettings.append(',');
            	}
                deviceSettings.append(DeviceSettings.TIMEOUT_KEY).append(':').append(settings.getTimeout());
            }
            listener.deviceFound(new DeviceScanInfo(deviceAddress.toString().trim(), deviceSettings.toString().trim(),
                    dataSets.get(0).getAddress().replaceAll("\\p{Cntrl}", "")));
            
        } catch (Exception e) {
            throw new ScanException("Failed to open serial port: " + e.getMessage());
            
		} finally {
        	if (connection != null) {
            	connection.close();
        	}
        }
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
			case ABC:
	        	return new ModeAbcConnection(address, settings);
			case D:
	        	return new ModeDConnection(address, settings);
			case SML:
	        	return new SmlConnection(address, settings);
			default:
				throw new ConnectionException("Smart Meter Settings invalid: "+settingsStr);
        	}
        } catch (Exception e) {
            throw new ConnectionException(e.getMessage());
        }
    }

}
