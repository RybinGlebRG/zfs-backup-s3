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
import ru.rerumu.zfs_backup_s3.s3.impl.operations.ListCallable;
import ru.rerumu.zfs_backup_s3.s3.impl.operations.OnepartUploadCallable;
import ru.rerumu.zfs_backup_s3.s3.models.S3Storage;
import ru.rerumu.zfs_backup_s3.s3.services.S3RequestService;
import ru.rerumu.zfs_backup_s3.s3.services.impl.S3RequestServiceImpl;
import ru.rerumu.zfs_backup_s3.s3.services.impl.requests.CallableSupplierFactory;
import ru.rerumu.zfs_backup_s3.utils.ImmutableList;
import ru.rerumu.zfs_backup_s3.utils.callables.impl.CallableExecutorImpl;
import software.amazon.awssdk.regions.Region;

import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

import static org.mockito.ArgumentMatchers.any;


@ExtendWith(MockitoExtension.class)
public class ITS3ListOperations {

    @Test
    void shouldListAll(@TempDir Path tempDir) throws Exception {
        ch.qos.logback.classic.Logger logger = (ch.qos.logback.classic.Logger) LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);
        logger.setLevel(Level.ERROR);
//        logger = (ch.qos.logback.classic.Logger) LoggerFactory.getLogger(software.amazon.awssdk.core.interceptor.ExecutionInterceptorChain.class);
//        logger.setLevel(Level.INFO);
//        logger = (ch.qos.logback.classic.Logger) LoggerFactory.getLogger(software.amazon.awssdk.core.internal.io.AwsChunkedEncodingInputStream.class);
//        logger.setLevel(Level.INFO);
//        logger = (ch.qos.logback.classic.Logger) LoggerFactory.getLogger(software.amazon.awssdk.auth.signer.Aws4Signer.class);
//        logger.setLevel(Level.INFO);

        S3Storage s3Storage = new S3Storage(
                Region.AWS_GLOBAL,
                "test",
                "1111",
                "1111",
                Paths.get("level-0"),
                new URI("http://127.0.0.1:9090/"),
                "STANDARD"
        );

        S3ClientFactory s3ClientFactory = new S3ClientFactoryImpl(new ImmutableList<>(List.of(s3Storage)));
        S3RequestService s3RequestService = new S3RequestServiceImpl(
                new CallableExecutorImpl(),
                new CallableSupplierFactory(
                        s3ClientFactory,
                        s3Storage
                ));

        byte[] data1 = new byte[1_000];
        byte[] data2 = new byte[1_000];
        byte[] data3 = new byte[1_000];

        new Random().nextBytes(data1);
        new Random().nextBytes(data2);
        new Random().nextBytes(data3);

        UUID uuid = UUID.randomUUID();
        LocalDateTime date = LocalDateTime.now();
        String dateFormatted =  date.format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HHmmss"));

        String key1 = "TestBucket/TestPool/level-0/shouldListAll__" + dateFormatted + "/" + uuid + ".part0";
        String key2 = "TestBucket/TestPool/level-0/shouldListAll__" + dateFormatted + "/" + uuid + ".part1";
        String key3 = "TestBucket/TestPool/level-0/shouldListAll__" + dateFormatted + "/" + uuid + ".part2";

        Path pathUpload1 = tempDir.resolve(UUID.randomUUID().toString());
        Path pathUpload2 = tempDir.resolve(UUID.randomUUID().toString());
        Path pathUpload3 = tempDir.resolve(UUID.randomUUID().toString());

        Files.write(
                pathUpload1,
                data1,
                StandardOpenOption.CREATE,
                StandardOpenOption.APPEND,
                StandardOpenOption.WRITE
        );
        Files.write(
                pathUpload2,
                data2,
                StandardOpenOption.CREATE,
                StandardOpenOption.APPEND,
                StandardOpenOption.WRITE
        );
        Files.write(
                pathUpload3,
                data3,
                StandardOpenOption.CREATE,
                StandardOpenOption.APPEND,
                StandardOpenOption.WRITE
        );

        new OnepartUploadCallable(pathUpload1, key1, s3RequestService).call();
        new OnepartUploadCallable(pathUpload2, key2, s3RequestService).call();
        new OnepartUploadCallable(pathUpload3, key3, s3RequestService).call();


        List<String> keys = new ListCallable(
                "TestBucket/TestPool/level-0/shouldListAll__" + dateFormatted + "/",
                s3RequestService
        ).call();

        Assertions.assertEquals(3, keys.size());

        Assertions.assertEquals(key1,keys.get(0));
        Assertions.assertEquals(key2,keys.get(1));
        Assertions.assertEquals(key3,keys.get(2));
    }

    @Test
    void shouldListOne(@TempDir Path tempDir) throws Exception {
        ch.qos.logback.classic.Logger logger = (ch.qos.logback.classic.Logger) LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);
        logger.setLevel(Level.ERROR);
