/*
 * Copyright 2016-18 ISC Konstanz
 *
 * This file is part of OpenIEC62056-21.
 * For more information visit https://github.com/isc-konstanz/OpenIEC62056-21.
 *
 * OpenIEC62056-21 is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * OpenIEC62056-21 is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with OpenIEC62056-21.  If not, see <http://www.gnu.org/licenses/>.
 *
 */
package org.openmuc.framework.driver.iec62056_21;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeoutException;

import org.openmuc.framework.config.ArgumentSyntaxException;
import org.openmuc.framework.config.DeviceScanInfo;
import org.openmuc.framework.config.DriverInfo;
import org.openmuc.framework.config.DriverInfoFactory;
import org.openmuc.framework.config.ScanException;
import org.openmuc.framework.config.ScanInterruptedException;
import org.openmuc.framework.driver.iec62056_21.settings.DeviceAddress;
import org.openmuc.framework.driver.iec62056_21.settings.DeviceScanSettings;
import org.openmuc.framework.driver.iec62056_21.settings.DeviceSettings;
import org.openmuc.framework.driver.spi.Connection;
import org.openmuc.framework.driver.spi.ConnectionException;
import org.openmuc.framework.driver.spi.DriverDeviceScanListener;
import org.openmuc.framework.driver.spi.DriverService;
import org.openmuc.iec62056_21.DataSet;
import org.openmuc.iec62056_21.Iec62056Settings;
import org.openmuc.iec62056_21.serial.SerialSettings;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component
public final class Iec62056Driver implements DriverService {
    private final static Logger logger = LoggerFactory.getLogger(Iec62056Driver.class);

    private final DriverInfo info = DriverInfoFactory.getPreferences(Iec62056Driver.class);

    private final Map<String, Iec62056Connection> connections = new HashMap<String, Iec62056Connection>();
    
    public Iec62056Driver() {
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
        
        SerialSettings serialSettings = new SerialSettings(settings.getSerialPort(), 
        		settings.getBaudrate(), settings.getDatabits(), settings.getStopbits(), settings.getParity());
        
        Iec62056Settings connectionSettings = new Iec62056Settings(serialSettings)
        		.setTimeout(settings.getTimeout());
        
        Iec62056Connection connection;
        synchronized(connections) {
            if (connections.containsKey(serialSettings.getPort())) {
                connection = connections.get(serialSettings.getPort());
            }
            else {
                connection = new Iec62056Connection(connectionSettings);
                connections.put(serialSettings.getPort(), connection);
            }
        }

        logger.debug("Scanning for devices at {}", serialSettings.getPort());
        synchronized(connection) {
            try {
                if (connection.open()) {
                    Integer timeout = settings.getTimeout();
                    if (timeout != null && timeout != connection.getTimeout()) {
                        connection.setTimeout(timeout);
                    }
                    
                    List<DataSet> dataSets = connection.read(connectionSettings);
                    
                    listener.deviceFound(new DeviceScanInfo("", settingsStr,
                            dataSets.get(0).getId().replaceAll("\\p{Cntrl}", "")));
                }
            } catch (IOException e) {
                logger.debug("Scanning channels at {} failed: {}", serialSettings.getPort(), e.getMessage());

                throw new ScanException(e);
            } catch (TimeoutException e) {
                logger.debug("Timeout while scanning channels at {} failed: {}", serialSettings.getPort(), e.getMessage());
                throw new ScanException(e);
            } finally {
                connection.close();
            }
        }
    }

    @Override
    public void interruptDeviceScan() throws UnsupportedOperationException {
        throw new UnsupportedOperationException();
    }

    @Override
    public Connection connect(String addressStr, String settingsStr)
            throws ArgumentSyntaxException, ConnectionException {

        logger.trace("Connect Raspberry Pi device address \"{}\": {}", addressStr, settingsStr);
        DeviceAddress address = info.parse(addressStr, DeviceAddress.class);
        DeviceSettings settings = info.parse(settingsStr, DeviceSettings.class);
        
        SerialSettings serialSettings = new SerialSettings(address.getSerialPort(), 
        		settings.getBaudrate(), settings.getDatabits(), settings.getStopbits(), settings.getParity());
        
        Iec62056Settings connectionSettings = new Iec62056Settings(serialSettings, address.getAddress())
        		.setPassword(settings.getPassword())
        		.setTimeout(settings.getTimeout())
        		.setVerification(settings.hasVerification())
        		.setEchoHandling(settings.hasEchoHandling())
        		.setHandshake(settings.hasHandshake())
        		.setBaudrateChangeDelay(settings.getBaudrateChangeDelay())
        		.setBaudrateMaximum(settings.getBaudrateMaximum());
        
        Iec62056Connection connection;
        synchronized(connections) {
            if (connections.containsKey(serialSettings.getPort())) {
                connection = connections.get(serialSettings.getPort());
            }
            else {
                connection = new Iec62056Connection(connectionSettings);
                connections.put(serialSettings.getPort(), connection);
            }
        }
        
        synchronized(connection) {
            try {
                connection.open();
                
                if (settings.hasVerification()) {
                    connection.read(connectionSettings);
                }
            } catch (IOException | TimeoutException e) {
            	connection.close();
            	
                throw new ConnectionException("Failed to open local serial port \"" +serialSettings.getPort() + "\": " + e.getMessage(), e);
            }
            logger.debug("Connected to device \"{}\" at {}", address.getAddress(), serialSettings.getPort());
        }
        return connection;
    }

}
