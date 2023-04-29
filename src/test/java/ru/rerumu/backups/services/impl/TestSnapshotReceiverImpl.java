package ru.rerumu.backups.services.impl;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;


import ru.rerumu.backups.services.ZFSFileReader;
import ru.rerumu.backups.factories.ZFSFileReaderFactory;
import ru.rerumu.backups.models.ZFSPool;
import ru.rerumu.backups.services.SnapshotReceiver;
import ru.rerumu.backups.factories.ZFSProcessFactory;
import ru.rerumu.backups.zfs_api.zfs.ZFSReceive;

import java.nio.file.Paths;

@Disabled
@ExtendWith(MockitoExtension.class)
class TestSnapshotReceiverImpl {

    @Mock
    ZFSProcessFactory zfsProcessFactory;
    @Mock
    ZFSPool zfsPool;
    @Mock
    ZFSFileReaderFactory zfsFileReaderFactory;
    @Mock
    ZFSFileReader zfsFileReader;
    @Mock
    ZFSReceive zfsReceive;

//    @Test
//    void shouldReceiveOne() throws Exception {
//        Mockito.when(zfsFileReaderFactory.getZFSFileReader(Mockito.any(),Mockito.any())).thenReturn(zfsFileReader);
//        Mockito.when(zfsProcessFactory.getZFSReceive(Mockito.any())).thenReturn(zfsReceive);
//
//        SnapshotReceiver snapshotReceiver = new SnapshotReceiverImpl(
//                zfsProcessFactory,
//                zfsPool,
//                zfsFileReaderFactory
//        );
//
//
//        snapshotReceiver.receiveSnapshotPart(Paths.get("ExternalPool-Applications@auto-20220326-150000__ExternalPool-Applications@auto-20220327-150000.part0.ready"));
//
//        InOrder inOrder = Mockito.inOrder(zfsProcessFactory,zfsFileReaderFactory, zfsFileReader, zfsReceive);
//
//        Mockito.verify(zfsProcessFactory,Mockito.times(1)).getZFSReceive(Mockito.any());
//        inOrder.verify(zfsProcessFactory).getZFSReceive(Mockito.any());
//
//        Mockito.verify(zfsFileReaderFactory,Mockito.times(1)).getZFSFileReader(Mockito.any(),Mockito.any());
//        inOrder.verify(zfsFileReaderFactory).getZFSFileReader(Mockito.any(),Mockito.any());
//
//        Mockito.verify(zfsFileReader,Mockito.times(1)).read();
//        inOrder.verify(zfsFileReader).read();
//
//        Mockito.verify(zfsReceive,Mockito.never()).close();
//    }
//
//    @Test
//    void shouldReceiveTwo() throws Exception {
//        Mockito.when(zfsFileReaderFactory.getZFSFileReader(Mockito.any(),Mockito.any())).thenReturn(zfsFileReader);
//        Mockito.when(zfsProcessFactory.getZFSReceive(Mockito.any())).thenReturn(zfsReceive);
//
//        SnapshotReceiver snapshotReceiver = new SnapshotReceiverImpl(
//                zfsProcessFactory,
//                zfsPool,
//                zfsFileReaderFactory
//        );
//
//        InOrder inOrder = Mockito.inOrder(zfsProcessFactory,zfsFileReaderFactory, zfsFileReader,zfsReceive);
//
//        snapshotReceiver.receiveSnapshotPart(Paths.get("ExternalPool-Applications@auto-20220326-150000__ExternalPool-Applications@auto-20220327-150000.part0"));
//        snapshotReceiver.receiveSnapshotPart(Paths.get("ExternalPool-Applications@auto-20220326-150000__ExternalPool-Applications@auto-20220328-150000.part0"));
//
//
//        Mockito.verify(zfsProcessFactory,Mockito.times(2)).getZFSReceive(Mockito.any());
//        inOrder.verify(zfsProcessFactory).getZFSReceive(Mockito.any());
//
//        Mockito.verify(zfsFileReaderFactory,Mockito.times(2)).getZFSFileReader(Mockito.any(),Mockito.any());
//        inOrder.verify(zfsFileReaderFactory).getZFSFileReader(Mockito.any(),Mockito.any());
//
//        Mockito.verify(zfsFileReader,Mockito.times(2)).read();
//        inOrder.verify(zfsFileReader).read();
//
//        Mockito.verify(zfsReceive,Mockito.times(1)).close();
//        inOrder.verify(zfsReceive).close();
//
//        inOrder.verify(zfsProcessFactory).getZFSReceive(Mockito.any());
//
//        inOrder.verify(zfsFileReaderFactory).getZFSFileReader(Mockito.any(),Mockito.any());
//
//        inOrder.verify(zfsFileReader).read();
//    }
//
//    @Test
//    void shouldReceiveMultipart() throws Exception {
//        Mockito.when(zfsFileReaderFactory.getZFSFileReader(Mockito.any(),Mockito.any())).thenReturn(zfsFileReader);
//        Mockito.when(zfsProcessFactory.getZFSReceive(Mockito.any())).thenReturn(zfsReceive);
//
//        SnapshotReceiver snapshotReceiver = new SnapshotReceiverImpl(
//                zfsProcessFactory,
//                zfsPool,
//                zfsFileReaderFactory
//        );
//
//        InOrder inOrder = Mockito.inOrder(zfsProcessFactory,zfsFileReaderFactory, zfsFileReader, zfsReceive);
//
//        snapshotReceiver.receiveSnapshotPart(Paths.get("ExternalPool-Applications@auto-20220326-150000__ExternalPool-Applications@auto-20220327-150000.part0"));
//        snapshotReceiver.receiveSnapshotPart(Paths.get("ExternalPool-Applications@auto-20220326-150000__ExternalPool-Applications@auto-20220327-150000.part1"));
//
//
//        Mockito.verify(zfsProcessFactory,Mockito.times(1)).getZFSReceive(Mockito.any());
//        inOrder.verify(zfsProcessFactory).getZFSReceive(Mockito.any());
//
//        Mockito.verify(zfsFileReaderFactory,Mockito.times(2)).getZFSFileReader(Mockito.any(),Mockito.any());
//        inOrder.verify(zfsFileReaderFactory).getZFSFileReader(Mockito.any(),Mockito.any());
//
//        Mockito.verify(zfsFileReader,Mockito.times(2)).read();
//        inOrder.verify(zfsFileReader).read();
//
//        Mockito.verify(zfsReceive,Mockito.never()).close();
//
//        inOrder.verify(zfsFileReaderFactory).getZFSFileReader(Mockito.any(),Mockito.any());
//
//        inOrder.verify(zfsFileReader).read();
//    }

}