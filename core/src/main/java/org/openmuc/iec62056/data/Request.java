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
package org.openmuc.iec62056.data;

import java.io.DataOutputStream;
import java.io.IOException;

public abstract class Request {

    protected final byte[] requestMessageBytes;

    protected Request(byte[] requestMessageBytes) {
    	this.requestMessageBytes = requestMessageBytes;
    }

    protected Request(String requestMessageStr) {
    	this(requestMessageStr.getBytes(Converter.ASCII_CHARSET));
    }

    public void send(DataOutputStream os) throws IOException {
        os.write(requestMessageBytes);
        os.flush();
    }

    @Override
    public String toString() {
        return Converter.toAsciiString(requestMessageBytes);
    }

}
