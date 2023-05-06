package ru.rerumu.backups.services;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.rerumu.s3.repositories.impl.S3StreamRepositoryImpl;
import ru.rerumu.backups.services.impl.SendServiceImpl;
import ru.rerumu.zfs.SnapshotNamingService;
import ru.rerumu.zfs.SnapshotService;
import ru.rerumu.zfs.ZFSService;
import ru.rerumu.zfs.factories.StdConsumerFactory;
import ru.rerumu.zfs.factories.ZFSCallableFactory;
import ru.rerumu.zfs.models.Dataset;
import ru.rerumu.zfs.models.Pool;
import ru.rerumu.zfs.models.Snapshot;

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

        SendServiceImpl sendService = new SendServiceImpl(
                s3StreamRepository,
                snapshotService,
                snapshotNamingService,
                zfsService,
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
        verify(zfsService).send(eq(shouldSnapshot),any());
    }
}
