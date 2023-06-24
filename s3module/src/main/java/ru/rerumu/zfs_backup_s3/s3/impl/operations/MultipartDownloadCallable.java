package ru.rerumu.zfs_backup_s3.s3.impl.operations;

import org.apache.commons.lang3.ArrayUtils;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.rerumu.zfs_backup_s3.s3.exceptions.IncorrectHashException;
import ru.rerumu.zfs_backup_s3.s3.services.S3RequestService;
import ru.rerumu.zfs_backup_s3.s3.services.impl.requests.models.Range;
import ru.rerumu.zfs_backup_s3.utils.ByteArray;
import ru.rerumu.zfs_backup_s3.utils.CallableOnlyOnce;
import ru.rerumu.zfs_backup_s3.utils.MD5;
import ru.rerumu.zfs_backup_s3.utils.ThreadSafe;

import java.io.IOException;
import java.nio.file.Path;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@ThreadSafe
public final class MultipartDownloadCallable extends CallableOnlyOnce<Void> {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final Path path;
    private final String key;
    private final Long maxPartSize;

    private final S3RequestService s3RequestService;


    public MultipartDownloadCallable(
            @NonNull Path path,
            @NonNull String key,
            int maxPartSize,
            @NonNull S3RequestService s3RequestService) {
        Objects.requireNonNull(path);
        Objects.requireNonNull(key);
        Objects.requireNonNull(s3RequestService);
        this.path = path;
        this.key = key;
        this.maxPartSize = Long.valueOf(maxPartSize);
        this.s3RequestService = s3RequestService;
    }

    private void finish( List<byte[]> md5List, int partNumber) throws NoSuchAlgorithmException, IOException, IncorrectHashException {
        String md5;
        String storedMd5Hex = s3RequestService.getMetadata(key).md5Hex();

        if (storedMd5Hex.contains("-")){
            byte[] concatenatedMd5 = md5List.stream()
                    .reduce(new byte[0],ArrayUtils::addAll,ArrayUtils::addAll);
            md5 = MD5.getMD5Hex(new ByteArray(concatenatedMd5))+"-"+partNumber;
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
    protected Void callOnce() throws IOException, NoSuchAlgorithmException, IncorrectHashException {
        List<byte[]> md5List = new ArrayList<>();
        long fileSize = s3RequestService.getMetadata(key).size();

        Range range = new Range(
                0L,
                Math.min(maxPartSize, fileSize)
        );

        while (range.start()<fileSize) {
            byte[] md5 = s3RequestService.getObjectRange(key,range.start(),range.end(),path);
            md5List.add(md5);

            range = new Range(
                    range.end(),
                    Math.min(range.end() + maxPartSize, fileSize)
            );
        }

        finish( md5List, md5List.size());

        return null;
    }
}
