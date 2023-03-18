package ru.rerumu.backups.services;

import ru.rerumu.backups.models.Snapshot;
import ru.rerumu.backups.models.ZFSDataset;

public interface SnapshotService {

    Snapshot createRecursiveSnapshot(ZFSDataset dataset, String name);
}
