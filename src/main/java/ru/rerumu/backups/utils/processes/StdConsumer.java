package ru.rerumu.backups.utils.processes;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.function.Consumer;

public class StdConsumer implements TriConsumer<BufferedInputStream,Runnable,Runnable> {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private final Consumer<String> consumer;

    public StdConsumer(Consumer<String> consumer ) {
        this.consumer = consumer;
    }

    @Override
    public void accept(BufferedInputStream bufferedInputStream, Runnable close, Runnable kill) {
        logger.info("Started reading std");
        try (InputStreamReader inputStreamReader = new InputStreamReader(bufferedInputStream, StandardCharsets.UTF_8);
             BufferedReader bufferedReader = new BufferedReader(inputStreamReader)) {
            String s = null;
            while ((s = bufferedReader.readLine()) != null) {
                consumer.accept(s);
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            kill.run();
            throw new RuntimeException(e);
        }
        logger.info("Finished reading std");
    }
}
