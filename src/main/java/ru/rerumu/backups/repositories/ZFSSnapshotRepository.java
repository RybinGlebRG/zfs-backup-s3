package ru.rerumu.backups.repositories;

import ru.rerumu.backups.models.Snapshot;
import ru.rerumu.backups.models.ZFSFileSystem;

import java.io.IOException;
import java.util.List;

public interface ZFSSnapshotRepository {

    List<Snapshot> getAllSnapshotsOrdered(ZFSFileSystem zfsFileSystem) throws IOException, InterruptedException;
    Snapshot getBaseSnapshot(ZFSFileSystem zfsFileSystem);
//    List<Snapshot> getIncrementalSnapshotsOrdered(ZFSFileSystem zfsFileSystem);

}
