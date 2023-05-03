package ru.rerumu.backups.utils.processes.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.rerumu.backups.utils.processes.StdProcessor;
import ru.rerumu.backups.utils.processes.TriConsumer;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.function.Consumer;

public class StdProcessorImpl implements StdProcessor {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private final ExecutorService executorService = Executors.newCachedThreadPool();
    private final Consumer<BufferedInputStream> stderrConsumer;
    private final Consumer<BufferedInputStream> stdoutConsumer;
    private final Consumer<BufferedOutputStream> stdinConsumer;

    private final List<Future<Void>> futureList = new ArrayList<>();

    public StdProcessorImpl(Consumer<BufferedInputStream> stderrConsumer, Consumer<BufferedInputStream> stdoutConsumer, Consumer<BufferedOutputStream> stdinConsumer) {
        this.stderrConsumer = stderrConsumer;
        this.stdoutConsumer = stdoutConsumer;
        this.stdinConsumer = stdinConsumer;
    }

    @Override
    public void processStd(BufferedInputStream stderr, BufferedInputStream stdout, BufferedOutputStream stdin) throws ExecutionException, InterruptedException, IOException {
        Objects.requireNonNull(stderr,"Stderr should not be null");
        Objects.requireNonNull(stdout,"Stdout should not be null");
        Future<Void> stdinFuture = null;

        try {
            if (stdin != null && stdinConsumer != null) {
                stdinFuture = executorService.submit(
                        () -> {
                            stdinConsumer.accept(stdin);
                            return null;
                        });
            }

            if (stderrConsumer != null) {
                futureList.add(executorService.submit(
                        () -> {
                            stderrConsumer.accept(stderr);
                            return null;
                        }
                ));
            }
            if (stdoutConsumer != null) {
                futureList.add(
                        executorService.submit(
                                () -> {
                                    stdoutConsumer.accept(stdout);
                                    return null;
                                }
                        )
                );
            }
        } finally {
            executorService.shutdown();
        }

        if (stdinFuture != null){
            try (stdin) {
                stdinFuture.get();
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
                throw e;
            }
        }

        for (Future<Void> future : futureList) {
            future.get();
        }
    }
}
