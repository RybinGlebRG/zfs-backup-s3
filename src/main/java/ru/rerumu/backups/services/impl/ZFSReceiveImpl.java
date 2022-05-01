package ru.rerumu.backups.services.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.rerumu.backups.services.InputStreamLogger;
import ru.rerumu.backups.services.ZFSReceive;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.util.Arrays;

public class ZFSReceiveImpl implements ZFSReceive {

//    private final ru.rerumu.backups.services.ZFSProcessWrapper ZFSProcessWrapper;
    private final Process process;
    private final Logger logger = LoggerFactory.getLogger(ZFSReceiveImpl.class);
    private final BufferedInputStream bufferedInputStream;
    private final BufferedInputStream bufferedErrorStream;
    private final BufferedOutputStream bufferedOutputStream;
    private final Thread errThread;
    private final Thread outThread;

    public ZFSReceiveImpl(String pool) throws IOException {
//        ZFSProcessWrapper = new ZFSProcessWrapper(Arrays.asList("zfs","receive","-duvF",pool), true);
        ProcessBuilder pb = new ProcessBuilder(Arrays.asList("zfs","receive","-duvF",pool));
        process = pb.start();
        bufferedInputStream = new BufferedInputStream(process.getInputStream());
        bufferedErrorStream = new BufferedInputStream(process.getErrorStream());
        bufferedOutputStream = new BufferedOutputStream(process.getOutputStream());
        // TODO: Log exception
        errThread = new Thread(new InputStreamLogger(bufferedErrorStream, LoggerFactory.getLogger(InputStreamLogger.class)));
        errThread.start();
        outThread = new Thread(new InputStreamLogger(bufferedInputStream, LoggerFactory.getLogger(InputStreamLogger.class)));
        outThread.start();
    }

    public BufferedOutputStream getBufferedOutputStream() {
        return bufferedOutputStream;
    }


    public void close() throws InterruptedException, IOException {
        logger.info("Closing process");
        bufferedOutputStream.close();
        int exitCode = process.waitFor();
        errThread.join();
        outThread.join();
//        bufferedInputStream.close();
//        bufferedErrorStream.close();

        if (exitCode != 0) {
            logger.info("Process closed with error");
            throw new IOException();
        }
        logger.info("Process closed");
    }

}
