package com.comphenix.bitspeak.console;

import com.comphenix.bitspeak.console.util.CharPredicates;
import com.comphenix.bitspeak.function.CharPredicate;
import org.junit.Test;

import static org.junit.jupiter.api.Assertions.*;

public class CharPredicatesTest {
    @Test
    public void testParsing() {
        CharPredicate predicate = CharPredicates.parsePredicate("a-z0-9@");
        comparePredicates(chr -> (chr >= 'a' && chr <= 'z') || (chr >= '0' && chr <= '9') || chr == '@', predicate);
    }

    @Test
    public void testList() {
        CharPredicate predicate = CharPredicates.parsePredicate("0123456789");
        comparePredicates(chr -> chr >= '0' && chr <= '9', predicate);
    }

    private void comparePredicates(CharPredicate expected, CharPredicate created) {
        // Test ASCII
        for (int i = 0; i < 255; i++) {
            char chr = (char) i;
            boolean expectedResult = expected.test(chr);
            boolean actualResult = created.test(chr);
            assertEquals(expectedResult, actualResult, "Character " + chr + " (" + i + ")");
        }
    }
}
