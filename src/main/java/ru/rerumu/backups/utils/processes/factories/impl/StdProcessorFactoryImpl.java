package ru.rerumu.backups.utils.processes.factories.impl;

import ru.rerumu.backups.utils.processes.StdProcessor;
import ru.rerumu.backups.utils.processes.factories.StdProcessorFactory;
import ru.rerumu.backups.utils.processes.impl.StdProcessorImpl;

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
