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
 * General format: 'SOH' B0 'ETX' 'BCC'
 * 
 */
public class EndRequest extends Request {

    public EndRequest() throws IOException {
    	super(parseRequest());
    }

    private static byte[] parseRequest() {
        try (ByteArrayOutputStream byteStream = new ByteArrayOutputStream()) {
            byteStream.write(new byte[] { 0x01, 0x42, 0x30, 0x03 });
            byteStream.write(Bcc.get(byteStream.toByteArray()));
            
        	return byteStream.toByteArray();
        	
        } catch (IOException e) {
        	return new byte[0];
		}
    }

}
