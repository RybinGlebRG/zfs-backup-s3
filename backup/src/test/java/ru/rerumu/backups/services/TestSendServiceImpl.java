package ru.rerumu.backups.services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.rerumu.zfs_backup_s3.backups.services.impl.SendServiceImpl;
import ru.rerumu.zfs_backup_s3.backups.services.impl.SnapshotNamingService4Mock;
import ru.rerumu.zfs_backup_s3.local_storage.services.LocalStorageService;
import ru.rerumu.zfs_backup_s3.local_storage.services.impl.LocalStorageService4Mock;
import ru.rerumu.zfs_backup_s3.zfs.ZFSService4Mock;
import ru.rerumu.zfs_backup_s3.zfs.models.Dataset;
import ru.rerumu.zfs_backup_s3.zfs.models.Pool;
import ru.rerumu.zfs_backup_s3.zfs.models.Snapshot;

import java.util.ArrayList;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

// TODO: Check
@ExtendWith(MockitoExtension.class)
public class TestSendServiceImpl {

    @Mock
    SnapshotNamingService4Mock snapshotNamingService;
    @Mock
    ZFSService4Mock zfsService;
    @Mock
    LocalStorageService4Mock localStorageService;

    private SendServiceImpl sendService;

    @BeforeEach
    public void beforeEach(){
        sendService = new SendServiceImpl(
                snapshotNamingService,
                zfsService,
                localStorageService
        );
    }

    @Test
    void shouldSend() throws Exception {
        /*
            Creating test objects
         */
        List<Dataset> datasetList = new ArrayList<>();
        datasetList.add(new Dataset("TestPool",new ArrayList<>()));
        Pool pool = new Pool("TestPool", datasetList);


        /*
            Mocking
         */
        when(zfsService.getPool("TestPool")).thenReturn(pool);
        when(localStorageService.areFilesPresent()).thenReturn(false);
        when(snapshotNamingService.generateName())
                .thenReturn("zfs-backup-s3__level-0__2023-03-22T194000");
        when(zfsService.createRecursiveSnapshot(
                new Dataset("TestPool",new ArrayList<>()),
                "zfs-backup-s3__level-0__2023-03-22T194000")
        )
                .thenReturn(new Snapshot("TestPool@zfs-backup-s3__level-0__2023-03-22T194000"));


        /*
            Steps
         */
        sendService.send("TestPool","TestBucket", null);


        /*
            Asserts
         */
        verify(zfsService).createRecursiveSnapshot(
                new Dataset("TestPool",new ArrayList<>()),
                "zfs-backup-s3__level-0__2023-03-22T194000"
        );
        verify(zfsService).send(
                eq(new Snapshot("TestPool@zfs-backup-s3__level-0__2023-03-22T194000")),
                any()
        );
    }
}
