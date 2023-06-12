package ru.rerumu.zfs_backup_s3.zfs.models;

import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public record Pool(@NonNull String name, @NonNull List<@NonNull Dataset> datasetList) {

    public Pool{
        Objects.requireNonNull(name,"Pool name cannot be null");
        Objects.requireNonNull(datasetList,"Dataset list itself cannot be null, but can be empty");
        datasetList.forEach(item->Objects.requireNonNull(item,"Dataset cannot be null"));

        datasetList = new ArrayList<>(datasetList);
    }

    public Optional<Dataset> getRootDataset(){
        return datasetList.stream().findFirst();
    }
}
