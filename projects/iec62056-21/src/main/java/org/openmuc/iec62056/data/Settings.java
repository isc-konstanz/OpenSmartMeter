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

import java.io.IOException;

public class Settings {

    protected final int baudRate;
    protected final int baudRateChangeDelay;
    protected final boolean handshake;

    protected final int timeout;

    protected final AuthenticationRequest authenticationRequest;
    protected final IdentificationRequest identificationRequest;

	public Settings(String address, String password, String msgStartChars, 
			int baudRate, int baudRateChangeDelay, boolean handshake, int timeout) 
				throws IOException {
		
		this.baudRate = baudRate;
		this.baudRateChangeDelay = baudRateChangeDelay;
		this.handshake = handshake;
		this.timeout = timeout;
		
		identificationRequest = new IdentificationRequest(address, msgStartChars);
		authenticationRequest = password == null ? null : new AuthenticationRequest(password);
	}

	public boolean hasAuthentication() {
		return authenticationRequest != null;
	}

	public AuthenticationRequest getAuthenticationRequest() {
		return authenticationRequest;
	}

	public IdentificationRequest getIdentificationRequest() {
		return identificationRequest;
	}

	public AcknowledgeMode getAcknowledgeMode() {
		return hasAuthentication() ? AcknowledgeMode.PROGRAMMING : AcknowledgeMode.DATA_READOUT;
	}

	public int getBaudRate() {
		return getBaudRate(ProtocolMode.A);
	}

	public int getBaudRate(ProtocolMode mode) {
		if (baudRate > 0) {
			return baudRate;
		}
		switch(mode) {
		case D:
			return 2400;
		default:
			return 300;
		}
	}

	public int getBaudRateChangeDelay() {
		return baudRateChangeDelay;
	}

	public boolean hasHandshake() {
		return handshake;
	}

	public int getTimeout() {
		return timeout;
	}
}
