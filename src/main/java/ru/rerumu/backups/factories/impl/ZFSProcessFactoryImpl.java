package ru.rerumu.backups.factories.impl;

import ru.rerumu.backups.factories.ProcessWrapperFactory;
import ru.rerumu.backups.models.Snapshot;
import ru.rerumu.backups.models.ZFSPool;
import ru.rerumu.backups.factories.ZFSProcessFactory;
import ru.rerumu.backups.zfs_api.*;
import ru.rerumu.backups.zfs_api.impl.*;

import java.io.IOException;

public class ZFSProcessFactoryImpl implements ZFSProcessFactory {
    private final ProcessWrapperFactory processWrapperFactory;

    public ZFSProcessFactoryImpl(
            ProcessWrapperFactory processWrapperFactory){
        this.processWrapperFactory = processWrapperFactory;
    }

    @Override
    public ZFSSend getZFSSendFull(Snapshot snapshot) throws IOException {
        return new ZFSSendFullEncrypted(snapshot, processWrapperFactory);
    }

    @Override
    public ZFSGetDatasetProperty getZFSGetDatasetProperty(String datasetName, String propertyName) throws IOException {
        return new ZFSGetDatasetPropertyImpl(propertyName, datasetName,processWrapperFactory);
    }

    @Override
    public ZFSSend getZFSSendIncremental(Snapshot baseSnapshot, Snapshot incrementalSnapshot) throws IOException {
        return new ZFSSendMultiIncrementalEncrypted(baseSnapshot,incrementalSnapshot, processWrapperFactory);
    }

    @Override
    public ZFSReceive getZFSReceive(ZFSPool zfsPool) throws IOException {
        return new ZFSReceiveImpl(zfsPool.getName(), processWrapperFactory);
    }

    @Override
    public ZFSListFilesystems getZFSListFilesystems(String parentFileSystem) throws IOException {
        return new ZFSListFilesystems(parentFileSystem, processWrapperFactory);
    }

    @Override
    public ZFSListSnapshots getZFSListSnapshots(String fileSystemName) throws IOException {
        return new ZFSListSnapshots(fileSystemName, processWrapperFactory);
    }


}
