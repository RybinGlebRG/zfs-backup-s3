package ru.rerumu.s3.repositories.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.rerumu.s3.exceptions.IncorrectHashException;
import ru.rerumu.s3.exceptions.S3MissesFileException;
import ru.rerumu.s3.factories.S3CallableFactory;
import ru.rerumu.s3.models.S3Storage;
import ru.rerumu.s3.repositories.S3Repository;
import ru.rerumu.s3.S3Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.NoSuchAlgorithmException;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.*;


public class S3RepositoryImpl implements S3Repository {
    private static final String PART_SUFFIX = ".part";
    private final static Long DELAY = 10L;


    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final S3CallableFactory s3CallableFactory;

    private final ScheduledExecutorService scheduledExecutorService = Executors.newSingleThreadScheduledExecutor();

    public S3RepositoryImpl(S3CallableFactory s3CallableFactory) {
        this.s3CallableFactory = s3CallableFactory;
    }

    private boolean isExists(String key) {
        List<String> keys = listAll(key);

        return keys.stream()
                .anyMatch(item -> item.equals(key));
    }

    private <T> T runWithRetry(Callable<T> callable) {
        Future<T> future = scheduledExecutorService.submit(callable);
        while (true) {
            try {
                return future.get();
            } catch (Exception e) {
                logger.warn(e.getMessage(), e);
            }
            future = scheduledExecutorService.schedule(
                    callable,
                    DELAY,
                    TimeUnit.SECONDS
            );
        }
    }

    @Override
    public void add(String prefix, Path path) {
        String key = prefix + path.getFileName().toString();
        logger.info(String.format("Trying to upload file '%s' to '%s'", path.toString(), key));

        runWithRetry(s3CallableFactory.getUploadCallable(path, key));

        while (!isExists(key)) {
            logger.warn(String.format("File '%s' is not found on S3. Trying to upload again", key));
            runWithRetry(s3CallableFactory.getUploadCallable(path, key));
        }
    }

    @Override
    public List<String> listAll(String prefix) {
        List<String> keys = runWithRetry(s3CallableFactory.getListCallable(prefix));
        keys.sort(
                Comparator.comparing(
                        item -> Integer.valueOf(item.substring(item.lastIndexOf(PART_SUFFIX) + PART_SUFFIX.length()))
                )
        );
        return keys;
    }

    @Override
    public void getOne(String prefix, Path targetPath) {
        try {
            logger.info(String.format("Trying to download file '%s' to '%s'", prefix, targetPath.toString()));
            runWithRetry(s3CallableFactory.getDownloadCallable(prefix, targetPath));
            logger.debug(String.format("Successfully downloaded file '%s' to  '%s'. Size = %d", prefix, targetPath.toString(), Files.size(targetPath)));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
