package ru.rerumu.zfs_backup_s3.s3.impl.operations;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.rerumu.zfs_backup_s3.s3.exceptions.IncorrectHashException;
import ru.rerumu.zfs_backup_s3.s3.services.S3RequestService;
import ru.rerumu.zfs_backup_s3.s3.services.impl.requests.models.UploadPartResult;
import ru.rerumu.zfs_backup_s3.s3.utils.InputStreamUtils;
import software.amazon.awssdk.services.s3.model.*;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.util.concurrent.Callable;

// TODO: Send with the same part number?
// TODO: Check thread safe
public class MultipartUploadCallable implements Callable<Void> {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final Path path;
    private final String key;
    private final int maxPartSize;
    private final List<byte[]> md5List = new ArrayList<>();
    private final List<CompletedPart> completedPartList = new ArrayList<>();
    private String uploadId = null;

    private final S3RequestService s3RequestService;


    public MultipartUploadCallable(Path path, String key, int maxPartSize, S3RequestService s3RequestService) {
        this.path = path;
        this.key = key;
        this.maxPartSize = maxPartSize;
        this.s3RequestService = s3RequestService;
    }

    @Override
    public Void call() throws IOException, NoSuchAlgorithmException, IncorrectHashException {
        try (BufferedInputStream bufferedInputStream = new BufferedInputStream(Files.newInputStream(path))) {
            int partNumber = 0;
            long uploadedBytesLen = 0;

            uploadId = s3RequestService.createMultipartUpload(key);

            logger.info(String.format("uploadId = '%s'", uploadId));

            Optional<byte[]> optionalBytes = InputStreamUtils.readNext(bufferedInputStream, maxPartSize);

            while (optionalBytes.isPresent()) {
                partNumber++;
                byte[] data = optionalBytes.get();

                UploadPartResult partResult = s3RequestService.uploadPart(
                        key,
                        uploadId,
                        partNumber,
                        data
                );
                md5List.add(partResult.md5());
                completedPartList.add(partResult.completedPart());
                uploadedBytesLen+=data.length;
                logger.debug(String.format("Uploaded %d bytes",uploadedBytesLen));

                optionalBytes = InputStreamUtils.readNext(bufferedInputStream, maxPartSize);
            }

            s3RequestService.completeMultipartUpload(
                    completedPartList,
                    key,
                    uploadId,
                    md5List
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
