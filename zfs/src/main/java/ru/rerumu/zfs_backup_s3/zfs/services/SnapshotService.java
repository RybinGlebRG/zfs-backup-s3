package ru.rerumu.zfs_backup_s3.zfs.services;

import ru.rerumu.zfs_backup_s3.zfs.models.Snapshot;
import ru.rerumu.zfs_backup_s3.zfs.models.Dataset;

public interface SnapshotService {

    Snapshot createRecursiveSnapshot(Dataset dataset, String name);
}
