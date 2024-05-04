package ru.rerumu.s3.integration;

import ch.qos.logback.classic.Level;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.rerumu.zfs_backup_s3.s3.S3Service;
import ru.rerumu.zfs_backup_s3.s3.S3ServiceFactory;
import ru.rerumu.zfs_backup_s3.s3.S3ServiceFactoryImpl;
import ru.rerumu.zfs_backup_s3.s3.exceptions.FileAlreadyPresentOnS3Exception;
import ru.rerumu.zfs_backup_s3.s3.models.S3Storage;
import software.amazon.awssdk.regions.Region;

import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

@ExtendWith(MockitoExtension.class)
public class ITS3ServiceUploadDownload {

    Map<String, String> env = System.getenv();

    @Test
    void shouldUploadDownloadSmall(@TempDir Path tempDir) throws Exception {
        ch.qos.logback.classic.Logger logger = (ch.qos.logback.classic.Logger) LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);
        logger.setLevel(Level.ERROR);
//        logger = (ch.qos.logback.classic.Logger) LoggerFactory.getLogger(software.amazon.awssdk.core.interceptor.ExecutionInterceptorChain.class);
//        logger.setLevel(Level.INFO);
//        logger = (ch.qos.logback.classic.Logger) LoggerFactory.getLogger(software.amazon.awssdk.core.internal.io.AwsChunkedEncodingInputStream.class);
//        logger.setLevel(Level.INFO);
//        logger = (ch.qos.logback.classic.Logger) LoggerFactory.getLogger(software.amazon.awssdk.auth.signer.Aws4Signer.class);
//        logger.setLevel(Level.INFO);

        S3Storage s3Storage = new S3Storage(
                Region.of(env.get("ZFS_BACKUP_S3_REGION")),
                env.get("TEST_BUCKET"),
                env.get("ZFS_BACKUP_S3_ACCESS_KEY_ID"),
                env.get("ZFS_BACKUP_S3_SECRET_ACCESS_KEY"),
                Paths.get(env.get("ZFS_BACKUP_S3_FULL_PREFIX")),
                new URI(env.get("ZFS_BACKUP_S3_ENDPOINT_URL")),
                env.get("ZFS_BACKUP_S3_FULL_STORAGE_CLASS")
        );

        S3ServiceFactory s3ServiceFactory = new S3ServiceFactoryImpl();
        S3Service s3Service = s3ServiceFactory.getS3Service(
                s3Storage,
                12_000_000
        );

        byte[] data = new byte[1_000];
        new Random().nextBytes(data);

        String key = "TestPool/level-0/shouldUploadDownloadSmall__"
                + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HHmmss"))
                + "/";

        String fileName = UUID.randomUUID().toString();
        Path tempFile = tempDir.resolve(fileName);
        Files.write(
                tempFile,
                data,
                StandardOpenOption.CREATE,
                StandardOpenOption.APPEND,
                StandardOpenOption.WRITE
        );
        byte[] srcBytes = Files.readAllBytes(tempFile);

        s3Service.upload(tempFile, key);
        Files.delete(tempFile);

        S3ServiceFactory s3ServiceFactoryDownload = new S3ServiceFactoryImpl();
        S3Service s3ServiceDownload = s3ServiceFactoryDownload.getS3Service(
                s3Storage,
                12_000_000
        );

        s3ServiceDownload.download(key + fileName, tempFile);
        byte[] resBytes = Files.readAllBytes(tempFile);

        Assertions.assertArrayEquals(srcBytes, resBytes);
    }

    @Test
    void shouldUploadDownloadBig(@TempDir Path tempDir) throws Exception {
        ch.qos.logback.classic.Logger logger = (ch.qos.logback.classic.Logger) LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);
        logger.setLevel(Level.ERROR);
