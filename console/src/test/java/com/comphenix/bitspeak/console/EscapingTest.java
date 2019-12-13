package com.comphenix.bitspeak.console;

import com.comphenix.bitspeak.console.util.Escaping;
import org.junit.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class EscapingTest {
    @Test
    public void testEscaping() {
        assertEquals("Hello\r\n", Escaping.unescapeString("Hello\\r\\n"));
        assertEquals("Unicode \u03b1-\t", Escaping.unescapeString("Unicode \\u03b1-\\t"));
        assertEquals("\u03B3-at-start", Escaping.unescapeString("\\u03B3-at-start"));
        assertEquals("Testing octal: \0 \12\377 ", Escaping.unescapeString("Testing octal: \\0 \\12\\377 "));
        assertEquals("Too large octal: \650 ", Escaping.unescapeString("Too large octal: \\650 "));
    }
}
