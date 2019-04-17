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
package org.openmuc.framework.driver.smartmeter.sml;

import java.io.IOException;
import java.io.InterruptedIOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.openmuc.framework.data.DoubleValue;
import org.openmuc.framework.data.Flag;
import org.openmuc.framework.data.Record;
import org.openmuc.framework.data.StringValue;
import org.openmuc.framework.data.Value;
import org.openmuc.framework.driver.smartmeter.SmartMeterDriver;
import org.openmuc.framework.driver.spi.ChannelRecordContainer;
import org.openmuc.framework.driver.spi.Connection;
import org.openmuc.framework.driver.spi.RecordsReceivedListener;
import org.openmuc.jrxtx.SerialPort;
import org.openmuc.jsml.structures.ASNObject;
import org.openmuc.jsml.structures.EMessageBody;
import org.openmuc.jsml.structures.Integer16;
import org.openmuc.jsml.structures.Integer32;
import org.openmuc.jsml.structures.Integer64;
import org.openmuc.jsml.structures.Integer8;
import org.openmuc.jsml.structures.OctetString;
import org.openmuc.jsml.structures.SmlFile;
import org.openmuc.jsml.structures.SmlListEntry;
import org.openmuc.jsml.structures.SmlMessage;
import org.openmuc.jsml.structures.Unsigned16;
import org.openmuc.jsml.structures.Unsigned32;
import org.openmuc.jsml.structures.Unsigned64;
import org.openmuc.jsml.structures.Unsigned8;
import org.openmuc.jsml.structures.responses.SmlGetListRes;
import org.openmuc.jsml.transport.SerialReceiver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SmlListener implements Runnable {
    private static Logger logger = LoggerFactory.getLogger(SmlListener.class);

    private Connection context;
    private RecordsReceivedListener listener;
    private List<ChannelRecordContainer> containers;

    private final SerialPort serialPort;
    private final SerialReceiver receiver;

    private volatile boolean running = true;

    protected long entryTime = -1;
    protected List<SmlListEntry> entries = Collections.synchronizedList(new ArrayList<SmlListEntry>());

    public SmlListener(Connection context, SerialPort serialPort, int baudRate) throws IOException {
        this.context = context;
        this.serialPort = serialPort;
        this.receiver = new SerialReceiver(serialPort);
    }

    public synchronized void register(RecordsReceivedListener listener, List<ChannelRecordContainer> containers) {
        this.containers = containers;
        this.listener = listener;
    }

    public synchronized void deregister() {
        containers = null;
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
            	entryTime = System.currentTimeMillis();
                entries.clear();
                entries.addAll(readEntries());
                if (listener != null) {
                	listener.newRecords(parseEntries(containers));
                }
            } catch (InterruptedIOException e) {
            } catch (IOException e) {
                listener.connectionInterrupted(SmartMeterDriver.info.getId(), context);
            }
        }
    }

    public synchronized List<SmlListEntry> readEntries() throws IOException, InterruptedIOException {
        SmlFile smlFile = receiver.getSMLFile();
        
        List<SmlMessage> messages = smlFile.getMessages();
        for (SmlMessage message : messages) {
            EMessageBody tag = message.getMessageBody().getTag();
            
            if (tag != EMessageBody.GET_LIST_RESPONSE) {
                continue;
            }
            SmlGetListRes getListResult = (SmlGetListRes) message.getMessageBody().getChoice();
            SmlListEntry[] smlListEntries = getListResult.getValList().getValListEntry();
            
            return new ArrayList<SmlListEntry>(Arrays.asList(smlListEntries));
        }
        throw new IOException("Error while reading SML message");
    }

    public synchronized List<ChannelRecordContainer> parseEntries(List<ChannelRecordContainer> containers) {
        for (ChannelRecordContainer container : containers) {
        	if (entryTime < 0) {
                container.setRecord(new Record(Flag.NO_VALUE_RECEIVED_YET));
        		continue;
        	}
            for (SmlListEntry entry : entries) {
                String address = new String(entry.getObjName().getValue(), Charset.forName("US-ASCII"));
                if (address.equals(container.getChannelAddress())) {
                	Value value = parseEntry(entry);
                	if (value == null) {
                        container.setRecord(new Record(Flag.DRIVER_ERROR_READ_FAILURE));
                	}
                	else {
                    	container.setRecord(new Record(value, entryTime));
                	}
                	break;
                }
            }
        }
        return containers;
    }

    protected static Value parseEntry(SmlListEntry entry) {
        double value = 0;
        ASNObject obj = entry.getValue().getChoice();
        if (obj.getClass().equals(Integer64.class)) {
            Integer64 val = (Integer64) obj;
            value = val.getVal();
        }
        else if (obj.getClass().equals(Integer32.class)) {
            Integer32 val = (Integer32) obj;
            value = val.getVal();
        }
        else if (obj.getClass().equals(Integer16.class)) {
            Integer16 val = (Integer16) obj;
            value = val.getVal();
        }
        else if (obj.getClass().equals(Integer8.class)) {
            Integer8 val = (Integer8) obj;
            value = val.getVal();
        }
        else if (obj.getClass().equals(Unsigned64.class)) {
            Unsigned64 val = (Unsigned64) obj;
            value = val.getVal();
        }
        else if (obj.getClass().equals(Unsigned32.class)) {
            Unsigned32 val = (Unsigned32) obj;
            value = val.getVal();
        }
        else if (obj.getClass().equals(Unsigned16.class)) {
            Unsigned16 val = (Unsigned16) obj;
            value = val.getVal();
        }
        else if (obj.getClass().equals(Unsigned8.class)) {
            Unsigned8 val = (Unsigned8) obj;
            value = val.getVal();
        }
        else if (obj.getClass().equals(OctetString.class)) {
            OctetString val = (OctetString) obj;
            return new StringValue(new String(val.getValue()));
        }
        else {
        	return null;
        }
        byte scaler = entry.getScaler().getVal();
        double scaledValue = value * Math.pow(10, scaler);
        
        return new DoubleValue(scaledValue);
    }

}
