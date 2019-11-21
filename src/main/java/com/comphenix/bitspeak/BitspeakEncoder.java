/*
 * Copyright (C) 2019  Kristian S. Stangeland
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 */

package com.comphenix.bitspeak;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.util.Objects;

/**
 * A bitspeak encoder. <i>Use {@link Bitspeak} for common encode operations.</i>
 * <p>
 * An encoder may be acquired using {@link Bitspeak#newEncoder()}. To encode a stream of bytes, call
 * {@link BitspeakEncoder#encodeBlock(byte[], int, int, char[], int, int)} repeatedly with a range
 * of input bytes and a range of output character to a char array. The method returns the number of written
 * characters to the output character array, use {@link #getReadCount()} to determine the number of read bytes (total accumulated
 * over the life-time of the decoder) from the byte arrays. Finally, call {@link BitspeakEncoder#finishBlock(char[], int, int)} to indicate that the stream of bytes
 * have ended (EOF).
 * <p>
 * WARNING: This class is not thread safe.
 * </p>
 */
public abstract class BitspeakEncoder {
    protected long readCount;
    protected long writeCount;

    static BitspeakEncoder newEncoder(Bitspeak.Format format, BitspeakConfig config) {
        switch (format) {
            case BS_6:
                return new SixBitEncoder(config);
            case BS_8:
                return new EightBitEncoder(config);
            default:
                throw new IllegalArgumentException("Unknown format: " + format);
        }
    }

