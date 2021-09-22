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
import java.io.InterruptedIOException;
import java.util.ArrayList;
import java.util.List;

import org.openmuc.framework.config.ArgumentSyntaxException;
import org.openmuc.framework.config.ScanException;
import org.openmuc.framework.config.Settings;
import org.openmuc.framework.data.DoubleValue;
import org.openmuc.framework.data.Flag;
import org.openmuc.framework.data.Record;
import org.openmuc.framework.data.StringValue;
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
import org.openmuc.iec62056.Iec62056Exception;
import org.openmuc.iec62056.data.DataMessage;
import org.openmuc.iec62056.data.DataSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ModeAbcDevice extends SmartMeterDevice implements DriverChannelScannerFactory, DriverChannelReader {
    private final static Logger logger = LoggerFactory.getLogger(ModeAbcDevice.class);

    private Iec62056Builder builder;
    private Iec62056 connection;

    @Override
    public DriverChannelScanner newScanner(Settings settings) throws ArgumentSyntaxException {
        return new IecScanner(this);
    }

    @Override
    public List<DataSet> getScannerDataSets() throws ConnectionException, ScanException {
        List<DataSet> dataSets;
        DataMessage dataMessage;
        try {
            dataMessage = connection.read();
            
        } catch (IOException e) {
            throw new ConnectionException(e);
        }
        dataSets = dataMessage.getDataSets();
        if (dataSets == null) {
            throw new ScanException("Scan timed out.");
        }
        return dataSets;
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
            connection = builder.build();
            
        } catch (IOException e) {
            throw new ConnectionException(e);
        }
        try {
            // FIXME: Sleep to avoid to early read after connection. Meters have some delay.
            Thread.sleep(getTimeout());
            
        } catch (InterruptedException e) {
            logger.debug("Interrupted while waiting for port to open");
        }
    }

    @Disconnect
    public void close() {
        connection.close();
    }

    @Listen
    public void listen(List<ObisChannel> channels, RecordsReceivedListener listener)
            throws UnsupportedOperationException, ConnectionException {
        
        IecListener modeDListener = new IecListener(this);
        modeDListener.register(listener, channels);
        try {
            connection.listen(modeDListener);
            
        } catch (IOException e) {
            throw new ConnectionException(e);
        }
    }

    @Read
    public void read(List<ObisChannel> channels, String samplingGroup) throws ConnectionException {
        List<DataSet> dataSets = null;
        DataMessage dataMessage;
        for (int i = 0; i <= getRetries(); ++i) {
            try {
                if (connection.getSettings().hasAuthentication()) {
                    List<String> addresses = new ArrayList<String>(channels.size());
                    for (ObisChannel channel : channels) {
                        addresses.add(channel.getCode());
                    }
                    dataMessage = connection.read(addresses);
                }
                else {
                    dataMessage = connection.read();
                }
                dataSets = dataMessage.getDataSets();
                if (dataSets != null && !dataSets.isEmpty()) {
                    break;
                }
            } catch (InterruptedIOException | Iec62056Exception e) {
                logger.warn("Reading from device failed: " + e);
                if (i >= getRetries()) {
                    Flag flag = (e instanceof Iec62056Exception) ? Flag.DRIVER_ERROR_READ_FAILURE : Flag.DRIVER_ERROR_TIMEOUT;
                    for (ObisChannel channel : channels) {
                        channel.setRecord(new Record(flag));
                    }
                    return;
                }
            } catch (IOException e) {
                for (ObisChannel channel : channels) {
                    channel.setRecord(new Record(Flag.DRIVER_ERROR_READ_FAILURE));
                }
                connection.close();
                throw new ConnectionException("Read failed: " + e.getMessage());
            }
        }
        long time = System.currentTimeMillis();
        for (ObisChannel channel : channels) {
            for (DataSet dataSet : dataSets) {
                if (dataSet.getAddress().equals(channel.getCode())) {
                    String value = dataSet.getValue();
                    if (value != null) {
                        try {
                            channel.setRecord(new Record(new DoubleValue(Double.parseDouble(dataSet.getValue())), time));
                            
                        } catch (NumberFormatException e) {
                            channel.setRecord(new Record(new StringValue(dataSet.getValue()), time));
                        }
                    }
                    break;
                }
            }
        }
    }

}
