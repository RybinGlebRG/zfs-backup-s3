package ru.rerumu.backups.utils.processes.impl;

import ru.rerumu.backups.utils.processes.ProcessFactory;

import java.io.BufferedInputStream;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.function.Consumer;

public class ProcessFactoryImpl implements ProcessFactory {
    @Override
    public ProcessWrapperImpl getProcessWrapper(
            List<String> args,
            Consumer<BufferedInputStream> stderrProcessor,
            Consumer<BufferedInputStream> stdoutProcessor
    ) {
        return new ProcessWrapperImpl(args, stderrProcessor, stdoutProcessor);
    }
}
