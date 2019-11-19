/*
 * Copyright (C) 2019  Kristian S. Stangeland
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 */

package com.comphenix.bitspeak;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;

/**
 * A bitspeak decoder. <i>Use {@link Bitspeak} for common decode operations.</i>
 * <p>
 * A decoder may be acquired using {@link Bitspeak#newDecoder()}. To decode a stream of characters, call
 * {@link BitspeakDecoder#decodeBlock(char[], int, int, byte[], int, int)} repeatedly with a range
 * of input characters and a range of output bytes to a byte array. The method returns the number of written
 * bytes in the byte array, use {@link #getReadCount()} to determine the number of read characters (total accumulated) from the character
 * array. Finally, call {@link BitspeakDecoder#finishBlock(byte[], int, int)} to indicate that the stream of characters have ended (EOF).
 * <p>
 * WARNING: This class is not thread safe.
 * </p>
 */
public abstract class BitspeakDecoder {
    protected long readCount;
    protected long writeCount;

    static BitspeakDecoder newDecoder(Bitspeak.Format format) {
        switch (format) {
            case BS_6:
                return new SixBitDecoder();
            case BS_8:
                return new EightBitDecoder();
            default:
                throw new IllegalArgumentException("Unknown format: " + format);
        }
    }

    /**
     * Create an input stream wrapping the given character input.
     * @param input the input.
     * @param byteBufferSize the byte buffer size.
     * @param charBufferSize the char buffer size.
     * @return The corresponding input stream.
     */
    public InputStream wrap(Reader input, int byteBufferSize, int charBufferSize) {
        if (byteBufferSize < 1) {
            throw new IllegalArgumentException("byteBufferSize cannot be less than 1");
        }
        if (charBufferSize < 1) {
            throw new IllegalArgumentException("charBufferSize cannot be less than 1");
        }
        // Wrap in a buffered input stream - that way, we don't have to implement every stream method
        return new BufferedInputStream(new InputStream() {
            private char[] buffer = new char[charBufferSize];
            private int bufferPosition = 0;
            private int bufferLength = 0; // -1 if EOF

            @Override
            public int read() throws IOException {
                // Note: Will not be called by the buffered input stream
                byte[] buffer = new byte[1];
                int read = read(buffer);
                return read > 0 ? buffer[0] : -1;
            }

            @Override
            public int read(byte[] b, int off, int len) throws IOException {
                fillBuffer();

                if (bufferLength > 0) {
                    long currentRead = getReadCount();
                    int written = decodeBlock(buffer, bufferPosition, bufferLength - bufferPosition, b, off, len);

                    bufferPosition += getReadCount() - currentRead;
                    return written;
                } else {
                    // Finalize block
                    int written = finishBlock(b, off, len);
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
        }, byteBufferSize);
    }

    /**
     * Retrieve the total number of character this decoder has read
     * @return The total number of read characters.
     */
    public long getReadCount() {
        return readCount;
    }

    /**
     * Retrieve the number of bytes this decoder has written.
     * @return Total number of written bytes.
     */
    public long getWriteCount() {
        return writeCount;
    }

    /**
     * Decode the bitspeak in the given source array to the given destination byte array, as a final block.
     *
     * @param source      the source character array with the bitspeak content.
     * @param destination the destination byte array.
     * @return Number of decoded bytes.
     */
    public int decodeFinal(char[] source, byte[] destination) {
        int written = decodeBlock(source, 0, source.length, destination, 0, destination.length);

        // Finalize block
        written += finishBlock(destination, written, destination.length - written);
        return written;
    }

    /**
     * Decode the bitspeak in the given source array to the given destination byte array.
     *
     * @param source            the source character array with the bitspeak content.
     * @param sourceOffset      the source offset.
     * @param sourceLength      the number of source characters to read from the character array.
     * @param destination       the destination byte array.
     * @param destinationOffset the starting position of the destination array.
     * @param destinationLength the maximum number of bytes to write to the destination array.
     * @return Number of decoded bytes.
     */
    public abstract int decodeBlock(char[] source, int sourceOffset, int sourceLength, byte[] destination, int destinationOffset, int destinationLength);

    /**
     * Finish decoding a stream of bitspeak content, writing the final output to the given destination.
     *
     * @param destination       the destination byte array.
     * @param destinationOffset the starting position of the destination array.
     * @param destinationLength the maximum number of bytes to write to the destination array.
     * @return Number of decoded bytes.
     */
    public abstract int finishBlock(byte[] destination, int destinationOffset, int destinationLength);

    private static class SixBitDecoder extends BitspeakDecoder {
        private final BitWriter bitWriter = new BitWriter();

        // Must start with a consonant
        private boolean nextConsonant = true;

        @Override
        public int decodeBlock(char[] source, int sourceOffset, int sourceLength, byte[] destination, int destinationOffset, int destinationLength) {
            int read = 0;
            int written = 0;
            boolean consonant = nextConsonant;

            for (read = 0; read < sourceLength && written < destinationLength; read++) {
                char character = source[read];

                if (consonant) {
                    switch (character) {
                        case 'p':
                            bitWriter.writeBits(0x0, 4);
                            break;
                        case 'b':
                            bitWriter.writeBits(0x1, 4);
                            break;
                        case 't':
                            bitWriter.writeBits(0x2, 4);
                            break;
                        case 'd':
                            bitWriter.writeBits(0x3, 4);
                            break;
                        case 'k':
                            bitWriter.writeBits(0x4, 4);
                            break;
                        case 'g':
                            bitWriter.writeBits(0x5, 4);
                            break;
                        case 'x':
                            bitWriter.writeBits(0x6, 4);
                            break;
                        case 'j':
                            bitWriter.writeBits(0x7, 4);
                            break;
                        case 'f':
                            bitWriter.writeBits(0x8, 4);
                            break;
                        case 'v':
                            bitWriter.writeBits(0x9, 4);
                            break;
                        case 'l':
                            bitWriter.writeBits(0xA, 4);
                            break;
                        case 'r':
                            bitWriter.writeBits(0xB, 4);
                            break;
                        case 'm':
                            bitWriter.writeBits(0xC, 4);
                            break;
                        case 'n':
                            bitWriter.writeBits(0xD, 4);
                            break;
                        case 's':
                            bitWriter.writeBits(0xE, 4);
                            break;
                        case 'z':
                            bitWriter.writeBits(0xF, 4);
                            break;
                        default:
                            throw new IllegalArgumentException("Illegal character at " + read + ", expected a consonant, got " + character);
                    }
                } else {
                    switch (character) {
                        case 'a':
                            bitWriter.writeBits(0x0, 2);
                            break;
                        case 'u':
                            bitWriter.writeBits(0x1, 2);
                            break;
                        case 'i':
                            bitWriter.writeBits(0x2, 2);
                            break;
                        case 'e':
                            bitWriter.writeBits(0x3, 2);
                            break;
                        default:
                            throw new IllegalArgumentException("Illegal character at " + read + ", expected a vowel, got " + character);
                    }
                }
                // Write if possible
                written += bitWriter.flush(destination,
                        destinationOffset + written, destinationLength - written);
                // Flip consonant state
                consonant = !consonant;
            }
            // Save current state
            nextConsonant = consonant;
            readCount += read;
            writeCount += written;
            return written;
        }

        @Override
        public int finishBlock(byte[] destination, int destinationOffset, int destinationLength) {
            int written = bitWriter.flush(destination, destinationOffset, destinationLength);

            if (bitWriter.getBufferLength() > 0) {
                // Discard bits if they are zero (due to padding)
                if (bitWriter.getBuffer() != 0) {
                    throw new IllegalStateException("Misalignment error: " + bitWriter.getBufferLength() + " bits offset.");
                } else {
                    bitWriter.clear();
                }
            }
            writeCount += written;
            return written;
        }
    }

    private static class EightBitDecoder extends BitspeakDecoder {
        private static final int STATE_BEGIN_CONSONANT = 0;
        private static final int STATE_ENDING_CONSONANT = 1;
        private static final int STATE_BEGIN_VOWEL = 2;
        private static final int STATE_ENDING_VOWEL = 3;

        private final BitWriter bitWriter = new BitWriter();

        // Must start with a consonant
        private int nextState = STATE_BEGIN_CONSONANT;
        private char prevChar = '\0';

        @Override
        public int decodeBlock(char[] source, int sourceOffset, int sourceLength, byte[] destination, int destinationOffset, int destinationLength) {
            int read = 0;
            int written = 0;

            int state = nextState;
            char prev = prevChar;

            for (read = 0; read < sourceLength && written < destinationLength; read++) {
                char character = source[read];

                if (state == STATE_BEGIN_CONSONANT) {
                    switch (character) {
                        case 'p':
                            bitWriter.writeBits(0x0, 4);
                            break;
                        case 'b':
                            bitWriter.writeBits(0x1, 4);
                            break;
                        case 't':
                            bitWriter.writeBits(0x2, 4);
                            break;
                        case 'd':
                            bitWriter.writeBits(0x3, 4);
                            break;
                        case 'k':
                            bitWriter.writeBits(0x4, 4);
                            break;
                        case 'g':
                            bitWriter.writeBits(0x5, 4);
                            break;
                        case 'c':   // ch   -- 0x6
                            // Wait for another character
                            state = STATE_ENDING_CONSONANT;
                            break;
                        case 'j':
                            bitWriter.writeBits(0x7, 4);
                            break;
                        case 'f':
                            bitWriter.writeBits(0x8, 4);
                            break;
                        case 'v':
                            bitWriter.writeBits(0x9, 4);
                            break;
                        case 'l':
                            bitWriter.writeBits(0xA, 4);
                            break;
                        case 'r':
                            bitWriter.writeBits(0xB, 4);
                            break;
                        case 'm':
                            bitWriter.writeBits(0xC, 4);
                            break;
                        case 'y':
                            bitWriter.writeBits(0xD, 4);
                            break;
                        case 's':
                            bitWriter.writeBits(0xE, 4);
                            break;
                        case 'z':
                            bitWriter.writeBits(0xF, 4);
                            break;
                        default:
                            throw new IllegalArgumentException("Illegal character at " + read + ", expected a consonant, got " + character);
                    }
                    // Toggle state (if not set already)
                    if (state == STATE_BEGIN_CONSONANT) {
                        state = STATE_BEGIN_VOWEL;
                    }

                } else if (state == STATE_ENDING_CONSONANT) {
                    if (character == 'h') {
                        bitWriter.writeBits(0x6, 4);
                    } else {
                        throw new IllegalArgumentException("Illegal character at " + (read - 1) + ", expected ch, got " + character);
                    }
                    state = STATE_BEGIN_VOWEL;

                } else if (state == STATE_BEGIN_VOWEL) {
                    // Must start with a vowel (but may also include other characters)
                    if (character != 'a' && character != 'e' && character != 'i' && character != 'o' && character != 'u') {
                        throw new IllegalArgumentException("Illegal character at " + read + ", expected a vowel prefix, got " + character);
                    }
                    // Always wait for next character
                    state = STATE_ENDING_VOWEL;

                } else if (state == STATE_ENDING_VOWEL) {
                    // Character is the last character in the vowel
                    if (character == 'n') {
                        switch (prev) {
                            case 'a':
                                bitWriter.writeBits(0x5, 4);
                                break;
                            case 'e':
                                bitWriter.writeBits(0x6, 4);
                                break;
                            case 'i':
                                bitWriter.writeBits(0x7, 4);
                                break;
                            case 'u':
                                bitWriter.writeBits(0x8, 4);
                                break;
                            case 'o':
                                bitWriter.writeBits(0x9, 4);
                                break;
                            default:
                                throw new IllegalArgumentException("Illegal characters at " + (read - 1) + ", expected a vowel, got " + prevChar + "" + character);
                        }
                    } else if (character == 'i') {
                        switch (prev) {
                            case 'a':
                                bitWriter.writeBits(0xA, 4);
                                break;
                            case 'e':
                                bitWriter.writeBits(0xB, 4);
                                break;
                            case 'o':
                                bitWriter.writeBits(0xC, 4);
                                break;
                            case 'u':
                                bitWriter.writeBits(0xD, 4);
                                break;
                            default:
                                throw new IllegalArgumentException("Illegal characters at " + (read - 1) + ", expected a vowel, got " + prevChar + "" + character);
                        }
                    } else if (character == 'w') {
                        switch (prev) {
                            case 'a':
                                bitWriter.writeBits(0xE, 4);
                                break;
                            case 'o':
                                bitWriter.writeBits(0xF, 4);
                                break;
                            default:
                                throw new IllegalArgumentException("Illegal characters at " + (read - 1) + ", expected a vowel, got " + prevChar + "" + character);
                        }
                    } else {
                        switch (prev) {
                            case 'a':
                                bitWriter.writeBits(0x0, 4);
                                break;
                            case 'e':
                                bitWriter.writeBits(0x1, 4);
                                break;
                            case 'i':
                                bitWriter.writeBits(0x2, 4);
                                break;
                            case 'o':
                                bitWriter.writeBits(0x3, 4);
                                break;
                            case 'u':
                                bitWriter.writeBits(0x4, 4);
                                break;
                            default:
                                throw new IllegalArgumentException("Illegal characters at " + read + ", expected a vowel, got " + prevChar);
                        }
                        // Read current character again
                        read--;
                    }
                    // Back to reading consonant
                    state = STATE_BEGIN_CONSONANT;
                }
                // Write if possible
                written += bitWriter.flush(destination,
                        destinationOffset + written, destinationLength - written);

                prev = character;
            }
            // Save current state
            nextState = state;
            prevChar = prev;

            readCount += read;
            writeCount += written;
            return written;
        }

        @Override
        public int finishBlock(byte[] destination, int destinationOffset, int destinationLength) {
            if (nextState == STATE_ENDING_CONSONANT) {
                throw new IllegalArgumentException("Expected ch, got c and EOF");

            } else if (nextState == STATE_ENDING_VOWEL) {
                switch (prevChar) {
                    case 'a':
                        bitWriter.writeBits(0x0, 4);
                        break;
                    case 'e':
                        bitWriter.writeBits(0x1, 4);
                        break;
                    case 'i':
                        bitWriter.writeBits(0x2, 4);
                        break;
                    case 'o':
                        bitWriter.writeBits(0x3, 4);
                        break;
                    case 'u':
                        bitWriter.writeBits(0x4, 4);
                        break;
                    default:
                        throw new IllegalArgumentException("Illegal character, expected a vowel, got " + prevChar);
                }
                nextState = STATE_BEGIN_CONSONANT;
            }
            int written = bitWriter.flush(destination, destinationOffset, destinationLength);

            if (bitWriter.getBufferLength() > 0) {
                throw new IllegalStateException("Misalignment error: " + bitWriter.getBufferLength() + " bits offset.");
            }
            return written;
        }
    }

    private static class BitWriter {
        private int buffer = 0;         // Current temporary buffer
        private int bufferLength = 0;   // Buffer lengths, in bits

        void writeBits(int bits, int bitsLength) {
            // Write to buffer (must be less than 32 bits)
            if (bitsLength > 0) {
                buffer = (buffer << bitsLength) | bits;
                bufferLength += bitsLength;
            }
        }

        void clear() {
            buffer = 0;
            bufferLength = 0;
        }

        int getBuffer() {
            return buffer;
        }

        int getBufferLength() {
            return bufferLength;
        }

        int flush(byte[] destination, int destinationOffset, int destinationLength) {
            int written = 0;

            // Do we have 8 bits?
            while (bufferLength >= 8) {
                destination[destinationOffset + written] = (byte) (buffer >>> (bufferLength - 8));
                buffer &= ~(0xFF << (bufferLength - 8));
                bufferLength -= 8;

                if (++written >= destinationLength) {
                    if (bufferLength > 0) {
                        throw new IllegalArgumentException("misalignment error (buffer length: " + bufferLength + ")");
                    }
                    break;
                }
            }
            // Stop decoding
            return written;
        }
    }
}
