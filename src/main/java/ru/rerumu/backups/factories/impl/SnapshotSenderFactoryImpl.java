package ru.rerumu.backups.factories.impl;

import ru.rerumu.backups.factories.SnapshotSenderFactory;
import ru.rerumu.backups.factories.ZFSFileWriterFactory;
import ru.rerumu.backups.factories.ZFSProcessFactory;
import ru.rerumu.backups.repositories.LocalBackupRepository;
import ru.rerumu.backups.repositories.RemoteBackupRepository;
import ru.rerumu.backups.services.SnapshotSender;
import ru.rerumu.backups.services.impl.SnapshotSenderByDataset;

import java.nio.file.Path;

public class SnapshotSenderFactoryImpl implements SnapshotSenderFactory {

    private final LocalBackupRepository localBackupRepository;
    private final ZFSProcessFactory zfsProcessFactory;
    private final ZFSFileWriterFactory zfsFileWriterFactory;
    private final Path tempDir;

    public SnapshotSenderFactoryImpl(
            LocalBackupRepository localBackupRepository,
            ZFSProcessFactory zfsProcessFactory,
            ZFSFileWriterFactory zfsFileWriterFactory,
            Path tempDir
    ){
        this.localBackupRepository = localBackupRepository;
        this.zfsProcessFactory = zfsProcessFactory;
        this.zfsFileWriterFactory = zfsFileWriterFactory;
        this.tempDir = tempDir;
    }

    @Override
    public SnapshotSender getSnapshotSender() {
            return new SnapshotSenderByDataset(
                    localBackupRepository,
                    zfsProcessFactory,
                    zfsFileWriterFactory,
                    tempDir
            );

    }
}
