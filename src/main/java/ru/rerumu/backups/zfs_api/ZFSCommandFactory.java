package ru.rerumu.backups.zfs_api;

import ru.rerumu.backups.models.zfs.Dataset;
import ru.rerumu.backups.services.zfs.impl.CreateSnapshot;
import ru.rerumu.backups.services.zfs.impl.ListSnapshots;

public interface ZFSCommandFactory {

//    SnapshotCommand getSnapshotCommand(
//            ZFSDataset dataset,
//            String name,
//            Boolean isRecursive
//    );

    CreateSnapshot getSnapshotCommand(
            Dataset dataset,
            String name,
            Boolean isRecursive
    );

    ListSnapshots getListSnapshotsCommand(Dataset dataset, Boolean isRecursive);
}
