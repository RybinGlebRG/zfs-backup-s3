package ru.rerumu.backups.services.impl;

import ru.rerumu.backups.services.SnapshotNamingService;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class SnapshotNamingServiceImpl implements SnapshotNamingService {
    private final static String SNAPSHOT_PREFIX="zfs-backup-s3";

    private void validate(String name){
        if (!name.matches("^[a-zA-Z0-9:_-]*$")){
            throw new IllegalArgumentException(String.format("Illegal characters in name '%s'",name));
        }
    }
    @Override
    public String generateName() {
        String tmp = SNAPSHOT_PREFIX+"__" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HHmmss"));
        validate(tmp);
        return tmp;
    }

    @Override
    public String generateName(LocalDateTime dateTime) {
        String tmp = SNAPSHOT_PREFIX+"__" +dateTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HHmmss"));
        validate(tmp);
        return tmp;
    }

    @Override
    public LocalDateTime extractTime(String snapshotName) {
        String timeStr = snapshotName.substring(SNAPSHOT_PREFIX.length()+2);
        LocalDateTime res = LocalDateTime.parse(timeStr,DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HHmmss"));
        return res;
    }
}
