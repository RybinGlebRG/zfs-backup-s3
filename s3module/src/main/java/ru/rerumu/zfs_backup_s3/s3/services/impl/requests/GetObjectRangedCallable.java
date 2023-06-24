package ru.rerumu.zfs_backup_s3.s3.services.impl.requests;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.rerumu.zfs_backup_s3.utils.ByteArray;
import ru.rerumu.zfs_backup_s3.utils.CallableOnlyOnce;
import ru.rerumu.zfs_backup_s3.utils.MD5;
import ru.rerumu.zfs_backup_s3.utils.ThreadSafe;
import software.amazon.awssdk.core.ResponseBytes;
import software.amazon.awssdk.core.sync.ResponseTransformer;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.HexFormat;
import java.util.Objects;
import java.util.concurrent.Callable;

@ThreadSafe
public final class GetObjectRangedCallable extends CallableOnlyOnce<byte[]> {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private final String key;
    private final String bucketName;

    private final Long startInclusive;
    private final Long endExclusive;
    private final S3Client s3Client;
    private final Path targetPath;

    public GetObjectRangedCallable(
            @NonNull String key,
            @NonNull String bucketName,
            @NonNull Long startInclusive,
            @NonNull Long endExclusive,
            @NonNull S3Client s3Client,
            @NonNull Path targetPath
    ) {
        Objects.requireNonNull(key);
        Objects.requireNonNull(bucketName);
        Objects.requireNonNull(startInclusive);
        Objects.requireNonNull(endExclusive);
        Objects.requireNonNull(s3Client);
        Objects.requireNonNull(targetPath);
        this.key = key;
        this.bucketName = bucketName;
        this.startInclusive = startInclusive;
        this.endExclusive = endExclusive;
        this.s3Client = s3Client;
        this.targetPath = targetPath;
    }

    @Override
    protected byte[] callOnce() throws Exception {
        logger.debug(String.format("Loading range from '%d' to '%d'", startInclusive, endExclusive-1));
        GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                .range(String.format("bytes=%d-%d", startInclusive, endExclusive-1))
                .key(key)
                .bucket(bucketName)
                .build();
        ResponseBytes<GetObjectResponse> response = s3Client.getObject(getObjectRequest, ResponseTransformer.toBytes());
        byte[] responseBytes = response.asByteArray();
        Files.write(
                targetPath,
                responseBytes,
                StandardOpenOption.CREATE,
                StandardOpenOption.APPEND,
                StandardOpenOption.WRITE
        );

        String bytesHex = HexFormat.of().formatHex(responseBytes);
        logger.trace(String.format(
                "Downloaded %d bytes:\n%s...%s",
                responseBytes.length,
                bytesHex.substring(0,50),
                bytesHex.substring(bytesHex.length()-50)
        ));

        byte[] md5 = MD5.getMD5Bytes(new ByteArray(responseBytes));
        logger.debug(String.format("Downloaded MD5 = '%s'",MD5.getMD5Hex(new ByteArray(responseBytes))));
        logger.debug("Finished loading range");

        return md5;
    }
}
