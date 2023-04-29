package ru.rerumu.backups.services.zfs.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.rerumu.backups.factories.ProcessWrapperFactory;
import ru.rerumu.backups.models.zfs.Pool;
import ru.rerumu.backups.services.zfs.ZFSService;

public class ZFSServiceImpl implements ZFSService {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private final ProcessWrapperFactory processWrapperFactory;

    public ZFSServiceImpl(ProcessWrapperFactory processWrapperFactory) {
        this.processWrapperFactory = processWrapperFactory;
    }

    @Override
    public Pool getPool(String name) {
        try {
            Pool pool = new GetPool(processWrapperFactory,name).call();
            return pool;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
