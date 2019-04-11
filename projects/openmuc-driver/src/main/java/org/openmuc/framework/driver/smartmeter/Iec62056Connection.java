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
import org.openmuc.framework.driver.spi.ChannelRecordContainer;
import org.openmuc.framework.driver.spi.ChannelValueContainer;
import org.openmuc.framework.driver.spi.Connection;
import org.openmuc.framework.driver.spi.ConnectionException;
import org.openmuc.framework.driver.spi.RecordsReceivedListener;
import org.openmuc.iec62056.Iec62056;
import org.openmuc.iec62056.data.DataMessage;
import org.openmuc.iec62056.data.DataSet;
import org.openmuc.iec62056.data.Settings;
import org.openmuc.jrxtx.SerialPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Iec62056Connection extends Iec62056 implements Connection {
    private final static Logger logger = LoggerFactory.getLogger(Iec62056Connection.class);

    private int retries;

    public Iec62056Connection(SerialPort serialPort, Settings settings, int retries) 
            throws ConnectionException, IOException {
        super(serialPort, settings);
        this.retries = retries;
        try {
            // FIXME: Sleep to avoid to early read after connection. Meters have some delay.
            Thread.sleep(settings.getTimeout());
            
        } catch (InterruptedException e) {
            logger.debug("Interrupted while waiting for port to open");
        }
    }

    @Override
    public List<ChannelScanInfo> scanForChannels(String settings)
            throws UnsupportedOperationException, ScanException, ConnectionException {
        
        List<DataSet> dataSets;
        DataMessage dataMessage;
        try {
            dataMessage = read();
            
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
            try {
                Double.parseDouble(dataSet.getValue());
                scanInfos.add(new ChannelScanInfo(dataSet.getAddress(), description, ValueType.DOUBLE, null));
            } catch (NumberFormatException e) {
                scanInfos.add(new ChannelScanInfo(dataSet.getAddress(), description, ValueType.STRING, dataSet.getValue().length()));
            }
        }
        return scanInfos;
    }

    @Override
    public Object read(List<ChannelRecordContainer> containers, Object containerListHandle, String samplingGroup)
            throws UnsupportedOperationException, ConnectionException {
        
        List<DataSet> dataSets = null;
        DataMessage dataMessage;
        for (int i = 0; i <= retries; ++i) {
            try {
                if (settings.hasAuthentication()) {
                	List<String> addresses = new ArrayList<String>(containers.size());
                    for (ChannelRecordContainer container : containers) {
                    	addresses.add(container.getChannelAddress());
                    }
                    dataMessage = read(addresses);
                }
                else {
                    dataMessage = read();
                }
                dataSets = dataMessage.getDataSets();
                if (dataSets != null && !dataSets.isEmpty()) {
                    i = retries;
                }
            } catch (IOException e) {
                logger.debug("Reading from device failed: " + e);
                if (i < retries) {
                    continue;
                }
                if (e instanceof InterruptedIOException) {
                    for (ChannelRecordContainer container : containers) {
                        container.setRecord(new Record(Flag.TIMEOUT));
                    }
                    return null;
                }
                for (ChannelRecordContainer container : containers) {
                    container.setRecord(new Record(Flag.DRIVER_ERROR_READ_FAILURE));
                }
                close();
                
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
    public void startListening(List<ChannelRecordContainer> containers, RecordsReceivedListener listener)
            throws UnsupportedOperationException, ConnectionException {
        
        Iec62056Listener iec62056Listener = new Iec62056Listener();
        iec62056Listener.registerOpenMucListener(containers, listener);
        try {
            listen(iec62056Listener);
            
        } catch (IOException e) {
            throw new ConnectionException(e);
        }
    }

    @Override
    public Object write(List<ChannelValueContainer> containers, Object containerListHandle)
            throws UnsupportedOperationException, ConnectionException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void disconnect() {
        close();
    }
}
