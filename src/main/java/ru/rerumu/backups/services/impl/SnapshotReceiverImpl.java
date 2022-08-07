package ru.rerumu.backups.services.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.rerumu.backups.exceptions.CompressorException;
import ru.rerumu.backups.exceptions.EncryptException;
import ru.rerumu.backups.exceptions.IncorrectFilePartNameException;
import ru.rerumu.backups.services.ZFSFileReader;
import ru.rerumu.backups.factories.ZFSFileReaderFactory;
import ru.rerumu.backups.models.ZFSPool;
import ru.rerumu.backups.models.ZFSStreamPart;
import ru.rerumu.backups.repositories.LocalBackupRepository;
import ru.rerumu.backups.services.SnapshotReceiver;
import ru.rerumu.backups.factories.ZFSProcessFactory;
import ru.rerumu.backups.zfs_api.ZFSReceive;

import java.io.EOFException;
import java.io.IOException;
import java.nio.file.Path;
import java.util.concurrent.ExecutionException;

public class SnapshotReceiverImpl implements SnapshotReceiver {
    private final Logger logger = LoggerFactory.getLogger(SnapshotReceiverImpl.class);

    private final ZFSProcessFactory zfsProcessFactory;
    private final ZFSPool zfsPool;
    private final ZFSFileReaderFactory zfsFileReaderFactory;

    private ZFSStreamPart previousStream;
    private ZFSStreamPart nextStream;
    private ZFSReceive zfsReceive;

    public SnapshotReceiverImpl(
            ZFSProcessFactory zfsProcessFactory,
            ZFSPool zfsPool,
            ZFSFileReaderFactory zfsFileReaderFactory
    ) {
        this.zfsProcessFactory = zfsProcessFactory;
        this.zfsPool = zfsPool;
        this.zfsFileReaderFactory = zfsFileReaderFactory;
    }

    private boolean isNextPart(ZFSStreamPart previousStream, ZFSStreamPart nextStream) {
        logger.info("isNextPart");
        logger.info(String.format("Previous stream - %s", previousStream.toString()));
        logger.info(String.format("Next stream - %s", nextStream.toString()));
        if (previousStream.getStreamName().equals(nextStream.getStreamName()) && nextStream.getPartNumber() == previousStream.getPartNumber() + 1) {
            logger.info("isNextPart - true");
            return true;
        }
        logger.info("isNextPart - false");
        return false;
    }

    @Override
    public void receiveSnapshotPart(Path path) throws IncorrectFilePartNameException, CompressorException, IOException, ClassNotFoundException, EncryptException, InterruptedException, ExecutionException {
        if (zfsReceive == null) {
            zfsReceive = zfsProcessFactory.getZFSReceive(zfsPool);
        }
        nextStream = new ZFSStreamPart(path);
        if (previousStream != null && !isNextPart(previousStream, nextStream)) {
            logger.info("Stream ended");
            zfsReceive.close();
            zfsReceive = zfsProcessFactory.getZFSReceive(zfsPool);
        }
        logger.info(String.format("Got next stream - %s", nextStream.toString()));
        ZFSFileReader zfsFileReader = zfsFileReaderFactory.getZFSFileReader(zfsReceive.getBufferedOutputStream(), nextStream.getFullPath());

        zfsFileReader.read();
        logger.info(String.format("End of file '%s'", nextStream.getFullPath().toString()));
        previousStream = nextStream;

    }

    @Override
    public void finish() throws IOException, InterruptedException, ExecutionException {
        if (zfsReceive != null) {
            zfsReceive.close();
        }
    }
}
