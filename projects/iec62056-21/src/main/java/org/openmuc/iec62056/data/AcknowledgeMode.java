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
 * 0 - data readout<br>
 * 1 - programming mode<br>
 * 2 - binary mode (HDLC), see Annex E<br>
 * 3-5 and A-Z - reserved for future applications<br>
 * 6-9 - manufacturer-specific use<br>
 */
public enum AcknowledgeMode {
    DATA_READOUT('0'),
    PROGRAMMING('1'),
    BINARY('2'),
    RESERVED('3'),
    MANUFACTURE('6');

    private char value;

    private AcknowledgeMode(char value) {
        this.value = value;
    }

    public int getValue() {
        return this.value;
    }

    public static AcknowledgeMode valueOf(char mode) {
        switch (mode) {
        case '0':
            return DATA_READOUT;
        case '1':
            return PROGRAMMING;
        case '2':
            return BINARY;
        case '3':
        case '4':
        case '5':
            return RESERVED;
        default:
            if ((mode >= '6' && mode <= '9') || (mode >= 'A' && mode <= 'Z')) {
                return MANUFACTURE;
            }
            else {
                throw new IllegalArgumentException("Unsupported control mode.");
            }
        }
    }

}
