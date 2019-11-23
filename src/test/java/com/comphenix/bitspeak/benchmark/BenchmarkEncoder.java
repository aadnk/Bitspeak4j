package com.comphenix.bitspeak.benchmark;

import com.comphenix.bitspeak.Bitspeak;
import com.comphenix.bitspeak.BitspeakConfig;
import com.google.common.io.CharStreams;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.Reader;
import java.util.Random;
import java.util.concurrent.TimeUnit;

@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@State(Scope.Benchmark)
@Fork(value = 2, jvmArgs = {"-Xms2G", "-Xmx2G"})
public class BenchmarkEncoder {
    @Param({"32000"})
    private int N;

    private byte[] DATA_FOR_TESTING;

    public static void main(String[] args) throws RunnerException {
        Options opt = new OptionsBuilder()
                .include(BenchmarkEncoder.class.getSimpleName())
                .forks(1)
                .build();

        new Runner(opt).run();
    }

    @Setup
    public void setup() {
        DATA_FOR_TESTING = createData();
    }

    @Benchmark
    public void testBs6(Blackhole bh) throws IOException {
        consume(bh, Bitspeak.bs6().newEncodeStream(new ByteArrayInputStream(DATA_FOR_TESTING)));
    }

    @Benchmark
    public void testBs6NoWrapping(Blackhole bh) throws IOException {
        consume(bh, Bitspeak.bs6().withConfig(BitspeakConfig.unlimitedWordSize()).newEncodeStream(new ByteArrayInputStream(DATA_FOR_TESTING)));
    }

    private void consume(Blackhole bh, Reader reader) throws IOException {
        char[] data = new char[4096];
        int read = 0;

        while ((read = reader.read(data)) != -1) {
            for (int i = 0; i < read; i++) {
                bh.consume(data[i]);
            }
        }
    }

    private byte[] createData() {
        byte[] data = new byte[N];
        Random rnd = new Random();

        rnd.nextBytes(data);
        return data;
    }
}
