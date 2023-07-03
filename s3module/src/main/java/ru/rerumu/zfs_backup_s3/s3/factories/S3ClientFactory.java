package ru.rerumu.zfs_backup_s3.s3.factories;

import ru.rerumu.zfs_backup_s3.s3.factories.impl.S3ClientFactoryImpl;
import ru.rerumu.zfs_backup_s3.s3.models.S3Storage;
import ru.rerumu.zfs_backup_s3.utils.NotThreadSafe;
import software.amazon.awssdk.services.s3.S3Client;

@NotThreadSafe
public sealed interface S3ClientFactory permits S3ClientFactoryImpl {

    S3Client getS3Client(S3Storage s3Storage);
}
