package ru.rerumu.backups.repositories.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.rerumu.backups.models.Snapshot;
import ru.rerumu.backups.repositories.ZFSSnapshotRepository;
import ru.rerumu.backups.factories.ZFSProcessFactory;
import ru.rerumu.backups.zfs_api.ProcessWrapper;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

public class ZFSSnapshotRepositoryImpl implements ZFSSnapshotRepository {

    private final Logger logger = LoggerFactory.getLogger(ZFSFileSystemRepositoryImpl.class);
    private final ZFSProcessFactory zfsProcessFactory;

    public ZFSSnapshotRepositoryImpl(ZFSProcessFactory zfsProcessFactory){
        this.zfsProcessFactory = zfsProcessFactory;
    }

    @Override
    public List<Snapshot> getAllSnapshotsOrdered(String fileSystemName) throws IOException, InterruptedException, ExecutionException {
        ProcessWrapper zfsListSnapshots = zfsProcessFactory.getZFSListSnapshots(fileSystemName);
        byte[] buf = zfsListSnapshots.getBufferedInputStream().readAllBytes();
        zfsListSnapshots.close();

//        String str = Arrays.toString(buf);
        String str = new String(buf, StandardCharsets.UTF_8);
        String[] lines = str.split("\\n");

        List<Snapshot> snapshotList = new ArrayList<>();

        for (String line : lines){
            Snapshot snapshot = new Snapshot(line);
            if (snapshot.getDataset().equals(fileSystemName)) {
                snapshotList.add(snapshot);
            }
        }

        return snapshotList;
    }
}
