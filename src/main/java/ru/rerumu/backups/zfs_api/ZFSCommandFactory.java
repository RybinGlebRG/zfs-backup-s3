package ru.rerumu.backups.zfs_api;

import ru.rerumu.backups.models.ZFSDataset;
import ru.rerumu.backups.zfs_api.zfs.ListSnapshotsCommand;
import ru.rerumu.backups.zfs_api.zfs.SnapshotCommand;

public interface ZFSCommandFactory {

    SnapshotCommand getSnapshotCommand(
            ZFSDataset dataset,
            String name,
            Boolean isRecursive
    );

    ListSnapshotsCommand getListSnapshotsCommand(ZFSDataset dataset);
}
