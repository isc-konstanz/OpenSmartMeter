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
package org.openmuc.iec62056.data;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.util.Arrays;

public class Converter {
    public static final Charset ASCII_CHARSET = Charset.forName("US-ASCII");

    public static String toHexString(byte b) {
        StringBuilder builder = new StringBuilder();
        appendHexString(b, builder);
        return builder.toString();
    }

    public static String toHexString(byte[] bytes) {
        return toHexString(bytes, 0, bytes.length);
    }

    public static String toHexString(byte[] bytes, int offset, int length) {
        StringBuilder builder = new StringBuilder();

        int l = 1;
        for (int i = offset; i < (offset + length); i++) {
            if ((l != 1) && ((l - 1) % 8 == 0)) {
                builder.append(' ');
            }
            if ((l != 1) && ((l - 1) % 16 == 0)) {
                builder.append('\n');
            }
            l++;
            appendHexString(bytes[i], builder);
            if (i != offset + length - 1) {
                builder.append(' ');
            }
        }

        return builder.toString();
    }

    /**
     * Returns the integer value as hex string filled with leading zeros. If you do not want leading zeros use
     * Integer.toHexString(int i) instead.
     * 
     * @param i
     *            the integer value to be converted
     * @return the hex string
     */
    public static String toShortHexString(int i) {
        byte[] bytes = new byte[] { (byte) (i >> 24), (byte) (i >> 16), (byte) (i >> 8), (byte) (i) };
        return toShortHexString(bytes);
    }

    /**
     * Returns the long value as hex string filled with leading zeros. If you do not want leading zeros use
     * Long.toHexString(long i) instead.
     * 
     * @param l
     *            the long value to be converted
     * @return the hex string
     */
    public static String toShortHexString(long l) {
        byte[] bytes = new byte[] { (byte) (l >> 56), (byte) (l >> 48), (byte) (l >> 40), (byte) (l >> 32),
                (byte) (l >> 24), (byte) (l >> 16), (byte) (l >> 8), (byte) (l) };
        return toShortHexString(bytes);
    }

    /**
     * Returns the byte as a hex string. If b is less than 16 the hex string returned contains a leading zero.
     * 
     * @param b
     *            the byte to be converted
     * @return the byte as a hex string.
     */
    public static String toShortHexString(byte b) {
        return toShortHexString(new byte[] { b });
    }

    public static String toShortHexString(byte[] bytes) {
        return toShortHexString(bytes, 0, bytes.length);
    }

    private final static char[] hexArray = "0123456789ABCDEF".toCharArray();

    public static String toShortHexString(byte[] bytes, int offset, int length) {
        char[] hexChars = new char[length * 2];
        for (int j = offset; j < (offset + length); j++) {
            int v = bytes[j] & 0xff;
            hexChars[j * 2] = hexArray[v >>> 4];
            hexChars[j * 2 + 1] = hexArray[v & 0x0f];
        }
        return new String(hexChars);
    }

    public static byte[] fromShortHexString(String shortHexString) throws NumberFormatException {

        validate(shortHexString);

        int length = shortHexString.length();

        byte[] data = new byte[length / 2];
        for (int i = 0; i < length; i += 2) {
            int firstCharacter = Character.digit(shortHexString.charAt(i), 16);
            int secondCharacter = Character.digit(shortHexString.charAt(i + 1), 16);

            if (firstCharacter == -1 || secondCharacter == -1) {
                throw new NumberFormatException("string is not a legal hex string.");
            }

            data[i / 2] = (byte) ((firstCharacter << 4) + secondCharacter);
        }
        return data;
    }

    public static void appendShortHexString(byte b, StringBuilder builder) {
        builder.append(toShortHexString(b));
    }

    public static void appendShortHexString(StringBuilder builder, byte[] bytes, int offset, int length) {
        builder.append(toShortHexString(bytes, offset, length));
    }

    public static void appendHexString(byte b, StringBuilder builder) {
        builder.append("0x");
        appendShortHexString(b, builder);
    }

    public static void appendHexString(StringBuilder builder, byte[] byteArray, int offset, int length) {
        int l = 1;
        for (int i = offset; i < (offset + length); i++) {
            if ((l != 1) && ((l - 1) % 8 == 0)) {
                builder.append(' ');
            }
            if ((l != 1) && ((l - 1) % 16 == 0)) {
                builder.append('\n');
            }
            l++;
            appendHexString(byteArray[i], builder);
            if (i != offset + length - 1) {
                builder.append(' ');
            }
        }
    }

