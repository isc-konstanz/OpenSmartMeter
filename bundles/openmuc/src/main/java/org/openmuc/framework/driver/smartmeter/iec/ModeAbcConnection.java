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
package org.openmuc.framework.driver.smartmeter.iec;

import java.io.IOException;
import java.io.InterruptedIOException;
import java.util.ArrayList;
import java.util.List;

import org.openmuc.framework.config.ChannelScanInfo;
import org.openmuc.framework.config.ScanException;
import org.openmuc.framework.data.DoubleValue;
import org.openmuc.framework.data.Flag;
import org.openmuc.framework.data.Record;
import org.openmuc.framework.data.StringValue;
import org.openmuc.framework.data.ValueType;
import org.openmuc.framework.driver.smartmeter.settings.DeviceAddress;
import org.openmuc.framework.driver.smartmeter.settings.DeviceSettings;
import org.openmuc.framework.driver.spi.ChannelRecordContainer;
import org.openmuc.framework.driver.spi.ChannelValueContainer;
import org.openmuc.framework.driver.spi.Connection;
import org.openmuc.framework.driver.spi.ConnectionException;
import org.openmuc.framework.driver.spi.RecordsReceivedListener;
import org.openmuc.iec62056.Iec62056;
import org.openmuc.iec62056.Iec62056Builder;
import org.openmuc.iec62056.Iec62056Exception;
import org.openmuc.iec62056.data.DataMessage;
import org.openmuc.iec62056.data.DataSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ModeAbcConnection implements Connection {
    private final static Logger logger = LoggerFactory.getLogger(ModeAbcConnection.class);

    private final Iec62056 connection;

    private final int retries;

    public ModeAbcConnection(DeviceAddress address, DeviceSettings settings) 
            throws ConnectionException, IOException {
        
        Iec62056Builder builder = Iec62056Builder.create(address.getSerialPort())
                .setDeviceAddress(address.getAddress())
        		.setPassword(settings.getPassword())
                .setMsgStartChars(settings.getMsgStartChars())
                .enableBaudRateHandshake(settings.hasHandshake())
                .setBaudRateChangeDelay(settings.getBaudRateChangeDelay())
                .setBaudRate(settings.getBaudRate())
				.setTimeout(settings.getTimeout());
        
        connection = builder.build();
        retries = settings.getRetries();
        try {
            // FIXME: Sleep to avoid to early read after connection. Meters have some delay.
            Thread.sleep(settings.getTimeout());
            
        } catch (InterruptedException e) {
            logger.debug("Interrupted while waiting for port to open");
        }
    }

    @Override
    public void disconnect() {
    	connection.close();
    }

    @Override
    public List<ChannelScanInfo> scanForChannels(String settings)
            throws UnsupportedOperationException, ScanException, ConnectionException {
        
        List<DataSet> dataSets;
        DataMessage dataMessage;
        try {
            dataMessage = connection.read();
            
        } catch (IOException e) {
            throw new ScanException(e);
        }
        dataSets = dataMessage.getDataSets();
        if (dataSets == null) {
            throw new ScanException("Scan timed out.");
        }
        
        List<ChannelScanInfo> scanInfos = new ArrayList<>(dataSets.size());
        for (DataSet dataSet : dataSets) {
            String description = dataSet.getAddress()+" ["+dataSet.getUnit()+"]";
            boolean readable = true;
            boolean writable = false;
            try {
                Double.parseDouble(dataSet.getValue());
                scanInfos.add(new ChannelScanInfo(dataSet.getAddress(), description, 
                		ValueType.DOUBLE, null, readable, writable));
                
            } catch (NumberFormatException e) {
                scanInfos.add(new ChannelScanInfo(dataSet.getAddress(), description, 
                		ValueType.STRING, dataSet.getValue().length(), readable, writable));
            }
        }
        return scanInfos;
    }

    @Override
    public void startListening(List<ChannelRecordContainer> containers, RecordsReceivedListener listener)
            throws UnsupportedOperationException, ConnectionException {
        
        ModeDListener modeDListener = new ModeDListener(this);
        modeDListener.register(listener, containers);
        try {
        	connection.listen(modeDListener);
            
        } catch (IOException e) {
            throw new ConnectionException(e);
        }
    }

    @Override
    public Object read(List<ChannelRecordContainer> containers, Object containerListHandle, String samplingGroup)
            throws UnsupportedOperationException, ConnectionException {
        
        List<DataSet> dataSets = null;
        DataMessage dataMessage;
        for (int i = 0; i <= retries; ++i) {
            try {
                if (connection.getSettings().hasAuthentication()) {
                	List<String> addresses = new ArrayList<String>(containers.size());
                    for (ChannelRecordContainer container : containers) {
                    	addresses.add(container.getChannelAddress());
                    }
                    dataMessage = connection.read(addresses);
                }
                else {
                    dataMessage = connection.read();
                }
                dataSets = dataMessage.getDataSets();
                if (dataSets != null && !dataSets.isEmpty()) {
                    break;
                }
            } catch (InterruptedIOException | Iec62056Exception e) {
                logger.warn("Reading from device failed: " + e);
                if (i >= retries) {
                	Flag flag = (e instanceof Iec62056Exception) ? Flag.DRIVER_ERROR_READ_FAILURE : Flag.DRIVER_ERROR_TIMEOUT;
                    for (ChannelRecordContainer container : containers) {
                        container.setRecord(new Record(flag));
                    }
                    return null;
                }
            } catch (IOException e) {
                for (ChannelRecordContainer container : containers) {
                    container.setRecord(new Record(Flag.DRIVER_ERROR_READ_FAILURE));
                }
                connection.close();
                throw new ConnectionException("Read failed: " + e.getMessage());
            }
        }
        long time = System.currentTimeMillis();
        for (ChannelRecordContainer container : containers) {
            for (DataSet dataSet : dataSets) {
                if (dataSet.getAddress().equals(container.getChannelAddress())) {
                    String value = dataSet.getValue();
                    if (value != null) {
                        try {
                            container.setRecord(new Record(new DoubleValue(Double.parseDouble(dataSet.getValue())), time));
                            
                        } catch (NumberFormatException e) {
                            container.setRecord(new Record(new StringValue(dataSet.getValue()), time));
                        }
                    }
                    break;
                }
            }
        }
        return null;
    }

    @Override
    public Object write(List<ChannelValueContainer> containers, Object containerListHandle)
            throws UnsupportedOperationException, ConnectionException {
        throw new UnsupportedOperationException();
    }

}
