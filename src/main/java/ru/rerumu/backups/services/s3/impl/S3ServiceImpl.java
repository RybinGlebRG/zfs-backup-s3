package ru.rerumu.backups.services.s3.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.rerumu.backups.services.s3.S3Service;
import ru.rerumu.backups.services.s3.factories.S3CallableFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.*;

public class S3ServiceImpl implements S3Service {
    private final static Long DELAY = 10L;

    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private final S3CallableFactory s3CallableFactory;

    private final ScheduledExecutorService scheduledExecutorService = Executors.newSingleThreadScheduledExecutor();

    public S3ServiceImpl(S3CallableFactory s3CallableFactory) {
        this.s3CallableFactory = s3CallableFactory;
    }

    private boolean isExists(String key) {
        List<String> keys = list(key);

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
    public void upload(Path path, String key) {
        logger.info(String.format("Trying to upload file '%s' to '%s'", path.toString(), key));

        runWithRetry(s3CallableFactory.getUploadCallable(path, key));

        while (!isExists(key)) {
            logger.warn(String.format("File '%s' is not found on S3. Trying to upload again", key));
            runWithRetry(s3CallableFactory.getUploadCallable(path, key));
        }


    }

    @Override
    public void download(String key, Path path) {
        try {
            logger.info(String.format("Trying to download file '%s' to '%s'", key, path.toString()));
            runWithRetry(s3CallableFactory.getDownloadCallable(key, path));
            logger.debug(String.format("Successfully downloaded file '%s' to  '%s'. Size = %d", key, path.toString(), Files.size(path)));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public List<String> list(String prefix) {
        List<String> keys = runWithRetry(s3CallableFactory.getListCallable(prefix));
        return keys;
    }

}
