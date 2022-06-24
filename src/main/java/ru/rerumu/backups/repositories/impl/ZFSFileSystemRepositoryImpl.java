package ru.rerumu.backups.repositories.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.rerumu.backups.models.Snapshot;
import ru.rerumu.backups.models.ZFSDataset;
import ru.rerumu.backups.models.zfs_dataset_properties.CompressionProperty;
import ru.rerumu.backups.models.zfs_dataset_properties.EncryptionProperty;
import ru.rerumu.backups.repositories.ZFSFileSystemRepository;
import ru.rerumu.backups.repositories.ZFSSnapshotRepository;
import ru.rerumu.backups.factories.ZFSProcessFactory;
import ru.rerumu.backups.zfs_api.ProcessWrapper;
import ru.rerumu.backups.zfs_api.ZFSGetDatasetProperty;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

public class ZFSFileSystemRepositoryImpl implements ZFSFileSystemRepository {

    private final Logger logger = LoggerFactory.getLogger(ZFSFileSystemRepositoryImpl.class);
    private final ZFSProcessFactory zfsProcessFactory;
    private final ZFSSnapshotRepository zfsSnapshotRepository;

    public ZFSFileSystemRepositoryImpl(ZFSProcessFactory zfsProcessFactory,
                                       ZFSSnapshotRepository zfsSnapshotRepository){
        this.zfsProcessFactory = zfsProcessFactory;
        this.zfsSnapshotRepository = zfsSnapshotRepository;
    }

    private String getProperty(String propertyName, String datasetName)
            throws IOException,
            ExecutionException,
            InterruptedException {
        ZFSGetDatasetProperty process = zfsProcessFactory.getZFSGetDatasetProperty(datasetName,propertyName);
        byte[] buf = process.getBufferedInputStream().readAllBytes();
        process.close();

        return (new String(buf, StandardCharsets.UTF_8)).strip();
    }

    private EncryptionProperty getEncryption(String datasetName)
            throws IOException, ExecutionException, InterruptedException {
        String tmp = getProperty("encryption",datasetName);
        if (tmp.equals("on")){
            return EncryptionProperty.ON;
        } else if (tmp.equals("off")){
            return EncryptionProperty.OFF;
        } else {
            throw new IllegalArgumentException();
        }
    }

    private CompressionProperty getCompression(String datasetName)
            throws IOException, ExecutionException, InterruptedException {
        String tmp = getProperty("compression",datasetName);
        if (tmp.equals("lz4")){
            return CompressionProperty.LZ4;
        } else {
            throw new IllegalArgumentException();
        }
    }

    @Override
    public List<ZFSDataset> getFilesystemsTreeList(String fileSystemName) throws IOException, InterruptedException, ExecutionException {
        ProcessWrapper zfsListFilesystems = zfsProcessFactory.getZFSListFilesystems(fileSystemName);
        byte[] buf = zfsListFilesystems.getBufferedInputStream().readAllBytes();
        zfsListFilesystems.close();

        String str = new String(buf, StandardCharsets.UTF_8);
        logger.debug(String.format("Got filesystems: \n%s",str));
        String[] lines = str.split("\\n");

        List<ZFSDataset> zfsDatasetList = new ArrayList<>();

        // Already sorted
        for (String line: lines){
            line = line.strip();
            logger.debug(String.format("Getting snapshots for filesystem '%s'",line));
            List<Snapshot> snapshotList = zfsSnapshotRepository.getAllSnapshotsOrdered(line);
            logger.debug(String.format("Got snapshots: \n%s",snapshotList));
            ZFSDataset zfsDataset = new ZFSDataset(line,snapshotList,getEncryption(line));
            zfsDatasetList.add(zfsDataset);
        }

        return zfsDatasetList;
    }
}
