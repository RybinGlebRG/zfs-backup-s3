package ru.rerumu.backups.services;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.rerumu.backups.exceptions.SendError;
import ru.rerumu.backups.factories.ZFSProcessFactory;
import ru.rerumu.backups.models.Snapshot;
import ru.rerumu.backups.models.s3.Bucket;
import ru.rerumu.backups.models.zfs.Dataset;
import ru.rerumu.backups.models.zfs.Pool;
import ru.rerumu.backups.services.s3.repositories.impl.S3StreamRepositoryImpl;
import ru.rerumu.backups.services.impl.SendServiceImpl;
import ru.rerumu.backups.services.zfs.ZFSService;
import ru.rerumu.backups.services.zfs.factories.ZFSCallableFactory;
import ru.rerumu.backups.zfs_api.zfs.ZFSSend;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class TestSendServiceImpl {

    @Mock
    ZFSProcessFactory zfsProcessFactory;

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

    @Test
    void shouldSend() throws Exception {
        List<Dataset> datasetList = new ArrayList<>();
        datasetList.add(new Dataset("TestPool",new ArrayList<>()));
        Pool pool = new Pool("TestPool", datasetList);
        Bucket bucket = new Bucket("TestBucket");
        ZFSSend zfsSend= Mockito.mock(ZFSSend.class);
        BufferedInputStream bufferedInputStream = Mockito.mock(BufferedInputStream.class);

        when(snapshotNamingService.generateName())
                .thenReturn("zfs-backup-s3_2023-03-22T19:40:00");
        when(snapshotService.createRecursiveSnapshot(any(),anyString()))
                .thenReturn(new Snapshot("TestPool@zfs-backup-s3_2023-03-22T19:40:00"));
        when(zfsProcessFactory.getZFSSendReplicate(any()))
                .thenReturn(zfsSend);
        when(zfsSend.getBufferedInputStream()).thenReturn(bufferedInputStream);

        SendServiceImpl sendService = new SendServiceImpl(
                zfsProcessFactory,
                s3StreamRepository,
                snapshotService,
                snapshotNamingService,
                zfsService,
                zfsCallableFactory
        );

        InOrder inOrder = Mockito.inOrder(
                zfsProcessFactory,s3StreamRepository,snapshotService,snapshotNamingService, zfsSend
        );

        sendService.send(pool,bucket);

        Dataset shouldDataset = new Dataset("TestPool", new ArrayList<>());
        Snapshot shouldSnapshot = new Snapshot("TestPool@zfs-backup-s3_2023-03-22T19:40:00");

        inOrder.verify(snapshotService).createRecursiveSnapshot(
                shouldDataset,
                "zfs-backup-s3_2023-03-22T19:40:00"
        );
        inOrder.verify(zfsProcessFactory).getZFSSendReplicate(shouldSnapshot);
        inOrder.verify(s3StreamRepository).add(
                "TestBucket/TestPool/level-0/zfs-backup-s3_2023-03-22T19:40:00/",
                bufferedInputStream
        );
        inOrder.verify(zfsSend).close();

       verify(zfsSend,never()).kill();
    }

    @Test
    void shouldKill() throws Exception {
        List<Dataset> datasetList = new ArrayList<>();
        datasetList.add(new Dataset("TestPool",new ArrayList<>()));
        Pool pool = new Pool("TestPool", datasetList);
        Bucket bucket = new Bucket("TestBucket");
        ZFSSend zfsSend= Mockito.mock(ZFSSend.class);
        BufferedInputStream bufferedInputStream = Mockito.mock(BufferedInputStream.class);

        when(snapshotNamingService.generateName())
                .thenReturn("zfs-backup-s3_2023-03-22T19:40:00");
        when(snapshotService.createRecursiveSnapshot(any(),anyString()))
                .thenReturn(new Snapshot("TestPool@zfs-backup-s3_2023-03-22T19:40:00"));
        when(zfsProcessFactory.getZFSSendReplicate(any()))
                .thenReturn(zfsSend);
        when(zfsSend.getBufferedInputStream()).thenReturn(bufferedInputStream);
        doThrow(IOException.class).when(s3StreamRepository).add(anyString(),any(BufferedInputStream.class));

        SendServiceImpl sendService = new SendServiceImpl(
                zfsProcessFactory,
                s3StreamRepository,
                snapshotService,
                snapshotNamingService,
                zfsService,
                zfsCallableFactory
        );

        InOrder inOrder = Mockito.inOrder(
                zfsProcessFactory,s3StreamRepository,snapshotService,snapshotNamingService, zfsSend
        );

        Assertions.assertThrows(SendError.class,()->sendService.send(pool,bucket));

        Dataset shouldDataset = new Dataset("TestPool", new ArrayList<>());
        Snapshot shouldSnapshot = new Snapshot("TestPool@zfs-backup-s3_2023-03-22T19:40:00");

        inOrder.verify(snapshotService).createRecursiveSnapshot(
                shouldDataset,
                "zfs-backup-s3_2023-03-22T19:40:00"
        );
        inOrder.verify(zfsProcessFactory).getZFSSendReplicate(shouldSnapshot);
        inOrder.verify(s3StreamRepository).add(
                "TestBucket/TestPool/level-0/zfs-backup-s3_2023-03-22T19:40:00/",
                bufferedInputStream
        );
        inOrder.verify(zfsSend).kill();
        inOrder.verify(zfsSend).close();

    }
}
