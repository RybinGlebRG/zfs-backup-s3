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
import java.util.concurrent.ExecutionException;


// TODO: User resume tokens?
public class SendServiceImpl implements SendService {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final ZFSProcessFactory zfsProcessFactory;
    private final S3StreamRepositoryImpl s3StreamRepository;


    public SendServiceImpl(
            ZFSProcessFactory zfsProcessFactory,
            S3StreamRepositoryImpl s3StreamRepository
    ) {
        this.zfsProcessFactory = zfsProcessFactory;
        this.s3StreamRepository = s3StreamRepository;
    }

    private String escapeSymbols(String srcString) {
        return srcString.replace('/', '-');
    }

    @Override
    public void send(Snapshot snapshot)
            throws IOException,
            CompressorException,
            EncryptException,
            S3MissesFileException,
            NoSuchAlgorithmException,
            IncorrectHashException {
        try (ZFSSend zfsSend = zfsProcessFactory.getZFSSendReplicate(snapshot)){
            String prefix = String.format(
                    "%s/level-0-%s/",
                    escapeSymbols(snapshot.getDataset()),
                    escapeSymbols(snapshot.getName())
            );
            try {
                s3StreamRepository.add(prefix, zfsSend.getBufferedInputStream());
            } catch (Exception e){
                zfsSend.kill();
                throw e;
            }
        }
    }
}
