package ru.rerumu.zfs_backup_s3.zfs.services;

import ru.rerumu.zfs_backup_s3.zfs.models.Dataset;
import ru.rerumu.zfs_backup_s3.zfs.models.Snapshot;

public final class SnapshotServiceMock implements SnapshotService {
    @Override
    public Snapshot createRecursiveSnapshot(Dataset dataset, String name) {
        return null;
    }
}
