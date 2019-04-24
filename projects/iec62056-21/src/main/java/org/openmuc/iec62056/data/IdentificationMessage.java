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

import org.openmuc.iec62056.Iec62056Exception;

/**
 * Format: '/' X X X Z Identification 'CR' 'LF'
 * <p>
 * X X X = manufacturer identification (three characters)
 * <p>
 * Z = baud rate identification, is also used to select the mode, e.g. if Z='A'...'F' then mode B is selected
 * <p>
 * Identification = manufacturer specific device ID that has a maximum length of 16. It may contain the escape character
 * '\' followed by W which is the enhanced baud rate and mode identification character.
 */
public class IdentificationMessage {

    private final String manufacturerId;
    private final ProtocolMode mode;
    private final int baudRate;
    private final String meterId;
    private final String enhancedId;

    public IdentificationMessage(DataInputStream is) throws IOException {
        byte b = is.readByte();
        if (b != '/') {
            throw new Iec62056Exception(
                    "Received unexpected identification message start byte: " + Converter.toShortHexString(b));
        }

        byte[] manufacturerIdBytes = new byte[3];
        is.readFully(manufacturerIdBytes);
        manufacturerId = new String(manufacturerIdBytes, Converter.ASCII_CHARSET);

        byte baudRateByte = is.readByte();
        switch (baudRateByte) {
        case 'A':
            baudRate = 600;
            mode = ProtocolMode.B;
            break;
        case 'B':
            baudRate = 1200;
            mode = ProtocolMode.B;
            break;
        case 'C':
            baudRate = 2400;
            mode = ProtocolMode.B;
            break;
        case 'D':
            baudRate = 4800;
            mode = ProtocolMode.B;
            break;
        case 'E':
            baudRate = 9600;
            mode = ProtocolMode.B;
            break;
        case 'F':
            baudRate = 19200;
            mode = ProtocolMode.B;
            break;
        case '0':
            baudRate = 300;
            mode = ProtocolMode.C;
            break;
        case '1':
            baudRate = 600;
            mode = ProtocolMode.C;
            break;
        case '2':
            baudRate = 1200;
            mode = ProtocolMode.C;
            break;
        case '3':
            baudRate = 2400;
            mode = ProtocolMode.C;
            break;
        case '4':
            baudRate = 4800;
            mode = ProtocolMode.C;
            break;
        case '5':
            baudRate = 9600;
            mode = ProtocolMode.C;
            break;
        case '6':
            baudRate = 19200;
            mode = ProtocolMode.C;
            break;
        default:
            baudRate = -1;
            mode = ProtocolMode.A;
        }

        b = is.readByte();
        String tempEnhancedId = "";
        while (b == 0x5c) {
            tempEnhancedId += (char) is.readByte();
            b = is.readByte();
        }
        enhancedId = tempEnhancedId;

        byte[] identificationBytes = new byte[16];
        int i = 0;
        while (b != '\r') {
            if (i == 16) {
                throw new Iec62056Exception("Expected carriage return character not received");
            }
            identificationBytes[i] = b;
            i++;
            b = is.readByte();
        }
        meterId = new String(identificationBytes, 0, i, Converter.ASCII_CHARSET);

        b = is.readByte();
        if (b != '\n') {
            throw new Iec62056Exception(
                    "Received unexpected identification message end byte: " + Converter.toShortHexString(b));
        }

    }

    public String getManufactureId() {
        return manufacturerId;
    }

    public String getMeterId() {
        return meterId;
    }

    public String getEnhancedId() {
        return enhancedId;
    }

    public int getBaudRate() {
        return baudRate;
    }

    public ProtocolMode getProtocolMode() {
        return mode;
    }

    @Override
    public String toString() {
        return "{\"identification message\": {\"manufacturer ID\": \"" + manufacturerId + "\", \"protocol mode\": \""
                + mode + "\", \"baud rate\": " + baudRate + ", \"meter ID\": \"" + meterId
                + "\", \"enhanced ID/capability\": \"" + enhancedId + "\"" + getEnhancedIdDescription(enhancedId)
                + "}}";
    }

    public static String getEnhancedIdDescription(String enhancedId) {
        if (enhancedId.equals("2")) {
            return "(HDLC)";
        }
        return "";
    }
}
