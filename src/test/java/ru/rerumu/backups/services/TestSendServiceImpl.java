package ru.rerumu.backups.services;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.rerumu.backups.exceptions.SendError;
import ru.rerumu.backups.services.zfs.factories.StdConsumerFactory;
import ru.rerumu.backups.services.zfs.models.Snapshot;
import ru.rerumu.backups.services.s3.models.Bucket;
import ru.rerumu.backups.services.zfs.models.Dataset;
import ru.rerumu.backups.services.zfs.models.Pool;
import ru.rerumu.backups.services.s3.repositories.impl.S3StreamRepositoryImpl;
import ru.rerumu.backups.services.impl.SendServiceImpl;
import ru.rerumu.backups.services.zfs.SnapshotNamingService;
import ru.rerumu.backups.services.zfs.SnapshotService;
import ru.rerumu.backups.services.zfs.ZFSService;
import ru.rerumu.backups.services.zfs.factories.ZFSCallableFactory;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class TestSendServiceImpl {

    @Mock
    S3StreamRepositoryImpl s3StreamRepository;

    @Mock
    SnapshotService snapshotService;

    @Mock
    SnapshotNamingService snapshotNamingService;

    @Mock
    ZFSService zfsService;

    @Mock
    ZFSCallableFactory zfsCallableFactory;

    @Mock
    StdConsumerFactory stdConsumerFactory;

    @Test
    void shouldSend() throws Exception {
        List<Dataset> datasetList = new ArrayList<>();
        datasetList.add(new Dataset("TestPool",new ArrayList<>()));
        Pool pool = new Pool("TestPool", datasetList);
        Callable<Void> sendReplica =(Callable<Void>) mock(Callable.class);

        when(zfsService.getPool(anyString())).thenReturn(pool);
        when(snapshotNamingService.generateName())
                .thenReturn("zfs-backup-s3__2023-03-22T194000");
        when(snapshotService.createRecursiveSnapshot(any(),anyString()))
                .thenReturn(new Snapshot("TestPool@zfs-backup-s3__2023-03-22T194000"));
        when(zfsCallableFactory.getSendReplica(any(),any())).thenReturn(sendReplica);


        SendServiceImpl sendService = new SendServiceImpl(
                s3StreamRepository,
                snapshotService,
                snapshotNamingService,
                zfsService,
                zfsCallableFactory,
                stdConsumerFactory
        );
        sendService.send("TestPool","TestBucket");


        Dataset shouldDataset = new Dataset("TestPool", new ArrayList<>());
        Snapshot shouldSnapshot = new Snapshot("TestPool@zfs-backup-s3__2023-03-22T194000");

        verify(snapshotService).createRecursiveSnapshot(
                shouldDataset,
                "zfs-backup-s3__2023-03-22T194000"
        );
        verify(stdConsumerFactory).getSendStdoutConsumer(any(),eq("TestBucket/TestPool/level-0/zfs-backup-s3__2023-03-22T194000/"));
        verify(zfsCallableFactory).getSendReplica(eq(shouldSnapshot),any());
    }
}
