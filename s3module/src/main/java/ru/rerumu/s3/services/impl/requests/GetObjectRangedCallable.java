package ru.rerumu.s3.services.impl.requests;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.rerumu.utils.MD5;
import software.amazon.awssdk.core.ResponseBytes;
import software.amazon.awssdk.core.sync.ResponseTransformer;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.HexFormat;
import java.util.concurrent.Callable;

public class GetObjectRangedCallable implements Callable<byte[]> {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private final String key;
    private final String bucketName;

    private final Long startInclusive;
    private final Long endExclusive;
    private final S3Client s3Client;
    private final Path targetPath;

    public GetObjectRangedCallable(String key, String bucketName, Long startInclusive, Long endExclusive, S3Client s3Client, Path targetPath) {
        this.key = key;
        this.bucketName = bucketName;
        this.startInclusive = startInclusive;
        this.endExclusive = endExclusive;
        this.s3Client = s3Client;
        this.targetPath = targetPath;
    }

    @Override
    public byte[] call() throws Exception {
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

        byte[] md5 = MD5.getMD5Bytes(responseBytes);
        logger.debug(String.format("Downloaded MD5 = '%s'",MD5.getMD5Hex(responseBytes)));
        logger.debug("Finished loading range");

        return md5;
    }
}