//        logger = (ch.qos.logback.classic.Logger) LoggerFactory.getLogger(software.amazon.awssdk.core.interceptor.ExecutionInterceptorChain.class);
//        logger.setLevel(Level.INFO);
//        logger = (ch.qos.logback.classic.Logger) LoggerFactory.getLogger(software.amazon.awssdk.core.internal.io.AwsChunkedEncodingInputStream.class);
//        logger.setLevel(Level.INFO);
//        logger = (ch.qos.logback.classic.Logger) LoggerFactory.getLogger(software.amazon.awssdk.auth.signer.Aws4Signer.class);
//        logger.setLevel(Level.INFO);

        S3Storage s3Storage = new S3Storage(
                Region.of(env.get("ZFS_BACKUP_S3_REGION")),
                env.get("TEST_BUCKET"),
                env.get("ZFS_BACKUP_S3_ACCESS_KEY_ID"),
                env.get("ZFS_BACKUP_S3_SECRET_ACCESS_KEY"),
                Paths.get(env.get("ZFS_BACKUP_S3_FULL_PREFIX")),
                new URI(env.get("ZFS_BACKUP_S3_ENDPOINT_URL")),
                env.get("ZFS_BACKUP_S3_FULL_STORAGE_CLASS")
        );

        S3ServiceFactory s3ServiceFactory = new S3ServiceFactoryImpl();
        S3Service s3Service = s3ServiceFactory.getS3Service(
                s3Storage,
                7_000_000
        );

        byte[] data = new byte[24_000_000];
        new Random().nextBytes(data);
        String fileName = UUID.randomUUID().toString();
        Path tempFile = tempDir.resolve(fileName);
        Files.write(
                tempFile,
                data,
                StandardOpenOption.CREATE,
                StandardOpenOption.APPEND,
                StandardOpenOption.WRITE
        );
        byte[] srcBytes = Files.readAllBytes(tempFile);


        String key = "TestPool/level-0/shouldUploadDownloadBig__"
                + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HHmmss"))
                + "/";

        s3Service.upload(tempFile, key);
        Files.delete(tempFile);

        S3ServiceFactory s3ServiceFactoryDownload = new S3ServiceFactoryImpl();
        S3Service s3ServiceDownload = s3ServiceFactoryDownload.getS3Service(
                s3Storage,
                7_000_000
        );

        s3ServiceDownload.download(key + fileName, tempFile);
        byte[] resBytes = Files.readAllBytes(tempFile);

        Assertions.assertArrayEquals(srcBytes, resBytes);
    }

    @Test
    void shouldNotSendIfPresent(@TempDir Path tempDir) throws Exception {
        ch.qos.logback.classic.Logger logger = (ch.qos.logback.classic.Logger) LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);
        logger.setLevel(Level.ERROR);
//        logger = (ch.qos.logback.classic.Logger) LoggerFactory.getLogger(software.amazon.awssdk.core.interceptor.ExecutionInterceptorChain.class);
//        logger.setLevel(Level.INFO);
//        logger = (ch.qos.logback.classic.Logger) LoggerFactory.getLogger(software.amazon.awssdk.core.internal.io.AwsChunkedEncodingInputStream.class);
//        logger.setLevel(Level.INFO);
//        logger = (ch.qos.logback.classic.Logger) LoggerFactory.getLogger(software.amazon.awssdk.auth.signer.Aws4Signer.class);
//        logger.setLevel(Level.INFO);

        S3Storage s3Storage = new S3Storage(
                Region.of(env.get("ZFS_BACKUP_S3_REGION")),
                env.get("TEST_BUCKET"),
                env.get("ZFS_BACKUP_S3_ACCESS_KEY_ID"),
                env.get("ZFS_BACKUP_S3_SECRET_ACCESS_KEY"),
                Paths.get(env.get("ZFS_BACKUP_S3_FULL_PREFIX")),
                new URI(env.get("ZFS_BACKUP_S3_ENDPOINT_URL")),
                env.get("ZFS_BACKUP_S3_FULL_STORAGE_CLASS")
        );

        S3ServiceFactory s3ServiceFactory = new S3ServiceFactoryImpl();
        S3Service s3Service = s3ServiceFactory.getS3Service(
                s3Storage,
                12_000_000
        );

        byte[] data = new byte[1_000];
        new Random().nextBytes(data);

        String key = "TestPool/level-0/shouldUploadDownloadSmall__"
                + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HHmmss"))
                + "/";

        String fileName = UUID.randomUUID().toString();
        Path tempFile = tempDir.resolve(fileName);
        Files.write(
                tempFile,
                data,
                StandardOpenOption.CREATE,
                StandardOpenOption.APPEND,
                StandardOpenOption.WRITE
        );

        s3Service.upload(tempFile, key);

        RuntimeException exception = Assertions.assertThrows(
                RuntimeException.class,
                () -> s3Service.upload(tempFile, key)
        );

        Assertions.assertEquals(
                FileAlreadyPresentOnS3Exception.class,
                exception.getCause().getClass()
        );
    }
}
