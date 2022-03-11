package ru.rerumu.backups.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class InputStreamLogger implements Runnable{

    private final BufferedInputStream inputStream;
    private final Logger logger;

    public InputStreamLogger(BufferedInputStream inputStream, Logger logger){
        this.logger = logger;
        this.inputStream = inputStream;
    }


    @Override
    public void run() {
        logger.info("Started reading stderr");
        byte[] buf = new byte[1024];
        int len = 0;
        try {
            while ((len = inputStream.read(buf)) >= 0) {
                logger.debug(new String(buf,0,len, StandardCharsets.UTF_8));
            }
        }
        catch (IOException e){
            logger.error(e.getMessage());
        }
        logger.info("Finished reading stderr");
    }
}
