package ru.rerumu.backups.services.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.rerumu.backups.exceptions.*;
import ru.rerumu.backups.repositories.RemoteBackupRepository;
import ru.rerumu.backups.factories.ZFSFileWriterFactory;
import ru.rerumu.backups.models.Snapshot;
import ru.rerumu.backups.repositories.LocalBackupRepository;
import ru.rerumu.backups.factories.ZFSProcessFactory;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.concurrent.ExecutionException;

@Deprecated
public class SnapshotSenderBySnapshot extends AbstractSnapshotSender {
    private final Logger logger = LoggerFactory.getLogger(SnapshotSenderBySnapshot.class);

    public SnapshotSenderBySnapshot(
            LocalBackupRepository localBackupRepository,
            RemoteBackupRepository remoteBackupRepository,
            ZFSProcessFactory zfsProcessFactory,
            ZFSFileWriterFactory zfsFileWriterFactory,
            boolean isLoadS3
    ) {
        super(
                localBackupRepository,
                remoteBackupRepository,
                zfsProcessFactory,
                zfsFileWriterFactory,
                isLoadS3
        );
    }

    @Override
    public void sendStartingFromFull(String datasetName, List<Snapshot> snapshotList) throws InterruptedException, CompressorException, IOException, EncryptException, NoSuchAlgorithmException, IncorrectHashException, ExecutionException, S3MissesFileException {
        boolean isBaseSent = false;

        Snapshot previousSnapshot = null;
        for (Snapshot snapshot : snapshotList) {
            if (!isBaseSent) {
                sendBaseSnapshot(snapshot);
                isBaseSent = true;
                previousSnapshot = snapshot;
                continue;
            }
            sendIncrementalSnapshot(previousSnapshot, snapshot);
            previousSnapshot = snapshot;

        }
    }

    @Override
    public void sendStartingFromIncremental(String datasetName,List<Snapshot> snapshotList) throws InterruptedException, CompressorException, IOException, EncryptException, NoSuchAlgorithmException, IncorrectHashException, ExecutionException, S3MissesFileException {
        boolean isBaseSkipped = false;

        Snapshot previousSnapshot = null;
        for (Snapshot snapshot : snapshotList) {
            if (!isBaseSkipped) {
                isBaseSkipped = true;
                previousSnapshot = snapshot;
                continue;
            }
            sendIncrementalSnapshot(previousSnapshot, snapshot);
            previousSnapshot = snapshot;

        }
    }

}
