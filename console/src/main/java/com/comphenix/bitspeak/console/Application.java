package com.comphenix.bitspeak.console;

import com.comphenix.bitspeak.Bitspeak;
import com.comphenix.bitspeak.BitspeakConfig;

import java.io.*;
import java.nio.CharBuffer;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Objects;

public class Application {
    private enum Mode {
        SHOW_HELP,
        ENCODE,
        DECODE
    }

    private enum InputSourceType {
        STDIN,
        FILE,
        TEXT
    }

    private static class InputSource {
        private final InputSourceType sourceType;
        private final String source;

        public InputSource(InputSourceType sourceType, String source) {
            this.sourceType = Objects.requireNonNull(sourceType, "sourceType cannot be NULL");
            this.source = source;
        }

        public InputSourceType getSourceType() {
            return sourceType;
        }

        public String getSource() {
            return source;
        }

        public Reader toReader() throws IOException {
            switch (sourceType) {
                case STDIN: return new InputStreamReader(System.in);
                case FILE: return Files.newBufferedReader(Paths.get(source));
                case TEXT: return new StringReader(source);
                default: throw new IllegalStateException("Unknwon source type: " + sourceType);
            }
        }

        public InputStream toInputStream() throws IOException {
            switch (sourceType) {
                case STDIN: return System.in;
                case FILE: return Files.newInputStream(Paths.get(source));
                case TEXT: throw new IllegalStateException("Not supported");
                default: throw new IllegalStateException("Unknwon source type: " + sourceType);
            }
        }

        @Override
        public String toString() {
            switch (sourceType) {
                case STDIN: return "STDIN";
                case FILE: return "file " + source;
                case TEXT: return "\"" + source + "\"";
                default: throw new IllegalStateException("Unknwon source type: " + sourceType);
            }
        }
    }

    private enum OutputSinkType {
        STDOUT,
        FILE
    }

    private static class OutputSink {
        private final OutputSinkType sourceType;
        private final String source;

        public OutputSink(OutputSinkType sourceType, String source) {
            this.sourceType = Objects.requireNonNull(sourceType, "sourceType cannot be NULL");
            this.source = source;
        }

        public OutputSinkType getSourceType() {
            return sourceType;
        }

        public String getSource() {
            return source;
        }

        public OutputStream toOutputStream() throws IOException {
            switch (sourceType) {
                case FILE: return Files.newOutputStream(Paths.get(source));
                case STDOUT: return System.out;
                default: throw new IllegalStateException("Unknwon source type: " + sourceType);
            }
        }

        public Writer toWriter() throws IOException {
            switch (sourceType) {
                case FILE: return Files.newBufferedWriter(Paths.get(source));
                case STDOUT: return adaptPrintStream(System.out);
                default: throw new IllegalStateException("Unknwon source type: " + sourceType);
            }
        }

        private Writer adaptPrintStream(PrintStream printStream) {
            return new Writer() {
                @Override
                public void write(char[] cbuf, int off, int len) throws IOException {
                    printStream.append(CharBuffer.wrap(cbuf, off, len));
                }

                @Override
                public void flush() throws IOException {
                    printStream.flush();
                }

                @Override
                public void close() throws IOException {
                    printStream.close();
                }
            };
        }

        @Override
        public String toString() {
            return sourceType == OutputSinkType.FILE ? "file " + source : "STOUT";
        }
    }

    private static class Parser {
        private Mode mode;
        private Bitspeak bitspeak;
        private InputSource inputSource;
        private OutputSink outputSink;
        private BitspeakConfig.Builder configBuilder = BitspeakConfig.newBuilder();

        public static Parser fromArguments(String[] args) {
            Parser parser = new Parser();
            parser.parse(args);
            return parser;
        }

