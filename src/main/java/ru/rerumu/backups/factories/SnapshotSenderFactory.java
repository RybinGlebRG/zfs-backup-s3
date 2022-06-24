package ru.rerumu.backups.factories;

import ru.rerumu.backups.services.SnapshotSender;

public interface SnapshotSenderFactory {
    SnapshotSender getSnapshotSender();
}
