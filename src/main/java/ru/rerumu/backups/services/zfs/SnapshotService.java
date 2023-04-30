package ru.rerumu.backups.services.zfs;

import ru.rerumu.backups.services.zfs.models.Snapshot;
import ru.rerumu.backups.services.zfs.models.Dataset;

public interface SnapshotService {

    Snapshot createRecursiveSnapshot(Dataset dataset, String name);
}
