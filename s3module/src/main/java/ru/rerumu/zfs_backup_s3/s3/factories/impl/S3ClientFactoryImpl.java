package ru.rerumu.zfs_backup_s3.s3.factories.impl;

import org.checkerframework.checker.nullness.qual.NonNull;
import ru.rerumu.zfs_backup_s3.s3.factories.S3ClientFactory;
import ru.rerumu.zfs_backup_s3.s3.models.S3Storage;
import ru.rerumu.zfs_backup_s3.utils.ImmutableList;
import ru.rerumu.zfs_backup_s3.utils.NotThreadSafe;
import ru.rerumu.zfs_backup_s3.utils.ThreadSafe;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.services.s3.S3Client;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

@ThreadSafe
public final class S3ClientFactoryImpl implements S3ClientFactory {
    private final Map<S3Storage,S3Client> map = new ConcurrentHashMap<>();

    public S3ClientFactoryImpl(@NonNull ImmutableList<S3Storage> storageList) {
        Objects.requireNonNull(storageList);
        for (S3Storage s3Storage: storageList.list()){
            S3Client s3Client = S3Client.builder()
                    .region(s3Storage.getRegion())
                    .endpointOverride(s3Storage.getEndpoint())
                    .credentialsProvider(StaticCredentialsProvider.create(s3Storage.getCredentials()))
                    .forcePathStyle(true)
                    .build();
            map.put(s3Storage,s3Client);
        }

    }

    @Override
    public S3Client getS3Client(S3Storage s3Storage) {
        return map.get(s3Storage);
    }
}