    private static void validate(String s) {
        if (s == null) {
            throw new IllegalArgumentException("string s may not be null");
        }

        if ((s.length() == 0) || ((s.length() % 2) != 0)) {
            throw new NumberFormatException("string is not a legal hex string.");
        }
    }

    /**
     * Converts a byte array to an ASCII string by looping through the array.<br>
     * IEC 62056-21 commands will be recognized and indicated with angle brackets, 
     * e.g. 0x01 will be printed as &lt;SOH&gt;. An empty hex value 0x00 will be 
     * converted to a blank space.
     *
     * @param bytes
     *             the message to be converted.
     * @return the message as an ASCII string.
     */
    public static String toAsciiString(byte[] bytes) {
    	CharsetDecoder ascii = ASCII_CHARSET.newDecoder();
        try (ByteArrayOutputStream byteStream = new ByteArrayOutputStream()) {
            for (int i = 0; i < bytes.length; i++) {
                int v = bytes[i] & 0xFF;

                switch (v) {
                    case 0x0D:
                    	byteStream.write("<CR>".getBytes(Converter.ASCII_CHARSET));
                        break;
                    case 0x0A:
                        byteStream.write("<LF>".getBytes(Converter.ASCII_CHARSET));
                        break;
                    case 0x01:
                        byteStream.write("<SOH>".getBytes(Converter.ASCII_CHARSET));
                        break;
                    case 0x02:
                        byteStream.write("<STX>".getBytes(Converter.ASCII_CHARSET));
                        break;
                    case 0x03:
                        byteStream.write("<ETX>".getBytes(Converter.ASCII_CHARSET));
                        break;
                    case 0x04:
                        byteStream.write("<EOT>".getBytes(Converter.ASCII_CHARSET));
                        break;
                    case 0x06:
                        byteStream.write("<ACK>".getBytes(Converter.ASCII_CHARSET));
                        break;
                    case 0x15:
                        byteStream.write("<NAK>".getBytes(Converter.ASCII_CHARSET));
                        break;
                    case 0x00:
                        byteStream.write(" ".getBytes(Converter.ASCII_CHARSET));
                        break;
                    default:
                        // Convert to hex string to an ascii letter
                        String hex = String.valueOf(hexArray[v >>> 4]) + String.valueOf(hexArray[v & 0x0F]);
                    	byteStream.write((byte) Integer.parseInt(hex, 16));
                        break;
                }
            }
            return ascii.decode(ByteBuffer.wrap(byteStream.toByteArray())).toString();
            
        } catch (IOException e) {
		}
        return "";
    }

    /**
     * Converts an ASCII string to hex by looping through every single character.
     * IEC 62056-21 commands are possible by using angle brackets, e.g &lt;BCC&gt; will be automatically calculated when calling <code>Bcc()</code>.
     *
     * @param ascii
     *             the string to be converted.
     * @return the message as a byte array.
     */
    public static byte[] toHexBytes(String ascii) {
        String[] hexArr = new String[ascii.length()];
        
        int i = 0, j = 0;
        while (j < ascii.length()) {
            String hexChar = null;
            
            if (ascii.charAt(j) == '<') {
                String command = "";
                do {
                    command += ascii.charAt(j);
                    if (ascii.charAt(j+1) == '>') command += '>';
                    j++;
                } while (ascii.charAt(j) != '>');

                switch (command) {
                    case "<CR>":
                        hexChar = "0D";
                        break;
                    case "<LF>":
                        hexChar = "0A";
                        break;
                    case "<SOH>":
                        hexChar = "01";
                        break;
                    case "<STX>":
                        hexChar = "02";
                        break;
                    case "<ETX>":
                        hexChar = "03";
                        break;
                    case "<EOT>":
                        hexChar = "04";
                        break;
                    case "<ACK>":
                        hexChar = "06";
                        break;
                    case "<NAK>":
                        hexChar = "15";
                        break;
                    case "<BCC>":
                        hexChar = Bcc.get(Arrays.copyOfRange(hexArr, 0, i + 1));
                        break;
                    default:
                        break;
                }
            }
            else {
                hexChar = Integer.toHexString(ascii.charAt(j));
            }
            hexArr[i] = hexChar;
            
            i++;
            j++;
        }
        
        hexArr = Arrays.copyOfRange(hexArr, 0, i + 1);
        byte[] byteArr = new byte[hexArr.length];
        for(i = 0; i < hexArr.length; i++) {
            byteArr[i] = (byte) Integer.parseInt(hexArr[i], 16);
        }
        return byteArr;
    }

    /**
     * Don't let anyone instantiate this class.
     */
    private Converter() {
    }
}
