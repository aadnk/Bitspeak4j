package com.comphenix.bitspeak.console.util;

import com.comphenix.bitspeak.function.CharPredicate;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class CharPredicates {
    private CharPredicates() {
        // Sealed
    }

    private static class CharRange implements Comparable<CharRange> {
        private final char first;
        private final char last;

        public CharRange(char first, char last) {
            this.first = first;
            this.last = last;
        }

        @Override
        public int compareTo(CharRange o) {
            return Character.compare(first, o.first);
        }
    }

    /**
     * Create a character predicate from the given string.
     * @param text string of character ranges.
     * @return The corresponding character predicate.
     */
    public static CharPredicate parsePredicate(String text) {
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
        return createPredicate(rangeList);
    }

    private static CharPredicate createPredicate(List<? extends CharRange> ranges) {
        // Predicable order
        Collections.sort(ranges);

        return chr -> {
            // Find the first range that is equal or exceed the current character
            for (CharRange range : ranges) {
                if (range.first <= chr && chr <= range.last) {
                    return true;
                }
            }
            return false;
        };
    }

}
