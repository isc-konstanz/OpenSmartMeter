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

import org.openmuc.framework.config.ArgumentSyntaxException;
import org.openmuc.framework.driver.Driver;
import org.openmuc.framework.driver.DriverContext;
import org.openmuc.framework.driver.smartmeter.configs.Configurations;
import org.openmuc.framework.driver.smartmeter.iec.ModeAbcDevice;
import org.openmuc.framework.driver.smartmeter.iec.ModeDListener;
import org.openmuc.framework.driver.smartmeter.sml.SmlDevice;
import org.openmuc.framework.driver.spi.ConnectionException;
import org.openmuc.framework.driver.spi.DriverService;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component(service = DriverService.class)
public class SmartMeterDriver extends Driver<Configurations> {
    private final static Logger logger = LoggerFactory.getLogger(SmartMeterDriver.class);

    public static final String ID = "smartmeter";
    private static final String NAME = "Smart Meter";
    private static final String DESCRIPTION = "This driver implements the communication with metering devices " +
    		"speaking the Smart Message Language (SML) or as an IEC 62056-21 mode A-D master, registering " + 
    		"one or several slaves such as gas, water, heat, or electricity meters.";

    @Override
    public String getId() {
        return ID;
    }

    @Override
    protected void onCreate(DriverContext context) {
        logger.debug("IEC 62056 part 21 Driver instantiated. Expecting rxtxserial.so in: " + 
                System.getProperty("java.library.path") + " for serial connections.");
        
        context.setName(NAME)
               .setDescription(DESCRIPTION)
			   .setDeviceScanner(SmartMeterScanner.class);
    }

    @Override
	protected SmartMeterDevice onCreateConnection(Configurations configs) throws ArgumentSyntaxException, ConnectionException {
        logger.debug("Connect IEC 62056 device {} {}", configs.getAddress(), configs.getSerialPort());
        try {
            switch(configs.getMode()) {
            case SML:
                return new SmlDevice(configs);
            case ABC:
                return new ModeAbcDevice(configs);
            case D:
                return new ModeDListener(configs);
            default:
                throw new ConnectionException("Smart Meter Settings invalid: "+configs.getMode());
            }
        } catch (Exception e) {
            throw new ConnectionException(e.getMessage());
        }
    }

}
