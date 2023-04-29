package ru.rerumu.backups.services.impl;

import ru.rerumu.backups.models.Snapshot;
import ru.rerumu.backups.models.zfs.Dataset;
import ru.rerumu.backups.services.SnapshotService;
import ru.rerumu.backups.services.zfs.impl.CreateSnapshot;
import ru.rerumu.backups.services.zfs.impl.ListSnapshots;
import ru.rerumu.backups.zfs_api.ZFSCommandFactory;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

public class SnapshotServiceImpl implements SnapshotService {

    private final ZFSCommandFactory zfsCommandFactory;

    public SnapshotServiceImpl(ZFSCommandFactory zfsCommandFactory) {
        this.zfsCommandFactory = zfsCommandFactory;
    }

    @Override
    public Snapshot createRecursiveSnapshot(Dataset dataset, String name) {
        CreateSnapshot createSnapshot = zfsCommandFactory.getSnapshotCommand(dataset,name,true);
        ListSnapshots listSnapshots = zfsCommandFactory.getListSnapshotsCommand(dataset, false);
        try {
            createSnapshot.call();
            List<Snapshot> snapshotList = listSnapshots.call();
            Optional<Snapshot> snapshot = snapshotList.stream()
                    .filter(item -> item.getName().equals(name))
                    .findFirst();

            return snapshot.orElseThrow();
        } catch (NoSuchElementException e){
            throw e;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
