package ru.rerumu.backups.utils.processes.impl;

import ru.rerumu.backups.utils.processes.ProcessFactory;
import ru.rerumu.backups.utils.processes.ProcessWrapper;
import ru.rerumu.backups.utils.processes.TriConsumer;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.util.List;
import java.util.concurrent.ExecutorService;


public class ProcessFactoryImpl implements ProcessFactory {
    private final ExecutorService executorService;

    public ProcessFactoryImpl(ExecutorService executorService) {
        this.executorService = executorService;
    }


    @Override
    public ProcessWrapper getProcessWrapper(List<String> args, TriConsumer<BufferedInputStream, Runnable, Runnable> stderrProcessor, TriConsumer<BufferedInputStream, Runnable, Runnable> stdoutProcessor) {
        return new ProcessWrapper(args,executorService,stderrProcessor,stdoutProcessor,null);
    }

    @Override
    public ProcessWrapper getProcessWrapper(
            List<String> args,
            TriConsumer<BufferedInputStream, Runnable, Runnable> stderrProcessor,
            TriConsumer<BufferedInputStream, Runnable, Runnable> stdoutProcessor,
            TriConsumer<BufferedOutputStream, Runnable, Runnable> stdinProcessor
    ) {
        return new ProcessWrapper(args,executorService,stderrProcessor,stdoutProcessor,stdinProcessor);
    }
}
