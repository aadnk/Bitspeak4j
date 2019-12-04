/*
 * Copyright (C) 2019  Kristian S. Stangeland
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 */

package com.comphenix.bitspeak;

import com.google.common.base.Splitter;
import com.google.common.io.BaseEncoding;
import com.google.common.io.ByteStreams;
import org.junit.Test;

import java.io.*;
import java.util.Random;
import java.util.stream.Collectors;

import static com.comphenix.bitspeak.TestPatterns.generatePattern;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.lessThanOrEqualTo;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class RoundtripTest {
    @Test
    public void testAbPattern() {
        testAbPattern(Bitspeak.bs6());
        testAbPattern(Bitspeak.bs8());
    }

    private void testAbPattern(Bitspeak bitspeak) {
        for (int i = 0; i < 64; i++) {
            byte[] input = generatePattern(0xAB, i);
            String encoded = bitspeak.encode(input);

            // Also check estimated lengths
            checkLengthEstimates(bitspeak, input, encoded);

            byte[] decoded = bitspeak.decode(encoded);
            assertArrayEquals(input, decoded);
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
        testByteArray(bitspeak, data);
        testReaderStream(bitspeak, data);
        testEncodeDecodeStreams(bitspeak, data);
    }

    private void testReaderStream(Bitspeak bitspeak, byte[] data) throws IOException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        // Pass through streams
        try (Reader reader = bitspeak.newEncodeStream(new ByteArrayInputStream(data));
             InputStream decoded = bitspeak.newDecodeStream(reader)) {
            ByteStreams.copy(decoded, outputStream);
        }
        // Verify output is the same
        assertArrayEquals(data, outputStream.toByteArray());
    }

    private void testEncodeDecodeStreams(Bitspeak bitspeak, byte[] data) throws IOException {
        StringWriter writer = new StringWriter();
        long encodeCount;

        try (InputStream inputStream = new ByteArrayInputStream(data)) {
            encodeCount = bitspeak.encodeStream(inputStream, writer);
        }
        String encoded = writer.toString();

        assertEquals(encoded.length(), encodeCount);
        ByteArrayOutputStream decoded = new ByteArrayOutputStream();

        // Decode from string
        long decodeCount;

        try (StringReader reader = new StringReader(encoded)) {
            decodeCount = bitspeak.decodeStream(reader, decoded);
        }
        byte[] roundTrip = decoded.toByteArray();

        assertEquals(roundTrip.length, decodeCount);
        assertArrayEquals(data, roundTrip);
    }

    private void testByteArray(Bitspeak bitspeak, byte[] data) {
        // Use byte arrays
        String encodedString = bitspeak.encode(data);
        byte[] decodedArray = bitspeak.decode(encodedString);

        // Test byte array conversion
        assertArrayEquals(data, decodedArray);
        checkLengthEstimates(bitspeak, data, encodedString);
    }

    private void checkLengthEstimates(Bitspeak bitspeak, byte[] input, String encoded) {
        assertThat(encoded.length(), lessThanOrEqualTo(bitspeak.estimateEncodeSize(input.length)));
        assertThat(input.length, lessThanOrEqualTo(bitspeak.estimateDecodeSize(encoded.length())));
    }

    //@Test
    public void printPatterns() {
        String[] hexes = { "01", "0102", "010203", "01020304", "DEADBEEF010203" };

        System.out.println("<table style=\"border-spacing: 4px\"");
        System.out.println("  <tr>");
        System.out.println("    <th>Example</th>");
        System.out.println("    <th>Output</th>");
        System.out.println("  </tr>");

        for (String hex : hexes) {
            for (Bitspeak bitspeak : Bitspeak.formats()) {
                System.out.println("  <tr>");

                String methodName = bitspeak.name().toLowerCase().replace("-", "");

                System.out.print("    <td><code>Bitspeak." + methodName + "().encode(new byte[] { ");
                System.out.print(Splitter.fixedLength(2).splitToList(hex).stream().map(x -> "" +
                        (byte)Integer.parseInt(x, 16)).collect(Collectors.joining(", ")));
                System.out.println(" })</code></td>");

                String output = bitspeak.encode(BaseEncoding.base16().decode(hex));
                System.out.println("    <td>\"" + output + "\"</td>");

                System.out.println("  </tr>");
            }
        }
        System.out.println("</table>");
    }
}
