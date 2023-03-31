package ru.rerumu.backups.services.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
    public void read() throws IOException {
        logger.info(String.format("Starting reading from file '%s'", path.toString()));

        try(InputStream inputStream = Files.newInputStream(path);
            BufferedInputStream bufferedInputStream = new BufferedInputStream(inputStream)) {
            int len;
            byte[] buf = new byte[8192];
            while((len=bufferedInputStream.read(buf))!=-1){
                bufferedOutputStream.write(buf,0,len);
            }
        }

    }
}
