package ru.rerumu.backups.zfs_api.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.rerumu.backups.zfs_api.ProcessWrapper;
import ru.rerumu.backups.zfs_api.StderrLogger;
import ru.rerumu.backups.zfs_api.ZFSSend;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class ProcessWrapperImpl implements ProcessWrapper {
    protected final Logger logger = LoggerFactory.getLogger(ProcessWrapperImpl.class);
    protected final Process process;
    protected final BufferedInputStream bufferedInputStream;
    protected final BufferedInputStream bufferedErrorStream;
    protected final BufferedOutputStream bufferedOutputStream;
    protected Thread errThread;
    protected Thread outThread;
    protected boolean isKilled = false;
    protected ExecutorService executorService;
    protected final List<Future<?>> futureList;

    public ProcessWrapperImpl(List<String> args) throws IOException {
        logger.debug(String.format("Running command '%s'", args));
        ProcessBuilder pb = new ProcessBuilder(args);
        process = pb.start();
        bufferedInputStream = new BufferedInputStream(process.getInputStream());
        bufferedErrorStream = new BufferedInputStream(process.getErrorStream());
        bufferedOutputStream = new BufferedOutputStream(process.getOutputStream());
        executorService = Executors.newCachedThreadPool();
        futureList = new ArrayList<>();
    }


    @Override
    public BufferedInputStream getBufferedInputStream() {
        return bufferedInputStream;
    }

    public void close() throws InterruptedException, IOException, ExecutionException {
        if (isKilled){
            logger.info("Already killed");
            return;
        }
        logger.info("Closing process");
        bufferedOutputStream.close();
        int exitCode = process.waitFor();
        // TODO: Log exception
        executorService.shutdown();
        for (Future<?> future: futureList){
            future.get();
        }
//        if (errThread!=null){
//            errThread.join();
//        }
//        if (outThread!=null){
//            outThread.join();
//        }

        if (exitCode != 0) {
            logger.info("Process closed with error");
            throw new IOException();
        }
        logger.info("Process closed");
    }

    @Override
    public void kill() throws InterruptedException, IOException, ExecutionException {
        logger.info("Killing process");
        bufferedOutputStream.close();
        process.destroy();
        // TODO: Log exception
        executorService.shutdown();
        for (Future<?> future: futureList){
            future.get();
        }
//        if (errThread!=null){
//            errThread.join();
//        }
//        if (outThread!=null){
//            outThread.join();
//        }
        isKilled = true;
        logger.info("Process killed");
    }
}
