package ru.rerumu.backups.services.zfs;

import ru.rerumu.backups.services.zfs.models.Dataset;
import ru.rerumu.backups.services.zfs.models.Pool;
import ru.rerumu.backups.services.zfs.models.Snapshot;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.util.concurrent.Callable;
import java.util.function.Consumer;

public interface ZFSService {

    Pool getPool(String name) throws Exception;
    void send(Snapshot snapshot, Consumer<BufferedInputStream> consumer) throws Exception;

    void receive(Pool pool, Consumer<BufferedOutputStream> consumer) throws Exception;
}
