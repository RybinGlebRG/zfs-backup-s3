package ru.rerumu.backups.models.zfs;

import ru.rerumu.backups.models.Snapshot;

import java.util.ArrayList;
import java.util.List;

public record Dataset(String name, List<Snapshot> snapshotList) {

    public Dataset{
        snapshotList = new ArrayList<>(snapshotList);
    }
}
