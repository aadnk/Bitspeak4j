package com.comphenix.bitspeak;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;

public abstract class BitspeakEncoder {
    protected int readCount;
    protected int writeCount;

    public static String encode(byte[] data, BitspeakFormat format) {
        return encode(data, 0, data.length, format);
    }

    public static String encode(byte[] data, int offset, int length, BitspeakFormat format) {
        // Calculate buffer size
        char[] buffer = new char[format.getMaxEncodeSize(length)];
        int written = 0;

        BitspeakEncoder encoder = BitspeakEncoder.newEncoder(format);

        written += encoder.encodeBlock(data, offset, length, buffer, 0, buffer.length);
        written += encoder.finishBlock(buffer, written, buffer.length - written);

        // Create final string
        return new String(buffer, 0, written);
    }

    /**
     * Create a decoder for bitspeaks.
     */
    public static BitspeakEncoder newEncoder(BitspeakFormat format) {
        switch (format) {
            case SIX_BITS:
                return new SixBitEncoder();
            case EIGHT_BITS:
                return new EightBitEncoder();
            default:
                throw new IllegalArgumentException("Unknown format: " + format);
        }
    }

    public static Reader newStream(InputStream input, BitspeakFormat format) {
        return newStream(input, format, 4096);
    }

    public static Reader newStream(InputStream input, BitspeakFormat format, int bufferSize) {
        if (bufferSize < 1) {
            throw new IllegalArgumentException("bufferSize cannot be less than 1");
        }
        return new BufferedReader(new Reader() {
            private BitspeakEncoder encoder = newEncoder(format);

            private byte[] buffer = new byte[bufferSize];
            private int bufferPosition = 0;
            private int bufferLength = 0; // -1 if EOF

            @Override
            public int read(char[] cbuf, int off, int len) throws IOException {
                fillBuffer();

                if (bufferLength > 0) {
                    int currentRead = encoder.getReadCount();
                    int written = encoder.encodeBlock(buffer, bufferPosition, bufferLength, cbuf, off, len);

                    bufferPosition += encoder.getReadCount() - currentRead;
                    return written;
                } else {
                    // Finalize block
                    int written = encoder.finishBlock(cbuf, off, len);
                    return written > 0 ? written : -1;
                }
            }

            private void fillBuffer() throws IOException {
                if (bufferLength != -1 && bufferLength - bufferPosition <= 0) {
                    // May also yield -1, if EOF
                    bufferPosition = 0;
                    bufferLength = input.read(buffer);
                }
            }

            @Override
            public void close() throws IOException {
                input.close();
            }
        });
    }

    /**
     * Retrieve the total number of bytes this decoder has read
     * @return The total number of read bytes.
     */
    public int getReadCount() {
        return readCount;
    }

    /**
     * Retrieve the number of chracters this decoder has written.
     * @return Total number of written characters.
     */
    public int getWriteCount() {
        return writeCount;
    }

    /**
     * Encode the bytes in the source array as bitspeak to the given destination character array.
     *
     * @param source            the source byte array.
     * @param sourceOffset      the source offset.
     * @param sourceLength      the number of bytes to read from the byte array.
     * @param destination       the destination character array.
     * @param destinationOffset the starting position of the destination array.
     * @param destinationLength the maximum number of characters to write to the destination array.
     * @return Number of encoded characters.
     */
    public abstract int encodeBlock(byte[] source, int sourceOffset, int sourceLength, char[] destination, int destinationOffset, int destinationLength);

    /**
     * Finish encoding the written data.
     * @param destination       the destination character array.
     * @param destinationOffset the starting position of the destination array.
     * @param destinationLength the maximum number of characters to write to the destination array.
     * @return Number of encoded characters.
     */
    public abstract int finishBlock(char[] destination, int destinationOffset, int destinationLength);

    private static class SixBitEncoder extends BitspeakEncoder {
        private static final char[] CONSONANTS = { 'p', 'b', 't', 'd', 'k', 'g', 'x', 'j', 'f', 'v', 'l', 'r', 'm', 'n', 's', 'z' };
        private static final char[] VOWELS = { 'a', 'u', 'i', 'e' };

        private final int CONSONANT_BITS = 4;
        private final int VOWELS_BITS = 2;

        private int currentBuffer;
        private int currentBufferLength;
        // Consonant first
        private boolean currentConsonant = true;

