package ru.rerumu.backups.services.impl;

import org.apache.commons.lang3.ArrayUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.rerumu.backups.exceptions.CompressorException;
import ru.rerumu.backups.exceptions.EncryptException;
import ru.rerumu.backups.exceptions.FileHitSizeLimitException;
import ru.rerumu.backups.exceptions.ZFSStreamEndedException;
import ru.rerumu.backups.services.ZFSFileWriter;
import ru.rerumu.backups.models.CryptoMessage;
import ru.rerumu.backups.services.Compressor;
import ru.rerumu.backups.services.Cryptor;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;

public class ZFSFileWriterFull implements ZFSFileWriter {
    private final Logger logger = LoggerFactory.getLogger(ZFSFileWriterFull.class);

    private final String password;
    private final int chunkSize;
    private final long filePartSize;

    public ZFSFileWriterFull(String password, int chunkSize, long filePartSize){
        this.password = password;
        this.chunkSize = chunkSize;
        this.filePartSize = filePartSize;
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
            if (filled == chunkSize) {
                return buf;
            }
        }
    }

    @Override
    public void write(BufferedInputStream bufferedInputStream, Path path)
            throws
            IOException,
            CompressorException, EncryptException, FileHitSizeLimitException, ZFSStreamEndedException {
        Cryptor cryptor = new AESCryptor(password);
        Compressor compressor = new GZIPCompressor();

        try (OutputStream outputStream = Files.newOutputStream(path);
             ObjectOutputStream objectOutputStream = new ObjectOutputStream(outputStream)) {
            logger.info(String.format("Writing stream to file '%s'", path.toString()));
            long written = 0;

            while (true) {
                byte[] buf = fillBuffer(bufferedInputStream);
                if (buf.length == 0) {
                    break;
                }
                buf = compressor.compressChunk(buf);
                CryptoMessage cryptoMessage = cryptor.encryptChunk(buf);
                objectOutputStream.writeUnshared(cryptoMessage);
                objectOutputStream.reset();
                written += cryptoMessage.getMessage().length + cryptoMessage.getSalt().length + cryptoMessage.getIv().length;
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
