package com.comphenix.bitspeak;

import com.google.common.io.BaseEncoding;
import org.junit.Test;

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

        assertArrayEquals(expected, BitspeakDecoder.decode(inputBS6, BitspeakFormat.BS_6));
        assertArrayEquals(expected, BitspeakDecoder.decode(inputBS8, BitspeakFormat.BS_8));
    }
}
