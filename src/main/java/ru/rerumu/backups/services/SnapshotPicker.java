package ru.rerumu.backups.services;

import ru.rerumu.backups.exceptions.BaseSnapshotNotFoundException;
import ru.rerumu.backups.exceptions.SnapshotNotFoundException;
import ru.rerumu.backups.models.Snapshot;
import ru.rerumu.backups.models.ZFSFileSystem;

import java.util.List;

public interface SnapshotPicker {

    List<Snapshot> pick(ZFSFileSystem zfsFileSystem, String snapshotName) throws BaseSnapshotNotFoundException, SnapshotNotFoundException;
}
