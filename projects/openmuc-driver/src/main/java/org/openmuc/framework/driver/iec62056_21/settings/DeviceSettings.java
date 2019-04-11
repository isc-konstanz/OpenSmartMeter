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

public class DeviceSettings extends Preferences {

    public static final PreferenceType TYPE = PreferenceType.SETTINGS_DEVICE;

    public static final String BAUD_RATE_KEY = "baudRate";

    public static final String TIMEOUT_KEY = "timeout";
    public static final int TIMEOUT_DEFAULT = 2000;

    @Option
    private String password = null;

    @Option
    private String msgStartChars = "";

    @Option
    private boolean handshake = true;

    @Option
    private int baudRateChangeDelay = 0;

    @Option
    private int baudRate = -1;

	@Option
    private int retries = 0;

    @Option
    private int timeout = TIMEOUT_DEFAULT;

    @Override
    public PreferenceType getPreferenceType() {
        return TYPE;
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
