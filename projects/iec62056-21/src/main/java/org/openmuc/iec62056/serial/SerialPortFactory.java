/*
 * Copyright 2011-16 Fraunhofer ISE
 *
 * This file is part of OpenMUC.
 * For more information visit http://www.openmuc.org
 *
 * OpenMUC is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * OpenMUC is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with OpenMUC.  If not, see <http://www.gnu.org/licenses/>.
 *
 */
package org.openmuc.iec62056.serial;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

import org.openmuc.jrxtx.DataBits;
import org.openmuc.jrxtx.Parity;
import org.openmuc.jrxtx.SerialPort;
import org.openmuc.jrxtx.SerialPortBuilder;
import org.openmuc.jrxtx.StopBits;

public class SerialPortFactory {
    private static final int BAUDRATE_DEFAULT = 300;

    private static final Map<String, SerialPort> ports = new HashMap<String, SerialPort>();

    public static SerialPort newSerialPort(String name) throws IOException {
        SerialPort port = ports.get(name);
        if (port == null || port.isClosed()) {
        	if (name.startsWith("/") && 
        			!Files.exists(Paths.get(name), LinkOption.NOFOLLOW_LINKS)) {
        		
        		throw new IOException("Serial Port does not exist: " + name);
        	}
            port = SerialPortBuilder.newBuilder(name)
                    .setDataBits(DataBits.DATABITS_7)
                    .setStopBits(StopBits.STOPBITS_1)
                    .setParity(Parity.EVEN)
                    .setBaudRate(BAUDRATE_DEFAULT)
                    .build();
        }
        return port;
    }

}
