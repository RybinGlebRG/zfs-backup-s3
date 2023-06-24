package ru.rerumu.zfs_backup_s3.zfs.services;

import ru.rerumu.zfs_backup_s3.utils.ThreadSafe;
import ru.rerumu.zfs_backup_s3.zfs.models.Snapshot;
import ru.rerumu.zfs_backup_s3.zfs.models.Dataset;
import ru.rerumu.zfs_backup_s3.zfs.services.impl.SnapshotServiceImpl;

@ThreadSafe
public sealed interface SnapshotService permits SnapshotServiceMock, SnapshotServiceImpl {

    Snapshot createRecursiveSnapshot(Dataset dataset, String name);
}