//        logger = (ch.qos.logback.classic.Logger) LoggerFactory.getLogger(software.amazon.awssdk.core.interceptor.ExecutionInterceptorChain.class);
//        logger.setLevel(Level.INFO);
//        logger = (ch.qos.logback.classic.Logger) LoggerFactory.getLogger(software.amazon.awssdk.core.internal.io.AwsChunkedEncodingInputStream.class);
//        logger.setLevel(Level.INFO);
//        logger = (ch.qos.logback.classic.Logger) LoggerFactory.getLogger(software.amazon.awssdk.auth.signer.Aws4Signer.class);
//        logger.setLevel(Level.INFO);

        S3Storage s3Storage = new S3Storage(
                Region.AWS_GLOBAL,
                "test",
                "1111",
                "1111",
                Paths.get("level-0"),
                new URI("http://127.0.0.1:9090/"),
                "STANDARD"
        );

        S3ClientFactory s3ClientFactory = new S3ClientFactoryImpl(new ImmutableList<>(List.of(s3Storage)));
        S3RequestService s3RequestService = new S3RequestServiceImpl(
                new CallableExecutorImpl(),
                new CallableSupplierFactory(
                        s3ClientFactory,
                        s3Storage
                ));

        byte[] data1 = new byte[1_000];
        byte[] data2 = new byte[1_000];
        byte[] data3 = new byte[1_000];

        new Random().nextBytes(data1);
        new Random().nextBytes(data2);
        new Random().nextBytes(data3);

        UUID uuid = UUID.randomUUID();
        LocalDateTime date = LocalDateTime.now();
        String dateFormatted =  date.format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HHmmss"));

        String key1 = "TestBucket/TestPool/level-0/shouldListAll__" + dateFormatted + "/" + uuid + ".part0";
        String key2 = "TestBucket/TestPool/level-0/shouldListAll__" + dateFormatted + "/" + uuid + ".part1";
        String key3 = "TestBucket/TestPool/level-0/shouldListAll__" + dateFormatted + "/" + uuid + ".part2";

        Path pathUpload1 = tempDir.resolve(UUID.randomUUID().toString());
        Path pathUpload2 = tempDir.resolve(UUID.randomUUID().toString());
        Path pathUpload3 = tempDir.resolve(UUID.randomUUID().toString());

        Files.write(
                pathUpload1,
                data1,
                StandardOpenOption.CREATE,
                StandardOpenOption.APPEND,
                StandardOpenOption.WRITE
        );
        Files.write(
                pathUpload2,
                data2,
                StandardOpenOption.CREATE,
                StandardOpenOption.APPEND,
                StandardOpenOption.WRITE
        );
        Files.write(
                pathUpload3,
                data3,
                StandardOpenOption.CREATE,
                StandardOpenOption.APPEND,
                StandardOpenOption.WRITE
        );

        new OnepartUploadCallable(pathUpload1, key1, s3RequestService).call();
        new OnepartUploadCallable(pathUpload2, key2, s3RequestService).call();
        new OnepartUploadCallable(pathUpload3, key3, s3RequestService).call();


        List<String> keys = new ListCallable(
                key2,
                s3RequestService
        ).call();

        Assertions.assertEquals(1, keys.size());

        Assertions.assertEquals(key2,keys.get(0));
    }

    @Test
    void shouldNotFind(@TempDir Path tempDir) throws Exception {
        ch.qos.logback.classic.Logger logger = (ch.qos.logback.classic.Logger) LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);
        logger.setLevel(Level.ERROR);
