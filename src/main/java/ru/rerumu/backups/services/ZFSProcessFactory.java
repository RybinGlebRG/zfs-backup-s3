package ru.rerumu.backups.services;

import ru.rerumu.backups.models.ZFSFileSystem;
import ru.rerumu.backups.zfs_api.ProcessWrapper;

import java.io.IOException;

public interface ZFSProcessFactory {
    ProcessWrapper getZFSListSnapshots(ZFSFileSystem zfsFileSystem) throws IOException;
}
