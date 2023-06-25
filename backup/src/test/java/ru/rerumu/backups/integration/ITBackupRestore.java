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

import ru.rerumu.zfs_backup_s3.backups.factories.StdConsumerFactory;
import ru.rerumu.zfs_backup_s3.backups.factories.impl.StdConsumerFactoryImpl;
import ru.rerumu.zfs_backup_s3.backups.services.ReceiveService;
import ru.rerumu.zfs_backup_s3.backups.services.SendService;
import ru.rerumu.zfs_backup_s3.backups.services.SnapshotNamingService;
import ru.rerumu.zfs_backup_s3.backups.services.impl.ReceiveServiceImpl;
import ru.rerumu.zfs_backup_s3.backups.services.impl.SendServiceImpl;
import ru.rerumu.zfs_backup_s3.backups.services.impl.SnapshotNamingServiceImpl;

import ru.rerumu.zfs_backup_s3.s3.S3ServiceFactory;
import ru.rerumu.zfs_backup_s3.s3.S3ServiceFactoryImpl;
import ru.rerumu.zfs_backup_s3.s3.models.S3Storage;
import ru.rerumu.zfs_backup_s3.s3.S3Service;

import ru.rerumu.zfs_backup_s3.zfs.ZFSServiceMock;
import ru.rerumu.zfs_backup_s3.zfs.models.Snapshot;
import ru.rerumu.zfs_backup_s3.zfs.models.Dataset;
import ru.rerumu.zfs_backup_s3.zfs.models.Pool;
import ru.rerumu.zfs_backup_s3.zfs.ZFSService;

import software.amazon.awssdk.regions.Region;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.net.URI;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.function.Consumer;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
public class ITBackupRestore {

    @Mock
    ZFSServiceMock zfsServiceSend;

    @Mock
    ZFSServiceMock zfsServiceRestore;

    Map<String,String> env = System.getenv();

    private SendService prepareSend(Path tempDir) throws Exception{

        S3Storage s3Storage = new S3Storage(
                Region.of(env.get("ZFS_BACKUP_S3_REGION")),
                env.get("TEST_BUCKET"),
                env.get("ZFS_BACKUP_S3_ACCESS_KEY_ID"),
                env.get("ZFS_BACKUP_S3_SECRET_ACCESS_KEY"),
                Paths.get(env.get("ZFS_BACKUP_S3_FULL_PREFIX")),
                new URI(env.get("ZFS_BACKUP_S3_ENDPOINT_URL")),
                env.get("ZFS_BACKUP_S3_FULL_STORAGE_CLASS")
        );
        S3ServiceFactory s3ServiceFactory =  new S3ServiceFactoryImpl();
        S3Service s3Service = s3ServiceFactory.getS3Service(
                s3Storage,
                12_000_000,
                30_000_000L,
                tempDir,
                UUID.randomUUID()
        );
        SnapshotNamingService snapshotNamingService = new SnapshotNamingServiceImpl();
        StdConsumerFactory stdConsumerFactory = new StdConsumerFactoryImpl(s3Service);
        SendService sendService = new SendServiceImpl(
                snapshotNamingService,
                zfsServiceSend,
                stdConsumerFactory
        );
        return sendService;

    }

    private ReceiveService prepareReceive(Path tempDir) throws Exception{
        S3Storage s3Storage = new S3Storage(
                Region.of(env.get("ZFS_BACKUP_S3_REGION")),
                env.get("TEST_BUCKET"),
                env.get("ZFS_BACKUP_S3_ACCESS_KEY_ID"),
                env.get("ZFS_BACKUP_S3_SECRET_ACCESS_KEY"),
                Paths.get(env.get("ZFS_BACKUP_S3_FULL_PREFIX")),
                new URI(env.get("ZFS_BACKUP_S3_ENDPOINT_URL")),
                env.get("ZFS_BACKUP_S3_FULL_STORAGE_CLASS")
        );
        S3ServiceFactory s3ServiceFactory =  new S3ServiceFactoryImpl();
        S3Service s3Service = s3ServiceFactory.getS3Service(
                s3Storage,
                12_000_000,
                30_000_000L,
                tempDir,
                UUID.randomUUID()
        );
        SnapshotNamingService snapshotNamingService = new SnapshotNamingServiceImpl();
        StdConsumerFactory stdConsumerFactory = new StdConsumerFactoryImpl(s3Service);
        ReceiveService receiveService = new ReceiveServiceImpl(
                zfsServiceRestore,
                snapshotNamingService,
                s3Service,
                stdConsumerFactory
        );



        return receiveService;
    }

    @Test
    void shouldBackupRestore(@TempDir Path tempDir1, @TempDir Path tempDir2) throws Exception{
        ch.qos.logback.classic.Logger logger = (ch.qos.logback.classic.Logger) LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);
        logger.setLevel(Level.DEBUG);

        String bucketName= "test";
        SnapshotNamingService snapshotNamingService = new SnapshotNamingServiceImpl();
        List<Dataset> datasets = new ArrayList<>();
        datasets.add(new Dataset(
                "TestPool",
                List.of(
                        new Snapshot("TestPool@tmp1"),
                        new Snapshot("TestPool@tmp2"),
                        new Snapshot("TestPool@tmp3")
                )
        ));
        datasets.add(new Dataset(
                "TestPool/encrypted",
                List.of(
                        new Snapshot("TestPool/encrypted@tmp1"),
                        new Snapshot("TestPool/encrypted@tmp2"),
                        new Snapshot("TestPool/encrypted@tmp3")
                )
        ));
        datasets.add(new Dataset(
                "TestPool/encrypted/tested",
                List.of(
                        new Snapshot("TestPool/encrypted/tested@tmp1"),
                        new Snapshot("TestPool/encrypted/tested@tmp2"),
                        new Snapshot("TestPool/encrypted/tested@tmp3")
                )
        ));
        Pool pool = new Pool("TestPool",datasets);
        Snapshot snapshot = new Snapshot("TestPool@"+snapshotNamingService.generateName());
        byte[] data = new byte[50_000_000];

        new Random().nextBytes(data);

        when(zfsServiceSend.getPool(anyString()))
                .thenReturn(pool);
        when(zfsServiceSend.createRecursiveSnapshot(any(),anyString()))
                .thenReturn(snapshot);
        doAnswer(invocationOnMock -> {
            Consumer<BufferedInputStream> tmpConsumer = invocationOnMock.getArgument(1);
            tmpConsumer.accept(new BufferedInputStream(new ByteArrayInputStream(data)));
            return null;
        })
                .when(zfsServiceSend).send(any(),any());

        SendService sendService = prepareSend(tempDir1);
        sendService.send("TestPool",bucketName);


        Pool restorePool = new Pool("RestorePool",new ArrayList<>());
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();

        when(zfsServiceRestore.getPool(anyString()))
                .thenReturn(restorePool);
        doAnswer(invocationOnMock -> {
            Consumer<BufferedOutputStream> tmpConsumer = invocationOnMock.getArgument(1);
            tmpConsumer.accept(new BufferedOutputStream(byteArrayOutputStream));
            return null;
        })
                .when(zfsServiceRestore).receive(any(),any());

        ReceiveService receiveService = prepareReceive(tempDir2);
        receiveService.receive(bucketName,"RestorePool","TestPool");

        byteArrayOutputStream.flush();
        byte[] dataRestored = byteArrayOutputStream.toByteArray();

        Assertions.assertArrayEquals(data,dataRestored);
    }
}
