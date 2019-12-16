package com.comphenix.bitspeak.console.util;

public final class HexEncoding {
    private HexEncoding() {
        // Sealed
    }

    /**
     * Decode the given hexadecimal representation of a byte array.
     * <p>
     * Each 4 bit value is represented using a hexadecimal integer, using the alphabet 0 - 9, and A - F
     * (lowercase or uppercase). The order is big-endian (high-order, then low-order).
     * @param hex string of hex characters.
     * @return The corresponding decoded byte array.
     */
    public static byte[] decodeHex(String hex) {
        if ((hex.length() % 2) != 0) {
            // Likely an error (although we could handle this with padding)
            throw new IllegalArgumentException("Hexadecimal string must be divisible by two");
        }
        byte[] data = new byte[hex.length() / 2];

        for (int i = 0; i < data.length; i++) {
            int value = (Character.digit(hex.charAt(2 * i), 16) << 4) |
                         Character.digit(hex.charAt(2 * i + 1), 16);
            data[i] = (byte) value;
        }
        return data;
    }
}
