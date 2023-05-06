package ru.rerumu.backups.services.zfs.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.rerumu.backups.services.zfs.consumers.ReceiveStdinConsumer;
import ru.rerumu.backups.services.zfs.models.Dataset;
import ru.rerumu.backups.services.zfs.models.Pool;
import ru.rerumu.backups.services.zfs.ZFSService;
import ru.rerumu.backups.services.zfs.factories.ZFSCallableFactory;
import ru.rerumu.backups.services.zfs.models.Snapshot;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.util.function.Consumer;

public class ZFSServiceImpl implements ZFSService {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private final ZFSCallableFactory zfsCallableFactory;

    public ZFSServiceImpl(ZFSCallableFactory zfsCallableFactory) {
        this.zfsCallableFactory = zfsCallableFactory;
    }

    @Override
    public Pool getPool(String name) throws Exception {
        Pool pool = zfsCallableFactory.getPoolCallable(name).call();
        return pool;

    }

    @Override
    public void send(Snapshot snapshot, Consumer<BufferedInputStream> consumer) throws Exception {
        zfsCallableFactory.getSendReplica(snapshot, consumer).call();
    }

    @Override
    public void receive(Pool pool, Consumer<BufferedOutputStream> consumer) throws Exception {
        zfsCallableFactory.getReceive(pool, consumer).call();
    }

}
