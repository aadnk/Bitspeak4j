package com.comphenix.bitspeak;

import com.comphenix.bitspeak.function.CharPredicate;

import java.util.Objects;

/**
 * Represents a bitspeak format configuration for line and word delimiters.
 * <p>
 * This enables customizing the maximum number of characters per word (by default, 8) and per line (default is 160),
 * before the encoder will output a delimiter, along with the specific delimiters between each word (default "-")
 * and line (line terminator).
 * </p>
 * It is also possible to specify the characters that are considered skippable whitepace (by default, whitespace, "-" and "_").
 */
public class BitspeakConfig {
    private static BitspeakConfig DEFAULT = BitspeakConfig.newBuilder().build();
    private static BitspeakConfig UNLIMITED_WORD_SIZE = BitspeakConfig.newBuilder().withMaxLineSize(-1).withMaxWordSize(-1).build();

    private final int maxWordSize;
    private final String wordDelimiter;
    private final int maxLineSize;
    private final String lineDelimiter;
    private final CharPredicate skipCharPredicate;

    private BitspeakConfig(Builder builder) {
        this.maxWordSize = builder.maxWordSize;
        this.wordDelimiter = builder.wordDelimiter;
        this.maxLineSize = builder.maxLineSize;
        this.lineDelimiter = builder.lineDelimiter;
        this.skipCharPredicate = builder.skipCharPredicate;
    }

    /**
     * Retrieve the maximum number of characters, including delimiters, in each encoded line.
     * @return Maximum length of encoded lines, or -1 if infinite.
     */
    public int getMaxLineSize() {
        return maxLineSize;
    }

    /**
     * Retrieve the delimiter inserted between each line by the encoder.
     * @return The word delimiter.
     */
    public String getLineDelimiter() {
        return lineDelimiter;
    }

    /**
     * Retrieve the maximum length of each encoded word.
     * @return Maximum length of encoded words, or -1 if infinite.
     */
    public int getMaxWordSize() {
        return maxWordSize;
    }

    /**
     * Retrieve the delimiter inserted between each word by the encoder.
     * @return The word delimiter.
     */
    public String getWordDelimiter() {
        return wordDelimiter;
    }

    /**
     * Retrieve the predicate that matches characters that will be silently skipped by the decoder.
     * <p>
     * By default, this is all whitespace characters (as given by {@link Character#isWhitespace(char)} and the
     * special characters "-" and "_".
     * @return A predicate matching characters that will be skipped.
     */
    public CharPredicate getSkipCharPredicate() {
        return skipCharPredicate;
    }

    /**
     * Retrieve the default bitspeak configuration.
     * @return The default configuration.
     */
    public static BitspeakConfig defaultConfig() {
        return DEFAULT;
    }

    /**
     * Retrieve a bitspeak configuration with infinite word and line sizes, disabling insertion of word and line delimiters.
     * <p>
     * The skip char predicate is the same as the default.
     * @return A bitspeak configuration.
     */
    public static BitspeakConfig unlimitedWordSize() {
        return UNLIMITED_WORD_SIZE;
    }

    /**
     * Retrieve a new builder of bitspeak configuration instances.
     * <p>
     * The builder is initialized to the default values.
     * @return A new builder.
     */
    public static Builder newBuilder() {
        return new Builder();
    }

    /**
     * Retrieve a new builder of bitspeak configuration instances, initialized as a copy of the given configuration-
     * @param template the configuration to copy, or NULL to use default values.
     * @return a new builder.
     */
    public static Builder newBuilder(BitspeakConfig template) {
        return new Builder(template);
    }

    /**
     * Represents a builder of bitspeak configuration instances.
     */
    public static class Builder {
        private int maxWordSize = 8;
        private String wordDelimiter = "-";
        private int maxLineSize = 160;
        private String lineDelimiter = System.lineSeparator();
        private CharPredicate skipCharPredicate = val -> Character.isWhitespace(val) || val == '_' || val == '-';

