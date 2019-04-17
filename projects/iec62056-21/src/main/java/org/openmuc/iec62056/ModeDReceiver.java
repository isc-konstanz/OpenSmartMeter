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
package org.openmuc.iec62056;

import java.io.DataInputStream;
import java.io.IOException;

import org.openmuc.iec62056.data.DataMessage;
import org.openmuc.iec62056.data.IdentificationMessage;
import org.openmuc.jrxtx.SerialPort;

public class ModeDReceiver extends Thread {

    protected final SerialPort serialPort;
    protected final DataInputStream is;

    protected final ModeDListener listener;

	public ModeDReceiver(ModeDListener listener, SerialPort serialPort) throws IOException {
		this.listener = listener;
		this.serialPort = serialPort;
		this.is = new DataInputStream(serialPort.getInputStream());
	}

    @Override
    public void run() {
        while (!serialPort.isClosed()) {
            try {
                listener.newDataMessage(read());
                
            } catch (Exception e) {
                if (serialPort.isClosed()) {
                    break;
                }
                listener.exceptionWhileListening(e);
            }
        }
    }

    public DataMessage read() throws IOException {
        try {
            IdentificationMessage identificationMessage = new IdentificationMessage(is);
            return DataMessage.readModeD(is, identificationMessage, serialPort);
            
        } catch (Exception e) {
            if (!serialPort.isClosed()) {
                int numBytesInStream;
                try {
                    numBytesInStream = is.available();
                    if (numBytesInStream > 0) {
                        byte[] bytesInStream = new byte[numBytesInStream];
                        is.read(bytesInStream);
                    }
                } catch (IOException e1) {
                	throw e1;
                }
            }
            throw e;
        }
    }

}
