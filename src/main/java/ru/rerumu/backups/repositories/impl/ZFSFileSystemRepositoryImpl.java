package ru.rerumu.backups.repositories.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.rerumu.backups.models.Snapshot;
import ru.rerumu.backups.models.ZFSFileSystem;
import ru.rerumu.backups.repositories.ZFSFileSystemRepository;
import ru.rerumu.backups.repositories.ZFSSnapshotRepository;
import ru.rerumu.backups.services.ZFSProcessFactory;
import ru.rerumu.backups.services.impl.ZFSProcessFactoryImpl;
import ru.rerumu.backups.zfs_api.ProcessWrapper;
import ru.rerumu.backups.zfs_api.ZFSListFilesystems;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ZFSFileSystemRepositoryImpl implements ZFSFileSystemRepository {

    private final Logger logger = LoggerFactory.getLogger(ZFSFileSystemRepositoryImpl.class);
    private final ZFSProcessFactory zfsProcessFactory;
    private final ZFSSnapshotRepository zfsSnapshotRepository;

    public ZFSFileSystemRepositoryImpl(ZFSProcessFactory zfsProcessFactory,
                                       ZFSSnapshotRepository zfsSnapshotRepository){
        this.zfsProcessFactory = zfsProcessFactory;
        this.zfsSnapshotRepository = zfsSnapshotRepository;
    }

    @Override
    public List<ZFSFileSystem> getFilesystemsTreeList(String fileSystemName) throws IOException, InterruptedException {
        ProcessWrapper zfsListFilesystems = zfsProcessFactory.getZFSListFilesystems(fileSystemName);
        byte[] buf = zfsListFilesystems.getBufferedInputStream().readAllBytes();
        zfsListFilesystems.close();

        String str = new String(buf, StandardCharsets.UTF_8);
        logger.debug(String.format("Got filesystems: \n%s",str));
        String[] lines = str.split("\\n");

        List<ZFSFileSystem> zfsFileSystemList = new ArrayList<>();

        // Already sorted
        for (String line: lines){
            logger.debug(String.format("Getting snapshots for filesystem '%s'",line));
            List<Snapshot> snapshotList = zfsSnapshotRepository.getAllSnapshotsOrdered(line);
            logger.debug(String.format("Got snapshots: \n%s",snapshotList));
            zfsFileSystemList.add(new ZFSFileSystem(line,snapshotList));
        }

        return zfsFileSystemList;
    }
}
