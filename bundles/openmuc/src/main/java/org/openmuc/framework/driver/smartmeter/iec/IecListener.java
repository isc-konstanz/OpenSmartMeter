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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.openmuc.framework.data.DoubleValue;
import org.openmuc.framework.data.Flag;
import org.openmuc.framework.data.Record;
import org.openmuc.framework.data.StringValue;
import org.openmuc.framework.driver.smartmeter.ObisChannel;
import org.openmuc.framework.driver.smartmeter.SmartMeterDriver;
import org.openmuc.framework.driver.spi.ChannelRecordContainer;
import org.openmuc.framework.driver.spi.Connection;
import org.openmuc.framework.driver.spi.RecordsReceivedListener;
import org.openmuc.iec62056.data.DataMessage;
import org.openmuc.iec62056.data.DataSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class IecListener implements org.openmuc.iec62056.ModeDListener {
    private final static Logger logger = LoggerFactory.getLogger(IecListener.class);

    private final Connection connection;

    private RecordsReceivedListener listener;
    private List<ObisChannel> channels;

    protected long dataSetTime = -1;
    protected List<DataSet> dataSets = Collections.synchronizedList(new ArrayList<DataSet>());

    public IecListener(Connection connection) {
    	this.connection = connection;
    }

    public synchronized void register(RecordsReceivedListener listener, List<ObisChannel> channels) {
        this.channels = channels;
        this.listener = listener;
    }

    public synchronized void deregister() {
        channels = null;
        listener = null;
    }

    @Override
    public synchronized void newDataMessage(DataMessage dataMessage) {
        dataSetTime = System.currentTimeMillis();
        dataSets.clear();
        dataSets.addAll(dataMessage.getDataSets());
        
        logger.debug("Received data message: {}", dataMessage.toString());
        if (listener != null) {
        	listener.newRecords(parseDataSets(channels));
        }
    }

    protected synchronized List<ChannelRecordContainer> parseDataSets(List<ObisChannel> channels) {
    	List<ChannelRecordContainer> containers = new ArrayList<ChannelRecordContainer>();
    	
        for (ObisChannel channel : channels) {
        	ChannelRecordContainer container = (ChannelRecordContainer) channel.getTaskContainer();
        	
        	if (dataSetTime < 0) {
                channel.setRecord(new Record(Flag.NO_VALUE_RECEIVED_YET));
                containers.add(container);
        		continue;
        	}
            for (DataSet dataSet : dataSets) {
                if (dataSet.getAddress().equals(container.getChannelAddress())) {
                    String value = dataSet.getValue();
                	if (value != null) {
                        try {
                            channel.setRecord(new Record(new DoubleValue(Double.parseDouble(dataSet.getValue())), dataSetTime));
                        
                        } catch (NumberFormatException e) {
                            channel.setRecord(new Record(new StringValue(dataSet.getValue()), dataSetTime));
                        }
                        containers.add(container);
                    }
                    break;
                }
            }
        }
        return containers;
    }

    @Override
    public void exceptionWhileListening(Exception e) {
    	logger.warn("Error while listening: {}", e.getMessage());
    	if (listener != null) {
        	listener.connectionInterrupted(SmartMeterDriver.ID, connection);
    	}
    }

}
