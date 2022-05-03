package ru.rerumu.backups.services.impl;

import ru.rerumu.backups.models.Snapshot;
import ru.rerumu.backups.models.ZFSFileSystem;
import ru.rerumu.backups.models.ZFSPool;
import ru.rerumu.backups.services.ZFSProcessFactory;
import ru.rerumu.backups.zfs_api.*;
import ru.rerumu.backups.zfs_api.impl.ProcessWrapperImpl;
import ru.rerumu.backups.zfs_api.impl.ZFSReceiveImpl;
import ru.rerumu.backups.zfs_api.impl.ZFSSendFull;
import ru.rerumu.backups.zfs_api.impl.ZFSSendIncremental;

import java.io.IOException;

public class ZFSProcessFactoryImpl implements ZFSProcessFactory {

    public ZFSSend getZFSSendFull(Snapshot snapshot) throws IOException {
        return new ZFSSendFull(snapshot);
    }

    public ZFSSend getZFSSendIncremental(Snapshot baseSnapshot, Snapshot incrementalSnapshot) throws IOException {
        return new ZFSSendIncremental(baseSnapshot, incrementalSnapshot);
    }

    public ZFSReceive getZFSReceive(ZFSPool zfsPool) throws IOException {
        return new ZFSReceiveImpl(zfsPool.getName());
    }

    public ZFSListFilesystems getZFSListFilesystems(String parentFileSystem) throws IOException {
        return new ZFSListFilesystems(parentFileSystem);
    }

    public ProcessWrapper getZFSListSnapshots(String fileSystemName) throws IOException {
        return new ZFSListSnapshots(fileSystemName);
    }


}
