package com.comphenix.bitspeak.console;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;

abstract class InputSource {
    public static InputSource standardInput() {
        return StandardInputSource.INSTANCE;
    }

    public static InputSource fromString(String text) {
        return new StringInputSource(text);
    }

    public static InputSource fromPath(Path path) {
        return new PathInputSource(path);
    }

    public abstract Reader toReader() throws IOException;

    public abstract InputStream toInputStream() throws IOException;

    static class StandardInputSource extends InputSource {
        static final StandardInputSource INSTANCE = new StandardInputSource();

        @Override
        public Reader toReader() {
            return new InputStreamReader(System.in, StandardCharsets.UTF_8);
        }

        public InputStream toInputStream() {
            return System.in;
        }

        @Override
        public String toString() {
            return "StandardInputSource{}";
        }
    }

    static class PathInputSource extends InputSource {
        private final Path path;

        public PathInputSource(Path path) {
            this.path = Objects.requireNonNull(path, "path cannot be NULL");
        }

        @Override
        public Reader toReader() throws IOException {
            return Files.newBufferedReader(path);
        }

        public InputStream toInputStream() throws IOException {
            return Files.newInputStream(path);
        }

        @Override
        public String toString() {
            return "PathInputSource{" +
                    "path=" + path +
                    '}';
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            PathInputSource that = (PathInputSource) o;
            return Objects.equals(path, that.path);
        }

        @Override
        public int hashCode() {
            return Objects.hash(path);
        }
    }

    static class StringInputSource extends InputSource {
        private final String text;

        public StringInputSource(String text) {
            this.text = Objects.requireNonNull(text, "text cannot be NULL");
        }

        @Override
        public Reader toReader() throws IOException {
            return new StringReader(text);
        }

        @Override
        public InputStream toInputStream() throws IOException {
            return new ByteArrayInputStream(text.getBytes(StandardCharsets.UTF_8));
        }

        @Override
        public String toString() {
            return "StringInputSource{" +
                    "text='" + text + '\'' +
                    '}';
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            StringInputSource that = (StringInputSource) o;
            return Objects.equals(text, that.text);
        }

        @Override
        public int hashCode() {
            return Objects.hash(text);
        }
    }
}
