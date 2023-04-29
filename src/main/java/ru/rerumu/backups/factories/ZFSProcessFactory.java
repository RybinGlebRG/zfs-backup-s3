package ru.rerumu.backups.factories;

import ru.rerumu.backups.models.Snapshot;
import ru.rerumu.backups.models.ZFSPool;
import ru.rerumu.backups.models.zfs.Pool;
import ru.rerumu.backups.zfs_api.zfs.*;

import java.io.IOException;

public interface ZFSProcessFactory {
    ZFSListSnapshots getZFSListSnapshots(String fileSystemName) throws IOException;
    ZFSListFilesystems getZFSListFilesystems(String parentFileSystem) throws IOException;
    @Deprecated
    ZFSReceive getZFSReceive(ZFSPool zfsPool) throws IOException;
    ZFSReceive getZFSReceive(Pool pool) throws IOException;
    ZFSSend getZFSSendIncremental(Snapshot baseSnapshot, Snapshot incrementalSnapshot) throws IOException;
//    ZFSSend getZFSSendMultiIncremental(Snapshot baseSnapshot, Snapshot incrementalSnapshot) throws IOException;
    ZFSSend getZFSSendFull(Snapshot snapshot) throws IOException;
    ZFSSend getZFSSendReplicate(Snapshot snapshot) throws IOException;

    ZFSGetDatasetProperty getZFSGetDatasetProperty(String datasetName, String propertyName) throws IOException;
}
