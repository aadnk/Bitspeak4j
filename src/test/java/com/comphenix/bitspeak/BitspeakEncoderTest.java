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
        assertEquals(expectedBS6, BitspeakEncoder.encode(hex, BitspeakFormat.BS_6));
        assertEquals(expectedBS8, BitspeakEncoder.encode(hex, BitspeakFormat.BS_8));
    }

    @Test
    public void testPatternAA() {
        assertEquals("", BitspeakEncoder.encode(generatePattern(0xAA, 0), BitspeakFormat.BS_6));
        // We are padding at the end, necessary for streaming
        assertEquals("lif", BitspeakEncoder.encode(generatePattern(0xAA, 1), BitspeakFormat.BS_6));
        assertEquals("lilil", BitspeakEncoder.encode(generatePattern(0xAA, 2), BitspeakFormat.BS_6));
        assertEquals("lililili", BitspeakEncoder.encode(generatePattern(0xAA, 3), BitspeakFormat.BS_6));
    }

    @Test
    public void testEncoder() {
        BitspeakEncoder encoder = BitspeakEncoder.newEncoder(BitspeakFormat.BS_6);
        byte[] source = BaseEncoding.base16().lowerCase().decode("f81f9644042f"); // zipuzigikupakare

        char[] destination = new char[16];
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
        assertEquals("zipuzigikupakare", new String(destination, 0, destinationPos));
    }

    @Test
    public void testReader() throws IOException {
        byte[] source = BaseEncoding.base16().lowerCase().decode("f81f9644042f");

        // Test using a very small buffer size
        Reader reader = BitspeakEncoder.newStream(new ByteArrayInputStream(source), BitspeakFormat.BS_6, 4);
        assertTrue(reader.markSupported());

        // Full string: zipuzigikupakare
        assertEquals('z', reader.read());

        reader.mark(1024);
        char[] chunkA = readFully(reader, 4); // ipuz

        reader.reset();
        char[] chunkB = readFully(reader, 4);
        assertArrayEquals(chunkA, chunkB);

        String remaining = CharStreams.toString(reader);
        assertEquals("igikupakare", remaining);
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
