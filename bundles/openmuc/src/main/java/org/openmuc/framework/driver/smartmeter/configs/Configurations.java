/*
 * Copyright 2016-20 ISC Konstanz
 *
 * This file is part of OpenSkeleton.
 * For more information visit https://github.com/isc-konstanz/OpenSkeleton.
 *
 * OpenSkeleton is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * OpenSkeleton is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with OpenSkeleton.  If not, see <http://www.gnu.org/licenses/>.
 *
 */
package org.openmuc.framework.driver.smartmeter.configs;

import org.openmuc.framework.driver.DeviceConfigs;
import org.openmuc.framework.options.Address;
import org.openmuc.framework.options.AddressSyntax;
import org.openmuc.framework.options.Setting;

@AddressSyntax(separator = ",",
               assignmentOperator = ":",
               keyValuePairs = true)
public class Configurations extends DeviceConfigs<ObisChannel> {

    public static final String SERIAL_PORT = "serialPort";

    public static final String ADDRESS = "address";
    public static final String ADDRESS_DEFAULT = "";

    public static final String MODE = "mode";

    public static final String BAUD_RATE = "baudRate";

    public static final String TIMEOUT = "timeout";
    public static final int TIMEOUT_DEFAULT = 2000;

    @Address(id = SERIAL_PORT,
             name = "Serial port",
             description = "The physical device address to uniquely identify a physical smart meter.<br><br>" + 
                     "<b>Example:</b><ol>" + 
                         "<li><b>Linux</b>: /dev/ttyS0 or /dev/ttyUSB0</li>" + 
                         "<li><b>Windows</b>: COM1</li>" + 
                     "</ol>",
             mandatory = true)
    private String serialPort;

    @Address(id = ADDRESS,
             name = "Device address",
             description = "The address to uniquely identify a device, representing an IEC62056-21 slave.<br>" + 
                     "For many devices, this is a number that can be read off a label on the meter.<br><br>" + 
                     "<i>Only used for modes A, B and C. If left empty, a single meter can be addressed without an identifier.</i>",
             mandatory = false)
    private String address = ADDRESS_DEFAULT;

    @Setting(id = MODE,
             name = "Protocol mode",
             description = "The protocol mode to use.<br>" + 
                     "IEC 62056-21 mode A, B and C polls data from the meter, while mode D and " + 
                     "SML listens for pushed data by the meter.",
              valueSelection = "SML:Smart Meter Language,ABC:IEC 62056-21 Mode A&C,D:IEC 62056-21 Mode D",
             mandatory = true)
    private ProtocolMode mode;

    @Setting(id = "msgStartChars",
             name = "Request message start character",
             description = "The start character may be used for manufacture specific identification request messages." + 
                     "With this option the default start character can be changed.<br><br>" + 
                     "<i>Only used for modes A, B and C.</i>",
             mandatory = false)
    private String msgStartChars = "";

    @Setting(id = "password",
             name = "Password",
             description = "Authorization password to access the meters programmable mode.<br><br>" + 
                     "<i>Only used for modes A, B and C.</i>",
             mandatory = false)
    private String password;

    @Setting(id = "handshake",
             name = "Handshake",
             description = "Use initial handshake to negotiate baud rate.<br><br>" + 
                     "<i>Only used for mode C.</i>",
             valueDefault = "true",
             mandatory = false)
    private boolean handshake = true;

    @Setting(id = "baudRateChangeDelay",
             name = "Baud Rate change delay",
             description = "This parameter can usually be set to zero for regular serial ports. " + 
                     "If a USB to serial converter is used, you might have to use a delay of around 250ms, " + 
                     "as otherwise the baud rate may be changed before the previous message " + 
                     "(i.e. the acknowledgment) has been completely sent.<br><br>" + 
                     "Only used, if an initial handshake will be performed.",
             valueDefault = "0",
             mandatory = false)
    private int baudRateChangeDelay = 0;

    @Setting(id = BAUD_RATE,
             name = "Baud Rate (initial)",
             description = "The baud rate for the serial communication.<br>" + 
                     "Defaults are 300 baud for modes A, B and C, 2400 baud for Mode D and 9600 baud for SML.",
             valueSelection = "-1:Default,300:300,1200:1200,2400:2400,4800:4800,9600:9600,19200:19200," +
                     "38400:38400,57600:57600,115200:115200,230400:230400,460800:460800,921600:921600",
             valueDefault = "-1",
             mandatory = false)
    private int baudRate = -1;

    @Setting(id = "retries",
             name = "Read retries",
             description = "Defines the maximum number of read retries.<br><br>" + 
                     "<i>Only used for IEC 62056-21 mode A, B and C.</i>",
             valueDefault = ""+TIMEOUT_DEFAULT,
             mandatory = false)
    private int retries = TIMEOUT_DEFAULT;

    @Setting(id = TIMEOUT,
             name = "Timeout",
             description = "The timeout, after which the blocking call to read from the serial port will be canceled.<br><br>" + 
                     "<i>Only used for IEC 62056-21 mode A, B and C.</i>",
             valueDefault = "2000",
             mandatory = false)
    private int timeout = 2000;

    public String getSerialPort() {
        return serialPort;
    }

    public String getAddress() {
        return address;
    }

    public ProtocolMode getMode() {
        return mode;
    }

    public String getPassword() {
        return password;
    }

    public String getMsgStartChars() {
        return msgStartChars;
    }

    public boolean hasHandshake() {
        return handshake;
    }

    public int getBaudRateChangeDelay() {
        return baudRateChangeDelay;
    }

    public int getBaudRate() {
        return baudRate;
    }

    public int getTimeout() {
        return timeout;
    }

    public int getRetries() {
        return retries;
    }

}
