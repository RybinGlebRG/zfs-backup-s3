package ru.rerumu.backups.services.impl;

import ru.rerumu.backups.services.SnapshotNamingService;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class SnapshotNamingServiceImpl implements SnapshotNamingService {
    @Override
    public String getNameWithCurrentTime(String prefix) {
        String tmp = prefix + LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        if (!tmp.matches("^[a-zA-Z0-9:-]*$")){
            throw new IllegalArgumentException("Illegal characters");
        }
        return tmp;
    }
}
