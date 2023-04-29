package ru.rerumu.backups.utils.processes;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.Callable;
import java.util.function.Consumer;

public class StdProcessor implements Callable<Void> {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final BufferedInputStream bufferedInputStream;
    private final Consumer<String> consumer;

    public StdProcessor(
            BufferedInputStream bufferedInputStream,
            Consumer<String> consumer
    ) {
        this.bufferedInputStream = bufferedInputStream;
        this.consumer = consumer;
    }

    @Override
    public Void call() throws Exception {
        logger.info("Started reading std");
        try(InputStreamReader inputStreamReader = new InputStreamReader(bufferedInputStream, StandardCharsets.UTF_8);
            BufferedReader bufferedReader = new BufferedReader(inputStreamReader)){
            String s = null;
            while ((s=bufferedReader.readLine())!=null){
                consumer.accept(s);
            }
        } catch (Exception e) {
            logger.error(e.getMessage(),e);
            throw e;
        }
        logger.info("Finished reading std");
        return null;
    }
}
