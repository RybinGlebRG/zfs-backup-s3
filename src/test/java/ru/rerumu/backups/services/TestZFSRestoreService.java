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

    @Test
    void shouldRestore(@TempDir Path tempDir) throws Exception{
        LocalBackupRepository localBackupRepository = Mockito.mock(LocalBackupRepository.class);
        SnapshotReceiver snapshotReceiver = Mockito.mock(SnapshotReceiver.class);

        Mockito.when(localBackupRepository.getDatasets()).thenReturn(List.of("Test"));
        Mockito.when(localBackupRepository.getNextPart(Mockito.eq("Test"),Mockito.eq(null)))
                .thenReturn(tempDir.resolve("part0"));
        Mockito.when(localBackupRepository.getNextPart(Mockito.eq("Test"),Mockito.eq("part0")))
                .thenReturn(tempDir.resolve("part1"));
        Mockito.when(localBackupRepository.getNextPart(Mockito.eq("Test"),Mockito.eq("part1")))
                .thenThrow(new FinishedFlagException());


        InOrder inOrder = Mockito.inOrder(localBackupRepository,snapshotReceiver);

        ZFSRestoreService zfsRestoreService = new ZFSRestoreService(
                localBackupRepository,
                snapshotReceiver,
                List.of("Test"));

        zfsRestoreService.zfsReceive();

        inOrder.verify(localBackupRepository).getNextPart(Mockito.eq("Test"),Mockito.eq(null));
        inOrder.verify(snapshotReceiver).receiveSnapshotPart(Mockito.eq(tempDir.resolve("part0")));
        inOrder.verify(localBackupRepository).getNextPart(Mockito.eq("Test"),Mockito.eq("part0"));
        inOrder.verify(snapshotReceiver).receiveSnapshotPart(Mockito.eq(tempDir.resolve("part1")));
        inOrder.verify(localBackupRepository).getNextPart(Mockito.eq("Test"),Mockito.eq("part1"));
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