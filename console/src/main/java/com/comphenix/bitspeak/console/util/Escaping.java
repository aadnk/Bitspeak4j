package com.comphenix.bitspeak.console.util;

import java.util.function.IntConsumer;

public final class Escaping {
    private Escaping() {
        // Sealed
    }

    public static String unescapeString(CharSequence escaped) {
        StringBuilder builder = new StringBuilder(escaped.length());

        for (int i = 0; i < escaped.length(); i++) {
            char current = escaped.charAt(i);

            if (current == '\\') {
                i = unescapeCharacter(escaped, i, chr -> builder.append((char) chr));
            } else {
                builder.append(current);
            }
        }
        return builder.toString();
    }

    static int unescapeCharacter(CharSequence text, int index, IntConsumer characterConsumer) {
        if (index >= text.length()) {
            throw new IllegalArgumentException("Expected escape \\ at " + index);
        }
        char first = text.charAt(index);

        if (first != '\\') {
            characterConsumer.accept(text.charAt(index));
            return index;
        }
        // Increment and check next character
        if (++index >= text.length()) {
            throw new IllegalArgumentException("Expected escape character at " + index);
        }
        char next = text.charAt(index);

        switch (next) {
            // Addition: Also allow - to be escaped
            case '-':
                characterConsumer.accept('-'); return index;
            case 't':
                characterConsumer.accept('\t'); return index;
            case 'b':
                characterConsumer.accept('\b'); return index;
            case 'n':
                characterConsumer.accept('\n'); return index;
            case 'r':
                characterConsumer.accept( '\r'); return index;
            case 'f':
                characterConsumer.accept( '\f'); return index;
            case '\'':
                characterConsumer.accept( '\''); return index;
            case '"':
                characterConsumer.accept( '"'); return index;
            case '\\':
                characterConsumer.accept( '\\'); return index;
            case 'u':
                // Must have 4 characters
                assertLength(index + 1, index+ 5, text.length());
                String unicode = text.subSequence(index + 1, index + 5).toString();
                index += 4;

                characterConsumer.accept(Integer.parseInt(unicode, 16));
                return index;
        }
        // Octal digits
        if (next >= '0' && next <= '7') {
            int octalEnd = index + 1;

            // Scan for more digits
            for (; octalEnd - index <= 3 && octalEnd < text.length(); octalEnd++) {
                char candidate = text.charAt(octalEnd);

                if (candidate < '0' || candidate > '7') {
                    // Not an octal
                    break;
                }
            }
            int value = Integer.parseInt(text.subSequence(index, octalEnd).toString(), 8);

            if (value > 0xFF) {
                // Remove the last octal
                octalEnd--;
                value >>= 3;
            }
            characterConsumer.accept(value);
            return octalEnd - 1;
        }
        throw new IllegalArgumentException("Unexpected escape character " + next + " at " + index);
    }

    private static void assertLength(int start, int end, int length) {
        if (start >= length) {
            throw new IllegalArgumentException("Expected escape character at " + start);
        }
        if (end > length) {
            throw new IllegalArgumentException("Expected escape character at " + (end - 1));
        }
    }
}


