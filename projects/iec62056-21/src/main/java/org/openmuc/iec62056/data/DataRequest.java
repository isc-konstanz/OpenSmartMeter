/*
 * Copyright 2013-17 Fraunhofer ISE
 *
 * This file is part of j62056.
 * For more information visit http://www.openmuc.org
 *
 * j62056 is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 2.1 of the License, or
 * (at your option) any later version.
 *
 * j62056 is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with j62056.  If not, see <http://www.gnu.org/licenses/>.
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
