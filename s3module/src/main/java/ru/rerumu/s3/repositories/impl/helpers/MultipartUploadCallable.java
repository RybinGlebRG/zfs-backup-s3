package ru.rerumu.s3.repositories.impl.helpers;

import org.apache.commons.lang3.ArrayUtils;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.rerumu.s3.exceptions.IncorrectHashException;
import ru.rerumu.s3.services.S3RequestService;
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

// TODO: Add multiple attempts?
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

//    private String start() {
//        CreateMultipartUploadResponse response = s3RequestService.createMultipartUpload(key);
//        String uploadId = response.uploadId();
//        Objects.requireNonNull(uploadId);
//        logger.info(String.format("uploadId '%s'", uploadId));
//
//        return uploadId;
//    }

//    private byte[] getNextPart(BufferedInputStream bufferedInputStream) throws IOException, EOFException {
//        byte[] tmp = new byte[maxPartSize];
//        int len = bufferedInputStream.read(tmp);
//        if (len == -1) {
//            throw new EOFException();
//        }
//        return Arrays.copyOf(tmp, len);
//    }

    private void uploadPart(
            @NonNull byte[] data,
            @NonNull Integer partNumber
    ) throws IOException, EOFException, NoSuchAlgorithmException, IncorrectHashException {
        logger.debug("Getting new part");
        logger.debug(String.format("Starting loading part '%d'", partNumber));

        Objects.requireNonNull(data);
        Objects.requireNonNull(partNumber);

        String md5 = MD5.getMD5Hex(data);

        UploadPartResponse response = s3RequestService.uploadPart(
                key,
                uploadId,
                partNumber,
                data
        );
        String eTag = response.eTag();

        logger.info(String.format("ETag='%s'", eTag));
        if (!(eTag.equals('"' + md5 + '"'))) {
            throw new IncorrectHashException(String.format("Got '%s', but expected '%s'", eTag, '"' + md5 + '"'));
        }
        md5List.add(MD5.getMD5Bytes(data));

        completedPartList.add(
                CompletedPart.builder().partNumber(partNumber).eTag(eTag).build()
        );
        logger.debug(String.format("Uploaded part '%d'", partNumber));
    }

    private void finish(
            Integer partNumber
    ) throws NoSuchAlgorithmException, IOException, IncorrectHashException {
        Objects.requireNonNull(partNumber);

        CompleteMultipartUploadResponse response = s3RequestService.completeMultipartUpload(
                completedPartList,
                key,
                uploadId
        );
        String eTag = response.eTag();

        byte[] concatenatedMd5 = new byte[0];
        for (byte[] bytes : md5List) {
            concatenatedMd5 = ArrayUtils.addAll(concatenatedMd5, bytes);
        }
        String md5 = MD5.getMD5Hex(concatenatedMd5) + "-" + partNumber;
        if (!eTag.equals('"' + md5 + '"')) {
            throw new IncorrectHashException(String.format("Got '%s', but expected '%s'", eTag, '"' + md5 + '"'));
        }
    }

    @Override
    public Void call() throws IOException, NoSuchAlgorithmException, IncorrectHashException {
        try (BufferedInputStream bufferedInputStream = new BufferedInputStream(Files.newInputStream(path))) {
            int partNumber = 0;

            uploadId = s3RequestService.createMultipartUpload(key).uploadId();
            Objects.requireNonNull(uploadId);

            logger.info(String.format("uploadId = '%s'", uploadId));

            Optional<byte[]> optionalBytes = InputStreamUtils.readNext(bufferedInputStream, maxPartSize);

            while (optionalBytes.isPresent()) {
                partNumber++;
                uploadPart(optionalBytes.get(), partNumber);
                optionalBytes = InputStreamUtils.readNext(bufferedInputStream, maxPartSize);
            }

            finish(partNumber);

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
