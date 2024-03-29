package ru.rerumu.zfs_backup_s3.s3.services.impl.requests.models;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import ru.rerumu.zfs_backup_s3.utils.ThreadSafe;

import java.util.Objects;

@ThreadSafe
public record Range(@NonNull Long start, @NonNull Long end) {

    public Range {
        Objects.requireNonNull(start);
        Objects.requireNonNull(end);
    }
}