        public void parse(String[] args) {
            for (int i = 0; i < args.length; i++) {
                String argument = args[i];

                // Detect mode
                if ("encode".equalsIgnoreCase(argument)) {
                    setMode(Mode.ENCODE);
                } else if ("decode".equalsIgnoreCase(argument)) {
                    setMode(Mode.DECODE);
                } else if (argument.startsWith("-")) {
                    switch (argument.toLowerCase()) {
                        case "-?":
                        case "--help":
                            mode = Mode.SHOW_HELP;
                            break;

                        case "-b":
                        case "-bitspeak":
                            setBitspeakFormat(parseBitspeak(getString(args, ++i)));
                            break;

                        // Handle text input
                        case "-i":
                        case "--input-text":
                            setInputSource(new InputSource(InputSourceType.TEXT, getString(args, ++i)));
                            break;

                        case "-o":
                        case "--output-file":
                            setOutputSink(new OutputSink(OutputSinkType.FILE, getString(args, ++i)));
                            break;

                        case "-ml":
                        case "--max-line-size":
                            configBuilder.withMaxLineSize(getInteger(args, ++i));
                            break;

                        case "-mw":
                        case "--max-word-size":
                            configBuilder.withMaxWordSize(getInteger(args, ++i));
                            break;

                        case "-ld":
                        case "--line-delimiter":
                            configBuilder.withLineDelimiter(getString(args, ++i));
                            break;

                        case "-wd":
                        case "--word-delimiter":
                            configBuilder.withWordDelimiter(getString(args, ++i));
                            break;

                        default:
                            throw new IllegalArgumentException("Unknown argument: " + argument);
                    }
                } else {
                    // Assume file
                    setInputSource(new InputSource(InputSourceType.FILE, argument));
                }
            }
            // Default to showing help if nothing is specified
            if (mode == null && inputSource == null) {
                mode = Mode.SHOW_HELP;
            }
            // Use standard values
            if (inputSource == null) {
                inputSource = new InputSource(InputSourceType.STDIN, null);
            }
            if (outputSink == null) {
                outputSink = new OutputSink(OutputSinkType.STDOUT, null);
            }
            if (bitspeak == null) {
                // BS-6 is default
                bitspeak = Bitspeak.bs6();
            }
        }

        private String getString(String[] args, int index) {
            if (index >= args.length) {
                throw new IllegalStateException("Expected argument at index " + index);
            }
            return args[index];
        }

        private int getInteger(String[] args, int index) {
            String element = getString(args, index);

            try {
                return Integer.parseInt(element);
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("Argument '" + element + "' at position " + index + " must be an integer.");
            }
        }

        public Bitspeak getBitspeak() {
            return bitspeak.withConfig(getConfig());
        }

        public InputSource getInputSource() {
            return inputSource;
        }

        public OutputSink getOutputSink() {
            return outputSink;
        }

        public BitspeakConfig getConfig() {
            return configBuilder.build();
        }

        public Mode getMode() {
            return mode;
        }

        private static Bitspeak parseBitspeak(String text) {
            switch (text.toLowerCase()) {
                case "bs6":
                case "bs-6":
                    return Bitspeak.bs6();
                case "bs8":
                case "bs-8":
                    return Bitspeak.bs8();
                default:
                    throw new IllegalArgumentException("Cannot parse " + text + " as a bitspeak format");
            }
        }

        private void setOutputSink(OutputSink outputSink) {
            if (this.outputSink != null) {
                throw new IllegalStateException("Output has already been set to " + outputSink);
            }
            this.outputSink = outputSink;
        }

        private void setBitspeakFormat(Bitspeak bitspeak) {
            if (this.bitspeak != null) {
                throw new IllegalStateException("Format has already been set to " + bitspeak);
            }
            this.bitspeak = bitspeak;
        }
        private void setInputSource(InputSource source) {
            if (this.inputSource != null) {
                throw new IllegalStateException("input has already been set to " + inputSource);
            }
            this.inputSource = source;
        }

        private void setMode(Mode mode) {
            if (this.mode != null) {
                throw new IllegalStateException("Mode has already been set to " + mode);
            }
            this.mode = mode;
        }
    }

    public static void main(String[] args) throws IOException {
        Parser parser = Parser.fromArguments(args);

        switch (parser.getMode()) {
            case SHOW_HELP:
                System.out.println("Help"); // TODO: Handle help
                break;
            case DECODE:
                try (Reader reader = parser.getInputSource().toReader();
                     OutputStream outputStream = parser.getOutputSink().toOutputStream()) {
                    long byteCount = parser.getBitspeak().decodeStream(reader, outputStream);

                    // Write status
                    if (parser.getOutputSink().getSourceType() != OutputSinkType.STDOUT) {
                        System.out.println("Written " + byteCount + " bytes to " + parser.getOutputSink());
                    }
                }
                break;

            case ENCODE:
                try (InputStream reader = parser.getInputSource().toInputStream();
                     Writer outputStream = parser.getOutputSink().toWriter()) {
                    long charCount = parser.getBitspeak().encodeStream(reader, outputStream);

                    // Write status
                    if (parser.getOutputSink().getSourceType() != OutputSinkType.STDOUT) {
                        System.out.println("Written " + charCount + " characters to " + parser.getOutputSink());
                    }
                }
                break;
        }
    }
}
