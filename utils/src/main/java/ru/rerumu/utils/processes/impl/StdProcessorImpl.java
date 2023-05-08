package ru.rerumu.utils.processes.impl;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.rerumu.utils.processes.StdProcessor;

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

public final class StdProcessorImpl implements StdProcessor {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private final ExecutorService executorService = Executors.newCachedThreadPool();
    private final Consumer<BufferedInputStream> stderrConsumer;
    private final Consumer<BufferedInputStream> stdoutConsumer;
    private final Consumer<BufferedOutputStream> stdinConsumer;

    private final List<Future<Void>> futureList = new ArrayList<>();

    public StdProcessorImpl(
            @NonNull Consumer<BufferedInputStream> stderrConsumer,
            @NonNull Consumer<BufferedInputStream> stdoutConsumer,
            @Nullable Consumer<BufferedOutputStream> stdinConsumer
    ) {
        Objects.requireNonNull(stderrConsumer, "StdProcessorImpl: stderrConsumer should not be null");
        Objects.requireNonNull(stdoutConsumer, "StdProcessorImpl: stdoutConsumer should not be null");
        this.stderrConsumer = stderrConsumer;
        this.stdoutConsumer = stdoutConsumer;
        this.stdinConsumer = stdinConsumer;
    }

    @Override
    public void processStd(
            @NonNull BufferedInputStream stderr,
            @NonNull BufferedInputStream stdout,
            @Nullable BufferedOutputStream stdin
    )
            throws ExecutionException, InterruptedException, IOException {
        Objects.requireNonNull(stderr, "StdProcessorImpl: Stderr should not be null");
        Objects.requireNonNull(stdout, "StdProcessorImpl: Stdout should not be null");

        Future<Void> stdinFuture = null;

        try {
            if (stdin == null || stdinConsumer == null){
                if (stdin != null){
                    throw new IllegalArgumentException("StdProcessorImpl: If stdin is not null, stdinConsumer should be present");
                } else if (stdinConsumer != null){
                    throw new IllegalArgumentException("StdProcessorImpl: If stdinConsumer is present, stdin should also be present");
                }
            } else {
                stdinFuture = executorService.submit(
                        () -> {
                            stdinConsumer.accept(stdin);
                            return null;
                        });
            }

            futureList.add(
                    executorService.submit(
                            () -> {
                                stderrConsumer.accept(stderr);
                                return null;
                            }
                    ));
            futureList.add(
                    executorService.submit(
                            () -> {
                                stdoutConsumer.accept(stdout);
                                return null;
                            }
                    )
            );
        } finally {
            executorService.shutdown();
        }

        if (stdinFuture != null) {
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
