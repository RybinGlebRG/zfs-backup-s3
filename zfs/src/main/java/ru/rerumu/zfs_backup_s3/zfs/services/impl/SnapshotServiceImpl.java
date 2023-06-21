package ru.rerumu.zfs_backup_s3.zfs.services.impl;

import ru.rerumu.zfs_backup_s3.zfs.models.Snapshot;
import ru.rerumu.zfs_backup_s3.zfs.models.Dataset;
import ru.rerumu.zfs_backup_s3.zfs.services.SnapshotService;
import ru.rerumu.zfs_backup_s3.zfs.factories.ZFSCallableFactory;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.concurrent.Callable;

// TODO: Check thread safe
public class SnapshotServiceImpl implements SnapshotService {

    private final ZFSCallableFactory zfsCallableFactory;

    public SnapshotServiceImpl( ZFSCallableFactory zfsCallableFactory) {
        this.zfsCallableFactory = zfsCallableFactory;
    }

    @Override
    public Snapshot createRecursiveSnapshot(Dataset dataset, String name) {
        Callable<Void> createSnapshot = zfsCallableFactory.getCreateSnapshotCallable(dataset,name,true);
        Callable<List<Snapshot>> listSnapshots = zfsCallableFactory.getListSnapshotsCallable(dataset);

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
