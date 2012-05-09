package org.moca.util;

public class Math {

	public static int crc16_ccitt(byte[] buffer, int offset, int size) {
		int crc = 0xFFFF; // Start value is 0xFFFF
		int poly = 0x1021; // Standard CCITT polynomial
		
		for (int j = 0; j < size; j++) {
			byte b = buffer[offset + j]; 
			for (int i = 0; i < 8; i++) {
				boolean bit = ((b   >> (7-i) & 1) == 1);
                boolean c15 = ((crc >> 15    & 1) == 1);
                crc <<= 1;
                if (c15 ^ bit) crc ^= poly;
			}
		}
		return crc & 0xffff;
	}

}
