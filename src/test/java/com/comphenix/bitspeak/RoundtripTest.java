package com.comphenix.bitspeak;

import org.junit.Test;

import static com.comphenix.bitspeak.TestPatterns.generatePattern;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.lessThanOrEqualTo;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;

public class RoundtripTest {
    @Test
    public void testAbPattern() {
        for (Bitspeak bitspeak : Bitspeak.formats()) {
            for (int i = 0; i < 64; i++) {
                byte[] input = generatePattern(0xAB, i);
                String encoded = bitspeak.encode(input);

                // Also check estimated lengths
                assertThat(encoded.length(), lessThanOrEqualTo(bitspeak.estimateEncodeSize(input.length)));
                assertThat(input.length, lessThanOrEqualTo(bitspeak.estimateDecodeSize(encoded.length())));

                byte[] decoded = bitspeak.decode(encoded);
                assertArrayEquals(input, decoded);
            }
        }
    }
}
