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
package org.openmuc.framework.driver.smartmeter.sml;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.openmuc.framework.config.ArgumentSyntaxException;
import org.openmuc.framework.config.ScanException;
import org.openmuc.framework.driver.ChannelScanner;
import org.openmuc.framework.driver.smartmeter.SmartMeterDevice;
import org.openmuc.framework.driver.smartmeter.configs.Configurations;
import org.openmuc.framework.driver.smartmeter.configs.ObisChannel;
import org.openmuc.framework.driver.spi.ConnectionException;
import org.openmuc.framework.driver.spi.RecordsReceivedListener;
import org.openmuc.jrxtx.DataBits;
import org.openmuc.jrxtx.FlowControl;
import org.openmuc.jrxtx.Parity;
import org.openmuc.jrxtx.SerialPortBuilder;
import org.openmuc.jrxtx.StopBits;

public class SmlDevice extends SmartMeterDevice {

    private final Configurations configs;

    private SmlListener listener;

    private ExecutorService executor;

    public SmlDevice(Configurations configs) {
    	this.configs = configs;
    }

    @Override
    protected void onCreate() {
        executor = Executors.newSingleThreadExecutor();
    }

	@Override
    protected ChannelScanner onCreateScanner(String settings) 
            throws UnsupportedOperationException, ArgumentSyntaxException, ScanException, ConnectionException {
        
		return new SmlScanner(listener.entries);
    }

	@Override
    protected void onConnect() throws ArgumentSyntaxException, ConnectionException {
    	int baudRate = configs.getBaudRate();
        if (baudRate < 0) {
            baudRate = 9600;
        }
        try {
	        SerialPortBuilder serialPortBuilder = SerialPortBuilder.newBuilder(configs.getSerialPort());
	        serialPortBuilder.setBaudRate(baudRate)
	                .setDataBits(DataBits.DATABITS_8)
	                .setStopBits(StopBits.STOPBITS_1)
	                .setParity(Parity.NONE)
	                .setFlowControl(FlowControl.RTS_CTS);
	        
	        listener = new SmlListener(this, serialPortBuilder.build(), baudRate);
	        executor.execute(listener);
	        
		} catch (IOException e) {
			throw new ConnectionException(e);
		}
    }

    @Override
    public void onDisconnect() {
        if (listener != null) {
            listener.stop();
            listener = null;
        }
    }

    @Override
    public void onDestroy() {
        executor.shutdown();
    }

    @Override
    public void onStartListening(List<ObisChannel> channels, RecordsReceivedListener listener)
            throws UnsupportedOperationException, ConnectionException {
        
        this.listener.register(listener, channels);
    }

	@Override
	protected Object onRead(List<ObisChannel> channels, Object containerListHandle, String samplingGroup)
			throws ConnectionException {
        
    	this.listener.parseEntries(channels);
    	return null;
    }

}
