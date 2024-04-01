package ru.rerumu.zfs_backup_s3.backups.services.impl;

import ru.rerumu.zfs_backup_s3.backups.services.SnapshotNamingService;

import java.time.LocalDateTime;

public final class SnapshotNamingService4Mock implements SnapshotNamingService {
    @Override
    public String generateName() {
        return null;
    }

    @Override
    public String generateName(LocalDateTime dateTime) {
        return null;
    }

    @Override
    public LocalDateTime extractTime(String snapshotName) {
        return null;
    }
}
