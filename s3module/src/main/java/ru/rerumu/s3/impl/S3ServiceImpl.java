package ru.rerumu.s3.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.rerumu.s3.S3Service;
import ru.rerumu.s3.exceptions.FileHitSizeLimitException;
import ru.rerumu.s3.exceptions.ZFSStreamEndedException;
import ru.rerumu.s3.factories.ZFSFileReaderFactory;
import ru.rerumu.s3.factories.ZFSFileWriterFactory;
import ru.rerumu.s3.utils.FileManager;
import ru.rerumu.s3.utils.ZFSFileReader;
import ru.rerumu.s3.utils.ZFSFileWriter;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Comparator;
import java.util.List;

public class S3ServiceImpl implements S3Service {
    private static final String PART_SUFFIX = ".part";

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final FileManager fileManager;

    private final ZFSFileWriterFactory zfsFileWriterFactory;
    private final ZFSFileReaderFactory zfsFileReaderFactory;

    private final S3CallableFactory s3CallableFactory;

    public S3ServiceImpl(FileManager fileManager, ZFSFileWriterFactory zfsFileWriterFactory, ZFSFileReaderFactory zfsFileReaderFactory, S3CallableFactory s3CallableFactory) {
        this.fileManager = fileManager;
        this.zfsFileWriterFactory = zfsFileWriterFactory;
        this.zfsFileReaderFactory = zfsFileReaderFactory;
        this.s3CallableFactory = s3CallableFactory;
    }

    private boolean isExists(String key) {
        List<String> keys = list(key);

        return keys.stream()
                .anyMatch(item -> item.equals(key));
    }


    private void upload(Path path, String prefix) {
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
    public void upload(BufferedInputStream bufferedInputStream, String key) {
        try {
            int n = 0;
            while (true) {
                Path newFilePath = fileManager.getNew(null, ".part" + n++);
                try (ZFSFileWriter zfsFileWriter = zfsFileWriterFactory.getZFSFileWriter(newFilePath)) {
                    zfsFileWriter.write(bufferedInputStream);
                } catch (FileHitSizeLimitException e) {
                    upload(newFilePath, key);
                    Files.delete(newFilePath);
                    logger.debug(String.format("File '%s' processed", newFilePath));
                } catch (ZFSStreamEndedException e) {
                    upload(newFilePath, key);
                    Files.delete(newFilePath);
                    logger.debug(String.format("File '%s' processed", newFilePath));
                    logger.info("End of stream. Exiting");
                    break;
                }

            }
        } catch (Exception e) {
            logger.error(e.getMessage(),e);
            throw new RuntimeException(e);
        }
    }


    private void download(String prefix, Path targetPath) {
        try {
            logger.info(String.format("Trying to download file '%s' to '%s'", prefix, targetPath.toString()));
            s3CallableFactory.getDownloadCallable(prefix, targetPath).call();
            logger.debug(String.format("Successfully downloaded file '%s' to  '%s'. Size = %d", prefix, targetPath.toString(), Files.size(targetPath)));
        } catch (Exception e){
            logger.error(e.getMessage(),e);
            throw new RuntimeException(e);
        }
    }

    @Override
    public void download(String prefix, BufferedOutputStream bufferedOutputStream) {
        try {

            List<String> keys = list(prefix);
            for (String key : keys) {
                Path part = fileManager.getNew(null, "-" + Paths.get(key).getFileName());
                download(key,part);
                ZFSFileReader zfsFileReader = zfsFileReaderFactory.getZFSFileReader(
                        bufferedOutputStream, part
                );
                zfsFileReader.read();
                fileManager.delete(part);
            }
        } catch (Exception e){
            logger.error(e.getMessage(),e);
            throw new RuntimeException(e);
        }
    }

    @Override
    public List<String> list(String prefix) {
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

}
