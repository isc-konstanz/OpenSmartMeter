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

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.openmuc.framework.config.ArgumentSyntaxException;
import org.openmuc.framework.config.ChannelScanInfo;
import org.openmuc.framework.config.ScanException;
import org.openmuc.framework.data.ByteArrayValue;
import org.openmuc.framework.data.StringValue;
import org.openmuc.framework.data.Value;
import org.openmuc.framework.data.ValueType;
import org.openmuc.framework.driver.ChannelScanner;
import org.openmuc.framework.driver.spi.ConnectionException;

public class SmlScanner extends ChannelScanner {

    protected final Map<String, SmlRecord> entries;

    public SmlScanner(Map<String, SmlRecord> entries) {
    	this.entries = entries;
    }

	@Override
	public List<ChannelScanInfo> doScan() throws ArgumentSyntaxException, ScanException, ConnectionException {
        List<ChannelScanInfo> channels = new LinkedList<ChannelScanInfo>();
        for (Entry<String, SmlRecord> entry : entries.entrySet()) {
        	SmlRecord record = entry.getValue();
            String address = entry.getKey();
            String description = record.getFullAddress()+" ["+record.getEntry().getUnit()+"]";
            
            Value value = record.getValue();
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

}