    public Reader wrap(InputStream input, int byteBufferSize, int charBufferSize) {
        if (byteBufferSize < 1) {
            throw new IllegalArgumentException("byteBufferSize cannot be less than 1");
        }
        if (charBufferSize < 1) {
            throw new IllegalArgumentException("charBufferSize cannot be less than 1");
        }
        return new BufferedReader(new Reader() {
            private byte[] buffer = new byte[byteBufferSize];
            private int bufferPosition = 0;
            private int bufferLength = 0; // -1 if EOF

            @Override
            public int read(char[] cbuf, int off, int len) throws IOException {
                fillBuffer();

                if (bufferLength > 0) {
                    long currentRead = getReadCount();
                    int written = encodeBlock(buffer, bufferPosition, bufferLength - bufferPosition, cbuf, off, len);

                    bufferPosition += getReadCount() - currentRead;
                    return written;
                } else {
                    // Finalize block
                    int written = finishBlock(cbuf, off, len);
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
        }, charBufferSize);
    }

    /**
     * Retrieve the total number of bytes this decoder has read
     * @return The total number of read bytes.
     */
    public long getReadCount() {
        return readCount;
    }

    /**
     * Retrieve the number of chracters this decoder has written.
     * @return Total number of written characters.
     */
    public long getWriteCount() {
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
        private final BitspeakConfig config;

        private final int CONSONANT_BITS = 4;
        private final int VOWELS_BITS = 2;

        private int currentBuffer;
        private int currentBufferLength;

        // Consonant first
        private boolean currentConsonant = true;

        public SixBitEncoder(BitspeakConfig config) {
            this.config = Objects.requireNonNull(config, "config cannot be NULL");
        }

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

        private static final int STATE_READ_BYTE = 0;                 // Read the next byte to output
        private static final int STATE_WRITE_CONSONANT_START = 1;     // Write first character of consonant
        private static final int STATE_WRITE_CONSONANT_END = 2;       // Write second character of consonant (if any)
        private static final int STATE_WRITE_VOWEL_START = 3;         // Write first character of vowel
        private static final int STATE_WRITE_VOWEL_END = 4;           // Write first character of vowel (if any)

        private final WhitespaceManager whitespaceManager;

        private int nextState = STATE_READ_BYTE;
        private int nextByte = 0;

        public EightBitEncoder(BitspeakConfig config) {
            this.whitespaceManager = new WhitespaceManager(config);
        }

        @Override
        public int encodeBlock(byte[] source, int sourceOffset, int sourceLength, char[] destination, int destinationOffset, int destinationLength) {
            int read = 0;
            int written = 0;

            int currentState = nextState;
            int currentByte = nextByte;

            for (; read < sourceLength && written < destinationLength; ) {
                // Read byte, if needed
                if (currentState == STATE_READ_BYTE) {
                    currentByte = source[sourceOffset + read++] & 0xFF;
                    currentState = STATE_WRITE_CONSONANT_START;
                }

                if (currentState == STATE_WRITE_CONSONANT_START || currentState == STATE_WRITE_CONSONANT_END) {
                    String consonant = CONSONANTS[currentByte >> 4];
                    int index = currentState - STATE_WRITE_CONSONANT_START;

                    destination[destinationOffset + written++] = consonant.charAt(index);
                    // Increment state if the next index is still a consonant character, otherwise move to the vowel
                    currentState = index + 1 < consonant.length() ? currentState + 1 : STATE_WRITE_VOWEL_START;

                } else if (currentState == STATE_WRITE_VOWEL_START || currentState == STATE_WRITE_VOWEL_END) {
                    String vowel = VOWELS[currentByte & 0xF];
                    int index = currentState - STATE_WRITE_VOWEL_START;

                    destination[destinationOffset + written++] = vowel.charAt(index);
                    currentState = index + 1 < vowel.length() ? currentState + 1 : STATE_READ_BYTE;
                }
            }
            nextByte = currentByte;
            nextState = currentState;

            readCount += read;
            writeCount += written;
            return written;
        }

        @Override
        public int finishBlock(char[] destination, int destinationOffset, int destinationLength) {
            int written = 0;

            for (; written < destinationLength && nextState != STATE_READ_BYTE; ) {
                if (nextState == STATE_WRITE_CONSONANT_START || nextState == STATE_WRITE_CONSONANT_END) {
                    String consonant = CONSONANTS[nextByte >> 4];
                    int index = nextState - STATE_WRITE_CONSONANT_START;

                    destination[destinationOffset + written++] = consonant.charAt(index);
                    nextState = index + 1 < consonant.length() ? nextState + 1 : STATE_WRITE_VOWEL_START;

                }
                if (nextState == STATE_WRITE_VOWEL_START || nextState == STATE_WRITE_VOWEL_END) {
                    String vowel = VOWELS[nextByte & 0xF];
                    int index = nextState - STATE_WRITE_VOWEL_START;

                    destination[destinationOffset + written++] = vowel.charAt(index);
                    nextState = index + 1 < vowel.length() ? nextState + 1 : STATE_READ_BYTE;
                }
            }
            writeCount += written;
            return written;
        }
    }

    private static class WhitespaceManager {
        private enum Delimiter {
            NONE,
            WORD,
            LINE
        }

        private final BitspeakConfig config;
        private final int maxWordSize;
        private final int maxLineSize;

        private int currentWordLength;
        private int currentLineLength;

        private Delimiter currentDelimiter = Delimiter.NONE;
        private int delimiterPosition;

        public WhitespaceManager(BitspeakConfig config) {
            this.config = Objects.requireNonNull(config, "config cannot be NULL");
            this.maxWordSize = config.getMaxWordSize();
            this.maxLineSize = config.getMaxLineSize();
        }

        public String getCurrentDelimiter() {
            return currentDelimiter == Delimiter.LINE ? config.getLineDelimiter() :
                    (currentDelimiter == Delimiter.WORD ? config.getWordDelimiter() : "");
        }

        /**
         * Attempt to write the given number of characters to the output.
         * @param length the number of characters to write.
         * @return The amount of characters permitted, or 0 if a delimiter must be written first.
         */
        public int beginOutput(int length) {
            Delimiter nextDelimiter = currentDelimiter;

            if (nextDelimiter != Delimiter.NONE) {
                // Writing a delimiter
                return 0;
            }
            int result = length;

            if (currentWordLength + length > maxWordSize && maxWordSize > 0) {
                if (currentWordLength == 0) {
                    // Current word has just started - we'll have to split it
                    result = maxWordSize - length;
                } else {
                    result = 0;
                    nextDelimiter = Delimiter.WORD;
                }
            }
            if (currentLineLength + result > maxLineSize && maxLineSize > 0) {
                if (currentLineLength == 0) {
                    // Current line has also just started - split that too
                    result = Math.min(result, maxLineSize - length);
                } else {
                    result = 0;
                    nextDelimiter = Delimiter.LINE;
                }
            }
            currentWordLength += result;
            currentLineLength += result;
            currentDelimiter = nextDelimiter;
            return result;
        }

        /**
         * Begin to write the current delimiter, if any, to the output.
         * @param destination the destination buffer.
         * @param destinationOffset the starting position.
         * @param destinationLength the maximum number of characters to write.
         * @return The number of characters written, or 0.
         */
        public int writeDelimiter(char[] destination, int destinationOffset, int destinationLength) {
            int written = 0;

            if (currentDelimiter != Delimiter.NONE) {
                String delimiter = currentDelimiter == Delimiter.WORD ? config.getWordDelimiter() : config.getLineDelimiter();
                int remaining = Math.min(destinationLength, delimiter.length() - delimiterPosition);

                for (; written < remaining; written++) {
                    destination[destinationOffset + written] = delimiter.charAt(delimiterPosition + written);
                }
                delimiterPosition += written;

                // See if the delimiter has finished writing
                if (delimiterPosition >= delimiter.length()) {
                    // Also reset line length
                    if (currentDelimiter == Delimiter.LINE) {
                        currentLineLength = 0;
                    }
                    currentWordLength = 0;
                    currentDelimiter = Delimiter.NONE;
                }
            }
            return written;
        }
    }
}
