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

import java.util.ArrayList;
import java.util.List;

import org.openmuc.framework.config.ArgumentSyntaxException;
import org.openmuc.framework.config.ChannelScanInfo;
import org.openmuc.framework.config.ScanException;
import org.openmuc.framework.data.ValueType;
import org.openmuc.framework.driver.ChannelScanner;
import org.openmuc.framework.driver.spi.ConnectionException;
import org.openmuc.iec62056.data.DataSet;

public class IecScanner extends ChannelScanner {

    protected final List<DataSet> dataSets;

    public IecScanner(List<DataSet> dataSets) {
    	this.dataSets = dataSets;
    }

	@Override
	public List<ChannelScanInfo> doScan() throws ArgumentSyntaxException, ScanException, ConnectionException {
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

}
