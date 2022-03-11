package ru.rerumu.backups.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.rerumu.backups.exceptions.*;
import ru.rerumu.backups.models.CryptoMessage;
import ru.rerumu.backups.repositories.FilePartRepository;
import ru.rerumu.backups.services.impl.AESCryptor;
import ru.rerumu.backups.services.impl.GZIPCompressor;

import java.io.*;
import java.nio.file.Path;
import java.util.Arrays;

public class ZFSBackupsService {

    private final String password;
    private final Logger logger = LoggerFactory.getLogger(ZFSBackupsService.class);


    public ZFSBackupsService(String password) {
        this.password = password;
    }

    public void zfsSend(ZFSSend zfsSend,
                        int chunkSize,
                        boolean isLoadS3,
                        long filePartSize,
                        FilePartRepository filePartRepository,
                        boolean isDelete,
                        S3Loader s3Loader) throws
            IOException,
            InterruptedException,
            CompressorException,
            EncryptException {

        Cryptor cryptor = new AESCryptor(password);
        Compressor compressor = new GZIPCompressor();

        byte[] buf = new byte[chunkSize];
        byte[] tmp;
        int len;
        long written;
        while (true) {
            written = 0;
            try (ObjectOutputStream objectOutputStream = new ObjectOutputStream(filePartRepository.newPart())) {
                logger.info(String.format("Writing stream to file '%s'",filePartRepository.getLastPart().toString()));
                while ((len = zfsSend.getBufferedInputStream().read(buf)) >= 0) {
                    logger.trace(String.format("Data in buffer: %d bytes",len));
                    tmp = Arrays.copyOfRange(buf, 0, len);
                    tmp = compressor.compressChunk(tmp);
                    CryptoMessage cryptoMessage = cryptor.encryptChunk(tmp);
                    objectOutputStream.writeUnshared(cryptoMessage);
                    objectOutputStream.reset();
                    written += cryptoMessage.getMessage().length + cryptoMessage.getSalt().length + cryptoMessage.getIv().length;
                    logger.trace(String.format("Data written: %d bytes",written));
                    if (written >= filePartSize) {
                        logger.debug(String.format("Written (%d bytes) is bigger than filePartSize (%d bytes)",written,filePartSize));
                        break;
                    }
                }
            }

            if (isLoadS3) {
                Path lastPart = filePartRepository.getLastPart();
                s3Loader.upload(lastPart);
                filePartRepository.deleteLastPart();
            } else {
                filePartRepository.markReadyLastPart();
                while (filePartRepository.isLastPartExists()){
                    logger.debug("Last part exists. Waiting 10 seconds before retry");
                    Thread.sleep(10000);
                }
            }
            if (len == -1) {
                logger.info("End of stream. Exiting");
                break;
            }

        }
        zfsSend.close();

    }

    public void zfsReceive(ZFSReceive zfsReceive,
                           FilePartRepository filePartRepository,
                           boolean isDelete) throws
            IOException,
            TooManyPartsException,
            EncryptException,
            CompressorException,
            InterruptedException,
            ClassNotFoundException {
        Cryptor cryptor = new AESCryptor(password);
        Compressor compressor = new GZIPCompressor();

        while (true) {
            try(ObjectInputStream objectInputStream = new ObjectInputStream(filePartRepository.getNextInputStream())) {
                logger.info(String.format("Reading file '%s'",filePartRepository.getLastPart().toString()));
                while (true) {
                    try {
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
                            zfsReceive.getBufferedOutputStream().write(tmp);
                            logger.trace("End writing chunk to stream");
                        } else {
                            logger.error("Object is not instance of CryptoMessage");
                            throw new IOException();
                        }
                    } catch (EOFException e) {
                        logger.info(String.format("End of file '%s'",filePartRepository.getLastPart().toString()));
                        break;
                    }
                }

            } catch (NoMorePartsException e){
                logger.debug("No files found. Waiting 10 seconds before retry");
                Thread.sleep(10000);
                continue;
            } catch (FinishedFlagException e) {
                logger.info("Finish flag found. Exiting loop");
                break;
            }

            if (isDelete) {
                filePartRepository.deleteLastPart();
            } else {
                filePartRepository.markReceivedLastPart();
            }
        }

        zfsReceive.close();
    }
}
