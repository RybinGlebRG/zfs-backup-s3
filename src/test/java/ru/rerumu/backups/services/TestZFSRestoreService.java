package ru.rerumu.backups.services;

import org.junit.jupiter.api.Test;
import org.mockito.InOrder;
import org.mockito.Mockito;
import ru.rerumu.backups.exceptions.*;
import ru.rerumu.backups.factories.ZFSProcessFactory;
import ru.rerumu.backups.repositories.LocalBackupRepository;

import java.io.IOException;
import java.util.concurrent.ExecutionException;

class TestZFSRestoreService {

    @Test
    void shouldRestoreOneFile() throws Exception {
        ZFSProcessFactory zfsProcessFactory = Mockito.mock(ZFSProcessFactory.class);
        LocalBackupRepository localBackupRepository = Mockito.mock(LocalBackupRepository.class);
        SnapshotReceiver snapshotReceiver = Mockito.mock(SnapshotReceiver.class);

        Mockito.when(localBackupRepository.getNextInputPath())
                .thenReturn(null)
                .thenThrow(new FinishedFlagException());

        InOrder inOrder = Mockito.inOrder(localBackupRepository,snapshotReceiver);

        ZFSRestoreService zfsRestoreService = new ZFSRestoreService(
                "test",
                zfsProcessFactory,
                true,
                localBackupRepository,
                snapshotReceiver);

        zfsRestoreService.zfsReceive();

        Mockito.verify(localBackupRepository,Mockito.times(2)).getNextInputPath();
        inOrder.verify(localBackupRepository).getNextInputPath();

        Mockito.verify(snapshotReceiver,Mockito.times(1)).receiveSnapshotPart(Mockito.any());
        inOrder.verify(snapshotReceiver).receiveSnapshotPart(Mockito.any());

        Mockito.verify(snapshotReceiver,Mockito.times(1)).finish();
        inOrder.verify(snapshotReceiver).finish();

    }

    @Test
    void shouldRestoreTwoFiles() throws Exception{
        ZFSProcessFactory zfsProcessFactory = Mockito.mock(ZFSProcessFactory.class);
        LocalBackupRepository localBackupRepository = Mockito.mock(LocalBackupRepository.class);
        SnapshotReceiver snapshotReceiver = Mockito.mock(SnapshotReceiver.class);

        Mockito.when(localBackupRepository.getNextInputPath())
                .thenReturn(null)
                .thenReturn(null)
                .thenThrow(new FinishedFlagException());

        InOrder inOrder = Mockito.inOrder(localBackupRepository,snapshotReceiver);

        ZFSRestoreService zfsRestoreService = new ZFSRestoreService("test",zfsProcessFactory,true, localBackupRepository,snapshotReceiver);

        zfsRestoreService.zfsReceive();

        Mockito.verify(localBackupRepository,Mockito.times(3)).getNextInputPath();
        inOrder.verify(localBackupRepository).getNextInputPath();

        Mockito.verify(snapshotReceiver,Mockito.times(2)).receiveSnapshotPart(Mockito.any());
        inOrder.verify(snapshotReceiver).receiveSnapshotPart(Mockito.any());

        Mockito.verify(snapshotReceiver,Mockito.times(1)).finish();
        inOrder.verify(snapshotReceiver).finish();
    }

    @Test
    void shouldWaitForParts() throws Exception {
        ZFSProcessFactory zfsProcessFactory = Mockito.mock(ZFSProcessFactory.class);
        LocalBackupRepository localBackupRepository = Mockito.mock(LocalBackupRepository.class);
        SnapshotReceiver snapshotReceiver = Mockito.mock(SnapshotReceiver.class);

        Mockito.when(localBackupRepository.getNextInputPath())
                .thenThrow(new NoMorePartsException())
                .thenThrow(new FinishedFlagException());

        InOrder inOrder = Mockito.inOrder(localBackupRepository,snapshotReceiver);

        ZFSRestoreService zfsRestoreService = new ZFSRestoreService("test",zfsProcessFactory,true, localBackupRepository,snapshotReceiver);

        zfsRestoreService.zfsReceive();

        inOrder.verify(localBackupRepository,Mockito.times(2)).getNextInputPath();

        Mockito.verify(snapshotReceiver,Mockito.never()).receiveSnapshotPart(Mockito.any());

        Mockito.verify(snapshotReceiver,Mockito.times(1)).finish();
        inOrder.verify(snapshotReceiver).finish();
    }

}