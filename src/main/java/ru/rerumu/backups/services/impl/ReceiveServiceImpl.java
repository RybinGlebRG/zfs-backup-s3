package ru.rerumu.backups.services.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.rerumu.backups.exceptions.ReceiveError;
import ru.rerumu.backups.factories.ZFSProcessFactory;
import ru.rerumu.backups.models.ZFSPool;
import ru.rerumu.backups.repositories.impl.S3StreamRepositoryImpl;
import ru.rerumu.backups.services.ReceiveService;
import ru.rerumu.backups.zfs_api.zfs.ZFSReceive;

public class ReceiveServiceImpl implements ReceiveService {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final S3StreamRepositoryImpl s3StreamRepository;
    private final ZFSProcessFactory zfsProcessFactory;

    public ReceiveServiceImpl(S3StreamRepositoryImpl s3StreamRepository, ZFSProcessFactory zfsProcessFactory) {
        this.s3StreamRepository = s3StreamRepository;
        this.zfsProcessFactory = zfsProcessFactory;
    }

    @Override
    public void receive(String prefix, ZFSPool zfsPool) {
        try (ZFSReceive zfsReceive = zfsProcessFactory.getZFSReceive(zfsPool)) {
            s3StreamRepository.getAll(zfsReceive.getBufferedOutputStream(), prefix);
        } catch (Exception e) {
            logger.error(e.getMessage(),e);
            throw new ReceiveError(e);
        }
    }
}
