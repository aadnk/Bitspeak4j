package com.comphenix.bitspeak;

import java.io.InputStream;
import java.io.Reader;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

/**
 * Represents a bitspeak format (BS-6 or BS-8)
 */
public class Bitspeak {
    enum Format {
        BS_6,
        BS_8
    }

    private static final Bitspeak BITSPEAK_BS_6 = new Bitspeak(Format.BS_6);
    private static final Bitspeak BITSPEAK_BS_8 = new Bitspeak(Format.BS_8);
    private static final Collection<Bitspeak> FORMATS = Collections.unmodifiableList(Arrays.asList(BITSPEAK_BS_6, BITSPEAK_BS_8));

    private final Bitspeak.Format format;

    private Bitspeak(Format format) {
        this.format = format;
    }

    /**
     * Retrieve an unmodifiable view of all the bitspeak formats.
     * @return Collection with every bitspeak format.
     */
    public static Collection<Bitspeak> formats() {
        return FORMATS;
    }

    public static Bitspeak bs6() {
        return BITSPEAK_BS_6;
    }

    public static Bitspeak bs8() {
        return BITSPEAK_BS_8;
    }

    public byte[] decode(String bitspeak) {
        return decode(bitspeak.toCharArray());
    }

    public byte[] decode(char[] bitspeak) {
        return decode(bitspeak, 0, bitspeak.length);
    }

    public byte[] decode(char[] bitspeak, int offset, int length) {
        // Calculate buffer size
        byte[] buffer = new byte[estimateDecodeSize(length)];
        int written = 0;

        BitspeakDecoder decoder = newDecoder();
        written += decoder.decodeBlock(bitspeak, offset, length, buffer, 0, buffer.length);
        written += decoder.finishBlock(buffer, written, buffer.length - written);

        // Create final array
        return written < buffer.length ? Arrays.copyOf(buffer, written) : buffer;
    }

    /**
     * Retrieve the number of bytes needed to store the given character array with this format of bitspeak.
     * @param characterCount the character count.
     * @return The number of bytes.
     */
    public int estimateDecodeSize(int characterCount) {
        if (characterCount < 0) {
            throw new IllegalArgumentException("characterCount cannot be negative");
        }
        switch (format) {
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

    public BitspeakDecoder newDecoder() {
        return BitspeakDecoder.newDecoder(format);
    }

    public InputStream newDecodeStream(Reader reader) {
        return newDecodeStream(reader, 4096);
    }

    public InputStream newDecodeStream(Reader reader, int bufferSize) {
        return BitspeakDecoder.newDecoder(format).wrap(reader, bufferSize);
    }

    public String encode(byte[] data) {
        return encode(data, 0, data.length);
    }

    public String encode(byte[] data, int offset, int length) {
        // Calculate buffer size
        char[] buffer = new char[estimateEncodeSize(length)];
        int written = 0;

        BitspeakEncoder encoder = newEncoder();
        written += encoder.encodeBlock(data, offset, length, buffer, 0, buffer.length);
        written += encoder.finishBlock(buffer, written, buffer.length - written);

        // Create final string
        return new String(buffer, 0, written);
    }

    /**
     * Retrieve the number of characters needed to store the given bytes with the current bitspeak format.
     * @param byteCount the number of bytes.
     * @return The maximum number of characters needed.
     */
    public int estimateEncodeSize(int byteCount) {
        if (byteCount < 0) {
            throw new IllegalArgumentException("byteCount cannot be negative");
        }
        switch (format) {
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

    public BitspeakEncoder newEncoder() {
        return BitspeakEncoder.newEncoder(format);
    }

    public Reader newEncodeStream(InputStream input) {
        return newEncodeStream(input, 4096);
    }

    public Reader newEncodeStream(InputStream input, int bufferSize) {
        return BitspeakEncoder.newEncoder(format).wrap(input, bufferSize);
    }

    @Override
    public String toString() {
        return "Bitspeak{" +
                "format=" + format +
                '}';
    }
}