        /**
         * Construct a new builder of bitspeak configuration instances.
         * <p>
         * The builder is initialized to the default values.
         */
        public Builder() {
            // Initial values
        }

        /**
         * Construct a new builder of bitspeak configuration instances, initialized as a copy of the given configuration-
         * @param template the configuration to copy, or NULL to use default values.
         */
        public Builder(BitspeakConfig template) {
            // Copy template
            if (template != null) {
                this.maxWordSize = template.getMaxWordSize();
                this.wordDelimiter = template.getWordDelimiter();
                this.maxLineSize = template.getMaxLineSize();
                this.lineDelimiter = template.getLineDelimiter();
                this.skipCharPredicate = template.getSkipCharPredicate();
            }
        }

        /**
         * Set the maximum number of characters the encoder will output in each word.
         * @param maxWordSize the new maximum number of characters, or -1 if infinite.
         * @return This builder, for chaining.
         */
        public Builder withMaxWordSize(int maxWordSize) {
            if (maxWordSize < -1 || maxWordSize == 0) {
                throw new IllegalArgumentException("maxWordSize must be positive, or -1.");
            }
            this.maxWordSize = maxWordSize;
            return this;
        }

        /**
         * Set the the delimiter inserted between each line by the encoder.
         * @param wordDelimiter the new word delimiter.
         * @return This builder, for chaining.
         */
        public Builder withWordDelimiter(CharSequence wordDelimiter) {
            this.wordDelimiter = Objects.requireNonNull(wordDelimiter, "wordDelimiter cannot be NULL").toString();
            return this;
        }

        /**
         * Set the the maximum number of characters, including delimiters, the encoder will output in each encoded line.
         * @param maxLineSize the new maximum length of encoded lines, or -1 if infinite.
         * @return This builder, for chaining.
         */
        public Builder withMaxLineSize(int maxLineSize) {
            if (maxWordSize < -1 || maxWordSize == 0) {
                throw new IllegalArgumentException("maxLineSize must be positive, or -1.");
            }
            this.maxLineSize = maxLineSize;
            return this;
        }

        /**
         * Set the the delimiter inserted between each line by the encoder.
         * @param lineDelimiter the new word delimiter.
         * @return This builder, for chaining.
         */
        public Builder withLineDelimiter(CharSequence lineDelimiter) {
            this.lineDelimiter = Objects.requireNonNull(lineDelimiter, "lineDelimiter cannot be NULL").toString();
            return this;
        }

        /**
         * Set the predicate that matches characters that will be silently skipped by the decoder.
         * @param skipCharPredicate the new predicate matching characters that will be skipped. Cannot be NULL.
         * @return This builder, for chaining.
         */
        public Builder withSkipCharPredicate(CharPredicate skipCharPredicate) {
            this.skipCharPredicate = Objects.requireNonNull(skipCharPredicate, "skipCharPredicate cannot be NULL");
            return this;
        }

        /**
         * Construct a new bitspeak configuration instance, using the values in this builder.
         * @return The new configuration instance.
         */
        public BitspeakConfig build() {
            return new BitspeakConfig(this);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BitspeakConfig that = (BitspeakConfig) o;
        return maxWordSize == that.maxWordSize &&
                maxLineSize == that.maxLineSize &&
                Objects.equals(wordDelimiter, that.wordDelimiter) &&
                Objects.equals(lineDelimiter, that.lineDelimiter) &&
                Objects.equals(skipCharPredicate, that.skipCharPredicate);
    }

    @Override
    public int hashCode() {
        return Objects.hash(maxWordSize, wordDelimiter, maxLineSize, lineDelimiter, skipCharPredicate);
    }

    @Override
    public String toString() {
        return "BitspeakConfig{" +
                "maxWordSize=" + maxWordSize +
                ", wordDelimiter='" + wordDelimiter + '\'' +
                ", maxLineSize=" + maxLineSize +
                ", lineDelimiter='" + lineDelimiter + '\'' +
                ", skipCharPredicate=" + skipCharPredicate +
                '}';
    }
}
