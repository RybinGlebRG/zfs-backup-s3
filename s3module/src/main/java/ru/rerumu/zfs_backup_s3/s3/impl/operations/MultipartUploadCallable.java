package ru.rerumu.zfs_backup_s3.s3.impl.operations;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.rerumu.zfs_backup_s3.s3.exceptions.IncorrectHashException;
import ru.rerumu.zfs_backup_s3.s3.exceptions.PartNumberTooLargeException;
import ru.rerumu.zfs_backup_s3.utils.ImmutableMap;
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

@ThreadSafe
public final class MultipartUploadCallable extends CallableOnlyOnce<Void> {
    private static final int MAX_PART_NUMBER = 10000;
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
    ) throws IOException {
        Objects.requireNonNull(path);
        Objects.requireNonNull(key);
        Objects.requireNonNull(s3RequestService);
        this.path = path;
        this.key = key;
        this.maxPartSize = calcFileNumber(maxPartSize, path);
        this.s3RequestService = s3RequestService;
    }

    // TODO: Should use calculated value by default; specified value should override default value unconditionally
    private int calcFileNumber(int specifiedPartSize, Path file) throws IOException {
        Long fileSize = Files.size(file);
        logger.debug("Files size='{}'", fileSize);
        logger.debug("Calculating minimal part size to fit in 10000 uploads...");
        long longPartSize = Math.round(Math.ceil((double)fileSize / MAX_PART_NUMBER));
        int minPartSize;

        if (longPartSize > Integer.MAX_VALUE ){
            throw new RuntimeException("Invalid part size");
        } else {
            minPartSize = (int) longPartSize;
        }
        logger.debug("Minimal part size='{}'", minPartSize);

        logger.debug("Choosing part size between specified part size='{}' and calculated value='{}'...", specifiedPartSize, minPartSize);
        int partSize = Math.max(specifiedPartSize, minPartSize);
        logger.debug("Chose part size='{}'", partSize);
         return partSize;
    }

    @Override
    protected Void callOnce() throws IOException, NoSuchAlgorithmException, IncorrectHashException, PartNumberTooLargeException {
        String uploadId = null;
        try (BufferedInputStream bufferedInputStream = new BufferedInputStream(Files.newInputStream(path))) {
            int partNumber = 0;
            long uploadedBytesLen = 0;
            List<ByteArray> md5List = new ArrayList<>();
            List<CompletedPart> completedPartList = new ArrayList<>();

            Map<String,String> metadata = new HashMap<>();
            metadata.put("x-multipart","true");
            metadata.put("x-multipart-part-size",String.valueOf(maxPartSize));

            uploadId = s3RequestService.createMultipartUpload(key,new ImmutableMap(metadata));

            logger.info(String.format("uploadId = '%s'", uploadId));

            Optional<ByteArray> optionalBytes = InputStreamUtils.readNext(bufferedInputStream, maxPartSize);

            while (optionalBytes.isPresent()) {
                partNumber++;
                if (partNumber > MAX_PART_NUMBER){
                    throw new PartNumberTooLargeException();
                }

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
