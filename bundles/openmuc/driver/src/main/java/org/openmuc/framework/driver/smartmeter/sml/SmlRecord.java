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

import java.text.MessageFormat;

import org.openmuc.framework.data.DoubleValue;
import org.openmuc.framework.data.Flag;
import org.openmuc.framework.data.Record;
import org.openmuc.framework.data.StringValue;
import org.openmuc.framework.data.Value;
import org.openmuc.jsml.structures.ASNObject;
import org.openmuc.jsml.structures.Integer16;
import org.openmuc.jsml.structures.Integer32;
import org.openmuc.jsml.structures.Integer64;
import org.openmuc.jsml.structures.Integer8;
import org.openmuc.jsml.structures.OctetString;
import org.openmuc.jsml.structures.SmlListEntry;
import org.openmuc.jsml.structures.Unsigned16;
import org.openmuc.jsml.structures.Unsigned32;
import org.openmuc.jsml.structures.Unsigned64;
import org.openmuc.jsml.structures.Unsigned8;

public class SmlRecord extends Record {

	private final SmlListEntry entry;

	public SmlRecord(SmlListEntry entry, Long timestamp) {
		super(parseEntry(entry), timestamp);
		this.entry = entry;
	}

	public SmlRecord(SmlListEntry entry, Flag flag) {
		super(flag);
		this.entry = entry;
	}

    public String getAddress() {
        return parseAddress(entry);
    }

    public String getFullAddress() {
    	return parseFullAddress(entry);
    }

    public SmlListEntry getEntry() {
    	return entry;
    }

    protected static String parseAddress(SmlListEntry entry) {
    	byte[] bytes = entry.getObjName().getValue();
        return MessageFormat.format("{0}.{1}.{2}", 
    		 bytes[2] & 0xFF, bytes[3] & 0xFF, bytes[4] & 0xFF);
    }

    protected static String parseFullAddress(SmlListEntry entry) {
    	byte[] bytes = entry.getObjName().getValue();
        return MessageFormat.format("{0}-{1}:{2}.{3}.{4}*{5}", 
       		 bytes[0] & 0xFF, bytes[1] & 0xFF, bytes[2] & 0xFF, bytes[3] & 0xFF, bytes[4] & 0xFF, bytes[5] & 0xFF);
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

    @Override
    public String toString() {
        return "entry : " + getAddress() + "; value: " + getValue() + "; timestamp: " + getTimestamp() + "; flag: " + getFlag();
    }

}
