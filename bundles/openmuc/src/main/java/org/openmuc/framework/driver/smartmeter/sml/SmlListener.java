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
package org.openmuc.framework.driver.smartmeter.sml;

import java.io.IOException;
import java.io.InterruptedIOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.openmuc.framework.data.Flag;
import org.openmuc.framework.data.Record;
import org.openmuc.framework.driver.smartmeter.ObisChannel;
import org.openmuc.framework.driver.smartmeter.SmartMeterDriver;
import org.openmuc.framework.driver.spi.ChannelRecordContainer;
import org.openmuc.framework.driver.spi.Connection;
import org.openmuc.framework.driver.spi.RecordsReceivedListener;
import org.openmuc.jrxtx.SerialPort;
import org.openmuc.jsml.structures.EMessageBody;
import org.openmuc.jsml.structures.SmlFile;
import org.openmuc.jsml.structures.SmlListEntry;
import org.openmuc.jsml.structures.SmlMessage;
import org.openmuc.jsml.structures.responses.SmlGetListRes;
import org.openmuc.jsml.transport.SerialReceiver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SmlListener implements Runnable {
    private static Logger logger = LoggerFactory.getLogger(SmlListener.class);

    private Connection context;
    private RecordsReceivedListener listener;
    private List<ObisChannel> channels;

    private final SerialPort serialPort;
    private final SerialReceiver receiver;

    private volatile boolean running = true;

    protected final Map<String, SmlRecord> entries = Collections.synchronizedMap(new HashMap<String, SmlRecord>());

    public SmlListener(Connection context, SerialPort serialPort, int baudRate) throws IOException {
        this.context = context;
        this.serialPort = serialPort;
        this.receiver = new SerialReceiver(serialPort);
    }

    public synchronized void register(RecordsReceivedListener listener, List<ObisChannel> channels) {
        this.channels = channels;
        this.listener = listener;
    }

    public synchronized void deregister() {
        channels = null;
        listener = null;
    }

    public void stop() {
        this.running = false;
        try {
            if (receiver != null) {
                receiver.close();
            }
            if (!serialPort.isClosed()) {
                serialPort.close();
            }
        } catch (IOException e) {
            logger.warn("Error while closing serial port.", e);
        }
    }

    @Override
    public void run() {
        while (this.running) {
            try {
                readEntries();
                if (listener != null) {
                	listener.newRecords(parseEntries(channels));
                }
            } catch (InterruptedIOException e) {
            } catch (IOException e) {
            	logger.warn("Error while reading SML entries: {}", e.getMessage());
                if (listener != null) {
                    listener.connectionInterrupted(SmartMeterDriver.ID, context);
                }
            }
        }
    }

    public synchronized void readEntries() throws IOException, InterruptedIOException {
        SmlFile file = receiver.getSMLFile();
        
    	long time = System.currentTimeMillis();
        List<SmlMessage> messages = file.getMessages();
        for (SmlMessage message : messages) {
            EMessageBody tag = message.getMessageBody().getTag();
            
            if (tag != EMessageBody.GET_LIST_RESPONSE) {
                continue;
            }
            SmlGetListRes result = (SmlGetListRes) message.getMessageBody().getChoice();
            for (SmlListEntry entry : result.getValList().getValListEntry()) {
            	try {
            		SmlRecord record = new SmlRecord(entry, time);
                    entries.put(record.getAddress(), record);
                    
                    if (logger.isDebugEnabled()) {
                        logger.debug("Received record {} for {}", record, entry);
                    }
            	} catch (IllegalArgumentException e) {
            		logger.warn("Received invalid value for entry: {}", SmlRecord.parseFullAddress(entry));
            		//record = new SmlRecord(entry, Flag.DRIVER_ERROR_DECODING_RESPONSE_FAILED);
            	}
            }
        }
    }

    public synchronized List<ChannelRecordContainer> parseEntries(List<ObisChannel> channels) {
    	List<ChannelRecordContainer> containers = new ArrayList<ChannelRecordContainer>();
        for (ObisChannel channel : channels) {
        	Record record = entries.get(channel.getCode());
        	if (record == null) {
                record = new Record(Flag.NO_VALUE_RECEIVED_YET);
        	}
        	channel.setRecord(record);
        	containers.add((ChannelRecordContainer) channel.getTaskContainer());
        }
        return containers;
    }

}
