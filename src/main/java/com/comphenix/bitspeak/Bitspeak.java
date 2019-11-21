/*
 * Copyright (C) 2019  Kristian S. Stangeland
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 */

package com.comphenix.bitspeak;

import java.io.InputStream;
import java.io.Reader;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Objects;

/**
 * Represents a bitspeak format (BS-6 or BS-8) for encoding a stream of bytes to pronounceable text, and back again.
 * <p>
 * A bitspeak format can be retrieved using the factory methods {@link Bitspeak#bs6()} or {@link Bitspeak#bs8()}. Then,
 * using a format, convert a byte array to a bitspeak string by calling its {@link Bitspeak#encode(byte[])} method. The
 * resulting string can be converted back to the original byte array by calling {@link Bitspeak#decode(String)}.
 * </p>
 * <p>
 * It is also possible to convert streams of bitspeak characters or byte arrays using {@link Bitspeak#newEncodeStream(InputStream)}
 * or {@link Bitspeak#newDecodeStream(Reader)}.
 * </p>
 * <br>
 * <h2>Examples</h2>
 * <table style="border-spacing: 4px">
 *   <tr>
 *     <th>Example</th>
 *     <th>Output</th>
 *   </tr>
 *   <tr>
 *     <td><code>Bitspeak.bs6().encode(new byte[] { 1 })</code></td>
 *     <td>"pak"</td>
 *   </tr>
 *   <tr>
 *     <td><code>Bitspeak.bs8().encode(new byte[] { 1 })</code></td>
 *     <td>"pe"</td>
 *   </tr>
 *   <tr>
 *     <td><code>Bitspeak.bs6().encode(new byte[] { 1, 2 })</code></td>
 *     <td>"pakat"</td>
 *   </tr>
 *   <tr>
 *     <td><code>Bitspeak.bs8().encode(new byte[] { 1, 2 })</code></td>
 *     <td>"pepi"</td>
 *   </tr>
 *   <tr>
 *     <td><code>Bitspeak.bs6().encode(new byte[] { 1, 2, 3 })</code></td>
 *     <td>"pakatape"</td>
 *   </tr>
 *   <tr>
 *     <td><code>Bitspeak.bs8().encode(new byte[] { 1, 2, 3 })</code></td>
 *     <td>"pepipo"</td>
 *   </tr>
 *   <tr>
 *     <td><code>Bitspeak.bs6().encode(new byte[] { 1, 2, 3, 4 })</code></td>
 *     <td>"pakatapepup"</td>
 *   </tr>
 *   <tr>
 *     <td><code>Bitspeak.bs8().encode(new byte[] { 1, 2, 3, 4 })</code></td>
 *     <td>"pepipopu"</td>
 *   </tr>
 *   <tr>
 *     <td><code>Bitspeak.bs6().encode(new byte[] { -34, -83, -66, -17, 1, 2, 3 })</code></td>
 *     <td>"nelinizisemabapipam"</td>
 *   </tr>
 *   <tr>
 *     <td><code>Bitspeak.bs8().encode(new byte[] { -34, -83, -66, -17, 1, 2, 3 })</code></td>
 *     <td>"yawluirawsowpepipo"</td>
 *   </tr>
 * </table>
 * @see <a href="https://github.com/MaiaVictor/Bitspeak">https://github.com/MaiaVictor/Bitspeak</a>
 */
public class Bitspeak {
    enum Format {
        BS_6,
        BS_8
    }

    private static final Bitspeak BITSPEAK_BS_6 = new Bitspeak(Format.BS_6, BitspeakConfig.defaultConfig());
    private static final Bitspeak BITSPEAK_BS_8 = new Bitspeak(Format.BS_8, BitspeakConfig.defaultConfig());
    private static final Collection<Bitspeak> FORMATS = Collections.unmodifiableList(Arrays.asList(BITSPEAK_BS_6, BITSPEAK_BS_8));

    private final Bitspeak.Format format;
    private final BitspeakConfig config;

    private Bitspeak(Format format, BitspeakConfig config) {
        this.format = Objects.requireNonNull(format, "format cannot be NULL");
        this.config = Objects.requireNonNull(config, "config cannot be NULL");;
    }

