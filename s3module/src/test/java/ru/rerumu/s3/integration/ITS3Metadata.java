package ru.rerumu.s3.integration;

import ch.qos.logback.classic.Level;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.rerumu.zfs_backup_s3.s3.factories.S3ClientFactory;
import ru.rerumu.zfs_backup_s3.s3.factories.impl.S3ClientFactoryImpl;
import ru.rerumu.zfs_backup_s3.s3.impl.operations.MultipartDownloadCallable;
import ru.rerumu.zfs_backup_s3.s3.impl.operations.MultipartUploadCallable;
import ru.rerumu.zfs_backup_s3.s3.impl.operations.OnepartUploadCallable;
import ru.rerumu.zfs_backup_s3.s3.models.S3Storage;
import ru.rerumu.zfs_backup_s3.s3.services.S3RequestService;
import ru.rerumu.zfs_backup_s3.s3.services.impl.S3RequestServiceImpl;
import ru.rerumu.zfs_backup_s3.s3.services.impl.requests.CallableSupplierFactory;
import ru.rerumu.zfs_backup_s3.s3.services.impl.requests.GetObjectMetadataCallable;
import ru.rerumu.zfs_backup_s3.utils.ImmutableList;
import ru.rerumu.zfs_backup_s3.utils.ImmutableMap;
import ru.rerumu.zfs_backup_s3.utils.callables.impl.CallableExecutorImpl;
import software.amazon.awssdk.regions.Region;

import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

@ExtendWith(MockitoExtension.class)
public class ITS3Metadata {

    Map<String,String> env = System.getenv();

    @Test
    void shouldUploadDownloadMetadata(@TempDir Path tempDir) throws Exception {
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
        S3ClientFactory s3ClientFactory = new S3ClientFactoryImpl(new ImmutableList<>(List.of(s3Storage)));
        S3RequestService s3RequestService = new S3RequestServiceImpl(
                new CallableExecutorImpl(),
                new CallableSupplierFactory(
                        s3ClientFactory,
                        s3Storage
                ));

        String key = "level-0/shouldUploadDownloadMetadata__"
                + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HHmmss"))
                + "/" + UUID.randomUUID() + ".part0";

        Path pathUpload = tempDir.resolve(UUID.randomUUID().toString());
        byte[] data = new byte[30_000_000];
        new Random().nextBytes(data);
        Files.write(
                pathUpload,
                data,
                StandardOpenOption.CREATE,
                StandardOpenOption.APPEND,
                StandardOpenOption.WRITE
        );
        MultipartUploadCallable uploadCallable = new MultipartUploadCallable(
                pathUpload,
                key,
                12_000_000,
                s3RequestService
        );
        uploadCallable.call();

        ImmutableMap metadata = s3RequestService.getObjectMetadata(key);

        metadata.map().entrySet()
                        .forEach(entry-> logger.error(String.format("Key: %s, Value: %s",entry.getKey(),entry.getValue())));
        Assertions.assertEquals("true", metadata.map().get("x-multipart"));
        Assertions.assertEquals("12000000", metadata.map().get("x-multipart-part-size"));
    }
}
