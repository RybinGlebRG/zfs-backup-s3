package ru.rerumu.backups.services;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.InOrder;
import org.mockito.Mockito;
import ru.rerumu.backups.exceptions.*;
import ru.rerumu.backups.models.Snapshot;
import ru.rerumu.backups.models.ZFSDataset;
import ru.rerumu.backups.models.zfs_dataset_properties.EncryptionProperty;
import ru.rerumu.backups.repositories.ZFSFileSystemRepository;

import java.io.*;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

public class TestZFSBackupService {



    @Test
    void shouldBackupInOrder() throws Exception {
        ZFSFileSystemRepository mockedZfsFileSystemRepository = Mockito.mock(ZFSFileSystemRepository.class);

        List<ZFSDataset> zfsDatasetList = new ArrayList<>();
        zfsDatasetList.add(new ZFSDataset(
                "ExternalPool",
                List.of(
                        new Snapshot("ExternalPool@auto-20220326-150000"),
                        new Snapshot("ExternalPool@auto-20220327-060000"),
                        new Snapshot("ExternalPool@auto-20220327-150000"),
                        new Snapshot("ExternalPool@auto-20220328-150000")
                ),
                EncryptionProperty.ON
        ));
        Mockito.when(mockedZfsFileSystemRepository.getFilesystemsTreeList("ExternalPool"))
                .thenReturn(zfsDatasetList);


        SnapshotSender mockedSnapshotSender = Mockito.mock(SnapshotSender.class);
        InOrder inOrder = Mockito.inOrder(mockedSnapshotSender);


//        S3Loader mockedS3Loader = Mockito.mock(S3Loader.class);

        ZFSBackupService zfsBackupService = new ZFSBackupService(
                mockedZfsFileSystemRepository,
                mockedSnapshotSender,
                new DatasetPropertiesChecker(false)
        );

        zfsBackupService.zfsBackupFull(
                "auto-20220327-150000",
                "ExternalPool"
        );

        inOrder.verify(mockedSnapshotSender).sendStartingFromFull("ExternalPool",
                List.of(
                new Snapshot("ExternalPool@auto-20220326-150000"),
                new Snapshot("ExternalPool@auto-20220327-060000"),
                new Snapshot("ExternalPool@auto-20220327-150000")
        ));


//        inOrder.verify(mockedSnapshotSender).checkSent(
//                List.of(
//                        new Snapshot("ExternalPool@auto-20220326-150000"),
//                        new Snapshot("ExternalPool@auto-20220327-060000"),
//                        new Snapshot("ExternalPool@auto-20220327-150000")
//                ),
//                mockedS3Loader
//        );
    }

    @Test
    void shouldBackupOnlyBase() throws Exception {
        ZFSFileSystemRepository mockedZfsFileSystemRepository = Mockito.mock(ZFSFileSystemRepository.class);

        List<ZFSDataset> zfsDatasetList = new ArrayList<>();
        zfsDatasetList.add(new ZFSDataset(
                "ExternalPool",
                List.of(
                        new Snapshot("ExternalPool@auto-20220326-150000"),
                        new Snapshot("ExternalPool@auto-20220327-060000"),
                        new Snapshot("ExternalPool@auto-20220327-150000"),
                        new Snapshot("ExternalPool@auto-20220328-150000")
                ),
                EncryptionProperty.ON
        ));
        Mockito.when(mockedZfsFileSystemRepository.getFilesystemsTreeList("ExternalPool"))
                .thenReturn(zfsDatasetList);


        SnapshotSender mockedSnapshotSender = Mockito.mock(SnapshotSender.class);
        InOrder inOrder = Mockito.inOrder(mockedSnapshotSender);


//        S3Loader mockedS3Loader = Mockito.mock(S3Loader.class);

        ZFSBackupService zfsBackupService = new ZFSBackupService(
                mockedZfsFileSystemRepository,
                mockedSnapshotSender,
                new DatasetPropertiesChecker(false)
        );

        zfsBackupService.zfsBackupFull(
                "auto-20220326-150000",
                "ExternalPool"
        );

        inOrder.verify(mockedSnapshotSender).sendStartingFromFull("ExternalPool",
                List.of(
                new Snapshot("ExternalPool@auto-20220326-150000")
        ));

//        inOrder.verify(mockedSnapshotSender).checkSent(
//                List.of(
//                        new Snapshot("ExternalPool@auto-20220326-150000")
//                ),
//                mockedS3Loader
//        );
    }

