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

/**
 * Only used in protocol mode C and E. This message can be used to enter three different modes: 1) data readout, 2)
 * programming and 3) manufacturer specific.
 * <p>
 * General format: 'ACK' V Z Y 'CR' 'LF'
 * <p>
 * Format to select data readout: 'ACK' '0' Z '0' 'CR' 'LF'
 * <p>
 * Z should be either identical to Z from the identification message or be equal to '0' to not change the baud rate
 */
public class AcknowledgeRequest extends Request {

    public AcknowledgeRequest(int baudRate, ProtocolControlCharacter protocolControlCharacter,
            AcknowledgeMode acknowledgeMode) {
    	
        super(new byte[] { 0x06, (byte) protocolControlCharacter.value(),
                getCodeFromBaudRate(baudRate), (byte) acknowledgeMode.getValue(), '\r', '\n' });
    }

    private static byte getCodeFromBaudRate(int baudRate) {
        switch (baudRate) {
        case 300:
            return '0';
        case 600:
            return '1';
        case 1200:
            return '2';
        case 2400:
            return '3';
        case 4800:
            return '4';
        case 9600:
            return '5';
        case 19200:
            return '6';
        default:
            throw new IllegalStateException("Unexpected baudRate: " + baudRate);
        }
    }

}
