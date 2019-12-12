package com.comphenix.bitspeak.console;

import java.io.*;

public class Application {
    public static void main(String[] args) throws IOException {
        ParsedCommand parser = ParsedCommand.fromArguments(args);
        boolean standardOutput = OutputSink.isStandardOutput(parser.getOutputSink());

        switch (parser.getMode()) {
            case SHOW_HELP:
                System.out.println("Help"); // TODO: Handle help
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
}
