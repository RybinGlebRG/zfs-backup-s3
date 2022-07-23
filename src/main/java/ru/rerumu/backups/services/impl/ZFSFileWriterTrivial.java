package ru.rerumu.backups.services.impl;

import org.apache.commons.lang3.ArrayUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.rerumu.backups.exceptions.FileHitSizeLimitException;
import ru.rerumu.backups.exceptions.ZFSStreamEndedException;
import ru.rerumu.backups.models.ZFSStreamChunk;
import ru.rerumu.backups.services.ZFSFileWriter;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;

public class ZFSFileWriterTrivial implements ZFSFileWriter {
    private final Logger logger = LoggerFactory.getLogger(ZFSFileWriterTrivial.class);

    private final int chunkSize;
    private final long filePartSize;
    private final Path path;

    public ZFSFileWriterTrivial( int chunkSize, long filePartSize, Path path) throws IOException {
        this.chunkSize = chunkSize;
        this.filePartSize = filePartSize;
        this.path = path;
        if (Files.exists(path)){
            throw new IOException("File already exists");
        }
    }

    private byte[] fillBuffer(BufferedInputStream bufferedInputStream) throws IOException {
        byte[] buf = new byte[0];
        int filled = 0;

        while (true) {
            byte[] readBuf = new byte[chunkSize - filled];
            int len = bufferedInputStream.read(readBuf);
            if (len == -1) {
                return buf;
            }
            byte[] tmp = Arrays.copyOfRange(readBuf, 0, len);
            buf = ArrayUtils.addAll(buf, tmp);
            filled += len;
            if (filled >= chunkSize) {
                return buf;
            }
        }
    }

    @Override
    public void write(BufferedInputStream bufferedInputStream)
            throws
            IOException,
            FileHitSizeLimitException,
            ZFSStreamEndedException {

        try (OutputStream outputStream = Files.newOutputStream(path);
             ObjectOutputStream objectOutputStream = new ObjectOutputStream(outputStream)) {
            logger.info(String.format("Writing stream to file '%s'", path.toString()));
            long written = 0;

            while (true) {
                byte[] buf = fillBuffer(bufferedInputStream);
                if (buf.length == 0) {
                    break;
                }
                ZFSStreamChunk zfsStreamChunk = new ZFSStreamChunk(buf);
                objectOutputStream.writeUnshared(zfsStreamChunk);
                objectOutputStream.reset();
                written += zfsStreamChunk.getChunk().length;
                logger.trace(String.format("Data written: %d bytes", written));
                if (written >= filePartSize) {
                    logger.debug(String.format("Written (%d bytes) is bigger than filePartSize (%d bytes)", written, filePartSize));
                    throw new FileHitSizeLimitException();
                }
            }
            throw new ZFSStreamEndedException();
        }

    }
}
