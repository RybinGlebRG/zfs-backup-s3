package ru.rerumu.backups.zfs_api;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.Callable;

public class StdProcessorCallable implements Callable<Integer> {
    private final Logger logger = LoggerFactory.getLogger(StdProcessorCallable.class);
    private final StdProcessor stdProcessor;
    private final BufferedInputStream bufferedInputStream;

    public StdProcessorCallable(BufferedInputStream bufferedInputStream, StdProcessor stdProcessor){
        this.stdProcessor = stdProcessor;
        this.bufferedInputStream = bufferedInputStream;
    }

    @Override
    public Integer call() {
        logger.info("Started reading std");
        try(InputStreamReader inputStreamReader = new InputStreamReader(bufferedInputStream, StandardCharsets.UTF_8);
            BufferedReader bufferedReader = new BufferedReader(inputStreamReader)){
            String s = null;
            while ((s=bufferedReader.readLine())!=null){
                stdProcessor.process(s);
            }
        } catch (Exception e) {
            logger.error(e.getMessage(),e);
            return 1;
        }
        logger.info("Finished reading std");
        return 0;
    }
}
