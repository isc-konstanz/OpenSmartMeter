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

import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * Used to request specific data in programming mode.
 * <p>
 * General format: 'SOH' X 'STX' ( Y ) 'ETX' 'BCC'
 * 
 */
public class DataRequest extends Request {

    public DataRequest(String address) throws IOException {
    	this((byte) 0x52, address);
    }

    protected DataRequest(byte d, String address) {
    	super(parseRequest(d, address));
    }

    protected static byte[] parseRequest(byte d, String address) {
    	byte[] byteId = address.trim().getBytes(Converter.ASCII_CHARSET);
        try (ByteArrayOutputStream byteStream = new ByteArrayOutputStream()) {
            byteStream.write(new byte[] { 0x01, d, 0x31, 0x02 });
            byteStream.write(byteId);
            byteStream.write(0x03);
            byteStream.write(Bcc.get(byteStream.toByteArray()));
            
        	return byteStream.toByteArray();
        	
        } catch (IOException e) {
        	return new byte[0];
		}
    }

}
