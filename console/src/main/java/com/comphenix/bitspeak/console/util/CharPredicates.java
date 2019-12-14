package com.comphenix.bitspeak.console.util;

import com.comphenix.bitspeak.function.CharPredicate;

import java.util.*;

public class CharPredicates {
    private CharPredicates() {
        // Sealed
    }

    /**
     * Represents a character change.
     */
    public static class CharRange implements Comparable<CharRange> {
        private final char first;
        private final char last;

        public CharRange(char first, char last) {
            this.first = first;
            this.last = last;
        }

        public char getFirst() {
            return first;
        }

        public char getLast() {
            return last;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            CharRange charRange = (CharRange) o;
            return first == charRange.first &&
                    last == charRange.last;
        }

        @Override
        public int hashCode() {
            return Objects.hash(first, last);
        }

        @Override
        public int compareTo(CharRange o) {
            return Character.compare(first, o.first);
        }

        @Override
        public String toString() {
            return "CharRange{" +
                    "first=" + first +
                    ", last=" + last +
                    '}';
        }
    }

    /**
     * Create a character predicate from the given string.
     * @param text string of character ranges.
     * @return The corresponding character predicate.
     */
    public static CharPredicate parsePredicate(String text) {
        return RangePredicate.parse(text).minimize();
    }

    static class RangePredicate implements CharPredicate {
        enum Algorithm {
            LINEAR_SCAN,
            BINARY_SEARCH
        }

        private static final int BINARY_SEARCH_MINIMUM = 32;

        private final List<CharRange> ranges;
        private final Algorithm algorithm;

        /**
         * TRUE if the ranges have been sorted by starting position, and minimized to
         * ensure there are no overlapping ranges.
         */
        private final boolean minimized;

        public RangePredicate(Collection<? extends CharRange> ranges) {
            this(ranges, false);
        }

        protected RangePredicate(Collection<? extends CharRange> ranges, boolean minimized) {
            this(ranges, minimized, minimized &&
                    ranges.size() >= BINARY_SEARCH_MINIMUM ? Algorithm.BINARY_SEARCH : Algorithm.LINEAR_SCAN);
        }

        protected RangePredicate(Collection<? extends CharRange> ranges, boolean minimized, Algorithm algorithm) {
            this.ranges = new ArrayList<>(ranges);
            this.minimized = minimized;
            this.algorithm = algorithm;
        }

        /**
         * Create a range predicate from the given string.
         * @param text the text.
         * @return The corresponding predicate.
         */
        public static RangePredicate parse(String text) {
            char[] buffer = new char[2];
            List<CharRange> rangeList = new ArrayList<>();

            for (int i = 0; i < text.length(); i++) {
                // Read range
                i = Escaping.unescapeCharacter(text, i, chr -> buffer[0] = (char)chr) + 1;

                if (i >= text.length() || text.charAt(i) != '-') {
                    // Add single character
                    rangeList.add(new CharRange(buffer[0], buffer[0]));
                    i--;
                    continue;
                }
                i = Escaping.unescapeCharacter(text, i + 1, chr -> buffer[1] = (char)chr);

                // Add to range
                rangeList.add(new CharRange(buffer[0], buffer[1]));
            }
            // Sort ranges
            return new RangePredicate(rangeList);
        }

        /**
         * Optimize the range representation of the current predicate.
         * @return The new range predicate.
         */
        public RangePredicate minimize() {
            if (minimized) {
                return this;
            }
            ArrayList<CharRange> sorted = new ArrayList<>(ranges);
            Collections.sort(sorted);

            int startChar = -1;
            int endChar = -1;

            // Minimize ranges
            List<CharRange> minimized = new ArrayList<>();
            for (CharRange range : sorted) {
                // Start a new range
                if (startChar < 0) {
                    startChar = range.first;
                    endChar = range.last;

                // Join range if possible
                } else if (range.last >= startChar - 1 && range.first <= endChar + 1) {
                    startChar = Math.min(range.first, startChar);
                    endChar = Math.max(range.last, endChar);

                // Output range and start new
                } else {
                    minimized.add(new CharRange((char) startChar, (char) endChar));
                    startChar = range.first;
                    endChar = range.last;
                }
            }
            if (startChar >= 0) {
                minimized.add(new CharRange((char)startChar, (char)endChar));
            }
            // Indicate that each range is sorted and distinct
            return new RangePredicate(minimized, true);
        }

        /**
         * Retrieve a view of the current character ranges.
         * @return Current character ranges.
         */
        public List<CharRange> ranges() {
            return Collections.unmodifiableList(ranges);
        }

        @Override
        public boolean test(char value) {
            if (algorithm == Algorithm.BINARY_SEARCH) {
                return binarySearch(value);
            }
            // Find the first range that is equal or exceed the current character
            for (CharRange range : ranges) {
                if (range.first <= value && value <= range.last) {
                    return true;
                }
            }
            return false;
        }

        private boolean binarySearch(char value) {
            int position = Collections.binarySearch(ranges, new CharRange(value, value));

            if (position >= 0) {
                return true;
            }
            int insertionPoint = -(position + 1);

            // Walk backwards from this position
            for (int i = insertionPoint - 1; i >= 0; i--) {
                CharRange range = ranges.get(i);

                if (range.first <= value && value <= range.last) {
                    return true;
                } else if (value > range.first) {
                    break;
                }
            }
            return false;
        }

        @Override
        public String toString() {
            StringBuilder builder = new StringBuilder();
            for (CharRange range : ranges) {
                writeLiteral(builder, range.getFirst());

                if (range.getFirst() != range.getLast()) {
                    builder.append('-');
                    writeLiteral(builder, range.getLast());
                }
            }
            return builder.toString();
        }

        private void writeLiteral(StringBuilder builder, char character) {
            // Only - and / are required to be escaped
            if (character == '-' || character == '\\') {
                builder.append('\\');
            }
            builder.append(character);
        }
    }
}
