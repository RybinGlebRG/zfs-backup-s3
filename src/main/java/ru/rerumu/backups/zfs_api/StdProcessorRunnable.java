package ru.rerumu.backups.zfs_api;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

public class StdProcessorRunnable implements Runnable{
    private final Logger logger = LoggerFactory.getLogger(StdProcessorRunnable.class);
    private final StdProcessor stdProcessor;
    private final BufferedInputStream bufferedInputStream;

    public StdProcessorRunnable(BufferedInputStream bufferedInputStream, StdProcessor stdProcessor){
        this.stdProcessor = stdProcessor;
        this.bufferedInputStream = bufferedInputStream;
    }

    @Override
    public void run() {
        logger.info("Started reading std");
        try(InputStreamReader inputStreamReader = new InputStreamReader(bufferedInputStream, StandardCharsets.UTF_8);
            BufferedReader bufferedReader = new BufferedReader(inputStreamReader)){
            String s = null;
            while ((s=bufferedReader.readLine())!=null){
                stdProcessor.process(s);
            }
        } catch (IOException e) {
            logger.error(e.getMessage(),e);
        }
        logger.info("Finished reading std");
    }
}
