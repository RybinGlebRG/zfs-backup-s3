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

import ru.rerumu.backups.controllers.BackupController;
import ru.rerumu.backups.controllers.RestoreController;
import ru.rerumu.backups.factories.StdConsumerFactory;
import ru.rerumu.backups.factories.impl.StdConsumerFactoryImpl;
import ru.rerumu.backups.services.ReceiveService;
import ru.rerumu.backups.services.SendService;
import ru.rerumu.backups.services.SnapshotNamingService;
import ru.rerumu.backups.services.impl.ReceiveServiceImpl;
import ru.rerumu.backups.services.impl.SendServiceImpl;
import ru.rerumu.backups.services.impl.SnapshotNamingServiceImpl;

import ru.rerumu.s3.S3ServiceFactory;
import ru.rerumu.s3.S3ServiceFactoryImpl;
import ru.rerumu.s3.models.S3Storage;
import ru.rerumu.s3.S3Service;

import ru.rerumu.zfs.models.Snapshot;
import ru.rerumu.zfs.models.Dataset;
import ru.rerumu.zfs.models.Pool;
import ru.rerumu.zfs.ZFSService;

import software.amazon.awssdk.regions.Region;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.net.URI;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.function.Consumer;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;


// TODO: Check
@ExtendWith(MockitoExtension.class)
public class ITBackupRestore {

    @Mock
    ZFSService zfsServiceSend;

    @Mock
    ZFSService zfsServiceRestore;

    private SendService prepareSend(Path tempDir) throws Exception{
        S3Storage s3Storage = new S3Storage(
                Region.AWS_GLOBAL,
                "test",
                "1111",
                "1111",
                Paths.get("level-0"),
                new URI("http://localhost:9090/"),
                "STANDARD"
        );
        S3ServiceFactory s3ServiceFactory =  new S3ServiceFactoryImpl();
        S3Service s3Service = s3ServiceFactory.getS3Service(
                s3Storage,
                12_000_000,
                30_000_000L,
                tempDir
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
                Region.AWS_GLOBAL,
                "test",
                "1111",
                "1111",
                Path.of(""),
                new URI("http://localhost:9090/"),
                "STANDARD"
        );
        S3ServiceFactory s3ServiceFactory =  new S3ServiceFactoryImpl();
        S3Service s3Service = s3ServiceFactory.getS3Service(
                s3Storage,
                12_000_000,
                30_000_000L,
                tempDir
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
        logger.setLevel(Level.INFO);

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


        BackupController backupController = new BackupController(prepareSend(tempDir1));
        backupController.backupFull("TestPool",bucketName);


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


        RestoreController restoreController = new RestoreController(prepareReceive(tempDir2));
        restoreController.restore(bucketName,"RestorePool");

        byteArrayOutputStream.flush();
        byte[] dataRestored = byteArrayOutputStream.toByteArray();

        Assertions.assertArrayEquals(data,dataRestored);
    }
}
