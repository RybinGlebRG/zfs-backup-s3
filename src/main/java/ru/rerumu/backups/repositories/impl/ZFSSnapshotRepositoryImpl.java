package ru.rerumu.backups.repositories.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.rerumu.backups.models.Snapshot;
import ru.rerumu.backups.models.ZFSFileSystem;
import ru.rerumu.backups.repositories.ZFSSnapshotRepository;
import ru.rerumu.backups.services.ZFSProcessFactory;
import ru.rerumu.backups.services.impl.ZFSProcessFactoryImpl;
import ru.rerumu.backups.zfs_api.ProcessWrapper;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ZFSSnapshotRepositoryImpl implements ZFSSnapshotRepository {

    private final Logger logger = LoggerFactory.getLogger(ZFSFileSystemRepositoryImpl.class);
    private final ZFSProcessFactory zfsProcessFactory;

    public ZFSSnapshotRepositoryImpl(ZFSProcessFactory zfsProcessFactory){
        this.zfsProcessFactory = zfsProcessFactory;
    }

    @Override
    public List<Snapshot> getAllSnapshotsOrdered(ZFSFileSystem zfsFileSystem) throws IOException, InterruptedException {
        ProcessWrapper zfsListSnapshots = zfsProcessFactory.getZFSListSnapshots(zfsFileSystem);
        byte[] buf = zfsListSnapshots.getBufferedInputStream().readAllBytes();
        zfsListSnapshots.close();

        String str = Arrays.toString(buf);
        String[] lines = str.split("\\n");

        List<Snapshot> snapshotList = new ArrayList<>();

        for (String line : lines){
            Snapshot snapshot = new Snapshot(line);
            if (snapshot.getDataset().equals(zfsFileSystem.getName())) {
                snapshotList.add(snapshot);
            }
        }

        return snapshotList;
    }

    @Override
    public Snapshot getBaseSnapshot(ZFSFileSystem zfsFileSystem) {
        return null;
    }

}
