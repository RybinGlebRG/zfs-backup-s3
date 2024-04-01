package ru.rerumu.zfs_backup_s3.backups.services;

import ru.rerumu.zfs_backup_s3.backups.services.impl.SnapshotNamingService4Mock;
import ru.rerumu.zfs_backup_s3.backups.services.impl.SnapshotNamingServiceImpl;
import ru.rerumu.zfs_backup_s3.utils.ThreadSafe;

import java.time.LocalDateTime;

@ThreadSafe
public sealed interface SnapshotNamingService permits SnapshotNamingService4Mock, SnapshotNamingServiceImpl {

    String generateName();
    String generateName(LocalDateTime dateTime);

    LocalDateTime extractTime(String snapshotName);
}
