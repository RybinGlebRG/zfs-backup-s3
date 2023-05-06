package ru.rerumu.zfs.models;

import java.util.ArrayList;
import java.util.List;

public record Dataset(String name, List<Snapshot> snapshotList) {

    public Dataset{
        snapshotList = new ArrayList<>(snapshotList);
    }
}
