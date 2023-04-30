package ru.rerumu.backups.services.zfs.impl;

import ru.rerumu.backups.services.zfs.models.Snapshot;
import ru.rerumu.backups.services.zfs.models.Dataset;
import ru.rerumu.backups.services.zfs.SnapshotService;
import ru.rerumu.backups.services.zfs.factories.ZFSCallableFactory;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

public class SnapshotServiceImpl implements SnapshotService {

    private final ZFSCallableFactory zfsCallableFactory;

    public SnapshotServiceImpl( ZFSCallableFactory zfsCallableFactory) {
        this.zfsCallableFactory = zfsCallableFactory;
    }

    @Override
    public Snapshot createRecursiveSnapshot(Dataset dataset, String name) {
        CreateSnapshot createSnapshot = zfsCallableFactory.getCreateSnapshotCallable(dataset,name,true);
        ListSnapshots listSnapshots = zfsCallableFactory.getListSnapshotsCallable(dataset);

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
