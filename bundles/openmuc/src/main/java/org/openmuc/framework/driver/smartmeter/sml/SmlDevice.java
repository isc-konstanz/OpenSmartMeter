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
import org.openmuc.framework.config.Settings;
import org.openmuc.framework.driver.DriverChannelScanner;
import org.openmuc.framework.driver.DriverChannelScannerFactory;
import org.openmuc.framework.driver.annotation.Connect;
import org.openmuc.framework.driver.annotation.Disconnect;
import org.openmuc.framework.driver.annotation.Listen;
import org.openmuc.framework.driver.annotation.Read;
import org.openmuc.framework.driver.smartmeter.ObisChannel;
import org.openmuc.framework.driver.smartmeter.SmartMeterDevice;
import org.openmuc.framework.driver.spi.ConnectionException;
import org.openmuc.framework.driver.spi.RecordsReceivedListener;
import org.openmuc.jrxtx.DataBits;
import org.openmuc.jrxtx.FlowControl;
import org.openmuc.jrxtx.Parity;
import org.openmuc.jrxtx.SerialPortBuilder;
import org.openmuc.jrxtx.StopBits;

public class SmlDevice extends SmartMeterDevice implements DriverChannelScannerFactory {

    private SmlListener listener;

    private ExecutorService executor;

    @Override
    public DriverChannelScanner newScanner(Settings settings) 
            throws ArgumentSyntaxException {
        
        return new SmlScanner(listener.entries);
    }

    @Connect
    public void connect() throws ConnectionException {
        this.executor = Executors.newSingleThreadExecutor();
        
        int baudRate = getBaudRate();
        if (baudRate < 0) {
            baudRate = 9600;
        }
        try {
            SerialPortBuilder serialPortBuilder = SerialPortBuilder.newBuilder(getSerialPort());
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

    @Disconnect
    public void close() {
        if (listener != null) {
            listener.stop();
            listener = null;
        }
        executor.shutdown();
    }

    @Listen
    public void listen(List<ObisChannel> channels, RecordsReceivedListener listener)
            throws UnsupportedOperationException, ConnectionException {
        
        this.listener.register(listener, channels);
    }

    @Read
    public void read(List<ObisChannel> channels, String samplingGroup) 
            throws ConnectionException {
        
        this.listener.parseEntries(channels);
    }

}
