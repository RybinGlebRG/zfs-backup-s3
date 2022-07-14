package ru.rerumu.backups.factories.impl;

import ru.rerumu.backups.factories.S3ClientFactory;
import ru.rerumu.backups.models.S3Storage;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.services.s3.S3Client;

public class S3ClientFactoryImpl implements S3ClientFactory {
    @Override
    public S3Client getS3Client(S3Storage s3Storage) {
        S3Client s3Client = S3Client.builder()
                .region(s3Storage.getRegion())
                .endpointOverride(s3Storage.getEndpoint())
                .credentialsProvider(StaticCredentialsProvider.create(s3Storage.getCredentials()))
                .build();
        return s3Client;
    }
}
