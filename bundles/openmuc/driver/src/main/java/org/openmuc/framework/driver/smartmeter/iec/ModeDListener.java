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
package org.openmuc.framework.driver.smartmeter.iec;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.openmuc.framework.data.DoubleValue;
import org.openmuc.framework.data.Flag;
import org.openmuc.framework.data.Record;
import org.openmuc.framework.data.StringValue;
import org.openmuc.framework.driver.smartmeter.SmartMeterDriver;
import org.openmuc.framework.driver.spi.ChannelRecordContainer;
import org.openmuc.framework.driver.spi.Connection;
import org.openmuc.framework.driver.spi.RecordsReceivedListener;
import org.openmuc.iec62056.data.DataMessage;
import org.openmuc.iec62056.data.DataSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ModeDListener implements org.openmuc.iec62056.ModeDListener {
    private final static Logger logger = LoggerFactory.getLogger(ModeDListener.class);

    private final Connection context;

    private RecordsReceivedListener listener;
    private List<ChannelRecordContainer> containers;

    protected long dataSetTime = -1;
    protected List<DataSet> dataSets = Collections.synchronizedList(new ArrayList<DataSet>());

    protected ModeDListener() {
    	this(null);
    }

    public ModeDListener(Connection context) {
    	this.context = context;
    }

    public synchronized void register(RecordsReceivedListener listener, List<ChannelRecordContainer> containers) {
        this.containers = containers;
        this.listener = listener;
    }

    public synchronized void deregister() {
        containers = null;
        listener = null;
    }

    @Override
    public synchronized void newDataMessage(DataMessage dataMessage) {
        dataSetTime = System.currentTimeMillis();
        dataSets.clear();
        dataSets.addAll(dataMessage.getDataSets());
        
        logger.debug("Received data message: {}", dataMessage.toString());
        if (listener != null) {
        	listener.newRecords(parseDataSets(containers));
        }
    }

    protected synchronized List<ChannelRecordContainer> parseDataSets(List<ChannelRecordContainer> containers) {
        for (ChannelRecordContainer container : containers) {
        	if (dataSetTime < 0) {
                container.setRecord(new Record(Flag.NO_VALUE_RECEIVED_YET));
        		continue;
        	}
            for (DataSet dataSet : dataSets) {
                if (dataSet.getAddress().equals(container.getChannelAddress())) {
                    String value = dataSet.getValue();
                	if (value != null) {
                        try {
                            container.setRecord(new Record(new DoubleValue(Double.parseDouble(dataSet.getValue())), dataSetTime));
                        
                        } catch (NumberFormatException e) {
                            container.setRecord(new Record(new StringValue(dataSet.getValue()), dataSetTime));
                        }
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
        	listener.connectionInterrupted(SmartMeterDriver.info.getId(), context);
    	}
    }

}
