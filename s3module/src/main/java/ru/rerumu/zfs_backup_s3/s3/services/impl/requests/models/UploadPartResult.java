package ru.rerumu.zfs_backup_s3.s3.services.impl.requests.models;

import ru.rerumu.zfs_backup_s3.utils.ByteArray;
import ru.rerumu.zfs_backup_s3.utils.ThreadSafe;
import software.amazon.awssdk.services.s3.model.CompletedPart;

@ThreadSafe
public record UploadPartResult(ByteArray md5, CompletedPart completedPart) {
}
