package ru.rerumu.backups.io.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.rerumu.backups.exceptions.CompressorException;
import ru.rerumu.backups.exceptions.EncryptException;
import ru.rerumu.backups.io.ZFSFileReader;
import ru.rerumu.backups.models.CryptoMessage;
import ru.rerumu.backups.services.Compressor;
import ru.rerumu.backups.services.Cryptor;
import ru.rerumu.backups.services.impl.AESCryptor;
import ru.rerumu.backups.services.impl.GZIPCompressor;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;

public class ZFSFileReaderFull implements ZFSFileReader {
    private final Logger logger = LoggerFactory.getLogger(ZFSFileReaderFull.class);
    private final String password;
    private final BufferedOutputStream bufferedOutputStream;
    private final Path path;

    public ZFSFileReaderFull(BufferedOutputStream bufferedOutputStream, Path path, String password){
        this.bufferedOutputStream = bufferedOutputStream;
        this.path = path;
        this.password = password;
    }

    @Override
    public void read() throws IOException, ClassNotFoundException, EncryptException, CompressorException, EOFException {
        logger.info(String.format("Starting reading from file '%s'", path.toString()));
        Cryptor cryptor = new AESCryptor(password);
        Compressor compressor = new GZIPCompressor();

        try (InputStream inputStream = Files.newInputStream(path);
             ObjectInputStream objectInputStream = new ObjectInputStream(inputStream)) {
            logger.info(String.format("Reading file '%s'", path.toString()));
            while (true) {
                logger.trace("Reading object from stream");
                Object object = objectInputStream.readUnshared();
                if (object instanceof CryptoMessage) {
                    logger.trace("Trying to cast to CryptoMessage");
                    CryptoMessage cryptoMessage = (CryptoMessage) object;

                    logger.trace("Trying to decrypt chunk");
                    byte[] tmp = cryptor.decryptChunk(cryptoMessage);

                    logger.trace("Trying to decompress chunk");
                    tmp = compressor.decompressChunk(tmp);

                    logger.trace("Writing chunk to stream");
                    bufferedOutputStream.write(tmp);
                    logger.trace("End writing chunk to stream");
                } else {
                    logger.error("Object is not instance of CryptoMessage");
                    throw new IOException();
                }

            }

        }
    }
}
