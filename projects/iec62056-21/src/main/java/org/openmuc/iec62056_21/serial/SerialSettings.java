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
package org.openmuc.iec62056_21.serial;

import org.openmuc.jrxtx.DataBits;
import org.openmuc.jrxtx.Parity;
import org.openmuc.jrxtx.StopBits;

public class SerialSettings {

    private final String port;

    private final int baudrate;
    private final int databits;
    private final int stopbits;
    private final int parity;

    public SerialSettings(String port, Integer baudrate,
            Integer databits, Integer stopbits, Integer parity) {
        
        this.port = port;
        this.baudrate = baudrate;
        this.databits = databits;
        this.stopbits = stopbits;
        this.parity = parity;
    }

    public String getPort() {
        return port;
    }

    public int getBaudrate() {
        return baudrate;
    }

    public DataBits getDatabits() {
    	switch (databits) {
	    case 5:
	    	return DataBits.DATABITS_5;
	    case 6:
	    	return DataBits.DATABITS_6;
	    case 7:
	    	return DataBits.DATABITS_7;
	    case 8:
	    	return DataBits.DATABITS_8;
	    default:
	    	return DataBits.DATABITS_7;
    	}
    }

    public StopBits getStopbits() {
    	switch (stopbits) {
	    case 1:
	    	return StopBits.STOPBITS_1;
	    case 3:
	    	return StopBits.STOPBITS_1_5;
	    case 2:
	    	return StopBits.STOPBITS_2;
	    default:
	    	return StopBits.STOPBITS_1;
    	}
    }

    public Parity getParity() {
    	switch (parity) {
	    case 0:
	    	return Parity.NONE;
	    case 1:
	    	return Parity.ODD;
	    case 2:
	    	return Parity.EVEN;
	    case 3:
	    	return Parity.MARK;
	    case 4:
	    	return Parity.SPACE;
	    default:
	    	return Parity.EVEN;
    	}
    }

}
