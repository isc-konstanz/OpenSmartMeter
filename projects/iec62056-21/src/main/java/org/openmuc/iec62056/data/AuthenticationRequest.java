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
