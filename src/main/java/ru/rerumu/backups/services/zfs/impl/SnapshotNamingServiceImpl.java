package ru.rerumu.backups.services.zfs.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.rerumu.backups.services.zfs.SnapshotNamingService;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class SnapshotNamingServiceImpl implements SnapshotNamingService {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final static String SNAPSHOT_PREFIX="zfs-backup-s3";

    private String formatDate(LocalDateTime localDateTime){
        return localDateTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HHmmss"));
    }

    @Override
    public String generateName() {
        String tmp = SNAPSHOT_PREFIX+"__" + formatDate(LocalDateTime.now());
        return tmp;
    }

    @Override
    public String generateName(LocalDateTime dateTime) {
        String tmp = SNAPSHOT_PREFIX+"__" +formatDate(dateTime);
        return tmp;
    }

    // TODO: Pass Snapshot?
    @Override
    public LocalDateTime extractTime(String snapshotName) {
        logger.debug(String.format("Extracting from string '%s'",snapshotName));
        String timeStr = snapshotName.substring(SNAPSHOT_PREFIX.length()+2);
        logger.debug(String.format("Time string = '%s'",timeStr));
        LocalDateTime res = LocalDateTime.parse(timeStr,DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HHmmss"));
        return res;
    }
}
