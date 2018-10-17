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

import gnu.io.CommPort;
import gnu.io.CommPortIdentifier;
import gnu.io.NoSuchPortException;
import gnu.io.PortInUseException;
import gnu.io.SerialPort;
import gnu.io.UnsupportedCommOperationException;


@SuppressWarnings("deprecation")
public class SerialConnection {
    private static final String APP_NAME = "org.openmuc.iec62056.SERIAL";
    
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
                serialPort = acquireSerialPort(settings.getPort());

            } catch (IOException e) {
                if (serialPort != null) {
                    serialPort.close();
                }
                throw e;
            }
        }
        closed = false;
    }

    public void close() {
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

    private SerialPort acquireSerialPort(String serialPortName) throws IOException {
        CommPortIdentifier portIdentifier;
        try {
            portIdentifier = CommPortIdentifier.getPortIdentifier(serialPortName);
            
        } catch (NoSuchPortException e) {
            throw new IOException("The specified port does not exist", e);
        }

        CommPort commPort;
        try {
            commPort = portIdentifier.open(APP_NAME, 2000);
            
        } catch (PortInUseException e) {
            throw new IOException("The specified port is already in use", e);
        }

        if (!(commPort instanceof SerialPort)) {
            // may never be the case
            commPort.close();
            throw new IOException("The specified CommPort is not a serial port");
        }

        try {
            SerialPort serialPort = (SerialPort) commPort;
            serialPort.setSerialPortParams(settings.getBaudrate(), settings.getDatabits(), settings.getStopbits(), settings.getParity());
            
            return serialPort;
            
        } catch (UnsupportedCommOperationException e) {
            if (commPort != null) {
                commPort.close();
            }
            throw new IOException("Unable to set the baud rate or other serial port parameters", e);
        }
    }

    public void setParameters(int baudrate, int dataBits, int stopBits, int parity) throws IOException {
        if (serialPort.getBaudRate() != baudrate ||
                serialPort.getDataBits() != dataBits || serialPort.getStopBits() != stopBits || serialPort.getParity() != parity) {

            try {
                serialPort.setSerialPortParams(baudrate, dataBits, stopBits, parity);
                
            } catch (UnsupportedCommOperationException e) {
                throw new IOException("Unable to set the baud rate or other serial port parameters", e);
            }
        }
    }

    public void setParameters(int baudrate) throws IOException {
        setParameters(baudrate, settings.getDatabits(), settings.getStopbits(), settings.getParity());
    }

    public void resetParameters() throws IOException {
        setParameters(settings.getBaudrate(), settings.getDatabits(), settings.getStopbits(), settings.getParity());
    }

}
