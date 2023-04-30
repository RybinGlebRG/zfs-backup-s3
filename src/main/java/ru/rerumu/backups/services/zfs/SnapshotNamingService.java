package ru.rerumu.backups.services.zfs;

import java.time.LocalDateTime;

public interface SnapshotNamingService {

    String generateName();
    String generateName(LocalDateTime dateTime);

    LocalDateTime extractTime(String snapshotName);
}
