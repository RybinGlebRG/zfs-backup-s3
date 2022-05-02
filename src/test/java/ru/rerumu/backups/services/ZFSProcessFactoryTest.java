package ru.rerumu.backups.services;

import ru.rerumu.backups.models.ZFSFileSystem;
import ru.rerumu.backups.zfs_api.ProcessWrapper;

import java.io.IOException;

public class ZFSProcessFactoryTest implements ZFSProcessFactory{

    @Override
    public ProcessWrapper getZFSListSnapshots(ZFSFileSystem zfsFileSystem) throws IOException {
        return null;
    }
}
