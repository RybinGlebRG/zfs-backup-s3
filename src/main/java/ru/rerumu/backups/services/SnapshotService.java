package ru.rerumu.backups.services;

import ru.rerumu.backups.models.Snapshot;
import ru.rerumu.backups.models.ZFSDataset;
import ru.rerumu.backups.models.zfs.Dataset;

public interface SnapshotService {

    Snapshot createRecursiveSnapshot(Dataset dataset, String name);
}
