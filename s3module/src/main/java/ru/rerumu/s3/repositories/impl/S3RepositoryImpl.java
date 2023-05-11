package ru.rerumu.s3.repositories.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.rerumu.s3.factories.S3CallableFactory;
import ru.rerumu.s3.repositories.S3Repository;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.*;


public class S3RepositoryImpl implements S3Repository {
    private static final String PART_SUFFIX = ".part";
    private final static Long DELAY = 10L;


    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final S3CallableFactory s3CallableFactory;

    public S3RepositoryImpl(S3CallableFactory s3CallableFactory) {
        this.s3CallableFactory = s3CallableFactory;
    }

    private boolean isExists(String key) {
        List<String> keys = listAll(key);

        return keys.stream()
                .anyMatch(item -> item.equals(key));
    }
    @Override
    public void add(String prefix, Path path) {
        String key = prefix + path.getFileName().toString();
        logger.info(String.format("Trying to upload file '%s' to '%s'", path.toString(), key));


        try {
            s3CallableFactory.getUploadCallable(path, key).call();

            while (!isExists(key)) {
                logger.warn(String.format("File '%s' is not found on S3. Trying to upload again", key));
                s3CallableFactory.getUploadCallable(path, key).call();
            }
        } catch (Exception e){
            logger.error(e.getMessage(),e);
            throw new RuntimeException(e);
        }
    }

    @Override
    public List<String> listAll(String prefix) {
        try {
            List<String> keys = s3CallableFactory.getListCallable(prefix).call();
            keys.sort(
                    Comparator.comparing(
                            item -> Integer.valueOf(item.substring(item.lastIndexOf(PART_SUFFIX) + PART_SUFFIX.length()))
                    )
            );
            return keys;
        } catch (Exception e){
            logger.error(e.getMessage(),e);
            throw new RuntimeException(e);
        }
    }

    @Override
    public void getOne(String prefix, Path targetPath) {
        try {
            logger.info(String.format("Trying to download file '%s' to '%s'", prefix, targetPath.toString()));
            s3CallableFactory.getDownloadCallable(prefix, targetPath).call();
            logger.debug(String.format("Successfully downloaded file '%s' to  '%s'. Size = %d", prefix, targetPath.toString(), Files.size(targetPath)));
        } catch (Exception e){
            logger.error(e.getMessage(),e);
            throw new RuntimeException(e);
        }
    }
}
