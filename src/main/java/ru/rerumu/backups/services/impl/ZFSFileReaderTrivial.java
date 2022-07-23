package ru.rerumu.backups.services.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.rerumu.backups.models.ZFSStreamChunk;
import ru.rerumu.backups.services.ZFSFileReader;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;

public class ZFSFileReaderTrivial implements ZFSFileReader {
    private final Logger logger = LoggerFactory.getLogger(ZFSFileReaderTrivial.class);
    private final BufferedOutputStream bufferedOutputStream;
    private final Path path;

    public ZFSFileReaderTrivial(BufferedOutputStream bufferedOutputStream, Path path){
        this.bufferedOutputStream = bufferedOutputStream;
        this.path = path;
    }

    @Override
    public void read() throws IOException, ClassNotFoundException, EOFException {
        logger.info(String.format("Starting reading from file '%s'", path.toString()));

        try (InputStream inputStream = Files.newInputStream(path);
             ObjectInputStream objectInputStream = new ObjectInputStream(inputStream)) {
            logger.info(String.format("Reading file '%s'", path.toString()));
            while (true) {
                logger.trace("Reading object from stream");
                Object object = objectInputStream.readUnshared();
                if (object instanceof ZFSStreamChunk) {
                    logger.trace("Trying to cast to ZFSStreamChunk");
                    ZFSStreamChunk zfsStreamChunk = (ZFSStreamChunk) object;

                    logger.trace("Writing chunk to stream");
                    bufferedOutputStream.write(zfsStreamChunk.getChunk());
                    logger.trace("End writing chunk to stream");
                } else {
                    logger.error("Object is not instance of ZFSStreamChunk");
                    throw new IOException();
                }

            }

        }
    }
}
