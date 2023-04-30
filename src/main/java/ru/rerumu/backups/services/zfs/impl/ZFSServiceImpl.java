package ru.rerumu.backups.services.zfs.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.rerumu.backups.services.zfs.models.Dataset;
import ru.rerumu.backups.services.zfs.models.Pool;
import ru.rerumu.backups.services.zfs.ZFSService;
import ru.rerumu.backups.services.zfs.factories.ZFSCallableFactory;

public class ZFSServiceImpl implements ZFSService {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private final ZFSCallableFactory zfsCallableFactory;

    public ZFSServiceImpl( ZFSCallableFactory zfsCallableFactory) {
        this.zfsCallableFactory = zfsCallableFactory;
    }

    @Override
    public Pool getPool(String name) {
        try {
            Pool pool = zfsCallableFactory.getPoolCallable(name).call();
            return pool;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Dataset getDataset(String name) {
        try {
            Dataset dataset = zfsCallableFactory.getDatasetCallable(name).call();
            return dataset;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }
}
