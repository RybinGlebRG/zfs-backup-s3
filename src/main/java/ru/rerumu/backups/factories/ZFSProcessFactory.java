package ru.rerumu.backups.factories;

import ru.rerumu.backups.models.Snapshot;
import ru.rerumu.backups.models.ZFSPool;
import ru.rerumu.backups.zfs_api.ProcessWrapper;
import ru.rerumu.backups.zfs_api.ZFSGetDatasetProperty;
import ru.rerumu.backups.zfs_api.ZFSReceive;
import ru.rerumu.backups.zfs_api.ZFSSend;

import java.io.IOException;

public interface ZFSProcessFactory {
    ProcessWrapper getZFSListSnapshots(String fileSystemName) throws IOException;
    ProcessWrapper getZFSListFilesystems(String parentFileSystem) throws IOException;
    ZFSReceive getZFSReceive(ZFSPool zfsPool) throws IOException;
    ZFSSend getZFSSendIncremental(Snapshot baseSnapshot, Snapshot incrementalSnapshot) throws IOException;
//    ZFSSend getZFSSendMultiIncremental(Snapshot baseSnapshot, Snapshot incrementalSnapshot) throws IOException;
    ZFSSend getZFSSendFull(Snapshot snapshot) throws IOException;

    ZFSGetDatasetProperty getZFSGetDatasetProperty(String datasetName, String propertyName) throws IOException;
}
