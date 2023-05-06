package ru.rerumu.utils.processes.factories.impl;

import ru.rerumu.utils.processes.StdProcessor;
import ru.rerumu.utils.processes.factories.StdProcessorFactory;
import ru.rerumu.utils.processes.impl.StdProcessorImpl;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.util.function.Consumer;

public class StdProcessorFactoryImpl implements StdProcessorFactory {
    @Override
    public StdProcessor getStdProcessor(Consumer<BufferedInputStream> stderrConsumer, Consumer<BufferedInputStream> stdoutConsumer, Consumer<BufferedOutputStream> stdinConsumer) {
        return new StdProcessorImpl(stderrConsumer,stdoutConsumer,stdinConsumer);
    }

    @Override
    public StdProcessor getStdProcessor(Consumer<BufferedInputStream> stderrConsumer, Consumer<BufferedInputStream> stdoutConsumer) {
        return new StdProcessorImpl(stderrConsumer,stdoutConsumer,null);
    }
}
