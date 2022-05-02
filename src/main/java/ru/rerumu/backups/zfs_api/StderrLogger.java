package ru.rerumu.backups.zfs_api;

import org.slf4j.Logger;

import java.io.BufferedInputStream;

public class StderrLogger extends InputStreamLogger{
    public StderrLogger(BufferedInputStream inputStream, Logger logger) {
        super(inputStream, logger);
    }

    @Override
    protected void logRecord(String str){
        logger.error(str);
    }
}
