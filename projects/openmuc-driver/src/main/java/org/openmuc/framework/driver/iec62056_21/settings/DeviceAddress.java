/*
 * Copyright 2016-18 ISC Konstanz
 *
 * This file is part of OpenIEC62056-21.
 * For more information visit https://github.com/isc-konstanz/OpenIEC62056-21.
 *
 * OpenIEC62056-21 is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * OpenIEC62056-21 is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with OpenIEC62056-21.  If not, see <http://www.gnu.org/licenses/>.
 *
 */
package org.openmuc.framework.driver.iec62056_21.settings;

import org.openmuc.framework.config.PreferenceType;
import org.openmuc.framework.config.Preferences;

public class DeviceAddress extends Preferences {

    public static final PreferenceType TYPE = PreferenceType.ADDRESS_DEVICE;

    @Option
    private String serialPort;

    @Option
    private String address = "";

    @Override
    public PreferenceType getPreferenceType() {
        return TYPE;
    }

    public String getSerialPort() {
        return serialPort;
    }

    public String getAddress() {
    	return address;
    }
}
