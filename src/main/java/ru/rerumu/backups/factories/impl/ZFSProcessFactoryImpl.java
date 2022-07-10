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
            return new ZFSSendFullEncrypted(snapshot);
        } else {
            return new ZFSSendFull(snapshot);
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
                return new ZFSSendMultiIncrementalEncrypted(baseSnapshot,incrementalSnapshot);
            } else {
                return new ZFSSendMultiIncremental(baseSnapshot, incrementalSnapshot);
            }
        } else {
            if (isNativeEncrypted){
                throw new IllegalArgumentException();
            } else {
                return new ZFSSendIncremental(baseSnapshot, incrementalSnapshot);
            }
        }
    }

    @Override
    public ZFSReceive getZFSReceive(ZFSPool zfsPool) throws IOException {
        return new ZFSReceiveImpl(zfsPool.getName(), processWrapperFactory);
    }

    @Override
    public ProcessWrapper getZFSListFilesystems(String parentFileSystem) throws IOException {
        return new ZFSListFilesystems(parentFileSystem);
    }

    @Override
    public ProcessWrapper getZFSListSnapshots(String fileSystemName) throws IOException {
        return new ZFSListSnapshots(fileSystemName);
    }


}
