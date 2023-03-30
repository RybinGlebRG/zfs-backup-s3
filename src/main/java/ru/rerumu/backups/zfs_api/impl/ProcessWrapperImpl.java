package ru.rerumu.backups.zfs_api.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.rerumu.backups.zfs_api.ProcessWrapper;
import ru.rerumu.backups.zfs_api.StdProcessor;
import ru.rerumu.backups.zfs_api.zfs.impl.StdProcessorCallable;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class ProcessWrapperImpl implements ProcessWrapper {
    protected final Logger logger = LoggerFactory.getLogger(ProcessWrapperImpl.class);
    private  Process process;
    private  BufferedInputStream bufferedInputStream;
    private  BufferedInputStream bufferedErrorStream;
    private  BufferedOutputStream bufferedOutputStream;
    private final List<String> args;
    protected boolean isKilled = false;
    protected final ExecutorService executorService;
    protected final List<Future<Integer>> futureList;

    public ProcessWrapperImpl(List<String> args)  {
        this.args = args;
        executorService = Executors.newCachedThreadPool();
        futureList = new ArrayList<>();
    }

    public void run() throws IOException {
        logger.debug(String.format("Running command '%s'", args));
        ProcessBuilder pb = new ProcessBuilder(args);
        process = pb.start();
        bufferedInputStream = new BufferedInputStream(process.getInputStream());
        bufferedErrorStream = new BufferedInputStream(process.getErrorStream());
        bufferedOutputStream = new BufferedOutputStream(process.getOutputStream());
    }

    public void setStderrProcessor(StdProcessor stderrProcessor){
        futureList.add(executorService.submit(new StdProcessorCallable(bufferedErrorStream,stderrProcessor)));
    }

    public void setStdinProcessor(StdProcessor stdinProcessor){
        futureList.add(executorService.submit(new StdProcessorCallable(bufferedInputStream,stdinProcessor)));
    }


    @Override
    public BufferedInputStream getBufferedInputStream() {
        return bufferedInputStream;
    }

    @Override
    public BufferedOutputStream getBufferedOutputStream() {
        return bufferedOutputStream;
    }

    public void close() throws InterruptedException, IOException, ExecutionException {
        if (isKilled){
            logger.info("Already killed");
            return;
        }
        logger.info("Closing process");
        bufferedOutputStream.close();
        int exitCode = process.waitFor();
        executorService.shutdown();
        int threadRes=0;
        for (Future<Integer> future: futureList){
            Integer tmp = future.get();
            if (tmp!=0){
                threadRes=tmp;
            }
        }

        if (exitCode != 0 || threadRes != 0) {
            logger.error("Process or thread closed with error");
            throw new IOException();
        }
        logger.info("Process closed");
    }

    @Override
    public void kill() throws InterruptedException, IOException, ExecutionException {
        logger.info("Killing process");
        bufferedOutputStream.close();
        process.destroy();
        int threadRes=0;
        executorService.shutdown();
        for (Future<Integer> future: futureList){
            Integer tmp = future.get();
            if (tmp!=0){
                threadRes=tmp;
            }
        }
        if ( threadRes != 0) {
            logger.error("Thread closed with error");
        }
        isKilled = true;
        logger.info("Process killed");
    }
}
