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
package org.openmuc.iec62056_21;

import org.openmuc.iec62056_21.serial.SerialSettings;

public class Iec62056Settings {

    private final SerialSettings serial;

    private final String address;
    
    private String password = null;

    private int timeout = 5000;

    private boolean verify = true;

    private boolean handleEcho = false;

    private boolean handshake = true;

    private int baudrateChangeDelay = 250;

    private Integer baudrateMax = null;

    public Iec62056Settings(SerialSettings settings, String address) {
        this.serial = settings;
        
        // Address strings length must by divisible by 4
        String zeros = "";
        for (int i=0; i<address.length() % 4; i++) {
            zeros += '0';
        }
        this.address = zeros + address;
    }

    public Iec62056Settings(SerialSettings settings) {
        this(settings, "");
    }

    public SerialSettings getSerialSettings() {
        return serial;
    }

    public String getSerialPort() {
        return serial.getPort();
    }

    public String getAddress() {
        return address;
    }

	public String getPassword() {
		return password;
	}

	public Iec62056Settings setPassword(String password) {
		this.password = password;
		return this;
	}

	public int getTimeout() {
		return timeout;
	}

	public Iec62056Settings setTimeout(int timeout) {
		this.timeout = timeout;
		return this;
	}

    public boolean hasVerification() {
        return verify;
    }

	public Iec62056Settings setVerification(boolean verify) {
		this.verify = verify;
		return this;
	}

	public boolean hasEchoHandling() {
		return handleEcho;
	}

	public Iec62056Settings setEchoHandling(boolean handleEcho) {
		this.handleEcho = handleEcho;
		return this;
	}

	public boolean hasHandshake() {
		return handshake;
	}

	public Iec62056Settings setHandshake(boolean handshake) {
		this.handshake = handshake;
		return this;
	}

	public int getBaudrateChangeDelay() {
		return baudrateChangeDelay;
	}

	public Iec62056Settings setBaudrateChangeDelay(int baudrateChangeDelay) {
		this.baudrateChangeDelay = baudrateChangeDelay;
		return this;
	}

	public Integer getBaudrateMaximum() {
		return baudrateMax;
	}

	public Iec62056Settings setBaudrateMaximum(Integer baudrateMax) {
		this.baudrateMax = baudrateMax;
		return this;
	}

}
