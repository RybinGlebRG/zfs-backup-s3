package ru.rerumu.zfs_backup_s3.s3.services.impl.requests.models;

import software.amazon.awssdk.services.s3.model.CompletedPart;

// TODO: Check thread safe
public record UploadPartResult(byte[] md5, CompletedPart completedPart) {
}
