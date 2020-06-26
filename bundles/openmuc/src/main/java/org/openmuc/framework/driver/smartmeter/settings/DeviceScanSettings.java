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
package org.openmuc.framework.driver.smartmeter.settings;

import org.openmuc.framework.config.PreferenceType;
import org.openmuc.framework.config.Preferences;

public class DeviceScanSettings extends Preferences {

    public static final PreferenceType TYPE = PreferenceType.SETTINGS_SCAN_DEVICE;

    @Option
    private ProtocolMode mode;

    @Option
    private int baudRate = -1;

    @Option
    private int timeout = DeviceSettings.TIMEOUT_DEFAULT;

    @Override
    public PreferenceType getPreferenceType() {
        return TYPE;
    }

    public ProtocolMode getMode() {
        return mode;
    }

    public int getBaudRate() {
        return baudRate;
    }

    public int getTimeout() {
    	return timeout;
    }

}
