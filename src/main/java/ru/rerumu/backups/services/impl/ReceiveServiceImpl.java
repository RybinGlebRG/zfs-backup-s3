package ru.rerumu.backups.services.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.rerumu.backups.factories.ZFSProcessFactory;
import ru.rerumu.backups.models.ZFSPool;
import ru.rerumu.backups.repositories.impl.S3StreamRepositoryImpl;
import ru.rerumu.backups.services.ReceiveService;
import ru.rerumu.backups.zfs_api.ZFSReceive;

public class ReceiveServiceImpl implements ReceiveService {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final S3StreamRepositoryImpl s3StreamRepository;
    private final ZFSProcessFactory zfsProcessFactory;
    private final ZFSPool zfsPool;


    @Override
    public void receive() {
        ZFSReceive zfsReceive = zfsProcessFactory.getZFSReceive(zfsPool);
    }
}
