package ru.rerumu.backups.services;

import org.junit.jupiter.api.Test;
import org.mockito.InOrder;
import org.mockito.Mockito;
import ru.rerumu.backups.exceptions.*;
import ru.rerumu.backups.repositories.FilePartRepository;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

class TestZFSRestoreService {

    @Test
    void shouldRestoreOneFile() throws FinishedFlagException, IncorrectFilePartNameException, CompressorException, NoMorePartsException, IOException, TooManyPartsException, ClassNotFoundException, InterruptedException, EncryptException {
        ZFSProcessFactory zfsProcessFactory = Mockito.mock(ZFSProcessFactory.class);
        FilePartRepository filePartRepository = Mockito.mock(FilePartRepository.class);
        SnapshotReceiver snapshotReceiver = Mockito.mock(SnapshotReceiver.class);

        Mockito.when(filePartRepository.getNextInputPath())
                .thenReturn(null)
                .thenThrow(new FinishedFlagException());

        InOrder inOrder = Mockito.inOrder(filePartRepository,snapshotReceiver);

        ZFSRestoreService zfsRestoreService = new ZFSRestoreService("test",zfsProcessFactory,true,filePartRepository,snapshotReceiver);

        zfsRestoreService.zfsReceive();

        Mockito.verify(filePartRepository,Mockito.times(2)).getNextInputPath();
        inOrder.verify(filePartRepository).getNextInputPath();

        Mockito.verify(snapshotReceiver,Mockito.times(1)).receiveSnapshotPart(Mockito.any());
        inOrder.verify(snapshotReceiver).receiveSnapshotPart(Mockito.any());

        Mockito.verify(snapshotReceiver,Mockito.times(1)).finish();
        inOrder.verify(snapshotReceiver).finish();

    }

    @Test
    void shouldRestoreTwoFiles() throws FinishedFlagException, NoMorePartsException, IOException, TooManyPartsException, IncorrectFilePartNameException, CompressorException, ClassNotFoundException, InterruptedException, EncryptException {
        ZFSProcessFactory zfsProcessFactory = Mockito.mock(ZFSProcessFactory.class);
        FilePartRepository filePartRepository = Mockito.mock(FilePartRepository.class);
        SnapshotReceiver snapshotReceiver = Mockito.mock(SnapshotReceiver.class);

        Mockito.when(filePartRepository.getNextInputPath())
                .thenReturn(null)
                .thenReturn(null)
                .thenThrow(new FinishedFlagException());

        InOrder inOrder = Mockito.inOrder(filePartRepository,snapshotReceiver);

        ZFSRestoreService zfsRestoreService = new ZFSRestoreService("test",zfsProcessFactory,true,filePartRepository,snapshotReceiver);

        zfsRestoreService.zfsReceive();

        Mockito.verify(filePartRepository,Mockito.times(3)).getNextInputPath();
        inOrder.verify(filePartRepository).getNextInputPath();

        Mockito.verify(snapshotReceiver,Mockito.times(2)).receiveSnapshotPart(Mockito.any());
        inOrder.verify(snapshotReceiver).receiveSnapshotPart(Mockito.any());

        Mockito.verify(snapshotReceiver,Mockito.times(1)).finish();
        inOrder.verify(snapshotReceiver).finish();
    }

    @Test
    void shouldWaitForParts() throws FinishedFlagException, IncorrectFilePartNameException, CompressorException, NoMorePartsException, IOException, TooManyPartsException, ClassNotFoundException, InterruptedException, EncryptException {
        ZFSProcessFactory zfsProcessFactory = Mockito.mock(ZFSProcessFactory.class);
        FilePartRepository filePartRepository = Mockito.mock(FilePartRepository.class);
        SnapshotReceiver snapshotReceiver = Mockito.mock(SnapshotReceiver.class);

        Mockito.when(filePartRepository.getNextInputPath())
                .thenThrow(new NoMorePartsException())
                .thenThrow(new FinishedFlagException());

        InOrder inOrder = Mockito.inOrder(filePartRepository,snapshotReceiver);

        ZFSRestoreService zfsRestoreService = new ZFSRestoreService("test",zfsProcessFactory,true,filePartRepository,snapshotReceiver);

        zfsRestoreService.zfsReceive();

        inOrder.verify(filePartRepository,Mockito.times(2)).getNextInputPath();

        Mockito.verify(snapshotReceiver,Mockito.never()).receiveSnapshotPart(Mockito.any());

        Mockito.verify(snapshotReceiver,Mockito.times(1)).finish();
        inOrder.verify(snapshotReceiver).finish();
    }

}