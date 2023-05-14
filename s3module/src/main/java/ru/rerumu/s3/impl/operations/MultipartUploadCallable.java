package ru.rerumu.s3.impl.operations;

import org.apache.commons.lang3.ArrayUtils;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.rerumu.s3.exceptions.IncorrectHashException;
import ru.rerumu.s3.services.S3RequestService;
import ru.rerumu.s3.services.impl.requests.models.UploadPartResult;
import ru.rerumu.s3.utils.InputStreamUtils;
import ru.rerumu.utils.MD5;
import ru.rerumu.utils.callables.CallableExecutor;
import software.amazon.awssdk.services.s3.model.*;

import java.io.BufferedInputStream;
import java.io.EOFException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.function.BiFunction;
import java.util.function.Supplier;

// TODO: Send with the same part number?
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

            uploadId = s3RequestService.createMultipartUpload(key);

            logger.info(String.format("uploadId = '%s'", uploadId));

            Optional<byte[]> optionalBytes = InputStreamUtils.readNext(bufferedInputStream, maxPartSize);

            while (optionalBytes.isPresent()) {
                partNumber++;

                UploadPartResult partResult = s3RequestService.uploadPart(
                        key,
                        uploadId,
                        partNumber,
                        optionalBytes.get()
                );
                md5List.add(partResult.md5());
                completedPartList.add(partResult.completedPart());

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
