package ru.rerumu.backups.services.s3.factories;

import ru.rerumu.backups.services.s3.models.S3Storage;
import software.amazon.awssdk.services.s3.S3Client;

public interface S3ClientFactory {

    S3Client getS3Client(S3Storage s3Storage);
}
