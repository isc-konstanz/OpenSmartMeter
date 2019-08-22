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
package org.openmuc.iec62056;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InterruptedIOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.openmuc.iec62056.Iec62056Builder.Settings;
import org.openmuc.iec62056.data.AcknowledgeMode;
import org.openmuc.iec62056.data.AcknowledgeRequest;
import org.openmuc.iec62056.data.AuthenticationRequest;
import org.openmuc.iec62056.data.Converter;
import org.openmuc.iec62056.data.DataMessage;
import org.openmuc.iec62056.data.DataRequest;
import org.openmuc.iec62056.data.DataSet;
import org.openmuc.iec62056.data.EndRequest;
import org.openmuc.iec62056.data.IdentificationMessage;
import org.openmuc.iec62056.data.IdentificationRequest;
import org.openmuc.iec62056.data.ProtocolControlCharacter;
import org.openmuc.iec62056.data.ProtocolMode;
import org.openmuc.jrxtx.SerialPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Represents a serial communication port that can be used to read meters using IEC 62056-21 modes A, B, C or D. Create
 * and open a port using {@link Iec62056Builder}.
 * 
 */
public class Iec62056 {
    private static final Logger logger = LoggerFactory.getLogger(Iec62056.class);

    protected final Settings settings;

    protected final SerialPort serialPort;
    protected final DataOutputStream os;
    protected final DataInputStream is;

    protected Iec62056(Iec62056Builder builder) throws IOException {
    	settings = builder.settings();
        serialPort = builder.serialPort();
        is = new DataInputStream(serialPort.getInputStream());
        os = new DataOutputStream(new BufferedOutputStream(serialPort.getOutputStream()));
    }

    /**
     * Returns true if this port has been closed.
     * 
     * @return true if this port has been closed
     */
    public boolean isClosed() {
        return serialPort.isClosed();
    }

	/**
     * Closes this communication port. Also closes the associated serial port, input stream and output stream.
     * <p>
     * The port cannot be opened again but has to be recreated.
     */
    public void close() {
        try {
            serialPort.close();
        } catch (IOException e) {
        }
    }

    /**
     * Clears the input stream of any lingering bytes
     * 
     * @return The cleared bytes.
     * @throws IOException
     *             if any kind of IO error occurs
     */
    public byte[] clear() throws IOException {
    	byte[] bytes = new byte[0];
    	
        // Clear input stream
        ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
        int bytesNum = is.available();
        while (bytesNum > 0) {
            byte[] buffer = new byte[bytesNum];
            is.read(buffer);
            
            byteStream.write(buffer);
            bytesNum = is.available();
        }
        bytes = byteStream.toByteArray();
        if (logger.isDebugEnabled() && bytes.length > 0) {
            logger.debug("Cleared input stream. Bytes read from stream: {}", 
            		Converter.toAsciiString(bytes));
        }
        return bytes;
    }

    /**
     * Initialize the meter readout and returns its identification message.
     * 
     * @return The response identification message.
     * @throws IOException
     *             if any kind of IO error occurs
     * @throws InterruptedIOException
     *             if a timeout is thrown while waiting for the meter response
     */
    private IdentificationMessage initiate() throws IOException, InterruptedIOException {
        if (isClosed()) {
            throw new IOException("Port is closed");
        }
        int initBaudRate = settings.getBaudRate();
        if (serialPort.getBaudRate() != initBaudRate) {
            if (logger.isDebugEnabled()) {
                logger.debug("Changing baud rate from {} to {}", serialPort.getBaudRate(), initBaudRate);
            }
            serialPort.setBaudRate(initBaudRate);
        }
        if (serialPort.getSerialPortTimeout() != settings.getTimeout()) {
        	serialPort.setSerialPortTimeout(settings.getTimeout());
        }
        clear();
        
        IdentificationRequest identificationRequest = settings.getIdentificationRequest();
        identificationRequest.send(os);
        logger.debug("Sending identification request {}", identificationRequest.toString());
        
        IdentificationMessage identificationMessage = new IdentificationMessage(is);
        logger.debug("Received identification message {}", identificationMessage.toString());
        
        if (identificationMessage.getProtocolMode() == ProtocolMode.C || settings.hasAuthentication()) {
        	int baudRate = settings.hasHandshake() ? identificationMessage.getBaudRate() : initBaudRate;
        	AcknowledgeMode mode = settings.getAcknowledgeMode();
            AcknowledgeRequest acknowledgeRequest = new AcknowledgeRequest(baudRate, 
            		ProtocolControlCharacter.NORMAL, mode);
            
            logger.debug("Sending acknowledge request {}", acknowledgeRequest.toString());
            acknowledgeRequest.send(os);
            
            if (settings.hasHandshake() && settings.getBaudRateChangeDelay() > 0) {
                logger.debug("Sleeping for: {} ms before changing the baud rate", settings.getBaudRateChangeDelay());
                try {
                    Thread.sleep(settings.getBaudRateChangeDelay());
                    
                } catch (InterruptedException e) {
                }
            }
        }
        
        if ((identificationMessage.getProtocolMode() == ProtocolMode.B 
                || (identificationMessage.getProtocolMode() == ProtocolMode.C) && settings.hasHandshake())) {
            logger.debug("Changing baud rate from {} to {}", 
            		serialPort.getBaudRate(), identificationMessage.getBaudRate());
            
            serialPort.setBaudRate(identificationMessage.getBaudRate());
        }
        
        if (settings.hasAuthentication()) {
            DataSet.readDataSet(is);
        	AuthenticationRequest authenticationRequest = settings.getAuthenticationRequest();
        	authenticationRequest.send(os);
            logger.debug("Sending authentication request {}", authenticationRequest.toString());
            
            byte b = is.readByte();
            if (b != 0x06) {
            	throw new Iec62056Exception("Received unexpected byte while waiting for acknowledgement: " + Converter.toShortHexString(b));
            }
        }
        return identificationMessage;
    }

