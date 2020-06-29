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
package org.openmuc.framework.driver.smartmeter;

import java.util.List;

import org.openmuc.framework.config.ArgumentSyntaxException;
import org.openmuc.framework.driver.Device;
import org.openmuc.framework.driver.smartmeter.configs.ObisChannel;
import org.openmuc.framework.driver.spi.ConnectionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class SmartMeterDevice extends Device<ObisChannel> {
	protected static final Logger logger = LoggerFactory.getLogger(SmartMeterDevice.class);

	@Override
	protected abstract void onConnect() throws ArgumentSyntaxException, ConnectionException;

	@Override
	protected abstract void onDisconnect();

    @Override
    protected abstract Object onRead(List<ObisChannel> channels, Object containerListHandle, String samplingGroup)
            throws ConnectionException;

}
