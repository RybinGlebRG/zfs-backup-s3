package ru.rerumu.backups.services;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.rerumu.zfs_backup_s3.backups.services.SnapshotNamingService;
import ru.rerumu.zfs_backup_s3.backups.services.impl.SendServiceImpl;
import ru.rerumu.zfs_backup_s3.local_storage.services.LocalStorageService;
import ru.rerumu.zfs_backup_s3.s3.S3Service;
import ru.rerumu.zfs_backup_s3.zfs.ZFSService;
import ru.rerumu.zfs_backup_s3.zfs.models.Dataset;
import ru.rerumu.zfs_backup_s3.zfs.models.Pool;
import ru.rerumu.zfs_backup_s3.zfs.models.Snapshot;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

// TODO: Check
@Disabled
@ExtendWith(MockitoExtension.class)
public class TestSendServiceImpl {

    @Mock
    SnapshotNamingService snapshotNamingService;
    @Mock
    ZFSService zfsService;
    @Mock
    LocalStorageService localStorageService;
    @Mock
    S3Service s3Service;

    @Test
    void shouldSend() throws Exception {
        List<Dataset> datasetList = new ArrayList<>();
        datasetList.add(new Dataset("TestPool",new ArrayList<>()));
        Pool pool = new Pool("TestPool", datasetList);
        Callable<Void> sendReplica =(Callable<Void>) mock(Callable.class);

        when(zfsService.getPool(anyString())).thenReturn(pool);
        when(snapshotNamingService.generateName())
                .thenReturn("zfs-backup-s3__2023-03-22T194000");
        when(zfsService.createRecursiveSnapshot(any(),anyString()))
                .thenReturn(new Snapshot("TestPool@zfs-backup-s3__2023-03-22T194000"));

        SendServiceImpl sendService = new SendServiceImpl(
                snapshotNamingService,
                zfsService,
                localStorageService
        );
        sendService.send("TestPool","TestBucket", null);


        Dataset shouldDataset = new Dataset("TestPool", new ArrayList<>());
        Snapshot shouldSnapshot = new Snapshot("TestPool@zfs-backup-s3__2023-03-22T194000");

        verify(zfsService).createRecursiveSnapshot(
                shouldDataset,
                "zfs-backup-s3__2023-03-22T194000"
        );
//        verify(stdConsumerFactory).getSendStdoutConsumer(any(),eq("TestBucket/TestPool/level-0/zfs-backup-s3__2023-03-22T194000/"));
        verify(zfsService).send(eq(shouldSnapshot),any());
    }
}
