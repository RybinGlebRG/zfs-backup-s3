package ru.rerumu.zfs;

import ru.rerumu.zfs.models.Snapshot;
import ru.rerumu.zfs.models.Dataset;

public interface SnapshotService {

    Snapshot createRecursiveSnapshot(Dataset dataset, String name);
}
