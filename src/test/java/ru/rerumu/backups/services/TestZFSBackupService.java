package ru.rerumu.backups.services;

import org.junit.jupiter.api.Test;
import org.mockito.InOrder;
import org.mockito.Mockito;
import ru.rerumu.backups.exceptions.BaseSnapshotNotFoundException;
import ru.rerumu.backups.exceptions.CompressorException;
import ru.rerumu.backups.exceptions.EncryptException;
import ru.rerumu.backups.exceptions.IncorrectHashException;
import ru.rerumu.backups.io.S3Loader;
import ru.rerumu.backups.models.Snapshot;
import ru.rerumu.backups.models.ZFSFileSystem;
import ru.rerumu.backups.repositories.ZFSFileSystemRepository;

import java.io.*;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

import static org.mockito.Matchers.*;

public class TestZFSBackupService {



    @Test
    void shouldBackupInOrder() throws CompressorException, IOException, InterruptedException, EncryptException, IllegalArgumentException, BaseSnapshotNotFoundException, NoSuchAlgorithmException, IncorrectHashException, ExecutionException {
        ZFSFileSystemRepository mockedZfsFileSystemRepository = Mockito.mock(ZFSFileSystemRepository.class);

        List<ZFSFileSystem> zfsFileSystemList = new ArrayList<>();
        zfsFileSystemList.add(new ZFSFileSystem(
                "ExternalPool",
                List.of(
                        new Snapshot("ExternalPool@auto-20220326-150000"),
                        new Snapshot("ExternalPool@auto-20220327-060000"),
                        new Snapshot("ExternalPool@auto-20220327-150000"),
                        new Snapshot("ExternalPool@auto-20220328-150000")
                )
        ));
        Mockito.when(mockedZfsFileSystemRepository.getFilesystemsTreeList("ExternalPool"))
                .thenReturn(zfsFileSystemList);


        SnapshotSender mockedSnapshotSender = Mockito.mock(SnapshotSender.class);
        InOrder inOrder = Mockito.inOrder(mockedSnapshotSender);


        S3Loader mockedS3Loader = Mockito.mock(S3Loader.class);

        ZFSBackupService zfsBackupService = new ZFSBackupService(
                true,
                mockedZfsFileSystemRepository,
                mockedSnapshotSender
        );

        zfsBackupService.zfsBackupFull(
                mockedS3Loader,
                "auto-20220327-150000",
                "ExternalPool"
        );

        inOrder.verify(mockedSnapshotSender).sendStartingFromFull(List.of(
                new Snapshot("ExternalPool@auto-20220326-150000"),
                new Snapshot("ExternalPool@auto-20220327-060000"),
                new Snapshot("ExternalPool@auto-20220327-150000")
        ));


        inOrder.verify(mockedSnapshotSender).checkSent(
                List.of(
                        new Snapshot("ExternalPool@auto-20220326-150000"),
                        new Snapshot("ExternalPool@auto-20220327-060000"),
                        new Snapshot("ExternalPool@auto-20220327-150000")
                ),
                mockedS3Loader
        );
    }

    @Test
    void shouldBackupOnlyBase() throws IOException, InterruptedException, CompressorException, EncryptException, BaseSnapshotNotFoundException, NoSuchAlgorithmException, IncorrectHashException, ExecutionException {
        ZFSFileSystemRepository mockedZfsFileSystemRepository = Mockito.mock(ZFSFileSystemRepository.class);

        List<ZFSFileSystem> zfsFileSystemList = new ArrayList<>();
        zfsFileSystemList.add(new ZFSFileSystem(
                "ExternalPool",
                List.of(
                        new Snapshot("ExternalPool@auto-20220326-150000"),
                        new Snapshot("ExternalPool@auto-20220327-060000"),
                        new Snapshot("ExternalPool@auto-20220327-150000"),
                        new Snapshot("ExternalPool@auto-20220328-150000")
                )
        ));
        Mockito.when(mockedZfsFileSystemRepository.getFilesystemsTreeList("ExternalPool"))
                .thenReturn(zfsFileSystemList);


        SnapshotSender mockedSnapshotSender = Mockito.mock(SnapshotSender.class);
        InOrder inOrder = Mockito.inOrder(mockedSnapshotSender);


        S3Loader mockedS3Loader = Mockito.mock(S3Loader.class);

        ZFSBackupService zfsBackupService = new ZFSBackupService(
                true,
                mockedZfsFileSystemRepository,
                mockedSnapshotSender
        );

        zfsBackupService.zfsBackupFull(
                mockedS3Loader,
                "auto-20220326-150000",
                "ExternalPool"
        );

        inOrder.verify(mockedSnapshotSender).sendStartingFromFull(List.of(
                new Snapshot("ExternalPool@auto-20220326-150000")
        ));

        inOrder.verify(mockedSnapshotSender).checkSent(
                List.of(
                        new Snapshot("ExternalPool@auto-20220326-150000")
                ),
                mockedS3Loader
        );
    }

