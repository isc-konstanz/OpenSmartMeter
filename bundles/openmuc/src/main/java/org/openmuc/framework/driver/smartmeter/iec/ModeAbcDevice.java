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
import org.openmuc.framework.data.DoubleValue;
import org.openmuc.framework.data.Flag;
import org.openmuc.framework.data.Record;
import org.openmuc.framework.data.StringValue;
import org.openmuc.framework.driver.ChannelScanner;
import org.openmuc.framework.driver.smartmeter.SmartMeterDevice;
import org.openmuc.framework.driver.smartmeter.configs.Configurations;
import org.openmuc.framework.driver.smartmeter.configs.ObisChannel;
import org.openmuc.framework.driver.spi.ConnectionException;
import org.openmuc.framework.driver.spi.RecordsReceivedListener;
import org.openmuc.iec62056.Iec62056;
import org.openmuc.iec62056.Iec62056Builder;
import org.openmuc.iec62056.Iec62056Exception;
import org.openmuc.iec62056.data.DataMessage;
import org.openmuc.iec62056.data.DataSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ModeAbcDevice extends SmartMeterDevice {
    private final static Logger logger = LoggerFactory.getLogger(ModeAbcDevice.class);

    private final Configurations configs;

    private Iec62056Builder builder;
    private Iec62056 connection;

    public ModeAbcDevice(Configurations configs) {
    	this.configs = configs;
    }

    @Override
    protected void onCreate() {
        builder = Iec62056Builder.create(configs.getSerialPort())
                .setDeviceAddress(configs.getAddress())
        		.setPassword(configs.getPassword())
                .setMsgStartChars(configs.getMsgStartChars())
                .enableBaudRateHandshake(configs.hasHandshake())
                .setBaudRateChangeDelay(configs.getBaudRateChangeDelay())
                .setBaudRate(configs.getBaudRate())
				.setTimeout(configs.getTimeout());
    }

	@Override
    protected ChannelScanner onCreateScanner(String settings) 
            throws UnsupportedOperationException, ArgumentSyntaxException, ScanException, ConnectionException {

        List<DataSet> dataSets;
        DataMessage dataMessage;
        try {
            dataMessage = connection.read();
            
        } catch (IOException e) {
            throw new ScanException(e);
        }
        dataSets = dataMessage.getDataSets();
        if (dataSets == null) {
            throw new ScanException("Scan timed out.");
        }
		return new IecScanner(dataSets);
    }

	@Override
    protected void onConnect() throws ArgumentSyntaxException, ConnectionException {
        try {
			connection = builder.build();
			
		} catch (IOException e) {
			throw new ConnectionException(e);
		}
        try {
            // FIXME: Sleep to avoid to early read after connection. Meters have some delay.
            Thread.sleep(configs.getTimeout());
            
        } catch (InterruptedException e) {
            logger.debug("Interrupted while waiting for port to open");
        }
	}

	@Override
	protected void onDisconnect() {
    	connection.close();
	}

    @Override
    public void onStartListening(List<ObisChannel> channels, RecordsReceivedListener listener)
            throws UnsupportedOperationException, ConnectionException {
        
        IecListener modeDListener = new IecListener(this);
        modeDListener.register(listener, channels);
        try {
        	connection.listen(modeDListener);
            
        } catch (IOException e) {
            throw new ConnectionException(e);
        }
    }

	@Override
	protected Object onRead(List<ObisChannel> channels, Object containerListHandle, String samplingGroup)
			throws ConnectionException {
        List<DataSet> dataSets = null;
        DataMessage dataMessage;
        for (int i = 0; i <= configs.getRetries(); ++i) {
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
                if (i >= configs.getRetries()) {
                	Flag flag = (e instanceof Iec62056Exception) ? Flag.DRIVER_ERROR_READ_FAILURE : Flag.DRIVER_ERROR_TIMEOUT;
                    for (ObisChannel channel : channels) {
                        channel.setRecord(new Record(flag));
                    }
                    return null;
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
        return null;
	}

}
