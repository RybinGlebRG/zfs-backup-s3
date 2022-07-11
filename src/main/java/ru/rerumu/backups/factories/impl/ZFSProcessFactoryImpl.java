package ru.rerumu.backups.factories.impl;

import ru.rerumu.backups.factories.ProcessWrapperFactory;
import ru.rerumu.backups.models.Snapshot;
import ru.rerumu.backups.models.ZFSPool;
import ru.rerumu.backups.factories.ZFSProcessFactory;
import ru.rerumu.backups.zfs_api.*;
import ru.rerumu.backups.zfs_api.impl.*;

import java.io.IOException;

public class ZFSProcessFactoryImpl implements ZFSProcessFactory {
    private final boolean isMultiIncremental;
    private final boolean isNativeEncrypted;
    private final ProcessWrapperFactory processWrapperFactory;

    public ZFSProcessFactoryImpl(
            boolean isMultiIncremental,
            boolean isNativeEncrypted,
            ProcessWrapperFactory processWrapperFactory){
        this.isMultiIncremental = isMultiIncremental;
        this.isNativeEncrypted = isNativeEncrypted;
        this.processWrapperFactory = processWrapperFactory;
    }

    @Override
    public ZFSSend getZFSSendFull(Snapshot snapshot) throws IOException {
        if (isNativeEncrypted){
            return new ZFSSendFullEncrypted(snapshot, processWrapperFactory);
        } else {
            return new ZFSSendFull(snapshot, processWrapperFactory);
        }
    }

    @Override
    public ZFSGetDatasetProperty getZFSGetDatasetProperty(String datasetName, String propertyName) throws IOException {
        return new ZFSGetDatasetPropertyImpl(propertyName, datasetName,processWrapperFactory);
    }

    @Override
    public ZFSSend getZFSSendIncremental(Snapshot baseSnapshot, Snapshot incrementalSnapshot) throws IOException {
        if (isMultiIncremental){
            if (isNativeEncrypted) {
                return new ZFSSendMultiIncrementalEncrypted(baseSnapshot,incrementalSnapshot, processWrapperFactory);
            } else {
                return new ZFSSendMultiIncremental(baseSnapshot, incrementalSnapshot, processWrapperFactory);
            }
        } else {
            if (isNativeEncrypted){
                throw new IllegalArgumentException();
            } else {
                return new ZFSSendIncremental(baseSnapshot, incrementalSnapshot, processWrapperFactory);
            }
        }
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
