package ru.rerumu.zfs_backup_s3.s3.factories;

import ru.rerumu.zfs_backup_s3.s3.models.S3Storage;
import software.amazon.awssdk.services.s3.S3Client;

public interface S3ClientFactory {

    S3Client getS3Client(S3Storage s3Storage);
}
