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
package org.openmuc.framework.driver.smartmeter.settings;

import org.openmuc.framework.config.PreferenceType;
import org.openmuc.framework.config.Preferences;

public class DeviceSettings extends Preferences {

    public static final PreferenceType TYPE = PreferenceType.SETTINGS_DEVICE;

    public static final String MODE_KEY = "mode";

    public static final String BAUD_RATE_KEY = "baudRate";

    public static final String TIMEOUT_KEY = "timeout";
    public static final int TIMEOUT_DEFAULT = 2000;

    @Option
    private ProtocolMode mode;

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
