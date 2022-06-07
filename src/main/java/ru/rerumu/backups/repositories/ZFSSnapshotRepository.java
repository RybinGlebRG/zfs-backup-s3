package ru.rerumu.backups.repositories;

import ru.rerumu.backups.models.Snapshot;
import ru.rerumu.backups.models.ZFSFileSystem;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.ExecutionException;

public interface ZFSSnapshotRepository {

    List<Snapshot> getAllSnapshotsOrdered(String fileSystemName) throws IOException, InterruptedException, ExecutionException;

}
