package ru.rerumu.backups.services;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestTemplate;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.InOrder;
import org.mockito.Mockito;
import ru.rerumu.backups.exceptions.*;
import ru.rerumu.backups.factories.ZFSProcessFactory;
import ru.rerumu.backups.repositories.LocalBackupRepository;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.ExecutionException;

class TestZFSRestoreService {

    // TODO: Multiple datasets
    @Test
    void shouldRestore(@TempDir Path tempDir, @TempDir Path localRepositoryDir) throws Exception{
        LocalBackupRepository localBackupRepository = Mockito.mock(LocalBackupRepository.class);
        SnapshotReceiver snapshotReceiver = Mockito.mock(SnapshotReceiver.class);

        Mockito.when(localBackupRepository.getDatasets()).thenReturn(List.of("Test"));
        Mockito.when(localBackupRepository.getParts(Mockito.eq("Test"))).thenReturn(List.of("part0","part1"));
        Mockito.when(localBackupRepository.getPart(Mockito.eq("Test"),Mockito.eq("part0")))
                .thenReturn(localRepositoryDir.resolve("Test").resolve("part0"));
        Mockito.when(localBackupRepository.getPart(Mockito.eq("Test"),Mockito.eq("part1")))
                .thenReturn(localRepositoryDir.resolve("Test").resolve("part1"));

        InOrder inOrder = Mockito.inOrder(localBackupRepository,snapshotReceiver);

        ZFSRestoreService zfsRestoreService = new ZFSRestoreService(
                localBackupRepository,
                snapshotReceiver,
                List.of("Test"));

        zfsRestoreService.zfsReceive();

        inOrder.verify(localBackupRepository).getDatasets();
        inOrder.verify(localBackupRepository).getParts("Test");
        inOrder.verify(localBackupRepository).getPart("Test","part0");
        inOrder.verify(snapshotReceiver).receiveSnapshotPart(localRepositoryDir.resolve("Test").resolve("part0"));
        inOrder.verify(localBackupRepository).getPart("Test","part1");
        inOrder.verify(snapshotReceiver).receiveSnapshotPart(localRepositoryDir.resolve("Test").resolve("part1"));
        inOrder.verify(snapshotReceiver).finish();

    }

    @Test
    void shouldNotContainDataset() throws Exception{
        LocalBackupRepository localBackupRepository = Mockito.mock(LocalBackupRepository.class);
        SnapshotReceiver snapshotReceiver = Mockito.mock(SnapshotReceiver.class);

        Mockito.when(localBackupRepository.getDatasets()).thenReturn(List.of("NotTest"));

        ZFSRestoreService zfsRestoreService = new ZFSRestoreService(
                localBackupRepository,
                snapshotReceiver,
                List.of("Test"));

        Assertions.assertThrows(IllegalArgumentException.class,zfsRestoreService::zfsReceive);
    }

}