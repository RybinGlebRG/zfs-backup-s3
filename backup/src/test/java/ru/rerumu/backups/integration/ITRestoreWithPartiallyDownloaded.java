package ru.rerumu.backups.integration;

import ch.qos.logback.classic.Level;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.rerumu.zfs_backup_s3.backups.services.ReceiveService;
import ru.rerumu.zfs_backup_s3.backups.services.SnapshotNamingService;
import ru.rerumu.zfs_backup_s3.backups.services.impl.ReceiveServiceImpl;
import ru.rerumu.zfs_backup_s3.backups.services.impl.SnapshotNamingServiceImpl;
import ru.rerumu.zfs_backup_s3.local_storage.factories.ZFSFileReaderFactory;
import ru.rerumu.zfs_backup_s3.local_storage.factories.ZFSFileWriterFactory;
import ru.rerumu.zfs_backup_s3.local_storage.factories.impl.ZFSFileReaderFactoryImpl;
import ru.rerumu.zfs_backup_s3.local_storage.factories.impl.ZFSFileWriterFactoryImpl;
import ru.rerumu.zfs_backup_s3.local_storage.services.LocalStorageService;
import ru.rerumu.zfs_backup_s3.local_storage.services.impl.ConsecutiveLocalStorageService;
import ru.rerumu.zfs_backup_s3.s3.S3Service;
import ru.rerumu.zfs_backup_s3.s3.S3ServiceFactory;
import ru.rerumu.zfs_backup_s3.s3.S3ServiceFactoryImpl;
import ru.rerumu.zfs_backup_s3.s3.models.S3Storage;
import ru.rerumu.zfs_backup_s3.zfs.ZFSService4Mock;
import ru.rerumu.zfs_backup_s3.zfs.models.Pool;
import ru.rerumu.zfs_backup_s3.zfs.models.Snapshot;
import software.amazon.awssdk.regions.Region;

import java.io.*;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.*;
import java.util.function.Consumer;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class ITRestoreWithPartiallyDownloaded {

    @Mock
    ZFSService4Mock zfsServiceSend;

    @Mock
    ZFSService4Mock zfsServiceRestore;

    S3Service s3ServiceSend;
    S3Service s3ServiceReceive;

    Map<String, String> env = System.getenv();

    private void prepareSend() throws Exception {

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
        s3ServiceSend = s3ServiceFactory.getS3Service(
                s3Storage,
                12_000_000
        );
    }

    private ReceiveService prepareReceive(Path tempDir) throws Exception {
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
        s3ServiceReceive = s3ServiceFactory.getS3Service(
                s3Storage,
                8_000_000
        );
        ZFSFileReaderFactory zfsFileReaderFactory = new ZFSFileReaderFactoryImpl();
        ZFSFileWriterFactory zfsFileWriterFactory = new ZFSFileWriterFactoryImpl(30_000_000L);
        LocalStorageService localStorageService = new ConsecutiveLocalStorageService(
                zfsFileReaderFactory,
                zfsFileWriterFactory,
                s3ServiceReceive,
                UUID.randomUUID().toString(),
                tempDir);
        ReceiveService receiveService = new ReceiveServiceImpl(
                zfsServiceRestore,
                s3ServiceReceive,
                localStorageService
        );


        return receiveService;
    }

    private Path getNew(String postfix, Path tempDir) {
        String unique = UUID.randomUUID().toString();
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(unique);
        if (postfix != null) {
            stringBuilder.append(postfix);
        }
        return tempDir.resolve(stringBuilder.toString());
    }

    private List<Path> writeToFiles(BufferedInputStream bufferedInputStream, Path tempDir) throws IOException {
        int n = 0;
        int len;
        List<Path> paths = new ArrayList<>();
        while (true) {
            Path path = getNew(".part" + n++, tempDir);
            paths.add(path);
            try (OutputStream outputStream = Files.newOutputStream(path);
                 BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(outputStream)) {
                long written = 0;

                byte[] buf = new byte[8192];
                while ((len = bufferedInputStream.read(buf)) != -1) {
                    bufferedOutputStream.write(buf, 0, len);
                    written += len;
                    if (written >= 30_000_000L) {
                        break;
                    }
                }
            }
            if (len == -1){
                break;
            }
        }
        return paths;
    }

    /**
     * One file is already downloaded, two remaining
     */
    @Test
    void shouldRestoreWithPartiallyDownloaded(@TempDir Path tempDir1, @TempDir Path tempDir2) throws Exception {
        ch.qos.logback.classic.Logger logger = (ch.qos.logback.classic.Logger) LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);
        logger.setLevel(Level.INFO);

        String bucketName = "test";
        SnapshotNamingService snapshotNamingService = new SnapshotNamingServiceImpl();
        Snapshot snapshot = new Snapshot("TestPool@" + snapshotNamingService.generateName());
        byte[] data = new byte[80_000_000];

        new Random().nextBytes(data);
        List<Path> paths = writeToFiles(new BufferedInputStream(new ByteArrayInputStream(data)), tempDir1);
        String key = String.format(
                "level-%d/%s/",
                0,
                snapshot.getName()
        );

        // Sending wrong first file while preserving data locally
        byte[] tempBytes = Files.readAllBytes(paths.get(0));
        Files.writeString(paths.get(0),"Random string", StandardCharsets.UTF_8, StandardOpenOption.TRUNCATE_EXISTING);

        prepareSend();
        for (Path file: paths){
            s3ServiceSend.upload(file,key);
            Files.delete(file);
        }

        // Make it look like first file is already downloaded
        Files.write(tempDir2.resolve(paths.get(0).getFileName()), tempBytes, StandardOpenOption.CREATE);


        ReceiveService receiveService = prepareReceive(tempDir2);

        Pool restorePool = new Pool("RestorePool", new ArrayList<>());
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();

        when(zfsServiceRestore.getPool(anyString()))
                .thenReturn(restorePool);
        doAnswer(invocationOnMock -> {
            Consumer<BufferedOutputStream> tmpConsumer = invocationOnMock.getArgument(1);
            tmpConsumer.accept(new BufferedOutputStream(byteArrayOutputStream));
            return null;
        })
                .when(zfsServiceRestore).receive(any(), any());


        receiveService.receive(bucketName, "RestorePool");

        byteArrayOutputStream.flush();
        byte[] dataRestored = byteArrayOutputStream.toByteArray();

        Assertions.assertArrayEquals(data, dataRestored);
    }
}
