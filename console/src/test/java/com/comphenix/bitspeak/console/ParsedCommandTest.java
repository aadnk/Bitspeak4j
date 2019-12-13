package com.comphenix.bitspeak.console;

import com.comphenix.bitspeak.Bitspeak;
import com.comphenix.bitspeak.BitspeakConfig;
import org.junit.jupiter.api.Test;

import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ParsedCommandTest {
    @Test
    public void testEncode() {
        ParsedCommand command = ParsedCommand.fromArguments("encode", "test.txt");

        assertEquals(new ParsedCommand(
                ParsedCommand.Mode.ENCODE, Bitspeak.bs6(),
                InputSource.fromPath(Paths.get("test.txt")),
                OutputSink.standardOutput(), BitspeakConfig.defaultConfig()), command);
    }

    @Test
    public void testEncodeText() {
        ParsedCommand command = ParsedCommand.fromArguments("encode", "-b", "bs-8", "-i", "Hello world", "-o", "output.txt", "-ml", "80", "-mw", "8");

        assertEquals(new ParsedCommand(
                ParsedCommand.Mode.ENCODE, Bitspeak.bs8(),
                InputSource.fromString("Hello world"),
                OutputSink.fromPath(Paths.get("output.txt")),
                BitspeakConfig.newBuilder().withMaxLineSize(80).withMaxWordSize(8).build()), command);
    }
}
