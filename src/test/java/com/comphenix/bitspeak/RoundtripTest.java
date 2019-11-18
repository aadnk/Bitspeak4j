package com.comphenix.bitspeak;

import com.google.common.io.ByteStreams;
import org.junit.Test;

import java.io.*;
import java.util.Random;

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

    @Test
    public void testStreaming() throws IOException {
        Random rnd = new Random(1);

        byte[] data = new byte[64 * 1024];
        rnd.nextBytes(data);
        
        testFormat(Bitspeak.bs6(), data);
        testFormat(Bitspeak.bs8(), data);
    }

    private void testFormat(Bitspeak bitspeak, byte[] data) throws IOException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        // Pass through streams
        try (Reader reader = bitspeak.newEncodeStream(new ByteArrayInputStream(data));
             InputStream decoded = bitspeak.newDecodeStream(reader)) {
            ByteStreams.copy(decoded, outputStream);
        }
        // Verify output is the same
        assertArrayEquals(data, outputStream.toByteArray());
    }
}
