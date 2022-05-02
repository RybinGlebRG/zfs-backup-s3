package ru.rerumu.backups.services;

import ru.rerumu.backups.models.ZFSFileSystem;
import ru.rerumu.backups.zfs_api.ProcessWrapper;
import ru.rerumu.backups.zfs_api.ZFSListSnapshotsTest;

import java.io.IOException;
import java.util.List;

public class ZFSProcessFactoryTest implements ZFSProcessFactory{

    private List<String> stringList;

    public void setStringList(List<String> stringList) {
        this.stringList = stringList;
    }

    @Override
    public ProcessWrapper getZFSListSnapshots(ZFSFileSystem zfsFileSystem) throws IOException {
        return new ZFSListSnapshotsTest(stringList);
    }
}
