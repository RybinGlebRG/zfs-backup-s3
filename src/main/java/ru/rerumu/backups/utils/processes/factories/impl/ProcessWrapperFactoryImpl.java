package ru.rerumu.backups.utils.processes.factories.impl;

import ru.rerumu.backups.utils.processes.factories.ProcessFactory;
import ru.rerumu.backups.utils.processes.factories.ProcessWrapperFactory;
import ru.rerumu.backups.utils.processes.ProcessWrapper;
import ru.rerumu.backups.utils.processes.TriConsumer;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.util.List;
import java.util.concurrent.ExecutorService;


public class ProcessWrapperFactoryImpl implements ProcessWrapperFactory {
    private final ExecutorService executorService;
    private final ProcessFactory processFactory;

    public ProcessWrapperFactoryImpl(ExecutorService executorService, ProcessFactory processFactory) {
        this.executorService = executorService;
        this.processFactory = processFactory;
    }

    @Override
    public ProcessWrapper getProcessWrapper(List<String> args, TriConsumer<BufferedInputStream, Runnable, Runnable> stderrProcessor, TriConsumer<BufferedInputStream, Runnable, Runnable> stdoutProcessor) {
        return new ProcessWrapper(args,stderrProcessor,stdoutProcessor,null,processFactory);
    }

    @Override
    public ProcessWrapper getProcessWrapper(
            List<String> args,
            TriConsumer<BufferedInputStream, Runnable, Runnable> stderrProcessor,
            TriConsumer<BufferedInputStream, Runnable, Runnable> stdoutProcessor,
            TriConsumer<BufferedOutputStream, Runnable, Runnable> stdinProcessor
    ) {
        return new ProcessWrapper(args,stderrProcessor,stdoutProcessor,stdinProcessor,processFactory);
    }
}
