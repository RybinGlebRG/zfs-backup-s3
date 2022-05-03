package ru.rerumu.backups.services;

import ru.rerumu.backups.models.Snapshot;
import ru.rerumu.backups.models.ZFSFileSystem;
import ru.rerumu.backups.models.ZFSPool;
import ru.rerumu.backups.zfs_api.*;

import java.io.IOException;
import java.util.List;

public class ZFSProcessFactoryTest implements ZFSProcessFactory{

    private List<String> stringList;

    public void setStringList(List<String> stringList) {
        this.stringList = stringList;
    }

    @Override
    public ProcessWrapper getZFSListSnapshots(String fileSystemName) throws IOException {
        return new ZFSListSnapshotsTest(stringList);
    }

    @Override
    public ZFSListFilesystems getZFSListFilesystems(String parentFileSystem) throws IOException {
        return null;
    }

    @Override
    public ZFSReceive getZFSReceive(ZFSPool zfsPool) throws IOException {
        return null;
    }

    @Override
    public ZFSSend getZFSSendIncremental(Snapshot baseSnapshot, Snapshot incrementalSnapshot) throws IOException {
        return null;
    }

    @Override
    public ZFSSend getZFSSendFull(Snapshot snapshot) throws IOException {
        return null;
    }
}
