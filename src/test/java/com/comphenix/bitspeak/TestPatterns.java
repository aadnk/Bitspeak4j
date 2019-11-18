package com.comphenix.bitspeak;

import java.util.Arrays;

class TestPatterns {
    public static byte[] generatePattern(int value, int length) {
        byte[] array = new byte[length];
        Arrays.fill(array, (byte) value);
        return array;
    }
}
