package ru.rerumu.zfs;

import ru.rerumu.zfs.models.Dataset;
import ru.rerumu.zfs.models.Pool;
import ru.rerumu.zfs.models.Snapshot;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.util.function.Consumer;

public interface ZFSService {

    Pool getPool(String name) throws Exception;
    void send(Snapshot snapshot, Consumer<BufferedInputStream> consumer) throws Exception;

    void receive(Pool pool, Consumer<BufferedOutputStream> consumer) throws Exception;

    Snapshot createRecursiveSnapshot(Dataset dataset, String name);
}
