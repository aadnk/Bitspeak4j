package com.comphenix.bitspeak;

import org.junit.Test;

import static com.comphenix.bitspeak.TestPatterns.generatePattern;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.lessThanOrEqualTo;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;

public class RoundtripTest {
    @Test
    public void testAbPattern() {
        for (BitspeakFormat format : BitspeakFormat.values()) {
            for (int i = 0; i < 64; i++) {
                byte[] input = generatePattern(0xAB, i);
                String encoded = BitspeakEncoder.encode(input, format);

                // Also check estimated lengths
                assertThat(encoded.length(), lessThanOrEqualTo(format.getMaxEncodeSize(input.length)));
                assertThat(input.length, lessThanOrEqualTo(format.getMaxDecodeSize(encoded.length())));

                byte[] decoded = BitspeakDecoder.decode(encoded, format);
                assertArrayEquals(input, decoded);
            }
        }
    }
}
