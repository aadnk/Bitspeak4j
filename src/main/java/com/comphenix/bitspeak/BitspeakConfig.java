package com.comphenix.bitspeak;

import com.comphenix.bitspeak.function.CharPredicate;

import java.util.Objects;

/**
 * Represents a custom bitspeak format configuration.
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
     * Retrieve the maximum length of encoded lines.
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
     * Retrieve the maximum length of encoded words.
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
     * Retrieve a bitspeak configuration with infinite word and line size.
     * @return A bitspeak configuration.
     */
    public static BitspeakConfig unlimitedWordSize() {
        return UNLIMITED_WORD_SIZE;
    }

    public static Builder newBuilder() {
        return new Builder();
    }

    public static Builder newBuilder(BitspeakConfig config) {
        return new Builder(config);
    }

    public static class Builder {
        private int maxWordSize = 8;
        private String wordDelimiter = "-";
        private int maxLineSize = 160;
        private String lineDelimiter = System.lineSeparator();
        private CharPredicate skipCharPredicate = val -> Character.isWhitespace(val) || val == '_' || val == '-';

        public Builder() {
            // Initial values
        }

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

        public Builder withMaxWordSize(int maxWordSize) {
            if (maxWordSize < -1 || maxWordSize == 0) {
                throw new IllegalArgumentException("maxWordSize must be positive, or -1.");
            }
            this.maxWordSize = maxWordSize;
            return this;
        }

        public Builder withWordDelimiter(CharSequence wordDelimiter) {
            this.wordDelimiter = Objects.requireNonNull(wordDelimiter, "wordDelimiter cannot be NULL").toString();
            return this;
        }

        public Builder withMaxLineSize(int maxLineSize) {
            if (maxWordSize < -1 || maxWordSize == 0) {
                throw new IllegalArgumentException("maxLineSize must be positive, or -1.");
            }
            this.maxLineSize = maxLineSize;
            return this;
        }

        public Builder withLineDelimiter(CharSequence lineDelimiter) {
            this.lineDelimiter = Objects.requireNonNull(lineDelimiter, "lineDelimiter cannot be NULL").toString();
            return this;
        }

        public Builder withSkipCharPredicate(CharPredicate skipCharPredicate) {
            this.skipCharPredicate = Objects.requireNonNull(skipCharPredicate, "skipCharPredicate cannot be NULL");
            return this;
        }

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
