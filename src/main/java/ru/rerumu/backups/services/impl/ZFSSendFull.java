package ru.rerumu.backups.services.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.rerumu.backups.models.Snapshot;
import ru.rerumu.backups.services.InputStreamLogger;
import ru.rerumu.backups.services.ZFSSend;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.util.Arrays;

public class ZFSSendFull implements ZFSSend {

    private final Logger logger = LoggerFactory.getLogger(ZFSSendFull.class);
    private final Process process;
    private final BufferedInputStream bufferedInputStream;
    private final BufferedInputStream bufferedErrorStream;
    private final Thread errThread;

    public ZFSSendFull(Snapshot fullSnapshot) throws IOException {
        logger.info(String.format("Sending snapshot '%s'",fullSnapshot.getFullName()));
        ProcessBuilder pb = new ProcessBuilder(Arrays.asList("zfs","send","-vR",fullSnapshot.getFullName()));
        process = pb.start();
        bufferedInputStream = new BufferedInputStream(process.getInputStream());
        bufferedErrorStream = new BufferedInputStream(process.getErrorStream());
        // TODO: Log exception
        errThread = new Thread(new InputStreamLogger(bufferedErrorStream, LoggerFactory.getLogger(InputStreamLogger.class)));
        errThread.start();
    }

    public BufferedInputStream getBufferedInputStream() {
        return bufferedInputStream;
    }

    public void close() throws InterruptedException, IOException {
        logger.info("Closing process");
        int exitCode = process.waitFor();
        errThread.join();
//        bufferedInputStream.close();
//        bufferedErrorStream.close();
        if (exitCode != 0) {
            logger.info("Process closed with error");
            throw new IOException();
        }
        logger.info("Process closed");
    }

    @Override
    public void kill() throws InterruptedException {
        logger.info("Killing process");
        process.destroy();
        errThread.join();
        logger.info("Process killed");
    }
}
