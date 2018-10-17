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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeoutException;

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
import org.openmuc.iec62056_21.DataSet;
import org.openmuc.iec62056_21.Iec62056Settings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Iec62056Connection extends org.openmuc.iec62056_21.Iec62056Connection implements Connection {
	private final static Logger logger = LoggerFactory.getLogger(Iec62056Connection.class);
	
	private Iec62056Settings settings;

    public Iec62056Connection(Iec62056Settings settings) {
		super(settings.getSerialSettings());
		this.settings = settings;
	}

    @Override
    public List<ChannelScanInfo> scanForChannels(String settings)
            throws UnsupportedOperationException, ScanException, ConnectionException {
    	
        List<DataSet> dataSets;
        try {
            dataSets = read(this.settings);
            
            if (dataSets == null) {
                throw new TimeoutException("No data sets received.");
            }
        } catch (IOException | TimeoutException e) {
            logger.debug("Scanning channels for device failed: " + e);
            throw new ScanException(e);
        }
        
        List<ChannelScanInfo> scanInfos = new ArrayList<>(dataSets.size());
        
        for (DataSet dataSet : dataSets) {
            try {
                Double.parseDouble(dataSet.getValue());
                scanInfos.add(new ChannelScanInfo(dataSet.getId(), "", ValueType.DOUBLE, null));
            } catch (NumberFormatException e) {
                scanInfos.add(new ChannelScanInfo(dataSet.getId(), "", ValueType.STRING, dataSet.getValue().length()));
            }

        }
        return scanInfos;
    }

    @Override
    public Object read(List<ChannelRecordContainer> containers, Object containerListHandle, String samplingGroup)
            throws UnsupportedOperationException, ConnectionException {
    	
        Map<String, ChannelRecordContainer> containersById = new HashMap<String, ChannelRecordContainer>();
        for (ChannelRecordContainer container : containers) {
            containersById.put(container.getChannelAddress(), container);
        }
        
        List<DataSet> dataSets = null;
        try {
            dataSets = read(settings, containersById.keySet());
            if (dataSets == null) {
                throw new TimeoutException("No data sets received.");
            }
            
	        long time = System.currentTimeMillis();
	        for (DataSet dataSet : dataSets) {
	            if (containersById.containsKey(dataSet.getId())) {
	                String value = dataSet.getValue();
	                if (value != null) {
	                    ChannelRecordContainer container = containersById.get(dataSet.getId());
	                    try {
	                        container.setRecord(new Record(new DoubleValue(Double.parseDouble(dataSet.getValue())), time));
	                    } catch (NumberFormatException e) {
	                        container.setRecord(new Record(new StringValue(dataSet.getValue()), time));
	                    }
	                }
	            }
	        }
        } catch (IOException | TimeoutException e) {
            logger.debug("Reading from device failed: " + e);
            
            Flag flag;
            if (e instanceof TimeoutException) {
            	flag = Flag.TIMEOUT;
            }
            else {
            	flag = Flag.DRIVER_ERROR_READ_FAILURE;
            }
            for (ChannelRecordContainer container : containers) {
                container.setRecord(new Record(flag));
            }
            close();
        	
            throw new ConnectionException("Read failed: " + e.getMessage());
        }
        return null;
    }

    @Override
    public void startListening(List<ChannelRecordContainer> containers, RecordsReceivedListener listener)
            throws UnsupportedOperationException, ConnectionException {
        throw new UnsupportedOperationException();
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
