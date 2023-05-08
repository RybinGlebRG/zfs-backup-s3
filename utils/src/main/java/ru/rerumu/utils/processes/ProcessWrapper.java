package ru.rerumu.utils.processes;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.rerumu.utils.processes.factories.ProcessFactory;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.*;

public class ProcessWrapper implements Callable<Void> {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private final List<String> args;
    private final ProcessFactory processFactory;

    private final StdProcessor stdProcessor;

    public ProcessWrapper(List<String> args, @NonNull ProcessFactory processFactory, @NonNull StdProcessor stdProcessor) {
        Objects.requireNonNull(processFactory);
        Objects.requireNonNull(stdProcessor);
        this.args = args;
        this.processFactory = processFactory;
        this.stdProcessor = stdProcessor;
    }

    @Override
    public Void call() throws Exception {
        Process process = processFactory.create(args);

        try {
            stdProcessor.processStd(
                    new BufferedInputStream(process.getErrorStream()),
                    new BufferedInputStream(process.getInputStream()),
                    new BufferedOutputStream(process.getOutputStream())
            );
        } catch (Exception e){
            logger.error(e.getMessage(),e);
            process.destroy();
        }

        int exitCode = process.waitFor();

        if (exitCode != 0) {
            logger.error("Process closed with an error");
            throw new IOException();
        }
        logger.info("Process closed");
        return null;
    }
}
