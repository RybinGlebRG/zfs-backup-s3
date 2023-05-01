package ru.rerumu.backups.utils.processes;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.rerumu.backups.utils.processes.TriConsumer;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

// TODO: Test concurrency?
public class ProcessWrapper implements Callable<Void> {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private final List<String> args;
    private final ExecutorService executorService;
    private final List<Future<Void>> futureList = new ArrayList<>();
    private BufferedInputStream bufferedInputStream;
    private BufferedOutputStream bufferedOutputStream;
    // TODO: volatile?
    private volatile Process process;
    private volatile boolean isKilled = false;
    private volatile boolean isClosed = false;
    //    private final Consumer<BufferedInputStream> stderrProcessor;
    private final TriConsumer<BufferedInputStream, Runnable, Runnable> stderrProcessor;
    private final TriConsumer<BufferedInputStream, Runnable, Runnable> stdoutProcessor;
    private final TriConsumer<BufferedOutputStream, Runnable, Runnable> stdinProcessor;

    public ProcessWrapper(
            List<String> args,
            ExecutorService executorService,
            TriConsumer<BufferedInputStream, Runnable, Runnable> stderrProcessor,
            TriConsumer<BufferedInputStream, Runnable, Runnable> stdoutProcessor,
            TriConsumer<BufferedOutputStream, Runnable, Runnable> stdinProcessor
    ) {
        this.args = args;
        this.executorService = executorService;
        this.stderrProcessor = stderrProcessor;
        this.stdoutProcessor = stdoutProcessor;
        this.stdinProcessor = stdinProcessor;
    }

    @Override
    public Void call() throws Exception {
        ProcessBuilder pb = new ProcessBuilder(args);
        process = pb.start();
        bufferedInputStream = new BufferedInputStream(process.getInputStream());
        bufferedOutputStream = new BufferedOutputStream(process.getOutputStream());

        if (stdinProcessor != null) {
            futureList.add(
                    executorService.submit(
                            () -> {
                                stdinProcessor.accept(
                                        bufferedOutputStream,
                                        this::close,
                                        this::kill
                                );
                                return null;
                            }
                    )
            );
        }

        if (stderrProcessor != null) {
            futureList.add(executorService.submit(
                    () -> {
                        stderrProcessor.accept(
                                new BufferedInputStream(process.getErrorStream()),
                                this::close,
                                this::kill
                        );
                        return null;
                    }
            ));
        }
        if (stdoutProcessor != null) {
            futureList.add(
                    executorService.submit(
                            () -> {
                                stdoutProcessor.accept(
                                        bufferedInputStream,
                                        this::close,
                                        this::kill
                                );
                                return null;
                            }
                    )
            );
        }

        int exitCode = process.waitFor();
        for (Future<Void> future : futureList) {
            future.get();
        }
        if (exitCode != 0) {
            logger.error("Process closed with an error");
            throw new IOException();
        }
        logger.info("Process closed");
        return null;
    }

    private synchronized void close() {
        try {
            if (!isClosed && !isKilled) {
                logger.info("Closing stdin");
                bufferedOutputStream.close();
                logger.info("stdin closed");
                isClosed = true;
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private synchronized void kill() {
        try {
            if (!isKilled) {
                logger.info("Killing process");
                bufferedOutputStream.close();
                process.destroy();
                logger.info("Process killed");
                isKilled = true;
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
