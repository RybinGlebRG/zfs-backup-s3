package ru.rerumu.zfs_backup_s3.s3.services.impl.requests;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.rerumu.zfs_backup_s3.s3.exceptions.IncorrectHashException;
import ru.rerumu.zfs_backup_s3.s3.services.impl.requests.models.UploadPartResult;
import ru.rerumu.zfs_backup_s3.utils.ByteArray;
import ru.rerumu.zfs_backup_s3.utils.CallableOnlyOnce;
import ru.rerumu.zfs_backup_s3.utils.MD5;
import ru.rerumu.zfs_backup_s3.utils.ThreadSafe;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.CompletedPart;
import software.amazon.awssdk.services.s3.model.UploadPartRequest;
import software.amazon.awssdk.services.s3.model.UploadPartResponse;

import java.util.HexFormat;
import java.util.Objects;
import java.util.concurrent.Callable;

@ThreadSafe
public final class UploadPartCallable extends CallableOnlyOnce<UploadPartResult> {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final String bucketName;
    private final String key;
    private final S3Client s3Client;
    private final String uploadId;
    private final Integer partNumber;
    private final ByteArray data;

    public UploadPartCallable(
            @NonNull String bucketName,
            @NonNull String key,
            @NonNull S3Client s3Client,
            @NonNull String uploadId,
            @NonNull Integer partNumber,
            @NonNull ByteArray data) {
        Objects.requireNonNull(bucketName);
        Objects.requireNonNull(key);
        Objects.requireNonNull(s3Client);
        Objects.requireNonNull(uploadId);
        Objects.requireNonNull(partNumber);
        Objects.requireNonNull(data);
        this.bucketName = bucketName;
        this.key = key;
        this.s3Client = s3Client;
        this.uploadId = uploadId;
        this.partNumber = partNumber;
        this.data = data;
    }

    @Override
    protected UploadPartResult callOnce() throws Exception {
        logger.debug("Uploading part '{}' of upload '{}' with key '{}' to bucket '{}'", partNumber, uploadId, key, bucketName);
        String md5 = MD5.getMD5Hex(data);

        UploadPartRequest uploadPartRequest = UploadPartRequest.builder()
                .bucket(bucketName)
                .key(key)
                .uploadId(uploadId)
                .partNumber(partNumber)
                .build();

        UploadPartResponse response = s3Client.uploadPart(
                uploadPartRequest, RequestBody.fromBytes(data.array())
        );

//        String bytesHex = HexFormat.of().formatHex(data.array());
//        logger.trace(String.format(
//                "Uploaded %d bytes:\n%s...%s",
//                data.array().length,
//                bytesHex.substring(0,50),
//                bytesHex.substring(bytesHex.length()-50)
//        ));

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

        return new UploadPartResult(new ByteArray(md5bytes),completedPart);
    }
}
