package ru.rerumu.zfs_backup_s3.s3.models;

import ru.rerumu.zfs_backup_s3.utils.ThreadSafe;

@ThreadSafe
public record Bucket(String name) {
}