    /**
     * Retrieve an unmodifiable view of all the bitspeak formats in the system.
     * @return Collection with every bitspeak format.
     */
    public static Collection<Bitspeak> formats() {
        return FORMATS;
    }

    /**
     * Retrieve a bitspeak format (BS-6) where every 2 characters represents 6 bits of data.
     * <p>
     * The format uses the following lookup tables to convert between bits and characters. The
     * first bits will be converted using the consonant table, then the vowel table, and so on, alternating
     * until the last chunk of bits.
     * </p>
     * <p>If the bit stream is not evenly divisible by 6, it might end with less bits than required by the
     * current lookup table (for instance, "11" when the consonant lookup table is active). In that case, the bit stream
     * is padded with zeroes at the end (making the end "1100" in the previous example).</p>
     * <h2>Consonants lookup table:</h2>
     * <table>
     *   <tr>
     *       <th>Bits</th>
     *       <th>Character</th>
     *   </tr>
     *   <tr>
     *     <td>0000</td>
     *     <td align="center">p</td>
     *   </tr>
     *   <tr>
     *     <td>0001</td>
     *     <td align="center">b</td>
     *   </tr>
     *   <tr>
     *     <td>0010</td>
     *     <td align="center">t</td>
     *   </tr>
     *   <tr>
     *     <td>0011</td>
     *     <td align="center">d</td>
     *   </tr>
     *   <tr>
     *     <td>0100</td>
     *     <td align="center">k</td>
     *   </tr>
     *   <tr>
     *     <td>0101</td>
     *     <td align="center">g</td>
     *   </tr>
     *   <tr>
     *     <td>0110</td>
     *     <td align="center">x</td>
     *   </tr>
     *   <tr>
     *     <td>0111</td>
     *     <td align="center">j</td>
     *   </tr>
     *   <tr>
     *     <td>1000</td>
     *     <td align="center">f</td>
     *   </tr>
     *   <tr>
     *     <td>1001</td>
     *     <td align="center">v</td>
     *   </tr>
     *   <tr>
     *     <td>1010</td>
     *     <td align="center">l</td>
     *   </tr>
     *   <tr>
     *     <td>1011</td>
     *     <td align="center">r</td>
     *   </tr>
     *   <tr>
     *     <td>1100</td>
     *     <td align="center">m</td>
     *   </tr>
     *   <tr>
     *     <td>1101</td>
     *     <td align="center">n</td>
     *   </tr>
     *   <tr>
     *     <td>1110</td>
     *     <td align="center">s</td>
     *   </tr>
     *   <tr>
     *     <td>1111</td>
     *     <td align="center">z</td>
     *   </tr>
     * </table>
     *
     * <br>
     * <h2>Vowels lookup table:</h2>
     * <table>
     *   <tr>
     *       <th>Bits</th>
     *       <th>Character</th>
     *   </tr>
     *   <tr>
     *     <td>00</td>
     *     <td align="center">a</td>
     *   </tr>
     *   <tr>
     *     <td>01</td>
     *     <td align="center">u</td>
     *   </tr>
     *   <tr>
     *     <td>10</td>
     *     <td align="center">i</td>
     *   </tr>
     *   <tr>
     *     <td>11</td>
     *     <td align="center">e</td>
     *   </tr>
     * </table>
     * @return A BS-6 bitspeak format.
     */
    public static Bitspeak bs6() {
        return BITSPEAK_BS_6;
    }

