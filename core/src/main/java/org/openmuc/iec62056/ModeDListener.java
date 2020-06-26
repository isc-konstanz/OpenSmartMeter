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
package org.openmuc.iec62056;

import org.openmuc.iec62056.data.DataMessage;

/**
 * Listener for incoming Mode D messages. The object of that class is registered as a listener through the
 * {@link Iec62056#listen(ModeDListener)}
 *
 */
public interface ModeDListener {

    /**
     * Is called if a new data message has been received and successfully parsed.
     * 
     * @param dataMessage
     *            the data message received
     */
    public void newDataMessage(DataMessage dataMessage);

    /**
     * Is called if an exception is thrown while listening for incoming mode D data messages.
     * 
     * @param e
     *            the exception thrown
     */
    public void exceptionWhileListening(Exception e);

}
