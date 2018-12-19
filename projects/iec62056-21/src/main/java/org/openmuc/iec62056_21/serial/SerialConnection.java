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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.TimeoutException;

import org.openmuc.jrxtx.SerialPort;
import org.openmuc.jrxtx.SerialPortBuilder;


public class SerialConnection {
    private static final long SLEEP_TIME = 10L;

    private SerialPort serialPort;
    private SerialSettings settings;

    private int timeout = 5000;
    private boolean closed = true;

    public SerialConnection(SerialSettings settings) {
        this.settings = settings;
    }

    public void setTimeout(int timeout) {
        this.timeout = timeout;
    }
    
    public int getTimeout() {
        return timeout;
    }
    
    public SerialSettings getSettings() {
        return settings;
    }

    public boolean isClosed() {
        return this.closed;
    }

    public void open() throws IOException {
        if (isClosed()) {
            try {
            	serialPort = SerialPortBuilder.newBuilder(settings.getPort())
                        .setDataBits(settings.getDatabits())
                        .setStopBits(settings.getStopbits())
                        .setParity(settings.getParity())
                        .setBaudRate(settings.getBaudrate())
                        .build();
                
                serialPort.setSerialPortTimeout(timeout);
                
            } catch (IOException e) {
                if (serialPort != null) {
                    serialPort.close();
                }
                throw e;
            }
        }
        closed = false;
    }

    public void close() throws IOException {
        if (serialPort != null) {
            serialPort.close();
        }
        closed = true;
    }

    public void write(byte[] data) throws IOException {
        serialPort.getOutputStream().write(data);
        serialPort.getOutputStream().flush();
    }

    public void listenForChar(ByteArrayOutputStream byteStream, int endChar) throws IOException, TimeoutException {
        int readByte;
        while ((readByte = read()) != endChar) {
            byteStream.write(readByte);
        }
        byteStream.write(readByte);
    }

    public void listenForEach(ByteArrayOutputStream byteStream, byte[] endChars) throws IOException, TimeoutException {
        boolean listening = true;
        while (listening) {
            int readByte = read();
            for (byte endChar : endChars) {
                if (readByte == endChar) listening = false;
            }
            byteStream.write(readByte);
        }
    }

    public int read() throws IOException, TimeoutException {
        try {
            InputStream is = serialPort.getInputStream();
            
            long elapsedTime = 0;
            do {
                if (is.available() > 0) {
                    return is.read();
                }
                try {
                    Thread.sleep(SLEEP_TIME);
                    elapsedTime += SLEEP_TIME;
                } catch (InterruptedException e) {
                    // ignore
                }

                if (closed) {
                    throw new IOException("Connection has been closed");
                }
            } while (timeout <= 0 || elapsedTime <= timeout);

        } catch (IOException e) {
            if (serialPort != null) {
                serialPort.close();
            }
            closed = true;
            
            throw e;
        }
        throw new TimeoutException("Timed out, while reading from the serial port");
    }

    public void setBaudRate(int baudrate) throws IOException {
    	if (serialPort.getBaudRate() != baudrate) {
    		serialPort.setBaudRate(baudrate);
    	}
    }

    public void resetBaudRate() throws IOException {
        setBaudRate(settings.getBaudrate());
    }

}