    @Test
    void shouldNotBackupAny() throws IOException, InterruptedException, CompressorException, EncryptException, BaseSnapshotNotFoundException, NoSuchAlgorithmException, IncorrectHashException, ExecutionException {
        ZFSFileSystemRepository mockedZfsFileSystemRepository = Mockito.mock(ZFSFileSystemRepository.class);

        List<ZFSFileSystem> zfsFileSystemList = new ArrayList<>();
        zfsFileSystemList.add(new ZFSFileSystem(
                "ExternalPool",
                List.of(
                        new Snapshot("ExternalPool@auto-20220326-150000"),
                        new Snapshot("ExternalPool@auto-20220327-060000"),
                        new Snapshot("ExternalPool@auto-20220327-150000"),
                        new Snapshot("ExternalPool@auto-20220328-150000")
                )
        ));
        Mockito.when(mockedZfsFileSystemRepository.getFilesystemsTreeList("ExternalPool"))
                .thenReturn(zfsFileSystemList);


        SnapshotSender mockedSnapshotSender = Mockito.mock(SnapshotSender.class);

        S3Loader mockedS3Loader = Mockito.mock(S3Loader.class);

        ZFSBackupService zfsBackupService = new ZFSBackupService(
                true,
                mockedZfsFileSystemRepository,
                mockedSnapshotSender
        );

        zfsBackupService.zfsBackupFull(
                mockedS3Loader,
                "auto-20220325-150000",
                "ExternalPool"
        );
        Mockito.verify(mockedSnapshotSender,Mockito.never()).sendStartingFromFull(Mockito.any());
        Mockito.verify(mockedSnapshotSender,Mockito.never()).checkSent(any(),any());
    }

    @Test
    void shouldNotBackupAny1() throws IOException, InterruptedException, CompressorException, EncryptException, BaseSnapshotNotFoundException, NoSuchAlgorithmException, IncorrectHashException, ExecutionException {
        ZFSFileSystemRepository mockedZfsFileSystemRepository = Mockito.mock(ZFSFileSystemRepository.class);

        List<ZFSFileSystem> zfsFileSystemList = new ArrayList<>();
        zfsFileSystemList.add(new ZFSFileSystem(
                "ExternalPool",
               new ArrayList<>()
        ));
        Mockito.when(mockedZfsFileSystemRepository.getFilesystemsTreeList("ExternalPool"))
                .thenReturn(zfsFileSystemList);


        SnapshotSender mockedSnapshotSender = Mockito.mock(SnapshotSender.class);

        S3Loader mockedS3Loader = Mockito.mock(S3Loader.class);

        ZFSBackupService zfsBackupService = new ZFSBackupService(
                true,
                mockedZfsFileSystemRepository,
                mockedSnapshotSender
        );

        zfsBackupService.zfsBackupFull(
                mockedS3Loader,
                "auto-20220326-150000",
                "ExternalPool"
        );
        Mockito.verify(mockedSnapshotSender,Mockito.never()).sendStartingFromFull(Mockito.any());
        Mockito.verify(mockedSnapshotSender,Mockito.never()).checkSent(any(),any());
    }
}
