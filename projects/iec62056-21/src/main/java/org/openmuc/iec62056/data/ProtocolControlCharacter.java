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
 * 0 - normal protocol procedure<br>
 * 1 - secondary protocol procedure<br>
 * 2 - HDLC protocol procedure, see Annex E<br>
 * 3-9 - reserved for future applications<br>
 */
public enum ProtocolControlCharacter {
    NORMAL('0'),
    SECONDARY('1'),
    HDLC('2'),
    RESERVED3('3'),
    RESERVED4('4'),
    RESERVED5('5'),
    RESERVED6('6'),
    RESERVED7('7'),
    RESERVED8('8'),
    RESERVED9('9');

    private char value;

    private final static ProtocolControlCharacter[] values = ProtocolControlCharacter.values();

    private ProtocolControlCharacter(char value) {
        this.value = value;
    }

    public char value() {
        return this.value;
    }

    public static ProtocolControlCharacter valueOf(char tagValue) {
        for (ProtocolControlCharacter c : values) {
            if (c.value == tagValue) {
                return c;
            }
        }
        throw new IllegalArgumentException("Unsupported control mode.");
    }

}
