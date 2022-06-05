package ru.rerumu.backups.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.rerumu.backups.exceptions.*;
import ru.rerumu.backups.io.ZFSFileReader;
import ru.rerumu.backups.io.impl.ZFSFileReaderFull;
import ru.rerumu.backups.models.CryptoMessage;
import ru.rerumu.backups.models.ZFSPool;
import ru.rerumu.backups.models.ZFSStreamPart;
import ru.rerumu.backups.repositories.FilePartRepository;
import ru.rerumu.backups.services.impl.AESCryptor;
import ru.rerumu.backups.services.impl.GZIPCompressor;
import ru.rerumu.backups.services.impl.SnapshotReceiverImpl;
import ru.rerumu.backups.zfs_api.ZFSReceive;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;

public class ZFSRestoreService {

    private final String password;
    private final Logger logger = LoggerFactory.getLogger(ZFSRestoreService.class);
    private final ZFSProcessFactory zfsProcessFactory;
    private final boolean isDelete;
    private final FilePartRepository filePartRepository;
    private final SnapshotReceiver snapshotReceiver;

    public ZFSRestoreService(String password,
                             ZFSProcessFactory zfsProcessFactory,
                             boolean isDelete,
                             FilePartRepository filePartRepository,
                             SnapshotReceiver snapshotReceiver) {
        this.password = password;
        this.zfsProcessFactory = zfsProcessFactory;
        this.isDelete = isDelete;
        this.filePartRepository = filePartRepository;
        this.snapshotReceiver = snapshotReceiver;
    }

    private void processReceivedFile(Path path) throws IOException {
        logger.info(String.format("Starting processing of file '%s'",path.toString()));
        if (isDelete) {
            filePartRepository.delete(path);
        } else {
            filePartRepository.markReceived(path);
        }
        logger.info(String.format("Finished processing of file '%s'",path.toString()));
    }

//    private void readZFSStream(ZFSReceive zfsReceive, ZFSStreamPart zfsStreamPart)
//            throws
//            IOException,
//            ClassNotFoundException,
//            NoMorePartsException,
//            FinishedFlagException,
//            TooManyPartsException,
//            EncryptException,
//            CompressorException,
//            EOFException {
//        logger.info(String.format("Starting reading from file '%s'", zfsStreamPart.getFullPath().toString()));
//        Cryptor cryptor = new AESCryptor(password);
//        Compressor compressor = new GZIPCompressor();
//
//
//        try (InputStream inputStream = Files.newInputStream(zfsStreamPart.getFullPath());
//             ObjectInputStream objectInputStream = new ObjectInputStream(inputStream)) {
//            logger.info(String.format("Reading file '%s'", zfsStreamPart.getFullPath().toString()));
//            while (true) {
//                logger.trace("Reading object from stream");
//                Object object = objectInputStream.readUnshared();
//                if (object instanceof CryptoMessage) {
//                    logger.trace("Trying to cast to CryptoMessage");
//                    CryptoMessage cryptoMessage = (CryptoMessage) object;
//
//                    logger.trace("Trying to decrypt chunk");
//                    byte[] tmp = cryptor.decryptChunk(cryptoMessage);
//
//                    logger.trace("Trying to decompress chunk");
//                    tmp = compressor.decompressChunk(tmp);
//
//                    logger.trace("Writing chunk to stream");
//                    zfsReceive.getBufferedOutputStream().write(tmp);
//                    logger.trace("End writing chunk to stream");
//                } else {
//                    logger.error("Object is not instance of CryptoMessage");
//                    throw new IOException();
//                }
//
//            }
//
//        }
//    }

    private boolean isNextPart(ZFSStreamPart previousStream, ZFSStreamPart nextStream){
        logger.info("isNextPart");
        logger.info(String.format("Previous stream - %s",previousStream.toString()));
        logger.info(String.format("Next stream - %s",nextStream.toString()));
        if (previousStream.getStreamName().equals(nextStream.getStreamName()) && nextStream.getPartNumber() == previousStream.getPartNumber()+1){
            logger.info("isNextPart - true");
            return true;
        }
        logger.info("isNextPart - false");
        return false;
    }

    private void readNextStream(ZFSReceive zfsReceive)
            throws
            FinishedFlagException,
            IOException,
            TooManyPartsException,
            CompressorException,
            ClassNotFoundException,
            EncryptException,
            InterruptedException,
            ZFSStreamEndedException,
            IncorrectFilePartNameException{

        ZFSStreamPart previousStream = null;
        ZFSStreamPart nextStream = null;

        while (true){
            try{
                nextStream = new ZFSStreamPart(filePartRepository.getNextInputPath());
                logger.info(String.format("Got next stream - %s",nextStream.toString()));
                if (previousStream != null && !isNextPart(previousStream,nextStream)){
                    logger.info("Stream ended");
                    throw new ZFSStreamEndedException();
                }
                ZFSFileReader zfsFileReader = new ZFSFileReaderFull(zfsReceive.getBufferedOutputStream(), nextStream.getFullPath(),password);
                zfsFileReader.read();
//                readZFSStream(zfsReceive,nextStream);
            } catch (EOFException e) {
                logger.info(String.format("End of file '%s'", nextStream.getFullPath().toString()));
                processReceivedFile(nextStream.getFullPath());
                previousStream = nextStream;
            } catch (NoMorePartsException e) {
                logger.debug("No acceptable files found. Waiting 1 second before retry");
                Thread.sleep(1000);
            }
        }




    }

    public void zfsReceive(ZFSPool zfsPool) throws
            IOException,
            TooManyPartsException,
            EncryptException,
            CompressorException,
            InterruptedException,
            ClassNotFoundException,
            FinishedFlagException,
            NoMorePartsException,
            IncorrectFilePartNameException{
        logger.info("Starting restore");
//        ZFSReceive zfsReceive = zfsProcessFactory.getZFSReceive(zfsPool);

        while (true) {
            try {
                ZFSReceive zfsReceive = null;
                try {
                    zfsReceive = zfsProcessFactory.getZFSReceive(zfsPool);
                    readNextStream(zfsReceive);
                } finally {
                    if (zfsReceive != null) {
                        zfsReceive.close();
                    }
                }
            } catch (ZFSStreamEndedException e){
                continue;
            } catch (FinishedFlagException e) {
                logger.info("Finish flag found. Exiting loop");
                break;
            }
        }

        logger.debug("Finished restore");
    }

    private void receive() throws FinishedFlagException, NoMorePartsException, IOException, TooManyPartsException, IncorrectFilePartNameException, CompressorException, ClassNotFoundException, EncryptException, InterruptedException {

        while (true){
            try {
                Path nextPath = filePartRepository.getNextInputPath();
                snapshotReceiver.receiveSnapshotPart(nextPath);
            } catch (NoMorePartsException e) {
                logger.debug("No acceptable files found. Waiting 1 second before retry");
                Thread.sleep(1000);
            } catch (FinishedFlagException e) {
                logger.info("Finish flag found. Exiting loop");
                break;
            } finally {
                snapshotReceiver.finish();
            }
        }

    }
}
