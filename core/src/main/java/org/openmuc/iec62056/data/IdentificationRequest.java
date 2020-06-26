/*
 * Copyright 2016-20 ISC Konstanz
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

/**
 * / ? Device address ! CR LF
 * <p>
 * Device address is optional
 */
public class IdentificationRequest extends Request {

    /**
     * Constructor for request message with device address.
     * 
     * @param deviceAddress
     *            the device address
     */
    public IdentificationRequest(String deviceAddress) {
        this(deviceAddress, null);
    }

    /**
     * Constructor for request message with device address and with specific start characters. <br>
     * If startCharacters is null or empty the default value '/?' will used.
     * 
     * @param address
     *            the device address
     * @param startCharacters
     *            specific start characters, default is '/?'
     */
    public IdentificationRequest(String address, String startCharacters) throws IllegalArgumentException {
    	super(parseRequest(address, startCharacters));
    }

    private static String parseRequest(String address, String startCharacters) throws IllegalArgumentException {
        if (address.length() > 32) {
            throw new IllegalArgumentException("Device address is longer than 32 characters");
        }
        if (startCharacters == null || startCharacters.isEmpty()) {
            startCharacters = "/?";
        }
        
        // Address strings length must by divisible by 4
        String zeros = "";
        for (int i=0; i<address.length() % 4; i++) {
            zeros += '0';
        }
        return startCharacters + zeros + address + "!\r\n";
    }

}
