package ru.rerumu.backups.services.impl;

import org.apache.commons.lang3.ArrayUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.rerumu.backups.exceptions.FileHitSizeLimitException;
import ru.rerumu.backups.exceptions.ZFSStreamEndedException;
import ru.rerumu.backups.models.ZFSStreamChunk;
import ru.rerumu.backups.services.ZFSFileWriter;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;

public class ZFSFileWriterTrivial implements ZFSFileWriter {
    private final Logger logger = LoggerFactory.getLogger(ZFSFileWriterTrivial.class);
    private final long filePartSize;
    private final Path path;

    public ZFSFileWriterTrivial(long filePartSize, Path path) throws IOException {
        this.filePartSize = filePartSize;
        this.path = path;
        if (Files.exists(path)){
            throw new IOException("File already exists");
        }
    }

    @Override
    public void write(BufferedInputStream bufferedInputStream)
            throws
            IOException,
            FileHitSizeLimitException,
            ZFSStreamEndedException {

        try(OutputStream outputStream = Files.newOutputStream(path);
            BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(outputStream)){
            long written = 0;
            int len;
            byte[] buf = new byte[8192];
            while((len=bufferedInputStream.read(buf))!=-1){
                bufferedOutputStream.write(buf,0,len);
                written+=len;
                if (written >= filePartSize) {
                    logger.debug(String.format("Written (%d bytes) is bigger than filePartSize (%d bytes)", written, filePartSize));
                    throw new FileHitSizeLimitException();
                }
            }
            throw new ZFSStreamEndedException();
        }
    }
}
