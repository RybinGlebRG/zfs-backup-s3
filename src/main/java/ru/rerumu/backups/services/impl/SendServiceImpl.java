package ru.rerumu.backups.services.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.rerumu.backups.exceptions.*;
import ru.rerumu.backups.factories.ZFSFileWriterFactory;
import ru.rerumu.backups.factories.ZFSProcessFactory;
import ru.rerumu.backups.models.Snapshot;
import ru.rerumu.backups.models.ZFSDataset;
import ru.rerumu.backups.repositories.LocalBackupRepository;
import ru.rerumu.backups.repositories.RemoteBackupRepository;
import ru.rerumu.backups.repositories.S3Repository;
import ru.rerumu.backups.repositories.impl.S3StreamRepositoryImpl;
import ru.rerumu.backups.services.SendService;
import ru.rerumu.backups.services.ZFSFileWriter;
import ru.rerumu.backups.services.impl.AbstractSnapshotSender;
import ru.rerumu.backups.zfs_api.ZFSSend;

import java.io.IOException;
import java.nio.file.Path;
import java.security.NoSuchAlgorithmException;

public class SendServiceImpl implements SendService {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    protected final ZFSProcessFactory zfsProcessFactory;
    protected final ZFSFileWriterFactory zfsFileWriterFactory;
    private final Path tempDir;

    private final RemoteBackupRepository remoteBackupRepository;

    private final S3Repository s3Repository;

    private final S3StreamRepositoryImpl s3StreamRepository;


    public SendServiceImpl(
            ZFSProcessFactory zfsProcessFactory,
            ZFSFileWriterFactory zfsFileWriterFactory,
            Path tempDir,
            RemoteBackupRepository remoteBackupRepository,
            S3Repository s3Repository,
            S3StreamRepositoryImpl s3StreamRepository
    ) {
        this.zfsProcessFactory = zfsProcessFactory;
        this.zfsFileWriterFactory = zfsFileWriterFactory;
        this.tempDir =tempDir;
        this.remoteBackupRepository = remoteBackupRepository;
        this.s3Repository = s3Repository;
        this.s3StreamRepository = s3StreamRepository;
    }

    @Override
    public void send(Snapshot snapshot)
            throws IOException,
            CompressorException,
            EncryptException
    {
        ZFSSend zfsSend = zfsProcessFactory.getZFSSendReplicate(snapshot);
        s3StreamRepository.add("",zfsSend.getBufferedInputStream());
    }
}
