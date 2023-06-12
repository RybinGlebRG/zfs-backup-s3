package ru.rerumu.zfs_backup_s3.backups.services;

import java.time.LocalDateTime;

public interface SnapshotNamingService {

    String generateName();
    String generateName(LocalDateTime dateTime);

    LocalDateTime extractTime(String snapshotName);
}
