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

/**
 * Only used after entering programming mode.
 * <p>
 * General format: 'SOH' P1 'STX' ( P ) 'ETX'
 * 
 */
public class AuthenticationRequest extends DataRequest {

	private final String password;

    public AuthenticationRequest(String password) throws IOException {
        super((byte) 0x50, '('+password+')');
        this.password = password;
    }

    public String getPassword() {
    	return password;
    }

}
