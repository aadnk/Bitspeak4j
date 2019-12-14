package com.comphenix.bitspeak.console.util;

import com.comphenix.bitspeak.function.CharPredicate;
import org.junit.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

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

    @Test
    public void testMinimizer() {
        CharPredicates.RangePredicate predicate = CharPredicates.RangePredicate.parse("9876543210a");
        CharPredicate expected = chr -> (chr >= '0' && chr <= '9') || chr == 'a';

        comparePredicates(expected, predicate);

        CharPredicates.RangePredicate minimized = predicate.minimize();
        comparePredicates(expected, minimized);

        assertEquals("0-9a", minimized.toString());
    }

    @Test
    public void testEscaping() {
        // Match 00 - 1F (NULL - US) and 7F (DEL)
        CharPredicate predicate = CharPredicates.parsePredicate("\\u0000-\\u001F\\177");
        comparePredicates(chr -> chr <= '\u001F' || chr == '\177', predicate);
    }

    @Test
    public void testUnification() {
        CharPredicates.RangePredicate predicate = CharPredicates.RangePredicate.parse("a-y0-53-789b-z").minimize();

        comparePredicates(chr -> (chr >= '0' && chr <= '9') || (chr >= 'a' && chr <= 'z'), predicate);
        assertEquals("0-9a-z", predicate.toString());
    }

    @Test
    public void testSeparate() {
        CharPredicates.RangePredicate predicate = CharPredicates.RangePredicate.parse("012346789").minimize();
        assertEquals("0-46-9", predicate.toString());
    }

    @Test
    public void testBinarySearch() {
        CharPredicates.RangePredicate lookup = CharPredicates.RangePredicate.parse("B-Yace-hj-nprs-y4-6").minimize();

        // Force predicate to use binary search (not API)
        CharPredicates.RangePredicate binarySearch = new CharPredicates.RangePredicate(lookup.ranges(), true,
                CharPredicates.RangePredicate.Algorithm.BINARY_SEARCH);

        comparePredicates(lookup, binarySearch);
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
