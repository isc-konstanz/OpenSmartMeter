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
package org.openmuc.iec62056.data;

import java.io.DataInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.openmuc.iec62056.Iec62056Exception;
import org.openmuc.jrxtx.SerialPort;

/**
 * Represents the data sent by the meter.
 * 
 * The data consists of the manufacturer ID, the meter ID (optional), the enhanced ID/capability (optional), and a list
 * of data sets.
 *
 */
public class DataMessage {
    private static final int FRAGMENT_TIMEOUT = 500;

    private final String manufacturerId;
    private final String enhancedId;
    private final String meterId;

    private final List<DataSet> dataSets;

    public DataMessage(IdentificationMessage id, List<DataSet> dataSets) {
        this.manufacturerId = id.getManufactureId();
        this.enhancedId = id.getEnhancedId();
        this.meterId = id.getMeterId();
        this.dataSets = dataSets;
    }

    /**
     * Reads a data set, consisting of the manufacturer ID, the meter ID (optional), the enhanced ID/capability (optional), and a list
     * of data sets, for Mode A,B and C
     * <p>
     * Programming format: 'SOH'(0x01) Address 'STX'(0x02) 'Data block' 'ETX'(0x03) 'BCC'
     * General format: 'STX'(0x02) 'Data block' '!' '\r'(0x0D) '\n'(0x0A) 'ETX'(0x03) 'BCC'
     * Data block:     Data sets, separated by CR and LF, Optionally the data block ends with a CR and LF
     * Data set:       Address '(' Value(optional) ('*' unit)(optional) ')'
     * 
     * @param is The steam to read the message from
     * @param id The information about manufacturer ID, the meter ID (optional), the enhanced ID/capability (optional)
     * 
     * @return The read data message.
     * @throws IOException
     *             if any kind of IO error occurs
     * 
     */
    public static DataMessage readModeABC(DataInputStream is, IdentificationMessage id) throws IOException {
    	// Clear trailing empty bytes
        byte b = is.readByte();
        while (b == 0x00) {
            b = is.readByte();
        }
        if (b != 0x01 && b != 0x02) {
            throw new Iec62056Exception("Received unexpected data message start byte: " + Converter.toShortHexString(b));
        }
        Bcc bcc = new Bcc();
        
        List<DataSet> dataSets = new ArrayList<>();
        DataSet dataSet;
        if (b == 0x01) {
        	dataSet = DataSet.readDataSet(is, bcc);
            dataSets.add(dataSet);
        }
        else {
            while ((dataSet = DataSet.readDataSet(is, bcc)) != null) {
                dataSets.add(dataSet);
            }
            
            b = is.readByte();
            if (b != '\r') {
                throw new Iec62056Exception("Received unexpected byte at end of data message: " + Converter.toShortHexString(b)
                        + ", expected: '\r'(");
            }
            bcc.value ^= b;
            
            b = is.readByte();
            if (b != '\n') {
                throw new Iec62056Exception("Received unexpected byte at end of data message: " + Converter.toShortHexString(b)
                        + ", expected: '\n'");
            }
            bcc.value ^= b;
        }
        b = is.readByte();
        if (b != 0x03) {
            throw new Iec62056Exception("Received unexpected byte at end of data message: " + Converter.toShortHexString(b)
                    + ", expected: 0x03");
        }
        bcc.value ^= b;
        
        b = is.readByte();
        if (b != bcc.value) {
            throw new Iec62056Exception("Block check character (BCC) does not match. Received: " + Converter.toHexString(b)
                    + ", expected: " + Converter.toHexString(bcc.value));
        }
        return new DataMessage(id, dataSets);
    }

    /**
     * Reads a data set, consisting of the manufacturer ID, the meter ID (optional), the enhanced ID/capability (optional), and a list
     * of data sets, for Mode D
     * <p>
     * General format: '\r' '\n' 'Data set' '!' '\r' '\n'
     * 
     * @param is The steam to read the message from
     * @param id The information about manufacturer ID, the meter ID (optional), the enhanced ID/capability (optional)
     * @param serialPort The serial port, used to alternate the internal timeout
     * 
     * @return The read data message.
     * @throws IOException
     *             if any kind of IO error occurs
     * 
     */
    public static DataMessage readModeD(DataInputStream is, IdentificationMessage id,
            SerialPort serialPort) throws IOException {
    	// Clear trailing empty bytes
        byte b = is.readByte();
        while (b == 0x00) {
            b = is.readByte();
        }
        if (b != '\r') {
            throw new Iec62056Exception("Received unexpected byte at beginning of data message: "
                    + Converter.toShortHexString(b) + ", expected: '\r'(");
        }

        serialPort.setSerialPortTimeout(FRAGMENT_TIMEOUT);
        try {

            b = is.readByte();
            if (b != '\n') {
                throw new Iec62056Exception("Received unexpected byte at beginning of data message: "
                        + Converter.toShortHexString(b) + ", expected: '\n'");
            }
            List<DataSet> dataSets = new ArrayList<>();
            DataSet dataSet;
            while ((dataSet = DataSet.readDataSet(is, new Bcc())) != null) {
                dataSets.add(dataSet);
            }
            
            b = is.readByte();
            if (b != '\r') {
                throw new Iec62056Exception("Received unexpected byte at end of data message: "
                        + Converter.toShortHexString(b) + ", expected: '\r'(");
            }
            b = is.readByte();
            if (b != '\n') {
                throw new Iec62056Exception("Received unexpected byte at end of data message: "
                        + Converter.toShortHexString(b) + ", expected: '\n'");
            }
            return new DataMessage(id, dataSets);

        } finally {
            serialPort.setSerialPortTimeout(0);
        }
    }

    /**
     * Returns the manufacturer identification of this data message.
     * 
     * @return the manufacturer identification
     */
    public String getManufacturerId() {
        return manufacturerId;
    }

    /**
     * Returns the identification string (except for the enhanced identification characters).
     * 
     * @return the identification string
     */
    public String getMeterId() {
        return meterId;
    }

    /**
     * Returns the enhanced identification/capability characters as a string.
     * 
     * @return the enhanced identification/capability characters
     */
    public String getEnhancedId() {
        return enhancedId;
    }

    /**
     * Returns the data sets of this data message.
     * 
     * @return the data sets
     */
    public List<DataSet> getDataSets() {
        return dataSets;
    }

    /**
     * Appends all data sets to this data message.
     * 
     * @param dataSets the data sets to be added to this message
     * 
     * @return true if this list changed as a result of the call
     */
    public boolean addDataSets(List<DataSet> dataSets) {
        return dataSets.addAll(dataSets);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("{\n\t\"data message\": {\n\t\t\"manufacturer ID\": \"")
                .append(manufacturerId)
                .append("\",\n\t\t\"meter ID\": \"")
                .append(meterId)
                .append("\",\n\t\t\"enhanced ID/capability\": \"")
                .append(enhancedId)
                .append("\"")
                .append(IdentificationMessage.getEnhancedIdDescription(enhancedId))
                .append(",\n\t\t\"data block\": {");
        for (DataSet dataSet : dataSets) {
            sb.append("\n\t\t\t").append(dataSet.toString()).append(',');
        }
        sb.deleteCharAt(sb.length() - 1);
        sb.append("\n\t\t}\n\t}\n}");
        return sb.toString();
    }

}
