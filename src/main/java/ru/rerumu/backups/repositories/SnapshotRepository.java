package ru.rerumu.backups.repositories;

import ru.rerumu.backups.models.Snapshot;
import java.io.IOException;

public class SnapshotRepository {

    private final Snapshot lastFullSnapshot;

    public SnapshotRepository(Snapshot lastFullSnapshot) {
        this.lastFullSnapshot = lastFullSnapshot;
    }

    public Snapshot getLastFullSnapshot(){
        return lastFullSnapshot;
    }

}
