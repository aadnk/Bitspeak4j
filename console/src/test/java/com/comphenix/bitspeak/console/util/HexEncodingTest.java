package com.comphenix.bitspeak.console.util;

import org.junit.Test;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;

public class HexEncodingTest {
    @Test
    public void testDecoding() {
        byte[] expected = new byte[] { 0x01, 0x54, (byte) 0x98, (byte) 0xAB, (byte) 0xFF};
        byte[] actual = HexEncoding.decodeHex("015498ABff");

        assertArrayEquals(expected, actual);
    }
}
