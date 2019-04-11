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
package org.openmuc.iec62056;

import java.io.IOException;

import org.openmuc.iec62056.data.Settings;
import org.openmuc.iec62056.serial.SerialPortFactory;
import org.openmuc.jrxtx.SerialPort;

/**
 * Represents a serial communication port that can be used to read meters using IEC 62056-21 modes A, B, C or D. Create
 * and open a port using {@link Iec62056Builder}.
 * 
 */
public class Iec62056Builder {

    private final String serialPortName;

    private String address = "";
    private String password = null;
    private String msgStartChars = "/?";

    // -1 indicates that the default initial baud rate should be used (i.e. 300 for modes A, B and C and 2400 for mode D
    private int baudRate = -1;
    private int baudRateChangeDelay = 0;
    private boolean baudRateHandshake = true;

    private int timeout = 2000;

    /**
     * Create an Iec62056Port builder.
     * 
     * @param serialPortName
     *            examples for serial port identifiers on Linux are "/dev/ttyS0" or "/dev/ttyUSB0" and on Windows
     *            "COM1"
     */
    public Iec62056Builder(String serialPortName) {
        if (serialPortName == null) {
            throw new IllegalArgumentException("serialPort may not be NULL");
        }
        this.serialPortName = serialPortName;
    }

    /**
     * Set the device address which is transmitted as part of the request message that is sent to the meter.
     * <p>
     * The default value is the empty string.
     * 
     * @param deviceAddress
     *            the device address
     * @return the builder
     */
    public Iec62056Builder setDeviceAddress(String deviceAddress) {
        this.address = deviceAddress;
        return this;
    }

    /**
     * Set the device password which will be sent after entering programming mode.
     * <p>
     * The default value is null, indicating to skip the programming mode initiation.
     * 
     * @param password
     *            the device password
     * @return the builder
     */
    public Iec62056Builder setPassword(String password) {
        this.password = password;
        return this;
    }

    /**
     * Sets the IdentificationRequest start characters.
     * <p>
     * Default value is: /? <br>
     * 
     * @param msgStartChars
     *            characters at the start of a IdentificationRequest
     * @return the builder
     */
    public Iec62056Builder setMsgStartChars(String msgStartChars) {
        this.msgStartChars = msgStartChars;
        return this;
    }

    /**
     * Set the initial baud rate.
     * <p>
     * The default is 300 baud for modes A, B, and C and 2400 baud for mode D. This function allows to change the
     * initial baud rate in case the meter does not use the default initial baud rate.
     * 
     * @param baudRate
     *            the initial baud rate
     * @return the builder
     */
    public Iec62056Builder setBaudRate(int baudRate) {
        this.baudRate = baudRate;
        return this;
    }

    /**
     * Set the time in ms to wait before changing the baud rate during message exchange. This parameter can usually
     * be set to zero for regular serial ports. If a USB to serial converter is used, you might have to use a delay
     * of around 250ms because otherwise the baud rate is changed before the previous message (i.e. the
     * acknowledgment) has been completely sent.
     * <p>
     * The default value is 0.
     * 
     * @param baudRateChangeDelay
     *            the baud rate change delay
     * @return the builder
     */
    public Iec62056Builder setBaudRateChangeDelay(int baudRateChangeDelay) {
        this.baudRateChangeDelay = baudRateChangeDelay;
        return this;
    }

    /**
     * Enable an initial handshake to negotiate the Baud Rate.
     * <p>
     * In mode C communication starts with baud rate 300 and then by default changes to a baud rate suggested by the
     * meter. Enable the handshake if the baud rate shall be changed.
     * 
     * @param baudRateHandshake
     *            if true enable fixed baud rate
     * @return the builder
     */
    public Iec62056Builder enableBaudRateHandshake(boolean baudRateHandshake) {
        this.baudRateHandshake = baudRateHandshake;
        return this;
    }

    /**
     * Set the maximum time in ms to wait for new data from the remote device. A timeout of zero is interpreted as
     * an infinite timeout.
     * <p>
     * The default value is 5000 (= 5 seconds).
     * 
     * @param timeout
     *            the maximum time in ms to wait for new data.
     * @return the builder
     */
    public Iec62056Builder setTimeout(int timeout) {
        this.timeout = timeout;
        return this;
    }

    /**
     * Build and open the Iec62056Port.
     * 
     * @return the opened Iec62056Port
     * 
     * @throws IOException
     *             if an error occurs while opening the associated serial port 
     *             (e.g. when the serial port is occupied).
     */
    public Iec62056 build() throws IOException {
        return new Iec62056(open(), setup());
    }

    public Settings setup() throws IOException {
    	return new Settings(address, password, msgStartChars, 
				baudRate, baudRateChangeDelay, baudRateHandshake, timeout);
    }

    public SerialPort open() throws IOException {
        SerialPort serialPort = SerialPortFactory.newSerialPort(serialPortName);
        serialPort.setSerialPortTimeout(timeout);
        
        return serialPort;
    }

}
