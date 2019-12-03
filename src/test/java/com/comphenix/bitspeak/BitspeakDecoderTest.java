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
import org.junit.Test;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;

public class BitspeakDecoderTest {
    @Test
    public void testSpecificationExamples() {
        testExample("zipuzigi", "zunbowven", "f81f96");
        testExample("kupakare", "kuputow", "44042f");
        testExample("tuzubugi", "tinyegen", "27d156");
        testExample("bunugiji", "binganvaw", "17559e");
        testExample("nevutave", "yawgitin", "de5227");
        testExample("tifagedi", "taipanmaw", "2a05ce");
        testExample("gupevuke", "gudongo", "543953");
        testExample("dimazava", "deipowtu", "3b0f24");
        testExample("muribanu", "mensedan", "c6e135");
        testExample("zadinexa", "zasuiyun", "f0edd8");
        testExample("pafesupi", "pidawki", "023e42"); // left-padded
        testExample("xesufage", "chowvunbin", "6f9817");
        testExample("tajagupi", "temanki", "21c542");
        testExample("tatenegi", "taruiyen", "20bdd6");
        testExample("defipexa", "dawtayun", "3e20d8");
        testExample("befipuka", "bawtaga", "1e2050");
        testExample("vakijeva", "vetinsu", "9127e4");
        testExample("kudizila", "kusowlun", "44efa8");
        testExample("mamejedu", "modinmui", "c337cd");
        testExample("fazemuzi", "fozoijaw", "83fc7e");
        testExample("nikepuve", "yondachin", "d93067");
        testExample("teguzenu", "tuigowzan", "2d5ff5");
        testExample("gaduxevu", "gayensan", "50d6e5");
        testExample("vibetiba", "vunjifu", "987284");
    }

    private void testExample(String inputBS6, String inputBS8, String expectedOutput) {
        byte[] expected = BaseEncoding.base16().lowerCase().decode(expectedOutput);

        assertArrayEquals(expected, Bitspeak.bs6().decode(inputBS6));
        assertArrayEquals(expected, Bitspeak.bs8().decode(inputBS8));

        testDecodeFinal(Bitspeak.bs6(), inputBS6, expected);
        testDecodeFinal(Bitspeak.bs8(), inputBS8, expected);
    }

    private void testDecodeFinal(Bitspeak bitspeak, String input, byte[] expected) {
        byte[] output = new byte[bitspeak.estimateDecodeSize(input.length())];

        int decoded = bitspeak.newDecoder().decodeFinal(input.toCharArray(), output);
        byte[] outputRange = Arrays.copyOf(output, decoded);

        assertArrayEquals(expected, outputRange);
    }

    @Test
    public void testEstimateError() {
        decodeUsingWrongEstimate(0);
        decodeUsingWrongEstimate(1);
    }

    private void decodeUsingWrongEstimate(int estimateLength) {
        // Test BS-6 with a broken decode estimate
        Bitspeak bitspeak = new Bitspeak(Bitspeak.Format.BS_6, BitspeakConfig.defaultConfig()) {
            @Override
            public int estimateDecodeSize(int characterCount) {
                return estimateLength;
            }
        };
        byte[] result = bitspeak.decode("zipuzigi");
        assertArrayEquals(BaseEncoding.base16().decode("F81F96"), result);
    }


}
