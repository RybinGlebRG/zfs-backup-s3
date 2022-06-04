package ru.rerumu.backups.services.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.rerumu.backups.exceptions.BaseSnapshotNotFoundException;
import ru.rerumu.backups.exceptions.SnapshotNotFoundException;
import ru.rerumu.backups.models.Snapshot;
import ru.rerumu.backups.models.ZFSFileSystem;
import ru.rerumu.backups.services.SnapshotPicker;

import java.util.ArrayList;
import java.util.List;

public class SnapshotPickerImpl implements SnapshotPicker {
    private final Logger logger = LoggerFactory.getLogger(SnapshotPickerImpl.class);

    @Override
    public List<Snapshot> pick(ZFSFileSystem zfsFileSystem, String snapshotName) throws BaseSnapshotNotFoundException, SnapshotNotFoundException {
        List<Snapshot> res = new ArrayList<>();
        if (!zfsFileSystem.isSnapshotExists(snapshotName)){
            logger.warn(String.format("No acceptable snapshots for dataset '%s'", zfsFileSystem.getName()));
            throw new SnapshotNotFoundException();
        }
        res.add(zfsFileSystem.getBaseSnapshot());
        logger.info(String.format("Found base snapshot '%s'",res.get(0).getFullName()));

        try {
            res.addAll(zfsFileSystem.getIncrementalSnapshots(snapshotName));
        } catch (SnapshotNotFoundException e) {
            logger.warn(String.format("No acceptable incremental snapshots for filesystem '%s'", zfsFileSystem.getName()));
        }

        logger.info(String.format("Picked snapshots '%s'",res));

        return res;
    }
}