    private void terminate() throws IOException {
        if (settings.hasAuthentication()) {
        	EndRequest endRequest = new EndRequest();
        	endRequest.send(os);
            logger.debug("Sending {}", endRequest.toString());
        }
    }

    /**
     * Requests meter data and returns the response.
     * <p>
     * Requests a data message from the remote device using IEC 62056-21 Mode A, B or C. The data message received is
     * parsed and returned. The returned data message also contains some information fields from the identification
     * message sent by the meter.
     * 
     * @return The response data message.
     * @throws IOException
     *             if any kind of IO error occurs
     * @throws InterruptedIOException
     *             if a timeout is thrown while waiting for the meter response
     * @throws Iec62056Exception
     *             if a protocol related error occurred, e.g. an invalid response
     */
    public DataMessage read() throws IOException, InterruptedIOException, Iec62056Exception {
    	try {
            DataMessage dataMessage = DataMessage.readModeABC(is, initiate());
            logger.debug("Received data message: {}", dataMessage.toString());
            
            terminate();
            return dataMessage;
    		
    	} catch(Iec62056Exception e) {
    		clear();
    		throw e;
    	}
    }

    /**
     * Requests meter data and returns the response.
     * <p>
     * Requests a specific data sets from the remote device using IEC 62056-21 Mode A, B or C programming mode.
     * The data message received is parsed and returned. The returned data message also contains some information
     * fields from the identification message sent by the meter.
     * 
     * @param addresses The set of addresses to request 
     * 
     * @return The response data message.
     * @throws IOException
     *             if any kind of IO error occurs
     * @throws InterruptedIOException
     *             if a timeout is thrown while waiting for the meter response
     * @throws Iec62056Exception
     *             if a protocol related error occurred, e.g. an invalid response
     */
    public DataMessage read(Collection<String> addresses) throws IOException, InterruptedIOException, Iec62056Exception {
    	try {
            IdentificationMessage identificationMessage = initiate();
            
            List<DataSet> dataSets = new ArrayList<DataSet>();
            for (String address : addresses) {
            	DataRequest dataRequest = new DataRequest(address);
                logger.debug("Sending {}", dataRequest.toString());
                
            	dataRequest.send(os);
            	
            	DataSet dataSet = DataSet.readDataSet(is);
                if (dataSet != null) {
                	dataSets.add(dataSet);
                }
            }
            DataMessage dataMessage = new DataMessage(identificationMessage, dataSets);
            logger.debug("Received data message: {}", dataMessage.toString());

            terminate();
            return dataMessage;
    		
    	} catch(Iec62056Exception e) {
    		clear();
    		throw e;
    	}
    }

    /**
     * Listen for mode D messages.
     * 
     * @param listener
     *            A listener for mode D messages
     * @throws IOException
     *             throws IOException
     */
    public void listen(ModeDListener listener) throws IOException {
        serialPort.setSerialPortTimeout(0);
        if (serialPort.getBaudRate() != settings.getBaudRate(ProtocolMode.D)) {
            logger.debug("Changing baud rate from {}", serialPort.getBaudRate(), settings.getBaudRate(ProtocolMode.D));
            serialPort.setBaudRate(settings.getBaudRate(ProtocolMode.D));
        }
        
        logger.debug("Starting to listen for mode D messages");
        new ModeDReceiver(listener, serialPort).start();
    }

    public Settings getSettings() {
    	return settings;
    }

}
