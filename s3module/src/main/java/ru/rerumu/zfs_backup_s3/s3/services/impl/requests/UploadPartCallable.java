package ru.rerumu.zfs_backup_s3.s3.services.impl.requests;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.rerumu.zfs_backup_s3.s3.exceptions.IncorrectHashException;
import ru.rerumu.zfs_backup_s3.s3.services.impl.requests.models.UploadPartResult;
import ru.rerumu.zfs_backup_s3.utils.MD5;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.CompletedPart;
import software.amazon.awssdk.services.s3.model.UploadPartRequest;
import software.amazon.awssdk.services.s3.model.UploadPartResponse;

import java.util.HexFormat;
import java.util.Objects;
import java.util.concurrent.Callable;

public class UploadPartCallable implements Callable<UploadPartResult> {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final String bucketName;
    private final String key;
    private final S3Client s3Client;
    private final String uploadId;
    private final Integer partNumber;
    private final byte[] data;

    public UploadPartCallable(String bucketName, String key, S3Client s3Client, String uploadId, Integer partNumber, byte[] data) {
        Objects.requireNonNull(data);
        Objects.requireNonNull(partNumber);
        this.bucketName = bucketName;
        this.key = key;
        this.s3Client = s3Client;
        this.uploadId = uploadId;
        this.partNumber = partNumber;
        this.data = data;
    }

    @Override
    public UploadPartResult call() throws Exception {
        String md5 = MD5.getMD5Hex(data);

        UploadPartRequest uploadPartRequest = UploadPartRequest.builder()
                .bucket(bucketName)
                .key(key)
                .uploadId(uploadId)
                .partNumber(partNumber).build();

        UploadPartResponse response = s3Client.uploadPart(
                uploadPartRequest, RequestBody.fromBytes(data)
        );

        String bytesHex = HexFormat.of().formatHex(data);
        logger.trace(String.format(
                "Uploaded %d bytes:\n%s...%s",
                data.length,
                bytesHex.substring(0,50),
                bytesHex.substring(bytesHex.length()-50)
        ));

        String eTag = response.eTag();

        logger.info(String.format("ETag='%s'", eTag));
        if (!(eTag.equals('"' + md5 + '"'))) {
            throw new IncorrectHashException(String.format("Got '%s', but expected '%s'", eTag, '"' + md5 + '"'));
        }

        byte[] md5bytes = MD5.getMD5Bytes(data);
        CompletedPart completedPart = CompletedPart.builder()
                .partNumber(partNumber)
                .eTag(eTag)
                .build();

        return new UploadPartResult(md5bytes,completedPart);
    }
}
