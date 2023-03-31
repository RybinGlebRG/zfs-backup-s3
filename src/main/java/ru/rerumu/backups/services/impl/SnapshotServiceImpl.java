package ru.rerumu.backups.services.impl;

import ru.rerumu.backups.models.Snapshot;
import ru.rerumu.backups.models.ZFSDataset;
import ru.rerumu.backups.services.SnapshotService;
import ru.rerumu.backups.zfs_api.ZFSCommandFactory;
import ru.rerumu.backups.zfs_api.zfs.ListSnapshotsCommand;
import ru.rerumu.backups.zfs_api.zfs.SnapshotCommand;

import java.util.List;
import java.util.Optional;

public class SnapshotServiceImpl implements SnapshotService {

    private final ZFSCommandFactory zfsCommandFactory;

    public SnapshotServiceImpl(ZFSCommandFactory zfsCommandFactory) {
        this.zfsCommandFactory = zfsCommandFactory;
    }

    @Override
    public Snapshot createRecursiveSnapshot(ZFSDataset dataset, String name) {
        SnapshotCommand snapshotCommand = zfsCommandFactory.getSnapshotCommand(dataset, name, true);
        snapshotCommand.execute();

        ListSnapshotsCommand listSnapshotsCommand = zfsCommandFactory.getListSnapshotsCommand(dataset, false);
        List<Snapshot> snapshotList = listSnapshotsCommand.execute();

        Optional<Snapshot> snapshot = snapshotList.stream()
                .filter(item -> item.getName().equals(name))
                .findFirst();

        return snapshot.orElseThrow();
    }
}
