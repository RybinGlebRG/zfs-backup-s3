package ru.rerumu.backups.services;

public interface SnapshotNamingService {

    String getNameWithCurrentTime(String prefix);
}
