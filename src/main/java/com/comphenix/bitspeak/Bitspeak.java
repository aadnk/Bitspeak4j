package com.comphenix.bitspeak;

import java.io.InputStream;
import java.io.Reader;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

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
 * <br></br>
 * <h2>Examples</h2>
 * <table cellpadding="4">
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

    private static final Bitspeak BITSPEAK_BS_6 = new Bitspeak(Format.BS_6);
    private static final Bitspeak BITSPEAK_BS_8 = new Bitspeak(Format.BS_8);
    private static final Collection<Bitspeak> FORMATS = Collections.unmodifiableList(Arrays.asList(BITSPEAK_BS_6, BITSPEAK_BS_8));

    private final Bitspeak.Format format;

    private Bitspeak(Format format) {
        this.format = format;
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
     * is padded with zeroes at the end (making the end "1100" in our example).</p>
     * <h2>Consonants lookup table:</h2>
     * <table>
     *   <tr>
     *       <th>Bits</th>
     *       <th>Character</th>
     *   </tr>
     *     <tr>
     *     <td>0000<td>
     *     <td>p</td>
     *   </tr>
     *   <tr>
     *     <td>0001<td>
     *     <td>b</td>
     *   </tr>
     *   <tr>
     *     <td>0010<td>
     *     <td>t</td>
     *   </tr>
     *   <tr>
     *     <td>0011<td>
     *     <td>d</td>
     *   </tr>
     *   <tr>
     *     <td>0100<td>
     *     <td>k</td>
     *   </tr>
     *   <tr>
     *     <td>0101<td>
     *     <td>g</td>
     *   </tr>
     *   <tr>
     *     <td>0110<td>
     *     <td>x</td>
     *   </tr>
     *   <tr>
     *     <td>0111<td>
     *     <td>j</td>
     *   </tr>
     *   <tr>
     *     <td>1000<td>
     *     <td>f</td>
     *   </tr>
     *   <tr>
     *     <td>1001<td>
     *     <td>v</td>
     *   </tr>
     *   <tr>
     *     <td>1010<td>
     *     <td>l</td>
     *   </tr>
     *   <tr>
     *     <td>1011<td>
     *     <td>r</td>
     *   </tr>
     *   <tr>
     *     <td>1100<td>
     *     <td>m</td>
     *   </tr>
     *   <tr>
     *     <td>1101<td>
     *     <td>n</td>
     *   </tr>
     *   <tr>
     *     <td>1110<td>
     *     <td>s</td>
     *   </tr>
     *   <tr>
     *     <td>1111<td>
     *     <td>z</td>
     *   </tr>
     * </table>
     *
     * <br></br>
     * <h2>Vowels lookup table:</h2>
     * <table>
     *   <tr>
     *       <th>Bits</th>
     *       <th>Character</th>
     *   </tr>
     *     <tr>
     *     <td>00<td>
     *     <td>a</td>
     *   </tr>
     *   <tr>
     *     <td>01<td>
     *     <td>u</td>
     *   </tr>
     *   <tr>
     *     <td>10<td>
     *     <td>i</td>
     *   </tr>
     *   <tr>
     *     <td>11<td>
     *     <td>e</td>
     *   </tr>
     * </table>
     * @return A BS-6 bitspeak format.
     */
    public static Bitspeak bs6() {
        return BITSPEAK_BS_6;
    }

    /**
     * Retrieve a bitspeak format where every 8 bits (1 byte) is converted into a consonant and vowel pair, each up two
     * 2 characters in length. The high-order of the byte is converted to a consonant, while the lowe-order is converted
     * to a vowel, using the following lookup tables.
     * <h2>Consonants lookup table:</h2>
     * <table>
     *   <tr>
     *     <th>Bits</th>
     *     <th>Character</th>
     *   </tr>
     *   <tr>
     *     <td>0000<td>
     *     <td>p</td>
     *   </tr>
     *   <tr>
     *     <td>0001<td>
     *     <td>b</td>
     *   </tr>
     *   <tr>
     *     <td>0010<td>
     *     <td>t</td>
     *   </tr>
     *   <tr>
     *     <td>0011<td>
     *     <td>d</td>
     *   </tr>
     *   <tr>
     *     <td>0100<td>
     *     <td>k</td>
     *   </tr>
     *   <tr>
     *     <td>0101<td>
     *     <td>g</td>
     *   </tr>
     *   <tr>
     *     <td>0110<td>
     *     <td>ch</td>
     *   </tr>
     *   <tr>
     *     <td>0111<td>
     *     <td>j</td>
     *   </tr>
     *   <tr>
     *     <td>1000<td>
     *     <td>f</td>
     *   </tr>
     *   <tr>
     *     <td>1001<td>
     *     <td>v</td>
     *   </tr>
     *   <tr>
     *     <td>1010<td>
     *     <td>l</td>
     *   </tr>
     *   <tr>
     *     <td>1011<td>
     *     <td>r</td>
     *   </tr>
     *   <tr>
     *     <td>1100<td>
     *     <td>m</td>
     *   </tr>
     *   <tr>
     *     <td>1101<td>
     *     <td>y</td>
     *   </tr>
     *   <tr>
     *     <td>1110<td>
     *     <td>s</td>
     *   </tr>
     *   <tr>
     *     <td>1111<td>
     *     <td>z</td>
     *   </tr>
     * </table>
     *
     * <br></br>
     * <h2>Vowels lookup table:</h2>
     * <table>
     *   <tr>
     *     <th>Bits</th>
     *     <th>Character</th>
     *   </tr>
     *     <tr>
     *     <td>0000<td>
     *     <td>a</td>
     *   </tr>
     *   <tr>
     *     <td>0001<td>
     *     <td>e</td>
     *   </tr>
     *   <tr>
     *     <td>0010<td>
     *     <td>i</td>
     *   </tr>
     *   <tr>
     *     <td>0011<td>
     *     <td>o</td>
     *   </tr>
     *   <tr>
     *     <td>0100<td>
     *     <td>u</td>
     *   </tr>
     *   <tr>
     *     <td>0101<td>
     *     <td>an</td>
     *   </tr>
     *   <tr>
     *     <td>0110<td>
     *     <td>en</td>
     *   </tr>
     *   <tr>
     *     <td>0111<td>
     *     <td>in</td>
     *   </tr>
     *   <tr>
     *     <td>1000<td>
     *     <td>un</td>
     *   </tr>
     *   <tr>
     *     <td>1001<td>
     *     <td>on</td>
     *   </tr>
     *   <tr>
     *     <td>1010<td>
     *     <td>ai</td>
     *   </tr>
     *   <tr>
     *     <td>1011<td>
     *     <td>ei</td>
     *   </tr>
     *   <tr>
     *     <td>1100<td>
     *     <td>oi</td>
     *   </tr>
     *   <tr>
     *     <td>1101<td>
     *     <td>ui</td>
     *   </tr>
     *   <tr>
     *     <td>1110<td>
     *     <td>aw</td>
     *   </tr>
     *   <tr>
     *     <td>1111<td>
     *     <td>ow</td>
     *   </tr>
     * </table>
     * @return A BS-8 bitspeak format.
     */
    public static Bitspeak bs8() {
        return BITSPEAK_BS_8;
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
        return BitspeakDecoder.newDecoder(format);
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
        return BitspeakDecoder.newDecoder(format).wrap(reader, 2 * bufferSize, bufferSize);
    }

    /**
     * Stream the bits represented by the bitspeak characters in the given reader as an input stream.
     * @param reader the reader.
     * @param byteBufferSize the number of bytes to buffer during decoding.
     * @param charBufferSize the number of characters to buffer during decoding.
     * @return The corresponding input stream.
     */
    public InputStream newDecodeStream(Reader reader, int byteBufferSize, int charBufferSize) {
        return BitspeakDecoder.newDecoder(format).wrap(reader, byteBufferSize, charBufferSize);
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

    /**
     * Create a new bitspeak encoder for the current format.
     * @return A new bitspeak encoder.
     */
    public BitspeakEncoder newEncoder() {
        return BitspeakEncoder.newEncoder(format);
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
        return BitspeakEncoder.newEncoder(format).wrap(input, 2 * bufferSize, bufferSize);
    }

    /**
     * Create a new stream of bitspeak characters, using the bytes in the given input stream and the current bitspeak format.
     * @param input the input stream.
     * @param byteBufferSize the number of bytes to buffer during decoding.
     * @param charBufferSize the number of characters to buffer during decoding.
     * @return A readable stream of bitspeak characters.
     */
    public Reader newEncodeStream(InputStream input, int byteBufferSize, int charBufferSize) {
        return BitspeakEncoder.newEncoder(format).wrap(input, byteBufferSize, charBufferSize);
    }

    /**
     * Retrieve the name of the current bitspeak format, either bs6 or bs8.
     * @return The name of the format,
     */
    public String name() {
        switch (format) {
            case BS_6: return "bs6";
            case BS_8: return "bs8";
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
