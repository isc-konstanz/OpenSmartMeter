/*
 * Copyright 2016-19 ISC Konstanz
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
import java.util.ArrayList;
import java.util.List;

import org.openmuc.framework.config.ChannelScanInfo;
import org.openmuc.framework.config.ScanException;
import org.openmuc.framework.data.ValueType;
import org.openmuc.framework.driver.smartmeter.settings.DeviceAddress;
import org.openmuc.framework.driver.smartmeter.settings.DeviceSettings;
import org.openmuc.framework.driver.spi.ChannelRecordContainer;
import org.openmuc.framework.driver.spi.ChannelValueContainer;
import org.openmuc.framework.driver.spi.Connection;
import org.openmuc.framework.driver.spi.ConnectionException;
import org.openmuc.framework.driver.spi.RecordsReceivedListener;
import org.openmuc.iec62056.Iec62056;
import org.openmuc.iec62056.Iec62056Builder;
import org.openmuc.iec62056.data.DataSet;

public class ModeDConnection extends ModeDListener implements Connection {

    private final Iec62056 connection;

    public ModeDConnection(DeviceAddress address, DeviceSettings settings) 
            throws ConnectionException, IOException {
    	
        Iec62056Builder builder = Iec62056Builder.create(address.getSerialPort())
                .setDeviceAddress(address.getAddress())
        		.setPassword(settings.getPassword())
                .setMsgStartChars(settings.getMsgStartChars())
                .enableBaudRateHandshake(settings.hasHandshake())
                .setBaudRateChangeDelay(settings.getBaudRateChangeDelay())
                .setBaudRate(settings.getBaudRate())
				.setTimeout(settings.getTimeout());
        
        connection = builder.build();
    	connection.listen(this);
    }

    @Override
    public void disconnect() {
        connection.close();
    }

    @Override
    public List<ChannelScanInfo> scanForChannels(String settings)
            throws UnsupportedOperationException, ScanException, ConnectionException {
        
        List<ChannelScanInfo> scanInfos = new ArrayList<>(dataSets.size());
        for (DataSet dataSet : dataSets) {
            String description = dataSet.getAddress()+" ["+dataSet.getUnit()+"]";
            boolean readable = true;
            boolean writable = false;
            try {
                Double.parseDouble(dataSet.getValue());
                scanInfos.add(new ChannelScanInfo(dataSet.getAddress(), description, 
                		ValueType.DOUBLE, null, readable, writable));
                
            } catch (NumberFormatException e) {
                scanInfos.add(new ChannelScanInfo(dataSet.getAddress(), description, 
                		ValueType.STRING, dataSet.getValue().length(), readable, writable));
            }
        }
        return scanInfos;
    }

    @Override
    public void startListening(List<ChannelRecordContainer> containers, RecordsReceivedListener listener)
            throws UnsupportedOperationException, ConnectionException {
        
        register(listener, containers);
    }

    @Override
    public Object read(List<ChannelRecordContainer> containers, Object containerListHandle, String samplingGroup)
            throws UnsupportedOperationException, ConnectionException {
        
    	parseDataSets(containers);
        return null;
    }

    @Override
    public Object write(List<ChannelValueContainer> containers, Object containerListHandle)
            throws UnsupportedOperationException, ConnectionException {
        throw new UnsupportedOperationException();
    }

}