    /**
     * Retrieve a bitspeak format where every 8 bits (1 byte) is converted into a consonant and vowel pair, each up two
     * 2 characters in length. The high-order of the byte is converted to a consonant, while the lower order is converted
     * to a vowel, using the following lookup tables.
     * <h2>Lookup tables:</h2>
     * <table>
     *   <tr>
     *     <th>Bits</th>
     *     <th align="center">Consonant</th>
     *     <th align="center">Vowel</th>
     *   </tr>
     *   <tr>
     *     <td>0000</td>
     *     <td align="center">p</td>
     *     <td align="center">a</td>
     *   </tr>
     *   <tr>
     *     <td>0001</td>
     *     <td align="center">b</td>
     *     <td align="center">e</td>
     *   </tr>
     *   <tr>
     *     <td>0010</td>
     *     <td align="center">t</td>
     *     <td align="center">i</td>
     *   </tr>
     *   <tr>
     *     <td>0011</td>
     *     <td align="center">d</td>
     *     <td align="center">o</td>
     *   </tr>
     *   <tr>
     *     <td>0100</td>
     *     <td align="center">k</td>
     *     <td align="center">u</td>
     *   </tr>
     *   <tr>
     *     <td>0101</td>
     *     <td align="center">g</td>
     *     <td align="center">an</td>
     *   </tr>
     *   <tr>
     *     <td>0110</td>
     *     <td align="center">ch</td>
     *     <td align="center">en</td>
     *   </tr>
     *   <tr>
     *     <td>0111</td>
     *     <td align="center">j</td>
     *     <td align="center">in</td>
     *   </tr>
     *   <tr>
     *     <td>1000</td>
     *     <td align="center">f</td>
     *     <td align="center">un</td>
     *   </tr>
     *   <tr>
     *     <td>1001</td>
     *     <td align="center">v</td>
     *     <td align="center">on</td>
     *   </tr>
     *   <tr>
     *     <td>1010</td>
     *     <td align="center">l</td>
     *     <td align="center">ai</td>
     *   </tr>
     *   <tr>
     *     <td>1011</td>
     *     <td align="center">r</td>
     *     <td align="center">ei</td>
     *   </tr>
     *   <tr>
     *     <td>1100</td>
     *     <td align="center">m</td>
     *     <td align="center">oi</td>
     *   </tr>
     *   <tr>
     *     <td>1101</td>
     *     <td align="center">y</td>
     *     <td align="center">ui</td>
     *   </tr>
     *   <tr>
     *     <td>1110</td>
     *     <td align="center">s</td>
     *     <td align="center">aw</td>
     *   </tr>
     *   <tr>
     *     <td>1111</td>
     *     <td align="center">z</td>
     *     <td align="center">ow</td>
     *   </tr>
     * </table>
     * @return A BS-8 bitspeak format.
     */
    public static Bitspeak bs8() {
        return BITSPEAK_BS_8;
    }

    /**
     * Retrieve a bitspeak format with the given configuration.
     * @param configuration the configuration, cannot be NULL.
     * @return The bitspeak format instance with this configuration.
     */
    public Bitspeak withConfig(BitspeakConfig configuration) {
        return new Bitspeak(format, configuration);
    }

    /**
     * Convert the given bitspeak-encoded string back to the original byte array.
     * @param bitspeak the bitspeak string.
     * @return The original byte array.
     */
    public byte[] decode(String bitspeak) {
        return decode(bitspeak.toCharArray());
    }

    /**
     * Convert the given bitspeak-encoded char array back to the original byte array.
     * @param bitspeak the bitspeak char array.
     * @return The original byte array.
     */
    public byte[] decode(char[] bitspeak) {
        return decode(bitspeak, 0, bitspeak.length);
    }

    /**
     * Convert the given bitspeak-encoded char array back to the original byte array.
     * @param bitspeak the bitspeak char array.
     * @param offset the position of the first character to read from the array.
     * @param length the number of characters to decode from the char array.
     * @return The original byte array.
     */
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

    /**
     * Create a new bitspeak decoder for the current format.
     * @return A new bitspeak decoder.
     */
    public BitspeakDecoder newDecoder() {
        return BitspeakDecoder.newDecoder(format, config);
    }

    /**
     * Stream the bits represented by the bitspeak characters in the given reader as an input stream.
     * @param reader the reader.
     * @return The corresponding input stream.
     */
    public InputStream newDecodeStream(Reader reader) {
        return newDecodeStream(reader, 4096);
    }

    /**
     * Stream the bits represented by the bitspeak characters in the given reader as an input stream.
     * @param reader the reader.
     * @param bufferSize the number of characters to buffer during decoding.
     * @return The corresponding input stream.
     */
    public InputStream newDecodeStream(Reader reader, int bufferSize) {
        return newDecoder().wrap(reader, 2 * bufferSize, bufferSize);
    }

    /**
     * Stream the bits represented by the bitspeak characters in the given reader as an input stream.
     * @param reader the reader.
     * @param byteBufferSize the number of bytes to buffer during decoding.
     * @param charBufferSize the number of characters to buffer during decoding.
     * @return The corresponding input stream.
     */
    public InputStream newDecodeStream(Reader reader, int byteBufferSize, int charBufferSize) {
        return newDecoder().wrap(reader, byteBufferSize, charBufferSize);
    }

