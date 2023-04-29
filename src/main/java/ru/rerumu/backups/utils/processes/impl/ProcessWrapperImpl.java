package ru.rerumu.backups.utils.processes.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.rerumu.backups.utils.processes.ProcessWrapper;
import ru.rerumu.backups.utils.processes.StdProcessor;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.function.Consumer;


public class ProcessWrapperImpl implements ProcessWrapper {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final List<String> args;
    private final ExecutorService executorService = Executors.newCachedThreadPool();
    private final List<Future<Void>> futureList = new ArrayList<>();
    private BufferedInputStream bufferedInputStream;
    private BufferedOutputStream bufferedOutputStream;
    private Process process;
    private boolean isKilled = false;
    private boolean isClosed = false;
    private final Consumer<BufferedInputStream> stderrProcessor;
    private final Consumer<BufferedInputStream> stdoutProcessor;


    public ProcessWrapperImpl(List<String> args, Consumer<BufferedInputStream> stderrProcessor, Consumer<BufferedInputStream> stdoutProcessor) {
        this.args = args;
        this.stderrProcessor = stderrProcessor;
        this.stdoutProcessor = stdoutProcessor;
    }

    @Override
    public Void call() throws Exception {
        ProcessBuilder pb = new ProcessBuilder(args);
        process = pb.start();
        bufferedInputStream = new BufferedInputStream(process.getInputStream());
        bufferedOutputStream = new BufferedOutputStream(process.getOutputStream());

        if (stderrProcessor != null) {
            futureList.add(executorService.submit(
                    ()->{
                        stderrProcessor.accept(
                                new BufferedInputStream(process.getErrorStream())
                        );
                        return null;
                    }
            ));
        }
        if (stdoutProcessor != null) {
            futureList.add(
                    executorService.submit(
                            () -> {
                                stdoutProcessor.accept(bufferedInputStream);
                                return null;
                            }
                    )
            );
        }

        int exitCode = process.waitFor();
        executorService.shutdown();
        for (Future<Void> future : futureList) {
            future.get();
        }
        if (exitCode != 0) {
            logger.error("Process or thread closed with error");
            throw new IOException();
        }
        logger.info("Process closed");
        return null;
    }

    public synchronized void close() {
        try {
            if (!isClosed && !isKilled) {
                logger.info("Closing stdin");
                bufferedOutputStream.close();
                logger.info("stdin closed");
                isClosed = true;
            }
        } catch (Exception e){
            throw new RuntimeException(e);
        }
    }

    public synchronized void kill() {
        try {
            if (!isKilled) {
                logger.info("Killing process");
                bufferedOutputStream.close();
                process.destroy();
                logger.info("Process killed");
                isKilled = true;
            }
        } catch (Exception e){
            throw new RuntimeException(e);
        }
    }
}
