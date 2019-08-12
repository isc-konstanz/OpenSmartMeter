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
import java.nio.charset.Charset;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.openmuc.framework.config.ChannelScanInfo;
import org.openmuc.framework.config.ScanException;
import org.openmuc.framework.data.ByteArrayValue;
import org.openmuc.framework.data.StringValue;
import org.openmuc.framework.data.Value;
import org.openmuc.framework.data.ValueType;
import org.openmuc.framework.driver.smartmeter.settings.DeviceAddress;
import org.openmuc.framework.driver.smartmeter.settings.DeviceSettings;
import org.openmuc.framework.driver.spi.ChannelRecordContainer;
import org.openmuc.framework.driver.spi.ChannelValueContainer;
import org.openmuc.framework.driver.spi.Connection;
import org.openmuc.framework.driver.spi.ConnectionException;
import org.openmuc.framework.driver.spi.RecordsReceivedListener;
import org.openmuc.jrxtx.DataBits;
import org.openmuc.jrxtx.FlowControl;
import org.openmuc.jrxtx.Parity;
import org.openmuc.jrxtx.SerialPortBuilder;
import org.openmuc.jrxtx.StopBits;
import org.openmuc.jsml.structures.SmlListEntry;

public class SmlConnection implements Connection {

    private final ExecutorService executor;

    private SmlListener listener;

    public SmlConnection(DeviceAddress address, DeviceSettings settings) 
            throws ConnectionException, IOException {
        
        executor = Executors.newSingleThreadExecutor();
        
    	int baudRate = settings.getBaudRate();
        if (baudRate < 0) {
            baudRate = 9600;
        }
        SerialPortBuilder serialPortBuilder = SerialPortBuilder.newBuilder(address.getSerialPort());
        serialPortBuilder.setBaudRate(baudRate)
                .setDataBits(DataBits.DATABITS_8)
                .setStopBits(StopBits.STOPBITS_1)
                .setParity(Parity.NONE)
                .setFlowControl(FlowControl.RTS_CTS);
        
        listener = new SmlListener(this, serialPortBuilder.build(), baudRate);
    }

    @Override
    public List<ChannelScanInfo> scanForChannels(String settings)
            throws UnsupportedOperationException, ScanException, ConnectionException {
        
        List<ChannelScanInfo> channels = new LinkedList<ChannelScanInfo>();
        for (SmlListEntry entry : listener.entries) {
            String address = new String(entry.getObjName().getValue(), Charset.forName("US-ASCII"));
            
            String description = address+" ["+entry.getUnit()+"]";
            
            Value value = SmlListener.parseEntry(entry);
            ValueType valueType;
            Integer valueTypeLength = null;
        	if (value instanceof StringValue) {
        		valueType = ValueType.STRING;
        		valueTypeLength = value.asString().length();
            }
        	else if (value instanceof ByteArrayValue) {
                byte[] byteValue = value.asByteArray();
                valueTypeLength = byteValue.length;
        		valueType = ValueType.BYTE_ARRAY;
        	}
        	else {
        		valueType = ValueType.DOUBLE;
        	}
            
            boolean readable = true;
            boolean writable = false;
            channels.add(new ChannelScanInfo(address, description, valueType, valueTypeLength, readable, writable));
        }
        return channels;
    }

    @Override
    public void disconnect() {
        if (listener != null) {
            listener.stop();
            listener = null;
        }
        executor.shutdown();
    }

    @Override
    public void startListening(final List<ChannelRecordContainer> containers, final RecordsReceivedListener listener)
            throws ConnectionException {
        
        this.listener.register(listener, containers);
    }

    @Override
    public Object read(List<ChannelRecordContainer> containers, Object containerListHandle, String samplingGroup)
            throws UnsupportedOperationException, ConnectionException {
        
    	listener.parseEntries(containers);
    	return null;
    }

    @Override
    public Object write(List<ChannelValueContainer> containers, Object containerListHandle)
            throws UnsupportedOperationException, ConnectionException {
        throw new UnsupportedOperationException();
    }

}
