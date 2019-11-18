package com.comphenix.bitspeak;

public enum BitspeakFormat {
    BS_6,
    BS_8;

    /**
     * Retrieve the number of bytes needed to store the given character array with bitspeak.
     * @param characterCount the character count.
     * @return The number of bytes.
     */
    public int getMaxDecodeSize(int characterCount) {
        if (characterCount < 0) {
            throw new IllegalArgumentException("characterCount cannot be negative");
        }

        switch (this) {
            case BS_6:
                // Compute number of bits needed to store the number
                long bits = (characterCount / 2) * (long)6 + ((characterCount & 1) == 1 ? 4 : 0);
                return (int) Math.ceil(bits / 8.0);

            case BS_8:
                // At least half (for instance, papapa)
                return (int) Math.ceil(characterCount / 2.0);
            default:
                throw new IllegalArgumentException("Unknown format: " + this);
        }
    }

    /**
     * Retrieve the number of characters needed to store the given bytes with bitspeak.
     * @param byteCount the number of bytes.
     * @return The maximum number of characters needed.
     */
    public int getMaxEncodeSize(int byteCount) {
        if (byteCount < 0) {
            throw new IllegalArgumentException("byteCount cannot be negative");
        }
        switch (this) {
            case BS_6:
                long bits = (long)byteCount * 8;
                return (int) (2 * (bits / 6) + (bits % 6 == 0 ? 0 : 1));
            case BS_8:
                // could be as much as 4 characters per byte (0110 0101 -> shan)
                return 4 * byteCount;
            default:
                throw new IllegalArgumentException("Unknown format: " + this);
        }
    }
}
