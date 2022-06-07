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
import java.util.concurrent.ExecutionException;

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

    public void zfsReceive() throws FinishedFlagException, NoMorePartsException, IOException, TooManyPartsException, IncorrectFilePartNameException, CompressorException, ClassNotFoundException, EncryptException, InterruptedException, ExecutionException {

        try {
            while (true) {
                try {
                    Path nextPath = filePartRepository.getNextInputPath();
                    snapshotReceiver.receiveSnapshotPart(nextPath);
                } catch (NoMorePartsException e) {
                    logger.debug("No acceptable files found. Waiting 1 second before retry");
                    Thread.sleep(1000);
                } catch (FinishedFlagException e) {
                    logger.info("Finish flag found. Exiting loop");
                    break;
                }
            }
        } finally {
            snapshotReceiver.finish();
        }

    }
}
