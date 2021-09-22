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
package org.openmuc.framework.driver.smartmeter.iec;

import java.io.IOException;
import java.util.List;

import org.openmuc.framework.config.ArgumentSyntaxException;
import org.openmuc.framework.config.ScanException;
import org.openmuc.framework.config.Settings;
import org.openmuc.framework.driver.DriverChannelScanner;
import org.openmuc.framework.driver.DriverChannelScannerFactory;
import org.openmuc.framework.driver.annotation.Configure;
import org.openmuc.framework.driver.annotation.Connect;
import org.openmuc.framework.driver.annotation.Disconnect;
import org.openmuc.framework.driver.annotation.Listen;
import org.openmuc.framework.driver.annotation.Read;
import org.openmuc.framework.driver.smartmeter.ObisChannel;
import org.openmuc.framework.driver.smartmeter.SmartMeterDevice;
import org.openmuc.framework.driver.smartmeter.iec.IecScanner.DriverChannelReader;
import org.openmuc.framework.driver.spi.ConnectionException;
import org.openmuc.framework.driver.spi.RecordsReceivedListener;
import org.openmuc.iec62056.Iec62056;
import org.openmuc.iec62056.Iec62056Builder;
import org.openmuc.iec62056.data.DataSet;

public class ModeDListener extends SmartMeterDevice implements DriverChannelScannerFactory, DriverChannelReader {

    private Iec62056Builder builder;
    private Iec62056 connection;

    private IecListener listener;

    @Override
    public DriverChannelScanner newScanner(Settings settings) throws ArgumentSyntaxException {
        return new IecScanner(this);
    }

    @Override
    public List<DataSet> getScannerDataSets() throws ConnectionException, ScanException {
        return listener.dataSets;
    }

    @Configure
    public void build() {
        builder = Iec62056Builder.create(getSerialPort())
                .setDeviceAddress(getAddress())
                .setPassword(getPassword())
                .setMsgStartChars(getMsgStartChars())
                .enableBaudRateHandshake(hasHandshake())
                .setBaudRateChangeDelay(getBaudRateChangeDelay())
                .setBaudRate(getBaudRate())
                .setTimeout(getTimeout());
    }

    @Connect
    public void connect() throws ConnectionException {
        try {
            listener = new IecListener(this);
            connection = builder.build();
            connection.listen(listener);
            
        } catch (IOException e) {
            throw new ConnectionException(e);
        }
    }

    @Disconnect
    public void close() {
        connection.close();
    }

    @Listen
    public void listen(List<ObisChannel> channels, RecordsReceivedListener listener)
            throws UnsupportedOperationException, ConnectionException {
        
        this.listener.register(listener, channels);
    }

    @Read
    public void read(List<ObisChannel> channels, String samplingGroup)
            throws ConnectionException {
        
        this.listener.parseDataSets(channels);
    }

}
