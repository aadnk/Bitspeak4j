package com.comphenix.bitspeak.console;

import com.comphenix.bitspeak.Bitspeak;
import com.comphenix.bitspeak.BitspeakConfig;
import com.comphenix.bitspeak.console.util.Escaping;

import java.nio.file.Paths;
import java.util.Objects;

/**
 * Parsed command line arguments.
 */
class ParsedCommand {
    public enum Mode {
        SHOW_HELP,
        ENCODE,
        DECODE
    }

    private final Mode mode;
    private final Bitspeak bitspeak;
    private final InputSource inputSource;
    private final OutputSink outputSink;
    private final BitspeakConfig config;

    protected ParsedCommand(Mode mode, Bitspeak bitspeak, InputSource inputSource, OutputSink outputSink, BitspeakConfig config) {
        this.mode = mode;
        this.bitspeak = bitspeak;
        this.inputSource = inputSource;
        this.outputSink = outputSink;
        this.config = config;
    }

    public static ParsedCommand fromArguments(String... args) {
        Mode mode = null;
        Bitspeak bitspeak = null;
        InputSource inputSource = null;
        OutputSink outputSink = null;
        BitspeakConfig.Builder configBuilder = BitspeakConfig.newBuilder();

        for (int i = 0; i < args.length; i++) {
            String argument = args[i];

            // Detect mode
            if ("encode".equalsIgnoreCase(argument)) {
                checkNull(mode, "Mode has already been set to %s");
                mode = Mode.ENCODE;
            } else if ("decode".equalsIgnoreCase(argument)) {
                checkNull(mode, "Mode has already been set to %s");
                mode = Mode.ENCODE;
            } else if (argument.startsWith("-")) {
                switch (argument.toLowerCase()) {
                    case "-?":
                    case "--help":
                        // Can overwrite mode
                        mode = Mode.SHOW_HELP;
                        break;

                    case "-b":
                    case "-bitspeak":
                        checkNull(bitspeak, "Bitspeak has already been set to %s");
                        bitspeak = parseBitspeak(getString(args, ++i));
                        break;

                    // Handle text input
                    case "-i":
                    case "--input-text":
                        checkNull(inputSource, "input has already been set to %s");
                        inputSource = InputSource.fromString(getString(args, ++i));
                        break;

                    case "-o":
                    case "--output-file":
                        checkNull(outputSink, "output has already been set to %s");
                        outputSink = OutputSink.fromPath(Paths.get(getString(args, ++i)));
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
                        String lineDelimiter = Escaping.unescapeString(getString(args, ++i));
                        configBuilder.withLineDelimiter(lineDelimiter);
                        break;

                    case "-wd":
                    case "--word-delimiter":
                        String wordDelimiter = Escaping.unescapeString(getString(args, ++i));
                        configBuilder.withWordDelimiter(wordDelimiter);
                        break;

                    default:
                        throw new IllegalArgumentException("Unknown argument: " + argument);
                }
            } else {
                // Assume file
                checkNull(inputSource, "input has already been set to %s");
                inputSource = InputSource.fromPath(Paths.get(argument));
            }
        }
        // Default to showing help if nothing is specified
        if (mode == null && inputSource == null) {
            mode = Mode.SHOW_HELP;
        }
        // Use standard values
        if (inputSource == null) {
            inputSource = InputSource.standardInput();
        }
        if (outputSink == null) {
            outputSink = OutputSink.standardOutput();
        }
        if (bitspeak == null) {
            // BS-6 is default
            bitspeak = Bitspeak.bs6();
        }
        return new ParsedCommand(mode, bitspeak, inputSource, outputSink, configBuilder.build());
    }

    private static void checkNull(Object value, String messageFormat) {
        if (value != null) {
            throw new IllegalStateException(String.format(messageFormat, value));
        }
    }

    private static String getString(String[] args, int index) {
        if (index >= args.length) {
            throw new IllegalStateException("Expected argument at index " + index);
        }
        return args[index];
    }

    private static int getInteger(String[] args, int index) {
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
        return config;
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ParsedCommand that = (ParsedCommand) o;
        return mode == that.mode &&
                Objects.equals(bitspeak, that.bitspeak) &&
                Objects.equals(inputSource, that.inputSource) &&
                Objects.equals(outputSink, that.outputSink) &&
                Objects.equals(config, that.config);
    }

    @Override
    public int hashCode() {
        return Objects.hash(mode, bitspeak, inputSource, outputSink, config);
    }

    @Override
    public String toString() {
        return "CommandParser{" +
                "mode=" + mode +
                ", bitspeak=" + bitspeak +
                ", inputSource=" + inputSource +
                ", outputSink=" + outputSink +
                ", config=" + config +
                '}';
    }
}
