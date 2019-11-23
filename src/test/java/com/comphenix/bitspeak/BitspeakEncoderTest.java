/*
 * Copyright (C) 2019  Kristian S. Stangeland
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 */

package com.comphenix.bitspeak;

import com.google.common.io.BaseEncoding;
import com.google.common.io.CharStreams;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.Reader;

import static com.comphenix.bitspeak.TestPatterns.generatePattern;
import static org.junit.jupiter.api.Assertions.*;

public class BitspeakEncoderTest {
    @Test
    public void testSpecificationExamples() {
        testExample("f81f96", "zipuzigi", "zunbowven");
        testExample("44042f", "kupakare", "kuputow");
        testExample("27d156", "tuzubugi", "tinyegen");
        testExample("17559e", "bunugiji", "binganvaw");
        testExample("de5227", "nevutave", "yawgitin");
        testExample("2a05ce", "tifagedi", "taipanmaw");
        testExample("543953", "gupevuke", "gudongo");
        testExample("3b0f24", "dimazava", "deipowtu");
        testExample("c6e135", "muribanu", "mensedan");
        testExample("f0edd8", "zadinexa", "zasuiyun");
        testExample("023e42", "pafesupi", "pidawki"); // left-padded
        testExample("6f9817", "xesufage", "chowvunbin");
        testExample("21c542", "tajagupi", "temanki");
        testExample("20bdd6", "tatenegi", "taruiyen");
        testExample("3e20d8", "defipexa", "dawtayun");
        testExample("1e2050", "befipuka", "bawtaga");
        testExample("9127e4", "vakijeva", "vetinsu");
        testExample("44efa8", "kudizila", "kusowlun");
        testExample("c337cd", "mamejedu", "modinmui");
        testExample("83fc7e", "fazemuzi", "fozoijaw");
        testExample("d93067", "nikepuve", "yondachin");
        testExample("2d5ff5", "teguzenu", "tuigowzan");
        testExample("50d6e5", "gaduxevu", "gayensan");
        testExample("987284", "vibetiba", "vunjifu");
    }

    private void testExample(String inputHex, String expectedBS6, String expectedBS8) {
        byte[] hex = BaseEncoding.base16().lowerCase().decode(inputHex);
        assertEquals(expectedBS6, Bitspeak.bs6().withConfig(BitspeakConfig.unlimitedWordSize()).encode(hex));
        assertEquals(expectedBS8, Bitspeak.bs8().withConfig(BitspeakConfig.unlimitedWordSize()).encode(hex));
    }

    @Test
    public void testPatternAA() {
        assertEquals("", Bitspeak.bs6().encode(generatePattern(0xAA, 0)));
        // We are padding at the end, necessary for streaming
        assertEquals("lif", Bitspeak.bs6().encode(generatePattern(0xAA, 1)));
        assertEquals("lilil", Bitspeak.bs6().encode(generatePattern(0xAA, 2)));
        assertEquals("lililili", Bitspeak.bs6().encode(generatePattern(0xAA, 3)));
    }

    @Test
    public void testEncode() {
        byte[] data = new byte[] { 1, 2, 3};
        assertEquals("pakatape", Bitspeak.bs6().encode(data, 0, 3));
        assertEquals("pafad", Bitspeak.bs6().encode(data, 1, 2));
        assertEquals("pam", Bitspeak.bs6().encode(data, 2, 1));
    }

    @Test
    public void testWordSplitter() {
        BitspeakConfig config = BitspeakConfig.newBuilder().withMaxLineSize(36).withMaxWordSize(6).build();
        Bitspeak bs6 = Bitspeak.bs6().withConfig(config);
        Bitspeak bs8 = Bitspeak.bs8().withConfig(config);

        byte[] data = BaseEncoding.base16().decode("0102030405060708091001020304050607080910");
        assertEquals("pakata-pepupa-gabipu-mafatu-bapaba-p" + System.lineSeparator() + "ipamak-abupuf-ajatap-ikup", bs6.encode(data));
        assertEquals("pepipo-pupanp-enpinp-unponb-apepip-o" + System.lineSeparator() + "pupanp-enpinp-unponb-a", bs8.encode(data));
    }

    @Test
    public void testEncoder() {
        BitspeakEncoder encoder = BitspeakEncoder.newEncoder(Bitspeak.Format.BS_6, BitspeakConfig.defaultConfig());
        byte[] source = BaseEncoding.base16().lowerCase().decode("f81f9644042f"); // zipuzigikupakare

        char[] destination = new char[32];
        int destinationPos = 0;

        // Write f8
        destinationPos += encoder.encodeBlock(source, 0, 1,
                destination, destinationPos, destination.length);
        // Write 1f964404
        destinationPos += encoder.encodeBlock(source, 1, 4,
                destination, destinationPos, destination.length - destinationPos);
        // Write 2f
        destinationPos += encoder.encodeBlock(source, 5, 1,
                destination, destinationPos, destination.length);

        // Must always end with finish block
        destinationPos += encoder.finishBlock(destination, destinationPos, destination.length - destinationPos);
        assertEquals("zipuzigi-kupakare", new String(destination, 0, destinationPos));
    }

    @Test
    public void testReader() throws IOException {
        byte[] source = BaseEncoding.base16().lowerCase().decode("f81f9644042f");

        // Test using a very small buffer size
        Reader reader = Bitspeak.bs6().newEncodeStream(new ByteArrayInputStream(source), 4);
        assertTrue(reader.markSupported());

        // Full string: zipuzigikupakare
        assertEquals('z', reader.read());

        reader.mark(1024);
        char[] chunkA = readFully(reader, 4); // ipuz

        reader.reset();
        char[] chunkB = readFully(reader, 4);
        assertArrayEquals(chunkA, chunkB);

        String remaining = CharStreams.toString(reader);
        assertEquals("igi-kupakare", remaining);
    }

    private char[] readFully(Reader reader, int length) throws IOException {
        char[] result = new char[length];
        int position = 0;

        while (position < length) {
            int read = reader.read(result, position, length - position);

            if (read < 0) {
                throw new IOException("EOF");
            }
            position += read;
        }
        return result;
    }
}
