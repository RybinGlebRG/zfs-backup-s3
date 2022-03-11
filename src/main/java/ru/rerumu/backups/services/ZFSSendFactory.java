package ru.rerumu.backups.services;

import ru.rerumu.backups.models.Snapshot;
import ru.rerumu.backups.repositories.SnapshotRepository;
import ru.rerumu.backups.services.impl.ZFSSendFull;

import java.io.IOException;

public class ZFSSendFactory {

    public ZFSSend getZFSSendFull(String fullSnapshot) throws IOException {
        Snapshot lastFullSnapshot = new SnapshotRepository(new Snapshot(fullSnapshot)).getLastFullSnapshot();
        return new ZFSSendFull(lastFullSnapshot);
    }
}
