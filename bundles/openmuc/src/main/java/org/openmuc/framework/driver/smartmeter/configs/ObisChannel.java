/*
 * Copyright 2016-20 ISC Konstanz
 *
 * This file is part of OpenSkeleton.
 * For more information visit https://github.com/isc-konstanz/OpenSkeleton.
 *
 * OpenSkeleton is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * OpenSkeleton is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with OpenSkeleton.  If not, see <http://www.gnu.org/licenses/>.
 *
 */
package org.openmuc.framework.driver.smartmeter.configs;

import org.openmuc.framework.driver.Channel;
import org.openmuc.framework.options.Address;

public class ObisChannel extends Channel {

    @Address(id = "obis",
    		 name = "OBIS code",
             description = "The ID is usually an OBIS code of the format A-B:C.D.E*F or an older EDIS code of the format C.D.E. " + 
             		"that specifies exactly what the value of this data set represents.<br>" + 
             		"C is the type of the measured quantity (e.g 1 = positive active power), D describes the measurement " + 
             		"mode and E is the tariff (e.g. 0 for total or 1 for tariff 1 only) associated with this value.",
             mandatory = true)
    protected String obis;

    public String getCode() {
    	return obis;
    }

}
