package ru.rerumu.zfs_backup_s3.zfs.models;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import org.checkerframework.checker.nullness.qual.NonNull;;

public record Dataset(@NonNull String name, @NonNull List<Snapshot> snapshotList) {

    public Dataset{
        Objects.requireNonNull(name,"Dataset name cannot be null");
        Objects.requireNonNull(snapshotList,"Snapshot list itself cannot be null, but can be empty");
        snapshotList = new ArrayList<>(snapshotList);
    }
}