//        logger = (ch.qos.logback.classic.Logger) LoggerFactory.getLogger(software.amazon.awssdk.core.interceptor.ExecutionInterceptorChain.class);
//        logger.setLevel(Level.INFO);
//        logger = (ch.qos.logback.classic.Logger) LoggerFactory.getLogger(software.amazon.awssdk.core.internal.io.AwsChunkedEncodingInputStream.class);
//        logger.setLevel(Level.INFO);
//        logger = (ch.qos.logback.classic.Logger) LoggerFactory.getLogger(software.amazon.awssdk.auth.signer.Aws4Signer.class);
//        logger.setLevel(Level.INFO);

        S3Storage s3Storage = new S3Storage(
                Region.AWS_GLOBAL,
                "test",
                "1111",
                "1111",
                Paths.get("level-0"),
                new URI("http://127.0.0.1:9090/"),
                "STANDARD"
        );

        S3ClientFactory s3ClientFactory = new S3ClientFactoryImpl(new ImmutableList<>(List.of(s3Storage)));
        S3RequestService s3RequestService = new S3RequestServiceImpl(
                new CallableExecutorImpl(),
                new CallableSupplierFactory(
                        s3ClientFactory,
                        s3Storage
                ));

        byte[] data1 = new byte[1_000];
        byte[] data2 = new byte[1_000];
        byte[] data3 = new byte[1_000];

        new Random().nextBytes(data1);
        new Random().nextBytes(data2);
        new Random().nextBytes(data3);

        UUID uuid = UUID.randomUUID();
        LocalDateTime date = LocalDateTime.now();
        String dateFormatted =  date.format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HHmmss"));

        String key1 = "TestBucket/TestPool/level-0/shouldListAll__" + dateFormatted + "/" + uuid + ".part0";
        String key2 = "TestBucket/TestPool/level-0/shouldListAll__" + dateFormatted + "/" + uuid + ".part1";
        String key3 = "TestBucket/TestPool/level-0/shouldListAll__" + dateFormatted + "/" + uuid + ".part2";

        Path pathUpload1 = tempDir.resolve(UUID.randomUUID().toString());
        Path pathUpload2 = tempDir.resolve(UUID.randomUUID().toString());
        Path pathUpload3 = tempDir.resolve(UUID.randomUUID().toString());

        Files.write(
                pathUpload1,
                data1,
                StandardOpenOption.CREATE,
                StandardOpenOption.APPEND,
                StandardOpenOption.WRITE
        );
        Files.write(
                pathUpload2,
                data2,
                StandardOpenOption.CREATE,
                StandardOpenOption.APPEND,
                StandardOpenOption.WRITE
        );
        Files.write(
                pathUpload3,
                data3,
                StandardOpenOption.CREATE,
                StandardOpenOption.APPEND,
                StandardOpenOption.WRITE
        );

        new OnepartUploadCallable(pathUpload1, key1, s3RequestService).call();
        new OnepartUploadCallable(pathUpload2, key2, s3RequestService).call();
        new OnepartUploadCallable(pathUpload3, key3, s3RequestService).call();


        List<String> keys = new ListCallable(
                "TestBucket/TestPool/level-0/shouldListAll__" + dateFormatted + "/" + uuid + ".part4",
                s3RequestService
        ).call();

        Assertions.assertEquals(0, keys.size());
    }

    @Test
    void shouldListPaged(@TempDir Path tempDir) throws Exception {
        ch.qos.logback.classic.Logger logger = (ch.qos.logback.classic.Logger) LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);
        logger.setLevel(Level.ERROR);
//        logger = (ch.qos.logback.classic.Logger) LoggerFactory.getLogger(software.amazon.awssdk.core.interceptor.ExecutionInterceptorChain.class);
//        logger.setLevel(Level.INFO);
//        logger = (ch.qos.logback.classic.Logger) LoggerFactory.getLogger(software.amazon.awssdk.core.internal.io.AwsChunkedEncodingInputStream.class);
//        logger.setLevel(Level.INFO);
//        logger = (ch.qos.logback.classic.Logger) LoggerFactory.getLogger(software.amazon.awssdk.auth.signer.Aws4Signer.class);
//        logger.setLevel(Level.INFO);

        S3Storage s3Storage = new S3Storage(
                Region.AWS_GLOBAL,
                "test",
                "1111",
                "1111",
                Paths.get("level-0"),
                new URI("http://127.0.0.1:9090/"),
                "STANDARD"
        );

        S3ClientFactory s3ClientFactory = new S3ClientFactoryImpl(new ImmutableList<>(List.of(s3Storage)));
        S3RequestService s3RequestService = new S3RequestServiceImpl(
                new CallableExecutorImpl(),
                new CallableSupplierFactory(
                        s3ClientFactory,
                        s3Storage
                ));

        List<byte[]> byteList = new ArrayList<>(1100);

        for(int i = 0; i<1100;i++){
            byte[] tmp = new byte[10];
            new Random().nextBytes(tmp);
            byteList.add(tmp);
        }

        UUID uuid = UUID.randomUUID();
        LocalDateTime date = LocalDateTime.now();
        String dateFormatted =  date.format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HHmmss"));
        for(int i=0;i<byteList.size();i++){
            String key = "TestBucket/TestPool/level-0/shouldListPaged__" + dateFormatted + "/" + uuid + ".part"+i;
            Path pathUpload = tempDir.resolve(UUID.randomUUID().toString());
            Files.write(
                    pathUpload,
                    byteList.get(i),
                    StandardOpenOption.CREATE,
                    StandardOpenOption.APPEND,
                    StandardOpenOption.WRITE
            );
            new OnepartUploadCallable(pathUpload, key, s3RequestService).call();
        }


        List<String> keys = new ListCallable(
                "TestBucket/TestPool/level-0/shouldListPaged__" + dateFormatted + "/",
                s3RequestService
        ).call();

        Assertions.assertEquals(1100, keys.size());
    }
}
