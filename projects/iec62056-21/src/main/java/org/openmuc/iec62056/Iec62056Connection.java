/*
 * Copyright 2011-16 Fraunhofer ISE
 *
 * This file is part of OpenMUC.
 * For more information visit http://www.openmuc.org
 *
 * OpenMUC is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * OpenMUC is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with OpenMUC.  If not, see <http://www.gnu.org/licenses/>.
 *
 */
package org.openmuc.iec62056;

//import java.io.ByteArrayOutputStream;
//import java.io.IOException;
//import java.nio.ByteBuffer;
//import java.nio.charset.Charset;
//import java.text.MessageFormat;
//import java.util.ArrayList;
//import java.util.Arrays;
//import java.util.List;
//import java.util.Set;
//import java.util.concurrent.TimeoutException;
//
//import org.openmuc.iec62056.Iec62056Connection;
//import org.openmuc.iec62056.data.DataSet;
//import org.openmuc.iec62056.serial.SerialConnection;
//import org.openmuc.iec62056.serial.SerialSettings;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//
//
//public class Iec62056Connection {
//    private static final Logger logger = LoggerFactory.getLogger(Iec62056Connection.class);
//
//    private final SerialConnection serial;
//
//    private static final int SOH = 0x01;
//    private static final int STX = 0x02;
//    private static final int ETX = 0x03;
//    private static final int EOT = 0x04;
//    private static final int ACK = 0x06;
//    private static final int CR = 0x0D;
//    private static final int LF = 0x0A;
//
//    /**
//     * {@code /? }
//     */
//    private static final byte[] INIT_PRIOR = new byte[] { 0x2F, 0x3F };
//
//    /**
//     * {@code !<CR><LF>}
//     */
//    private static final byte[] INIT_POST = new byte[] { 0x21, CR, LF };
//
//    /**
//     * {@code <ACK>000<CR><LF>}
//     * <p>
//     * ACK [protocol procedure] [initial bd 300] [data readout mode]
//     * <p>
//     */
//    private static final byte[] ACKNOWLEDGE = new byte[] { ACK, 0x30, 0x30, 0x30, CR, LF };
//
//    /**
//     * {@code <SOH>R1<STX>( }
//     */
//    private static final byte[] REQUEST_PRIOR = new byte[] { SOH, 0x52, 0x31, STX };
//
//    /**
//     * {@code )<ETX>}
//     */
//    private static final byte[] REQUEST_POST = new byte[] { ETX };
//
//    private static final char[] hexArray = "0123456789ABCDEF".toCharArray();
//    private static final Charset charset = Charset.forName("US-ASCII");
//
//    /**
//     * Creates a Connection object. You must call <code>open()</code> before
//     * calling <code>read()</code> in order to read data. The timeout is set by
//     * default to 5s.
//     * 
//     * @param serialSettings
//     *            serial port settings, containing serial parameters and port name.
//     *            Defaults are 300bd 7E1 by 5s timeout, whereas examples for serial 
//     *            port identifiers are on Linux "/dev/ttyS0" or "/dev/ttyUSB0" 
//     *            and on Windows "COM1"
//     */
//    public Iec62056Connection(SerialSettings serialSettings) {
//        this.serial = new SerialConnection(serialSettings);
//    }
//
//    /**
//     * Creates a Connection object. You must call <code>open()</code> before
//     * calling <code>read()</code> in order to read data. The timeout is set by
//     * default to 5s.
//     * 
//     * @param serial
//     *            serial port connection object.
//     */
//    public Iec62056Connection(SerialConnection serial) {
//        this.serial = serial;
//    }
//
//    /**
//     * Sets the maximum time in ms to wait for new data from the remote device.
//     * A timeout of zero is interpreted as an infinite timeout.
//     * 
//     * @param timeout
//     *            the maximum time in ms to wait for new data.
//     * @throws IOException
//     *             if any kind of error occurs setting the serial port timeout.
//     */
//    public void setTimeout(int timeout) throws IOException {
//        serial.setTimeout(timeout);
//    }
//
//    /**
//     * Returns the timeout in ms.
//     * 
//     * @return the timeout in ms.
//     */
//    public int getTimeout() {
//        return serial.getTimeout();
//    }
//
//    /**
//     * Opens the serial port associated with this connection.
//     * 
//     * @return if the connection is already open, or was successfully opened.
//     * @throws IOException
//     *             if any kind of error occurs opening the serial port.
//     */
//    public boolean open() throws IOException {
//        serial.open();
//        return true;
//    }
//
//    /**
//     * Closes the serial port.
//     * 
//     * @return if the connection is already closed, or was successfully closed.
//     */
//    public boolean close() {
//        try {
//			serial.close();
//			
//		} catch (IOException e) {
//			logger.error("Error while closing serial connection: {}", e.getMessage());
//			return false;
//		}
//        return true;
//    }
//
//    /**
//     * Requests a data message from the remote device using IEC 62056-21.
//     * The data message received is parsed and a list of data sets is returned.
//     * 
//     * @param settings
//     *            connection settings, containing configurations regarding authentication,
//     *            maximum baud rate, handshake or to tell the connection to throw away 
//     *            echos of outgoing messages. Echos are caused by some optical transceivers. 
//     *            A maximum baud rate can be set while the handshake is performed. For some
//     *            devices it is necessary to wait before changing the baud rate during 
//     *            message exchange. This parameter can usually be set to zero for regular
//     *            serial ports. If a USB to serial converter is used, you might have to 
//     *            use a delay of around 250ms because otherwise the baud rate is changed 
//     *            before the previous message (i.e. the acknowledgment) has been
//     *            completely sent.
//     * @param ids
//     *             a set of IDs/Addresses of data sets. The ID is usually an OBIS code
//     *             of the format A-B:C.D.E*F or on older EDIS code of the format C.D.E. 
//     *             that specifies exactly what the value of this data set represents.
//     *             C is the type of the measured quantity (e.g 1 = positive active power),
//     *             D describes the measurement mode and E is the tariff (e.g. 0 for total 
//     *             or 1 for tariff 1 only) associated with this value.
//     * @return A list of data sets contained in the data message response from
//     *             the remote device. The first data set will contain the
//     *             "identification" of the meter as the id and empty strings for
//     *             value and unit.
//     * @throws IOException
//     *             if any kind of error other than timeout occurs while trying
//     *             to read the remote device. Note that the connection is not
//     *             closed when an IOException is thrown.
//     * @throws TimeoutException
//     *             if no response at all (not even a single byte) was received
//     *             from the meter within the timeout span.
//     */
//    public synchronized List<DataSet> read(Iec62056Settings settings, Set<String> ids) 
//            throws IOException, TimeoutException {
//        
//        List<DataSet> dataSets = new ArrayList<DataSet>();
//        
//        // Set serial parameters configured as default for this device connection
//        serial.resetBaudRate();
//        serial.setTimeout(settings.getTimeout());
//        
//        byte[] identification = null;
//        if (settings.hasHandshake()) {
//            identification = initiateWithHandshake(settings);
//        }
//        else {
//            identification = initiateWithoutHandshake(settings);
//        }
//        
//        int offset = 0;
//        if (settings.hasEchoHandling()) {
//            offset = 5;
//        }
//        dataSets.add(new DataSet(
//                new String(identification, offset + 5, identification.length - offset - 7, charset), "", ""));
//        
//        if (settings.getPassword() != null) {
//            authenticateWithPassword(settings.getPassword());
//            
//            if (ids != null && !ids.isEmpty()) {
//                for (String id : ids) {
//                    dataSets.add(requestDataSet(id));
//                }
//            }
//        }
//        else {
//            byte[] response = listenForResponse();
//            dataSets.addAll(parseDataSets(response));
//        }
//        
//        return dataSets;
//    }
//
//    /**
//     * Requests a data message from the remote device using IEC 62056-21.
//     * The data message received is parsed and a list of data sets is returned.
//     * 
//     * @param settings
//     *            connection settings, containing configurations regarding authentication,
//     *            maximum baud rate, handshake or to tell the connection to throw away 
//     *            echos of outgoing messages. Echos are caused by some optical transceivers. 
//     *            A maximum baud rate can be set while the handshake is performed. For some
//     *            devices it is necessary to wait before changing the baud rate during 
//     *            message exchange. This parameter can usually be set to zero for regular
//     *            serial ports. If a USB to serial converter is used, you might have to 
//     *            use a delay of around 250ms because otherwise the baud rate is changed 
//     *            before the previous message (i.e. the acknowledgment) has been
//     *            completely sent.
//     * @return A list of data sets contained in the data message response from
//     *             the remote device. The first data set will contain the
//     *             "identification" of the meter as the id and empty strings for
//     *             value and unit.
//     * @throws IOException
//     *             if any kind of error other than timeout occurs while trying
//     *             to read the remote device. Note that the connection is not
//     *             closed when an IOException is thrown.
//     * @throws TimeoutException
//     *             if no response at all (not even a single byte) was received
//     *             from the meter within the timeout span.
//     */
//    public synchronized List<DataSet> read(Iec62056Settings settings) 
//            throws IOException, TimeoutException {
//        
//        return read(settings, null);
//    }
//
//    private byte[] initiateWithHandshake(Iec62056Settings settings) throws IOException, TimeoutException {
//        
//        byte[] response = initiateCommunication(settings.getAddress());
//        
//        int offset = 0;
//        if (settings.hasEchoHandling()) {
//            offset = 5;
//        }
//        if (response.length <= offset + 4) {
//        	throw new IOException("Invalid response while initiating handshake: " + byteToAscii(response));
//        }
//        char baudRateSetting = (char) response[offset + 4];
//        
//        byte[] ackMsg = Arrays.copyOf(ACKNOWLEDGE, ACKNOWLEDGE.length);;
//        
//        int baudrate = -1;
//        if (settings.getBaudrateMaximum() != null) {
//            baudrate = settings.getBaudrateMaximum();
//            ackMsg[2] = encodeBaudrate(settings.getBaudrateMaximum());
//        }
//        else {
//            baudrate = decodeBaudrate(baudRateSetting);
//            ackMsg[2] = (byte) baudRateSetting;
//        }
//        
//        if (settings.getPassword() != null) {
//            ackMsg[3] = 0x31;
//        }
//        else {
//            ackMsg[3] = 0x30;
//        }
//        
//        if (logger.isTraceEnabled()) {
//            logger.trace("Sending message: {}", byteToAscii(ackMsg));
//        }
//        serial.write(ackMsg);
//        
//        if (baudrate != -1) {
//            // Sleep for about 250 milliseconds to make sure, that the
//            // acknowledge message has been completely transmitted prior
//            // to changing the baud rate
//            try {
//                Thread.sleep(settings.getBaudrateChangeDelay());
//            } catch (InterruptedException e) {
//            }
//            serial.setBaudRate(baudrate);
//        }
//        
//        if (settings.getPassword() != null) {
//            // If programmable mode was requested, a response will acknowledge that
//            listenForResponse();
//        }
//        return response;
//    }
//
//    private byte[] initiateWithoutHandshake(Iec62056Settings settings) throws IOException, TimeoutException {
//        return initiateCommunication(settings.getAddress());
//    }
//    
//    private byte[] initiateCommunication(String address) throws IOException, TimeoutException {
//
//        byte[] addressBytes = address.trim().getBytes();
//        
//        byte[] requestMsg = ByteBuffer.allocate(INIT_PRIOR.length + addressBytes.length + INIT_POST.length)
//                .put(INIT_PRIOR)
//                .put(addressBytes)
//                .put(INIT_POST)
//                .array();
//
//        if (logger.isTraceEnabled()) {
//            logger.trace("Sending message: {}", byteToAscii(requestMsg));
//        }
//        serial.write(requestMsg);
//        
//        try {
//            return listenForIdentificationMessage();
//            
//        } catch (IOException e) {
//            throw new IOException(MessageFormat.format("Exception occurred while sending request message ({0}): {1}", 
//                    byteToAscii(requestMsg), e.getMessage()), e);
//        }
//    }
//
//    private byte[] authenticateWithPassword(String password) throws IOException, TimeoutException {
//        
//        byte[] passwordBytes = password.trim().getBytes();
//        
//        // Alter the request message to ask for authentication and add BCC:
//        // <SOH>P1<STX>(password)<ETX><BCC>
//        try (ByteArrayOutputStream byteStream = new ByteArrayOutputStream()) {
//            byte[] requestMsg = Arrays.copyOf(REQUEST_PRIOR, REQUEST_PRIOR.length);
//            requestMsg[1] = 0x50;
//            
//            byteStream.write(requestMsg);
//            byteStream.write(0x28);
//            byteStream.write(passwordBytes);
//            byteStream.write(0x29);
//            byteStream.write(REQUEST_POST);
//            byteStream.write(getBCC(byteStream.toByteArray()));
//            
//            requestMsg = byteStream.toByteArray();
//            
//            if (logger.isTraceEnabled()) {
//                logger.trace("Sending message: {}", byteToAscii(requestMsg));
//            }
//            serial.write(requestMsg);
//        }
//        return listenForResponse();
//    }
//
//    private DataSet requestDataSet(String identifier) throws IOException, TimeoutException {
//
//        byte[] idBytes = identifier.trim().getBytes();
//
//        try (ByteArrayOutputStream byteStream = new ByteArrayOutputStream()) {
//            byteStream.write(REQUEST_PRIOR);
//            byteStream.write(idBytes);
//            byteStream.write(REQUEST_POST);
//            byteStream.write(getBCC(byteStream.toByteArray()));
//            
//            byte[] requestMsg = byteStream.toByteArray();
//            
//            if (logger.isTraceEnabled()) {
//                logger.trace("Sending message: {}", byteToAscii(requestMsg));
//            }
//            serial.write(requestMsg);
//        }
//        byte[] responseMsg = listenForResponse();
//        byte[] dataMsg = verifyDataMessage(responseMsg);
//        
//        return parseDataSet(dataMsg);
//    }
//
//    private byte[] verifyDataMessage(byte[] buffer) throws IOException {
//
//        if (buffer.length < 8) {
//            throw new IOException("Data message does not have a minimum length of 8.");
//        }
//
//        int stx = -1;
//        int etx = -1;
//        for (int i = 0; i < buffer.length; i++) {
//            if (buffer[i] == STX) {
//                stx = i;
//            }
//            else if (i > stx && (buffer[i] == ETX || buffer[i] == EOT)) {
//                etx = i;
//                break;
//            }
//        }
//        if (stx == -1 || buffer.length - stx < 7) {
//            throw new IOException("STX (0x02) character is expected but not received as first byte of data message: " + byteToAscii(buffer));
//        }
//        else if (etx == -1 || buffer.length - etx < 1) {
//            throw new IOException("ETX (0x03) character is expected but not received as last byte of data message: " + byteToAscii(buffer));
//        }
//
//        int bcc = getBCC(Arrays.copyOfRange(buffer, stx, etx + 1));
//        if (bcc != buffer[etx + 1]) {
//            logger.trace("Calculated BCC (Block Check Character) \"{}\" is unequal received \"{}\"", bcc, buffer[etx + 1]);
//            throw new IOException("Received invalid BCC (Block Check Character) in message: " + byteToAscii(buffer));
//        }
//
//        return Arrays.copyOfRange(buffer, stx + 1, etx);
//    }
//
//    private List<DataSet> parseDataSets(byte[] buffer) throws IOException {
//
//        List<DataSet> datasets = new ArrayList<DataSet>();
//        
//        byte[] dataMsg = verifyDataMessage(buffer);
//        int i = 0;
//        for (int j = 0; j < dataMsg.length; j++) {
//            if (dataMsg[j] == 0x21 || dataMsg[j] == CR || j == dataMsg.length - 1) {
//                datasets.add(parseDataSet(Arrays.copyOfRange(dataMsg, i, j)));
//                
//                j++; while((dataMsg[j] == 0x00 || dataMsg[j] == 0x21 || dataMsg[j] == CR || dataMsg[j] == LF) && j < dataMsg.length - 1) j++;
//                i = j;
//            }
//        }
//        
//        return datasets;
//    }
//    
//    private DataSet parseDataSet(byte[] buffer) throws IOException {
//        int index = 0;
//        
//        String id = null;
//        for (int i = index; i < buffer.length; i++) {
//            if (buffer[i] == 0x28) {
//                // found '('
//                id = new String(buffer, index, i - index, charset);
//                index = i + 1;
//                break;
//            }
//        }
//        if (id == null) {
//            throw new IOException("'(' (0x28) character is expected but not received inside data block of data message: "
//                            + byteToAscii(buffer));
//        }
//
//        String value = "";
//        String unit = "";
//        for (int i = index; i < buffer.length; i++) {
//            if (buffer[i] == 0x2A) {
//                // found '*'
//                if (i > index) {
//                    value = new String(buffer, index, i - index, charset);
//                }
//                index = i + 1;
//
//                for (int j = index; j < buffer.length; j++) {
//                    if (buffer[j] == 0x29) {
//                        // found ')'
//                        unit = new String(buffer, index, j - index, charset);
//                        index = j + 1;
//                        break;
//                    }
//                }
//
//                break;
//            } else if (buffer[i] == 0x29) {
//                // found ')'
//                if (i > index) {
//                    value = new String(buffer, index, i - index, charset);
//                }
//                index = i + 1;
//                break;
//            }
//        }
//        if (buffer[index - 1] != 0x29) {
//            throw new IOException("')' (0x29) character is expected but not received inside data block of data message: "
//                            + byteToAscii(buffer));
//        }
//        
//        if (logger.isTraceEnabled()) {
//            logger.trace("Received data set: {}", byteToAscii(buffer));
//        }
//        return new DataSet(id, value, unit);
//    }
//
//    private byte[] listenForResponse() throws IOException, TimeoutException {
//        int timeout = serial.getTimeout();
//        
//        byte[] response;
//        try (ByteArrayOutputStream byteStream = new ByteArrayOutputStream()) {
//            try {
//                serial.listenForEach(byteStream, new byte[]{ ETX, EOT, ACK });
//                
//                // Reduce timeout, as following bytes have to already be in the buffer
//                serial.setTimeout(100);
//                try {
//                    // Complete reading <CR><LF> or <ETX><BCC>, <EOT><BCC>
//                    response = byteStream.toByteArray();
//                    if (response[response.length - 1] == ACK) {
//                        serial.listenForChar(byteStream, CR);
//                    }
//                    int b = serial.read();
//                    byteStream.write(b);
//                
//                } catch (TimeoutException e) {
//                    // This timeout may be thrown for legacy IEC 1107 meters, responding with plain <ACK>
//                }
//            } catch (TimeoutException e) {
//                if (logger.isTraceEnabled()) {
//                    response = byteStream.toByteArray();
//                    if (response.length > 0) {
//                        logger.trace("Received partial message when timed out: {}", byteToAscii(response));
//                    }
//                }
//                throw new TimeoutException(e.getMessage());
//                
//            } finally {
//                serial.setTimeout(timeout);
//            }
//            response = byteStream.toByteArray();
//            
//            if (logger.isTraceEnabled()) {
//                logger.trace("Received message: {}", byteToAscii(response));
//            }
//        }
//        return response;
//    }
//
//    private byte[] listenForIdentificationMessage() throws IOException, TimeoutException {
//        byte[] response;
//        try (ByteArrayOutputStream byteStream = new ByteArrayOutputStream()) {
//            try {
//                serial.listenForChar(byteStream, CR);
//                int b = serial.read();
//                byteStream.write(b);
//
//            } catch (TimeoutException e) {
//                if (logger.isTraceEnabled()) {
//                    response = byteStream.toByteArray();
//                    if (response.length > 0) {
//                        logger.trace("Received partial message when timed out: {}", byteToAscii(response));
//                    }
//                }
//                throw new TimeoutException(e.getMessage());
//            }
//            byte[] buffer = byteStream.toByteArray();
//            
//            if (logger.isTraceEnabled()) {
//                logger.trace("Received message: {}", byteToAscii(buffer));
//            }
//
//            int start = 0;
//            while (start < buffer.length) {
//                if (buffer[start] == 0x2F) {
//                    
//                    if (buffer[start + 1] == 0x00 | buffer[start + 1] == 0x7F) {
//                        start++;
//                    }
//                    else {
//                        break;
//                    }
//                    continue;
//                }
//                start++;
//            }
//            response = Arrays.copyOfRange(buffer, start, buffer.length);
//        }
//        return response;
//    }
//
//    /**
//     * Returns the baud rate chosen by the server for this communication
//     * 
//     * @param baudCharacter
//     *            Encoded baud rate (see IEC 62056-21 6.3.14 13c)
//     * @return The decoded baud rate
//     * @throws IOException
//     *             if the received char does not comply with IEC 62056-21 6.3.14 13c.
//     */
//    private int decodeBaudrate(char baudCharacter) throws IOException {
//        
//        if (baudCharacter == ':') {
//            // Legacy identification message for IEC 1107 meters
//            return -1;
//        }
//        if (baudCharacter == '0') {
//            return 300;
//        }
//        else if (baudCharacter == '1' || baudCharacter == 'A') {
//            return 600;
//        }
//        else if (baudCharacter == '2' || baudCharacter == 'B') {
//            return 1200;
//        }
//        else if (baudCharacter == '3' || baudCharacter == 'C') {
//            return 2400;
//        }
//        else if (baudCharacter == '4' || baudCharacter == 'D') {
//            return 4800;
//        }
//        else if (baudCharacter == '5' || baudCharacter == 'E') {
//            return 9600;
//        }
//        else if (baudCharacter == '6' || baudCharacter == 'F') {
//            return 19200;
//        }
//        else {
//            throw new IOException(String.format(
//                    "Syntax error in identification message received: unknown baud rate received. Baud character was 0x%02X. or char '%s'",
//                    (byte) baudCharacter, String.valueOf(baudCharacter)));
//        }
//    }
//
//    /**
//     * Returns the baud rate to acknowledge the server for this communication
//     * 
//     * @param baudCharacter
//     *            The decoded baud rate
//     * @return Encoded baud rate (see IEC 62056-21 6.3.14 13c)
//     * @throws IOException
//     *             if the received char does not comply with IEC 62056-21 6.3.14 13c.
//     */
//    private byte encodeBaudrate(int baudrate) throws IOException {
//        
//        switch (baudrate) {
//        case 300:
//            return 0x30;
//        case 600:
//            return 0x31;
//        case 1200:
//            return 0x32;
//        case 2400:
//            return 0x33;
//        case 4800:
//            return 0x34;
//        case 9600:
//            return 0x35;
//        case 19200:
//            return 0x36;
//        default:
//            throw new IOException("Unable to encode baud rate: " + baudrate);
//        }
//    }
//
//    /**
//     * Converts a byte array to an ASCII string by looping through the array.<br>
//     * IEC 62056-21 commands will be recognized and indicated with angle brackets, 
//     * e.g. 0x01 will be printed as &lt;SOH&gt;. An empty hex value 0x00 will be 
//     * converted to a blank space.
//     *
//     * @param bytes
//     *             the message to be converted.
//     * @return the message as an ASCII string.
//     */
//    public static String byteToAscii(byte[] bytes) {
//        StringBuilder ascii = new StringBuilder();
//        
//        String lastChar = "";
//        for (int i = 0; i < bytes.length; i++) {
//            int v = bytes[i] & 0xFF;
//            String hex = String.valueOf(hexArray[v >>> 4]) + String.valueOf(hexArray[v & 0x0F]);
//            
//            String newChar = null;
//            switch (hex) {
//                case "0D":
//                    newChar = "<CR>";
//                    break;
//                case "0A":
//                    newChar = "<LF>";
//                    break;
//                case "01":
//                    newChar = "<SOH>";
//                    break;
//                case "02":
//                    newChar = "<STX>";
//                    break;
//                case "03":
//                    newChar = "<ETX>";
//                    break;
//                case "04":
//                    newChar = "<EOT>";
//                    break;
//                case "06":
//                    newChar = "<ACK>";
//                    break;
//                case "15":
//                    newChar = "<NAK>";
//                    break;
//                case "00":
//                    if (ascii.length() > 0 && !lastChar.equals(" "))
//                        newChar = " ";
//                    break;
//                default:
//                    // Convert to hex string to an ascii letter
//                    newChar = Character.toString((char) Integer.parseInt(hex, 16));
//                    break;
//            }
//            
//            if (newChar != null) {
//                ascii.append(newChar);
//                lastChar = newChar;
//            }
//        }
//        return ascii.toString();
//    }
//
//    /**
//     * Converts an ASCII string to hex by looping through every single character.
//     * IEC 62056-21 commands are possible by using angle brackets, e.g &lt;BCC&gt; will be automatically calculated when calling <code>getBCC()</code>.
//     *
//     * @param ascii
//     *             the string to be converted.
//     * @return the message as a byte array.
//     */
//    public static byte[] asciiToHexBytes(String ascii) {
//        String[] hexArr = new String[ascii.length()];
//
//        int i = 0, j = 0;
//        while (j < ascii.length()) {
//            String hexChar = null;
//            
//            if (ascii.charAt(j) == '<') {
//                String command = "";
//                do {
//                    command += ascii.charAt(j);
//                    if (ascii.charAt(j+1) == '>') command += '>';
//                    j++;
//                } while (ascii.charAt(j) != '>');
//
//                switch (command) {
//                    case "<CR>":
//                        hexChar = "0D";
//                        break;
//                    case "<LF>":
//                        hexChar = "0A";
//                        break;
//                    case "<SOH>":
//                        hexChar = "01";
//                        break;
//                    case "<STX>":
//                        hexChar = "02";
//                        break;
//                    case "<ETX>":
//                        hexChar = "03";
//                        break;
//                    case "<EOT>":
//                        hexChar = "04";
//                        break;
//                    case "<ACK>":
//                        hexChar = "06";
//                        break;
//                    case "<NAK>":
//                        hexChar = "15";
//                        break;
//                    case "<BCC>":
//                        hexChar = getBCC(Arrays.copyOfRange(hexArr, 0, i + 1));
//                        break;
//                    default:
//                        logger.debug("Unknown IEC 62056-21 protocol command: " + command);
//                        break;
//                }
//            }
//            else {
//                hexChar = Integer.toHexString(ascii.charAt(j));
//            }
//            hexArr[i] = hexChar;
//            
//            i++;
//            j++;
//        }
//
//        hexArr = Arrays.copyOfRange(hexArr, 0, i + 1);
//        byte[] byteArr = new byte[hexArr.length];
//        for(i = 0; i < hexArr.length; i++) {
//            byteArr[i] = (byte) Integer.parseInt(hexArr[i], 16);
//        }
//        return byteArr;
//    }
//    
//    /**
//     * Returns the Block Check Character of a given hex string array.
//     * 
//     * @param hexString
//     *            the hex string of which the BCC will be calculated.
//     * @return The calculated BCC.
//     */
//    private static String getBCC(String[] hexArr) {
//        byte bcc = 0x00;
//        for(int i = 1; i < hexArr.length; i++) {
//            bcc^= (byte)Integer.parseInt(hexArr[i], 16); 
//        }
//        String bccStr = Integer.toHexString(bcc & 0xFF);
//        
//        if (bccStr.length() < 2) bccStr = "0" + bccStr;
//
//        return bccStr;
//    }
//
//    private static int getBCC(byte[] byteArr) {
//        byte bcc = 0x00;
//        for(int i = 1; i < byteArr.length; i++) {
//            bcc ^= byteArr [i];
//        }
//
//        return bcc;
//    }
//
//}