    /**
     * Encode all the bytes in the given byte array as bitspeak characters, using the current format.
     * @param data the bytes to encode.
     * @return The encoded characters.
     */
    public String encode(byte[] data) {
        return encode(data, 0, data.length);
    }

    /**
     * Encode a range of bytes in the given byte array as bitspeak characters, using the current format.
     * @param data the bytes to encode.
     * @param offset the index of the first byte to encode.
     * @param length the number of bytes to encode.
     * @return The encoded characters.
     */
    public String encode(byte[] data, int offset, int length) {
        // Buffer was not large enough
        StringBuilder builder = new StringBuilder(estimateEncodeSize(length));

        BitspeakEncoder encoder = newEncoder();
        char[] buffer = new char[4096];

        while (encoder.getReadCount() < length) {
            int readCount = (int) encoder.getReadCount();

            int written = encoder.encodeBlock(data, readCount, length - readCount,
                    buffer, 0, buffer.length);
            builder.append(buffer, 0, written);
        }

        // Finish writing
        while (true) {
            int written = encoder.finishBlock(buffer, 0, buffer.length);

            if (written <= 0) {
                break;
            }
            builder.append(buffer, 0, written);
        }
        return builder.toString();
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
                int characters = (int) (2 * (bits / 6) + (bits % 6 == 0 ? 0 : 1));

                return adjustDelimiterCount(characters);

            case BS_8:
                // could be as much as 4 characters per byte (0110 0101 -> shan)
                return adjustDelimiterCount(4 * byteCount);
            default:
                throw new IllegalArgumentException("Unknown format: " + this);
        }
    }

    private int adjustDelimiterCount(int characters) {
        if (config.getMaxWordSize() > 0 || config.getMaxLineSize() > 0) {
            int words = config.getMaxWordSize() > 0 ? (int) Math.ceil(characters / (double) config.getMaxWordSize()) : 0;

            characters += config.getWordDelimiter().length() * words;
            int lines = config.getMaxLineSize() > 0 ? (int) Math.ceil(characters / (double) config.getMaxLineSize()) : 0;

            characters += config.getLineDelimiter().length() * lines;
        }
        return characters;
    }

    /**
     * Create a new bitspeak encoder for the current format.
     * @return A new bitspeak encoder.
     */
    public BitspeakEncoder newEncoder() {
        return BitspeakEncoder.newEncoder(format, config);
    }

    /**
     * Create a new stream of bitspeak characters, using the bytes in the given input stream and the current bitspeak format.
     * @param input the input stream.
     * @return A readable stream of bitspeak characters.
     */
    public Reader newEncodeStream(InputStream input) {
        return newEncodeStream(input, 4096);
    }

    /**
     * Create a new stream of bitspeak characters, using the bytes in the given input stream and the current bitspeak format.
     * @param input the input stream.
     * @param bufferSize the number of characters to buffer.
     * @return A readable stream of bitspeak characters.
     */
    public Reader newEncodeStream(InputStream input, int bufferSize) {
        return newEncoder().wrap(input, 2 * bufferSize, bufferSize);
    }

    /**
     * Create a new stream of bitspeak characters, using the bytes in the given input stream and the current bitspeak format.
     * @param input the input stream.
     * @param byteBufferSize the number of bytes to buffer during decoding.
     * @param charBufferSize the number of characters to buffer during decoding.
     * @return A readable stream of bitspeak characters.
     */
    public Reader newEncodeStream(InputStream input, int byteBufferSize, int charBufferSize) {
        return newEncoder().wrap(input, byteBufferSize, charBufferSize);
    }

    /**
     * Retrieve the name of the current bitspeak format, either bs6 or bs8.
     * @return The name of the format,
     */
    public String name() {
        switch (format) {
            case BS_6: return "BS-6";
            case BS_8: return "BS-8";
            default:
                throw new IllegalStateException("Unknown format: " + format);
        }
    }

    @Override
    public String toString() {
        return "Bitspeak{" +
                "name=" + name() +
                '}';
    }
}
