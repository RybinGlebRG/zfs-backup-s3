package ru.rerumu.backups.services.zfs.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.rerumu.backups.factories.ProcessWrapperFactory;
import ru.rerumu.backups.models.zfs.Pool;
import ru.rerumu.backups.services.zfs.ZFSService;
import ru.rerumu.backups.services.zfs.factories.ZFSCallableFactory;
import ru.rerumu.backups.utils.processes.ProcessFactory;

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
}
