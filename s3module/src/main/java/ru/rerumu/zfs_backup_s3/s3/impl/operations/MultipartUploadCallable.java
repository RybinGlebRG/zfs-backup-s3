package ru.rerumu.zfs_backup_s3.s3.impl.operations;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.rerumu.zfs_backup_s3.s3.exceptions.IncorrectHashException;
import ru.rerumu.zfs_backup_s3.s3.services.S3RequestService;
import ru.rerumu.zfs_backup_s3.s3.services.impl.requests.models.UploadPartResult;
import ru.rerumu.zfs_backup_s3.s3.utils.InputStreamUtils;
import ru.rerumu.zfs_backup_s3.utils.*;
import software.amazon.awssdk.services.s3.model.*;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.util.concurrent.Callable;

// TODO: Send with the same part number?
@ThreadSafe
public final class MultipartUploadCallable extends CallableOnlyOnce<Void> {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final Path path;
    private final String key;
    private final int maxPartSize;
    private final S3RequestService s3RequestService;


    public MultipartUploadCallable(
            @NonNull Path path,
            @NonNull String key,
            int maxPartSize,
            @NonNull S3RequestService s3RequestService
    ) {
        Objects.requireNonNull(path);
        Objects.requireNonNull(key);
        Objects.requireNonNull(s3RequestService);
        this.path = path;
        this.key = key;
        this.maxPartSize = maxPartSize;
        this.s3RequestService = s3RequestService;
    }

    @Override
    protected Void callOnce() throws IOException, NoSuchAlgorithmException, IncorrectHashException {
        String uploadId = null;
        try (BufferedInputStream bufferedInputStream = new BufferedInputStream(Files.newInputStream(path))) {
            int partNumber = 0;
            long uploadedBytesLen = 0;
            List<ByteArray> md5List = new ArrayList<>();
            List<CompletedPart> completedPartList = new ArrayList<>();

            uploadId = s3RequestService.createMultipartUpload(key);

            logger.info(String.format("uploadId = '%s'", uploadId));

            Optional<ByteArray> optionalBytes = InputStreamUtils.readNext(bufferedInputStream, maxPartSize);

            while (optionalBytes.isPresent()) {
                partNumber++;
                ByteArray data = optionalBytes.get();

                UploadPartResult partResult = s3RequestService.uploadPart(
                        key,
                        uploadId,
                        partNumber,
                        data
                );
                md5List.add(partResult.md5());
                completedPartList.add(partResult.completedPart());
                uploadedBytesLen+=data.array().length;
                logger.debug(String.format("Uploaded %d bytes",uploadedBytesLen));

                optionalBytes = InputStreamUtils.readNext(bufferedInputStream, maxPartSize);
            }

            s3RequestService.completeMultipartUpload(
                    new ImmutableList<>(completedPartList),
                    key,
                    uploadId,
                    new ByteArrayList(md5List)
            );

        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            if (uploadId != null) {
                s3RequestService.abortMultipartUpload(key, uploadId);
            }
            throw e;
        }
        return null;
    }
}
