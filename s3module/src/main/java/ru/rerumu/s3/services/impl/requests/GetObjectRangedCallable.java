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
import java.util.concurrent.Callable;

public class GetObjectRangedCallable implements Callable<byte[]> {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private final String key;
    private final String bucketName;
    private final Long start;
    private final Long finish;
    private final S3Client s3Client;
    private final Path targetPath;

    public GetObjectRangedCallable(String key, String bucketName, Long start, Long finish, S3Client s3Client, Path targetPath) {
        this.key = key;
        this.bucketName = bucketName;
        this.start = start;
        this.finish = finish;
        this.s3Client = s3Client;
        this.targetPath = targetPath;
    }

    @Override
    public byte[] call() throws Exception {
        logger.debug(String.format("Loading range from '%d' to '%d'", start, finish));
        GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                .range(String.format("bytes=%d-%d", start, finish))
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
        byte[] md5 = MD5.getMD5Bytes(responseBytes);
        logger.debug(String.format("Downloaded MD5 = '%s'",MD5.getMD5Hex(responseBytes)));
        logger.debug("Finished loading range");

        return md5;
    }
}
