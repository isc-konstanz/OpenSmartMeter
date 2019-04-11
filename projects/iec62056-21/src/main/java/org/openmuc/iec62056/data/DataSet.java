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
package org.openmuc.iec62056.data;

import java.io.DataInputStream;
import java.io.IOException;

/**
 * A data message contains a list of data sets. Each data set consists of 3 fields "address", "value", and "unit". Each
 * of these fields is optional an may thus be equal to the empty string.
 * <p>
 * General format: '(' Value(optional) ('*' unit)(optional) ')'
 * 
 */
public class DataSet {
    private static final int BUFFER_LENGTH = 100;

    private String address;
    private String value;
    private String unit;

    private DataSet(String id, String value, String unit) {
        this.address = id;
        this.value = value;
        this.unit = unit;
    }

    /**
     * Reads a data set for a single programming mode data request
     * <p>
     * General format: 'STX'(0x02) Address '(' Value(optional) ('*' unit)(optional) ')' 'ETX'(0x03) 'BCC'
     * 
     * @param is The steam to read the data set from
     * 
     * @return The read data message.
     * @throws IOException
     *             if any kind of IO error occurs
     * 
     */
    public static DataSet readDataSet(DataInputStream is) throws IOException {
    	// Clear trailing empty bytes
        byte b = is.readByte();
        while (b == 0x00) {
            b = is.readByte();
        }
        if (b != 0x01 && b != 0x02) {
            throw new IOException("Received unexpected data message start byte: " + Converter.toShortHexString(b));
        }
        Bcc bcc = new Bcc();
        
        DataSet dataSet = DataSet.readDataSet(is, bcc);
        
        b = is.readByte();
        if (b != 0x03) {
            throw new IOException("Received unexpected byte at end of data message: " + Converter.toShortHexString(b)
                    + ", expected: 0x03");
        }
        bcc.value ^= b;
        
        b = is.readByte();
        if (b != bcc.value) {
            throw new IOException("Block check character (BCC) does not match. Received: " + Converter.toHexString(b)
                    + ", expected: " + Converter.toHexString(bcc.value));
        }
        return dataSet;
    }

    static DataSet readDataSet(DataInputStream is, Bcc bcc) throws IOException {
        byte b = readByteAndCalculateBcc(is, bcc);
        if (b == '\r') {
            b = readByteAndCalculateBcc(is, bcc);
            if (b != '\n') {
                throw new IOException(
                        "Received unexpected data message start byte: " + Converter.toShortHexString(b));
            }
            b = readByteAndCalculateBcc(is, bcc);
        }
        if (b == '!') {
            return null;
        }
        
        byte[] buffer = new byte[BUFFER_LENGTH];
        
        int i = 0;
        while (b != '(') {
            if (i == BUFFER_LENGTH) {
                throw new IOException("Expected '(' character not received.");
            }
            if (b != 0x02) {
                buffer[i] = b;
                i++;
            }
            b = readByteAndCalculateBcc(is, bcc);
        }
        String address = new String(buffer, 0, i, Converter.ASCII_CHARSET);
        
        int start = i;
        while ((b = readByteAndCalculateBcc(is, bcc)) != '*' && b != ')') {
            if (i == BUFFER_LENGTH) {
                throw new IOException("Expected '*' or ')' character not received.");
            }
            buffer[i] = b;
            i++;
        }
        String value = new String(buffer, start, i, Converter.ASCII_CHARSET);

        String unit;
        if (b == ')') {
            unit = "";
        }
        else {
            start = i;
            while ((b = readByteAndCalculateBcc(is, bcc)) != ')') {
                if (i == BUFFER_LENGTH) {
                    throw new IOException("Expected ')' character not received.");
                }
                buffer[i] = b;
                i++;
            }
            unit = new String(buffer, start, i, Converter.ASCII_CHARSET);
        }

        return new DataSet(address, value, unit);
    }

    static byte readByteAndCalculateBcc(DataInputStream is, Bcc bcc) throws IOException {
        byte b = is.readByte();
        bcc.value ^= b;
        return b;
    }

    void setAddress(byte[] address) {
    	this.address = new String(address, Converter.ASCII_CHARSET);;
    }

    void setAddress(String address) {
    	this.address = address;
    }

    /**
     * Returns the address/ID of this data set.
     * <p>
     * The address is usually an OBIS code of the format A-B:C.D.E*F or on older EDIS code of the format C.D.E. that
     * specifies exactly what the value of this data set represents. C is the type of the measured quantity (e.g 1 =
     * positive active power), D describes the measurement mode and E is the tariff (e.g. 0 for total or 1 for tariff 1
     * only) associated with this value.
     * <p>
     * If this data set contains no address this function returns the empty string.
     * 
     * @return the address
     */
    public String getAddress() {
        return address;
    }

    /**
     * Returns the value of this data set as a string.
     * <p>
     * The value is usually a decimal number that can be converted to a Double using
     * {@link java.lang.Double#parseDouble(String)}. But the value may also be a date or have some other format.
     * <p>
     * If this data set contains no value this function returns the empty string.
     * 
     * @return the value
     */
    public String getValue() {
        return value;
    }

    /**
     * Returns the unit of this data set as a string.
     * <p>
     * If this data set contains no unit this function returns the empty string.
     * 
     * @return the unit
     */
    public String getUnit() {
        return unit;
    }

    @Override
    public String toString() {
        return "\"data set\": {\"address\": \"" + address + "\", \"value\": \"" + value + "\", \"unit\": \"" + unit
                + "\"}";
    }
}