    @Test
    void shouldNotBackupAny() throws Exception {
        ZFSFileSystemRepository mockedZfsFileSystemRepository = Mockito.mock(ZFSFileSystemRepository.class);

        List<ZFSDataset> zfsDatasetList = new ArrayList<>();
        zfsDatasetList.add(new ZFSDataset(
                "ExternalPool",
                List.of(
                        new Snapshot("ExternalPool@auto-20220326-150000"),
                        new Snapshot("ExternalPool@auto-20220327-060000"),
                        new Snapshot("ExternalPool@auto-20220327-150000"),
                        new Snapshot("ExternalPool@auto-20220328-150000")
                ),
                EncryptionProperty.ON
        ));
        Mockito.when(mockedZfsFileSystemRepository.getFilesystemsTreeList("ExternalPool"))
                .thenReturn(zfsDatasetList);


        SnapshotSender mockedSnapshotSender = Mockito.mock(SnapshotSender.class);

//        S3Loader mockedS3Loader = Mockito.mock(S3Loader.class);

        ZFSBackupService zfsBackupService = new ZFSBackupService(
                mockedZfsFileSystemRepository,
                mockedSnapshotSender,
                new DatasetPropertiesChecker(false)
        );

        zfsBackupService.zfsBackupFull(
                "auto-20220325-150000",
                "ExternalPool"
        );
        Mockito.verify(mockedSnapshotSender,Mockito.never()).sendStartingFromFull(Mockito.any(),Mockito.any());
//        Mockito.verify(mockedSnapshotSender,Mockito.never()).checkSent(any(),any());
    }

    @Test
    void shouldNotBackupAny1() throws Exception {
        ZFSFileSystemRepository mockedZfsFileSystemRepository = Mockito.mock(ZFSFileSystemRepository.class);

        List<ZFSDataset> zfsDatasetList = new ArrayList<>();
        zfsDatasetList.add(new ZFSDataset(
                "ExternalPool",
               new ArrayList<>(),
                EncryptionProperty.ON
        ));
        Mockito.when(mockedZfsFileSystemRepository.getFilesystemsTreeList("ExternalPool"))
                .thenReturn(zfsDatasetList);


        SnapshotSender mockedSnapshotSender = Mockito.mock(SnapshotSender.class);

//        S3Loader mockedS3Loader = Mockito.mock(S3Loader.class);

        ZFSBackupService zfsBackupService = new ZFSBackupService(
                mockedZfsFileSystemRepository,
                mockedSnapshotSender,
                new DatasetPropertiesChecker(false)
        );

        zfsBackupService.zfsBackupFull(
                "auto-20220326-150000",
                "ExternalPool"
        );
        Mockito.verify(mockedSnapshotSender,Mockito.never()).sendStartingFromFull(Mockito.any(),Mockito.any());
//        Mockito.verify(mockedSnapshotSender,Mockito.never()).checkSent(any(),any());
    }

    @Test
    void shouldNotSendUnencrypted() throws Exception{
        ZFSFileSystemRepository zfsFileSystemRepository = Mockito.mock(ZFSFileSystemRepository.class);

        List<ZFSDataset> zfsDatasetList = new ArrayList<>();
        zfsDatasetList.add(new ZFSDataset(
                "ExternalPool",
                new ArrayList<>(),
                EncryptionProperty.OFF
        ));
        Mockito.when(zfsFileSystemRepository.getFilesystemsTreeList("ExternalPool"))
                .thenReturn(zfsDatasetList);

        SnapshotSender mockedSnapshotSender = Mockito.mock(SnapshotSender.class);

        ZFSBackupService zfsBackupService = new ZFSBackupService(
                zfsFileSystemRepository,
                mockedSnapshotSender,
                new DatasetPropertiesChecker(true)
        );

        Assertions.assertThrows(IncompatibleDatasetException.class,()->{
            zfsBackupService.zfsBackupFull(
                    "auto-20220326-150000",
                    "ExternalPool"
            );
        });
    }
}
