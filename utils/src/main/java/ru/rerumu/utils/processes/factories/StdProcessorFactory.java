package ru.rerumu.utils.processes.factories;

import ru.rerumu.utils.processes.StdProcessor;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.util.function.Consumer;

public interface StdProcessorFactory {

    StdProcessor getStdProcessor(Consumer<BufferedInputStream> stderrConsumer, Consumer<BufferedInputStream> stdoutConsumer, Consumer<BufferedOutputStream> stdinConsumer);

    // TODO: Thread safe?
    StdProcessor getStdProcessor(Consumer<BufferedInputStream> stderrConsumer, Consumer<BufferedInputStream> stdoutConsumer);
}
