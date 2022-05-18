package ru.rerumu.backups.zfs_api.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.rerumu.backups.zfs_api.ProcessWrapper;
import ru.rerumu.backups.zfs_api.StderrLogger;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

public class ProcessWrapperImpl implements ProcessWrapper {
    protected final Logger logger = LoggerFactory.getLogger(ProcessWrapperImpl.class);
    private final Process process;
    private final BufferedInputStream bufferedInputStream;
    private final BufferedInputStream bufferedErrorStream;
    private final Thread errThread;

    public ProcessWrapperImpl(List<String> args) throws IOException {
        logger.debug(String.format("Running command '%s'", args));
        ProcessBuilder pb = new ProcessBuilder(args);
        process = pb.start();
        bufferedInputStream = new BufferedInputStream(process.getInputStream());
        bufferedErrorStream = new BufferedInputStream(process.getErrorStream());

        // TODO: Log exception
        errThread = new Thread(new StderrLogger(bufferedErrorStream, LoggerFactory.getLogger(StderrLogger.class)));
        errThread.start();
    }

    public BufferedInputStream getBufferedInputStream() {
        return bufferedInputStream;
    }

    public void close() throws InterruptedException, IOException {
        logger.info("Closing process");
        int exitCode = process.waitFor();
        errThread.join();
        if (exitCode != 0) {
            logger.info("Process closed with error");
            throw new IOException();
        }
        logger.info("Process closed");
    }
}
