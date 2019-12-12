package com.comphenix.bitspeak.console;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.Writer;
import java.nio.CharBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;

abstract class OutputSink {
    public static OutputSink fromPath(Path path) {
        return new PathOutputSink(path);
    }

    public static OutputSink standardOutput() {
        return StandardOutputSink.INSTANCE;
    }

    public static boolean isStandardOutput(OutputSink outputSink) {
        return outputSink instanceof StandardOutputSink;
    }

    public abstract OutputStream toOutputStream() throws IOException;

    public abstract Writer toWriter() throws IOException;

    static class StandardOutputSink extends OutputSink {
        static final StandardOutputSink INSTANCE = new StandardOutputSink();

        public OutputStream toOutputStream() throws IOException {
            return System.out;
        }

        public Writer toWriter() throws IOException {
            return new Writer() {
                private final PrintStream out = System.out;

                @Override
                public void write(char[] cbuf, int off, int len) throws IOException {
                    out.append(CharBuffer.wrap(cbuf, off, len));
                }

                @Override
                public void flush() throws IOException {
                    out.flush();
                }

                @Override
                public void close() throws IOException {
                    out.close();
                }
            };
        }

        @Override
        public String toString() {
            return "STDOUT";
        }
    }

    static class PathOutputSink extends OutputSink {
        private final Path path;

        public PathOutputSink(Path path) {
            this.path = Objects.requireNonNull(path, "path cannot be NULL");
        }

        public OutputStream toOutputStream() throws IOException {
            return Files.newOutputStream(path);
        }

        public Writer toWriter() throws IOException {
            return Files.newBufferedWriter(path);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            PathOutputSink that = (PathOutputSink) o;
            return Objects.equals(path, that.path);
        }

        @Override
        public int hashCode() {
            return Objects.hash(path);
        }

        @Override
        public String toString() {
            return path.toString();
        }
    }
}
