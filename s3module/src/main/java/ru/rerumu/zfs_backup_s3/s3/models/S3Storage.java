package ru.rerumu.zfs_backup_s3.s3.models;

import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.regions.Region;

import java.net.URI;
import java.nio.file.Path;

public class S3Storage {
    private final Region region;
    private final String bucketName;
    private final AwsBasicCredentials credentials;
    private final Path prefix;
    private final URI endpoint;
    private final String storageClass;

    public S3Storage(Region region, String bucketName, String keyId, String secretKey, Path prefix, URI endpoint,
                     String storageClass){
        this.region = region;
        this.bucketName = bucketName;
        this.credentials = AwsBasicCredentials.create(keyId,secretKey);
        this.prefix = prefix;
        this.endpoint = endpoint;
        this.storageClass = storageClass;
    }

    public AwsBasicCredentials getCredentials() {
        return credentials;
    }

    public Region getRegion() {
        return region;
    }

    public String getBucketName() {
        return bucketName;
    }

    public Path getPrefix() {
        return prefix;
    }

    public URI getEndpoint() {
        return endpoint;
    }

    public String getStorageClass() {
        return storageClass;
    }
}
