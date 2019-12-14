package com.comphenix.bitspeak.console;

import java.io.*;
import java.nio.charset.StandardCharsets;

public class Application {
    public static void main(String[] args) throws IOException {
        ParsedCommand parser = ParsedCommand.fromArguments(args);
        boolean standardOutput = OutputSink.isStandardOutput(parser.getOutputSink());

        switch (parser.getMode()) {
            case SHOW_HELP:
                String helpText = getResourceAsString("help.txt");
                System.out.println(helpText);
                break;
            case DECODE:
                try (Reader reader = parser.getInputSource().toReader();
                     OutputStream outputStream = parser.getOutputSink().toOutputStream()) {
                    long byteCount = parser.getBitspeak().decodeStream(reader, outputStream);

                    // Write status
                    if (!standardOutput) {
                        System.out.println("Written " + byteCount + " bytes to " + parser.getOutputSink());
                    }
                }
                break;

            case ENCODE:
                try (InputStream reader = parser.getInputSource().toInputStream();
                     Writer outputStream = parser.getOutputSink().toWriter()) {
                    long charCount = parser.getBitspeak().encodeStream(reader, outputStream);

                    // Write status
                    if (!standardOutput) {
                        System.out.println("Written " + charCount + " characters to " + parser.getOutputSink());
                    }
                }
                break;
        }
    }

    private static String getResourceAsString(String name) {
        InputStream inputStream = Application.class.getResourceAsStream(name);

        if (inputStream == null) {
            throw new IllegalArgumentException("Unable to find " + name);
        }
        try (InputStream stream = inputStream;
             InputStreamReader reader = new InputStreamReader(stream, StandardCharsets.UTF_8);
             BufferedReader bufferedReader = new BufferedReader(reader)) {

            StringBuilder builder = new StringBuilder();
            String line;

            while ((line = bufferedReader.readLine()) != null) {
                builder.append(line);
                builder.append(System.lineSeparator());
            }
            return builder.toString();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
