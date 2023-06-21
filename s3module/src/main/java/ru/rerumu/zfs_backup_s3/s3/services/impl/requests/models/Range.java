package ru.rerumu.zfs_backup_s3.s3.services.impl.requests.models;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.Objects;

// TODO: Check thread safe
public record Range(@NonNull Long start, @NonNull Long end) {

    public Range {
        Objects.requireNonNull(start);
        Objects.requireNonNull(end);
    }
}
