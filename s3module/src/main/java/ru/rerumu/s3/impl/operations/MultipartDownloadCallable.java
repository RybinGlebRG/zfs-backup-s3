package ru.rerumu.s3.impl.operations;

import org.apache.commons.lang3.ArrayUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.rerumu.s3.exceptions.IncorrectHashException;
import ru.rerumu.s3.factories.S3ClientFactory;
import ru.rerumu.s3.models.S3Storage;
import ru.rerumu.s3.services.S3RequestService;
import ru.rerumu.s3.services.impl.requests.models.Range;
import ru.rerumu.utils.MD5;

import java.io.IOException;
import java.nio.file.Path;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

import static ru.rerumu.utils.MD5.getMD5Bytes;
import static ru.rerumu.utils.MD5.getMD5Hex;

public class MultipartDownloadCallable implements Callable<Void> {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final Path path;
    private final String key;
    private final S3Storage s3Storage;
    private final S3ClientFactory s3ClientFactory;

    private final Long maxPartSize;

    private final S3RequestService s3RequestService;


    public MultipartDownloadCallable(Path path, String key, S3Storage s3Storage, S3ClientFactory s3ClientFactory, int maxPartSize, S3RequestService s3RequestService) {
        this.path = path;
        this.key = key;
        this.s3Storage = s3Storage;
        this.s3ClientFactory = s3ClientFactory;
        this.maxPartSize = Long.valueOf(maxPartSize);
        this.s3RequestService = s3RequestService;
    }

    private void finish( List<byte[]> md5List, int partNumber) throws NoSuchAlgorithmException, IOException, IncorrectHashException {
        String md5;
        String storedMd5Hex = s3RequestService.getMetadata(key).md5Hex();

        if (storedMd5Hex.contains("-")){
            byte[] concatenatedMd5 = md5List.stream()
                    .reduce(new byte[0],ArrayUtils::addAll,ArrayUtils::addAll);
            md5 = MD5.getMD5Hex(concatenatedMd5)+"-"+partNumber;
        } else {
            md5 = MD5.getMD5Hex(path);
        }

        logger.info(String.format("Calculated md5='%s'", md5));
        logger.info(String.format("Stored md5='%s'", storedMd5Hex));

        if (!storedMd5Hex.equals(md5)) {
            throw new IncorrectHashException(String.format("Got '%s', but expected '%s'",storedMd5Hex, md5));
        }
    }


    @Override
    public Void call() throws IOException, NoSuchAlgorithmException, IncorrectHashException {
        List<byte[]> md5List = new ArrayList<>();
        long fileSize = s3RequestService.getMetadata(key).size();

        Range range = new Range(
                0L,
                Math.min(maxPartSize, fileSize) - 1
        );

        while (range.start()<=fileSize-1) {
            byte[] md5 = s3RequestService.getObjectRange(key,range.start(),range.end(),path);
            md5List.add(md5);

            range = new Range(
                    range.end()+1,
                    Math.min(range.start() + maxPartSize, fileSize) - 1
            );
        }

        finish( md5List, md5List.size());

        return null;
    }
}
