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

public class Bcc {

    public byte value = 0;

    /**
     * Returns the Block Check Character of a given byte array.
     * 
     * @param byteArr
     *            the byte array of which the BCC will be calculated.
     * @return The calculated BCC.
     */
    public static byte get(byte[] byteArr) {
        byte bcc = 0x00;
        for(int i = 1; i < byteArr.length; i++) {
            bcc ^= byteArr [i];
        }

        return bcc;
    }

    /**
     * Returns the Block Check Character of a given hex string array.
     * 
     * @param hexArr
     *            the hex string of which the BCC will be calculated.
     * @return The calculated BCC.
     */
    public static String get(String[] hexArr) {
        byte bcc = 0x00;
        for(int i = 1; i < hexArr.length; i++) {
            bcc^= (byte)Integer.parseInt(hexArr[i], 16); 
        }
        String bccStr = Integer.toHexString(bcc & 0xFF);
        
        if (bccStr.length() < 2) bccStr = "0" + bccStr;

        return bccStr;
    }

}