        @Override
        public int encodeBlock(byte[] source, int sourceOffset, int sourceLength, char[] destination, int destinationOffset, int destinationLength) {
            int read = 0;
            int written = 0;

            int buffer = currentBuffer;
            int bufferLength = currentBufferLength;
            boolean consonant = currentConsonant;

            for (; read < sourceLength && written < destinationLength; written++) {
                // Add bits to buffer if needed
                if (bufferLength < (consonant ? 4 : 2)) {
                    buffer = buffer << 8 | source[sourceOffset + read] & 0xFF;
                    bufferLength += 8;
                    read++;
                }

                if (consonant) {
                    destination[destinationOffset + written] = CONSONANTS[buffer >>> (bufferLength - CONSONANT_BITS)];
                    buffer &= ~(0xF << (bufferLength - CONSONANT_BITS));
                    bufferLength -= CONSONANT_BITS;
                } else {
                    destination[destinationOffset + written] = VOWELS[buffer >>> (bufferLength - VOWELS_BITS)];
                    buffer &= ~(0x3 << (bufferLength - VOWELS_BITS));
                    bufferLength -= VOWELS_BITS;
                }
                consonant = !consonant;
            }
            // Save state
            currentBuffer = buffer;
            currentBufferLength = bufferLength;
            currentConsonant = consonant;

            readCount += read;
            writeCount += written;
            return written;
        }

        @Override
        public int finishBlock(char[] destination, int destinationOffset, int destinationLength) {
            int written = 0;

            int buffer = currentBuffer;
            int bufferLength = currentBufferLength;
            boolean consonant = currentConsonant;

            for (; written < destinationLength && bufferLength > 0; written++) {
                if (consonant) {
                    // Pad with zero
                    if (bufferLength < 4) {
                        int padding = 4 - bufferLength;
                        buffer <<= padding;
                        bufferLength += padding;
                    }
                    destination[destinationOffset + written] = CONSONANTS[buffer >>> (bufferLength - CONSONANT_BITS)];
                    buffer &= ~(0xF << (bufferLength - CONSONANT_BITS));
                    bufferLength -= CONSONANT_BITS;
                } else {
                    destination[destinationOffset + written] = VOWELS[buffer >>> (bufferLength - VOWELS_BITS)];
                    buffer &= ~(0x3 << (bufferLength - VOWELS_BITS));
                    bufferLength -= VOWELS_BITS;
                }
                consonant = !consonant;
            }
            currentBuffer = buffer;
            currentBufferLength = bufferLength;
            currentConsonant = consonant;

            writeCount += written;
            return written;
        }
    }

    private static class EightBitEncoder extends BitspeakEncoder {
        private static final String[] CONSONANTS = { "p", "b", "t", "d", "k", "g", "ch", "j", "f", "v", "l", "r", "m", "y", "s", "z" };
        private static final String[] VOWELS = { "a", "e", "i", "o", "u", "an", "en", "in", "un", "on", "ai", "ei", "oi", "ui", "aw", "ow" };

        private char[] buffer = new char[4];
        private int bufferPosition = 0;
        private int bufferLength = 0;

        @Override
        public int encodeBlock(byte[] source, int sourceOffset, int sourceLength, char[] destination, int destinationOffset, int destinationLength) {
            int read = 0;
            int written = 0;

            // Read from overflow first
            while (bufferLength > bufferPosition) {
                destination[destinationOffset + written++] = buffer[bufferPosition++];
            }
            for (; read < sourceLength && written < destinationLength; read++) {
                int nextByte = source[read] & 0xFF;
                String consonant = CONSONANTS[nextByte >> 4];
                String vowel = VOWELS[nextByte & 0xF];

                // Handle overflow?
                if (consonant.length() + vowel.length() > destinationLength - written) {
                    int remaining = destinationLength - written;

                    // Write to overflow buffer
                    copyTo(consonant, buffer, 0);
                    copyTo(vowel, buffer, consonant.length());
                    bufferLength = consonant.length() + vowel.length();
                    bufferPosition = remaining;

                    // Write as much as possible to the output
                    for (int i = 0; i < remaining; i++) {
                        destination[destinationOffset + written] = buffer[i];
                        written++;
                    }
                } else {
                    written += copyTo(consonant,  destination, destinationOffset + written);
                    written += copyTo(vowel, destination, destinationOffset + written);
                }
            }
            readCount += read;
            writeCount += written;
            return written;
        }

        @Override
        public int finishBlock(char[] destination, int destinationOffset, int destinationLength) {
            int written = 0;

            // Read from overflow first
            while (bufferLength > bufferPosition) {
                destination[destinationOffset + written++] = buffer[bufferPosition++];
            }
            writeCount += written;
            return written;
        }

        private static int copyTo(String source, char[] destination, int destinationOffset) {
            for (int i = 0; i < source.length(); i++) {
                destination[destinationOffset + i] = source.charAt(i);
            }
            return source.length();
        }
    }
}
