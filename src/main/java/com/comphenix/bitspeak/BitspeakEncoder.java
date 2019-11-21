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
 * over the life-time of the decoder) from the byte arrays.
 * <p>
 *  Finally, call {@link BitspeakEncoder#finishBlock(char[], int, int)} to indicate that the stream of bytes
 *  have ended (EOF). Repeat until the <i>finishBlock</i> method returns zero (0).
 * </p>
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
     * @return Number of encoded characters, or 0 if the encoder is finished.
     */
    public abstract int finishBlock(char[] destination, int destinationOffset, int destinationLength);

    private static class SixBitEncoder extends BitspeakEncoder {
        private static final char[] CONSONANTS = { 'p', 'b', 't', 'd', 'k', 'g', 'x', 'j', 'f', 'v', 'l', 'r', 'm', 'n', 's', 'z' };
        private static final char[] VOWELS = { 'a', 'u', 'i', 'e' };

        private static final int STATE_MASK_TYPE = 1;               // Mask for consonant/vowel
        private static final int STATE_WRITE_CONSONANT = 0;         // Write first character of consonant
        private static final int STATE_WRITE_VOWEL = 1;             // Write first character of vowel
        private static final int STATE_FLAG_WRITE_WHITESPACE = 2;   // Must write whitespace first

        private final WhitespaceManager whitespaceManager;

        private final int CONSONANT_BITS = 4;
        private final int VOWELS_BITS = 2;

        private int currentBuffer;
        private int currentBufferLength;

        // Consonant first
        private int currentState = STATE_WRITE_CONSONANT;

        SixBitEncoder(BitspeakConfig config) {
            this.whitespaceManager = new WhitespaceManager(config);
        }

        @Override
        public int encodeBlock(byte[] source, int sourceOffset, int sourceLength, char[] destination, int destinationOffset, int destinationLength) {
            int read = 0;
            int written = 0;

            int buffer = currentBuffer;
            int bufferLength = currentBufferLength;
            int state = currentState;

            for (; written < destinationLength; ) {
                if ((state & STATE_FLAG_WRITE_WHITESPACE) != 0) {
                    int whitespace = whitespaceManager.writeDelimiter(destination,
                            destinationOffset + written, destinationLength - written);

                    // More whitespace characters to write
                    if (whitespace > 0) {
                        written += whitespace;
                        continue;
                    }
                    // Done writing whitespace
                    state &= ~STATE_FLAG_WRITE_WHITESPACE;
                }
                boolean consonant = (state & STATE_MASK_TYPE) == STATE_WRITE_CONSONANT;

                // Add bits to buffer if needed
                if (bufferLength < (consonant ? 4 : 2)) {
                    if (source == null || read >= sourceLength) {
                        // We are done
                        break;
                    }
                    buffer = buffer << 8 | source[sourceOffset + read++] & 0xFF;
                    bufferLength += 8;
                }

                if (whitespaceManager.beginOutput(1) < 1) {
                    // Write whitespace first
                    state |= STATE_FLAG_WRITE_WHITESPACE;
                    continue;
                }
                if (consonant) {
                    destination[destinationOffset + written++] = CONSONANTS[buffer >>> (bufferLength - CONSONANT_BITS)];
                    buffer &= ~(0xF << (bufferLength - CONSONANT_BITS));
                    bufferLength -= CONSONANT_BITS;
                    state |= STATE_WRITE_VOWEL;
                } else {
                    destination[destinationOffset + written++] = VOWELS[buffer >>> (bufferLength - VOWELS_BITS)];
                    buffer &= ~(0x3 << (bufferLength - VOWELS_BITS));
                    bufferLength -= VOWELS_BITS;
                    state &= ~STATE_WRITE_VOWEL;
                }
            }
            // Save state
            currentBuffer = buffer;
            currentBufferLength = bufferLength;
            currentState = state;

            readCount += read;
            writeCount += written;
            return written;
        }

        @Override
        public int finishBlock(char[] destination, int destinationOffset, int destinationLength) {
            int written = 0;

            int buffer = currentBuffer;
            int bufferLength = currentBufferLength;
            int state = currentState;

            for (; written < destinationLength && bufferLength > 0; ) {
                if ((state & STATE_FLAG_WRITE_WHITESPACE) != 0) {
                    int whitespace = whitespaceManager.writeDelimiter(destination,
                            destinationOffset + written, destinationLength - written);

                    // More whitespace characters to write
                    if (whitespace > 0) {
                        written += whitespace;
                        continue;
                    }
                    // Done writing whitespace
                    state &= ~STATE_FLAG_WRITE_WHITESPACE;
                }
                boolean consonant = (state & STATE_MASK_TYPE) == STATE_WRITE_CONSONANT;

                if (whitespaceManager.beginOutput(1) < 1) {
                    // Write whitespace first
                    state |= STATE_FLAG_WRITE_WHITESPACE;
                    continue;
                }
                if (consonant) {
                    // Pad with zero
                    if (bufferLength < 4) {
                        int padding = 4 - bufferLength;
                        buffer <<= padding;
                        bufferLength += padding;
                    }
                    destination[destinationOffset + written++] = CONSONANTS[buffer >>> (bufferLength - CONSONANT_BITS)];
                    buffer &= ~(0xF << (bufferLength - CONSONANT_BITS));
                    bufferLength -= CONSONANT_BITS;
                    state |= STATE_WRITE_VOWEL;
                } else {
                    destination[destinationOffset + written++] = VOWELS[buffer >>> (bufferLength - VOWELS_BITS)];
                    buffer &= ~(0x3 << (bufferLength - VOWELS_BITS));
                    bufferLength -= VOWELS_BITS;
                    state &= ~STATE_WRITE_VOWEL;
                }
            }
            currentBuffer = buffer;
            currentBufferLength = bufferLength;
            currentState = state;

            writeCount += written;
            return written;
        }
    }

    private static class EightBitEncoder extends BitspeakEncoder {
        private static final String[] CONSONANTS = { "p", "b", "t", "d", "k", "g", "ch", "j", "f", "v", "l", "r", "m", "y", "s", "z" };
        private static final String[] VOWELS = { "a", "e", "i", "o", "u", "an", "en", "in", "un", "on", "ai", "ei", "oi", "ui", "aw", "ow" };

        private static final int STATE_READ_BYTE = 0;                 // Read the next byte to output

        private static final int STATE_FLAG_WRITE_CONSONANT = 1;      // Write first character of consonant
        private static final int STATE_FLAG_WRITE_VOWEL = 2;          // Write first character of vowel
        private static final int STATE_FLAG_WRITTEN_FIRST = 4;        // The first character in the consonant/vowel has been written
        private static final int STATE_FLAG_WHITESPACE_CHECKED = 8;   // Whitespace has been checked.
        private static final int STATE_FLAG_WRITE_WHITESPACE = 16;    // Must write whitespace first

        private final WhitespaceManager whitespaceManager;

        private int nextState = STATE_READ_BYTE;
        private int nextByte = 0;

        EightBitEncoder(BitspeakConfig config) {
            this.whitespaceManager = new WhitespaceManager(config);
        }

        @Override
        public int encodeBlock(byte[] source, int sourceOffset, int sourceLength, char[] destination, int destinationOffset, int destinationLength) {
            Objects.requireNonNull(source, "source cannot be NULL");
            return performEncode(source, sourceOffset, sourceLength, destination, destinationOffset, destinationLength);
        }

        @Override
        public int finishBlock(char[] destination, int destinationOffset, int destinationLength) {
            return performEncode(null, 0, 0, destination, destinationOffset, destinationLength);
        }

        private int performEncode(byte[] source, int sourceOffset, int sourceLength, char[] destination, int destinationOffset, int destinationLength) {
            int read = 0;
            int written = 0;

            int currentState = nextState;
            int currentByte = nextByte;

            for (; written < destinationLength; ) {
                if ((currentState & STATE_FLAG_WRITE_WHITESPACE) != 0) {
                    int whitespace = whitespaceManager.writeDelimiter(destination,
                            destinationOffset + written, destinationLength - written);

                    // More whitespace characters to write
                    if (whitespace > 0) {
                        written += whitespace;
                        continue;
                    }
                    // Done writing whitespace
                    currentState &= ~(STATE_FLAG_WRITE_WHITESPACE | STATE_FLAG_WHITESPACE_CHECKED);
                }
                // Read byte, if needed
                if (currentState == STATE_READ_BYTE) {
                    if (source == null || read >= sourceLength) {
                        // Finish block
                        break;
                    }
                    currentByte = source[sourceOffset + read++] & 0xFF;
                    currentState = STATE_FLAG_WRITE_CONSONANT;
                }
                // Load the current symbol
                String symbol = (currentState & STATE_FLAG_WRITE_CONSONANT) != 0 ?
                        CONSONANTS[currentByte >> 4] : VOWELS[currentByte & 0xF];

                // Starting index of the symbol
                int index = (currentState & STATE_FLAG_WRITTEN_FIRST) != 0 ? 1 : 0;

                // Check whitespace?
                if ((currentState & STATE_FLAG_WHITESPACE_CHECKED) == 0) {
                    int needed = symbol.length() - index;
                    int permitted = whitespaceManager.beginOutput(symbol.length() - index); // 0, 1 or 2

                    currentState |= STATE_FLAG_WHITESPACE_CHECKED;

                    if (permitted == 0) {
                        currentState |= STATE_FLAG_WRITE_WHITESPACE;
                        continue;
                    } else if (permitted != needed) {
                        // Write the next character, but then write whitespace
                        currentState |= STATE_FLAG_WRITE_WHITESPACE;
                    }
                }
                // Write next character
                destination[destinationOffset + written++] = symbol.charAt(index);

                if (index + 1 < symbol.length()) {
                    // Write the last character in the symbol
                    currentState |= STATE_FLAG_WRITTEN_FIRST;
                } else {
                    // Always clear last char and whitespace checked
                    currentState &= ~(STATE_FLAG_WRITTEN_FIRST | STATE_FLAG_WHITESPACE_CHECKED);

                    // Next symbol or byte
                    if ((currentState & STATE_FLAG_WRITE_CONSONANT) != 0) {
                        // Switch to vowel
                        currentState = (currentState & ~STATE_FLAG_WRITE_CONSONANT) | STATE_FLAG_WRITE_VOWEL;
                    } else {
                        // Read next char (after possibly writing whitespace)
                        currentState = (currentState & ~STATE_FLAG_WRITE_VOWEL);
                    }
                }
            }
            nextByte = currentByte;
            nextState = currentState;

            readCount += read;
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

        WhitespaceManager(BitspeakConfig config) {
            this.config = Objects.requireNonNull(config, "config cannot be NULL");
            this.maxWordSize = config.getMaxWordSize();
            this.maxLineSize = config.getMaxLineSize();
        }

        /**
         * Attempt to write the given number of characters to the output.
         * @param length the number of characters to write.
         * @return The amount of characters permitted, or 0 if a delimiter must be written first.
         */
        int beginOutput(int length) {
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
                if (currentDelimiter == Delimiter.WORD) {
                    currentLineLength += written;
                }
                delimiterPosition += written;

                // See if the delimiter has finished writing
                if (delimiterPosition >= delimiter.length()) {
                    // Also reset line length
                    if (currentDelimiter == Delimiter.LINE) {
                        currentLineLength = 0;
                    }
                    currentDelimiter = Delimiter.NONE;
                    currentWordLength = 0;
                    delimiterPosition = 0;
                }
            }
            return written;
        }
    }
}
