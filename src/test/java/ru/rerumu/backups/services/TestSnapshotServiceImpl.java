package ru.rerumu.backups.services;


import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.rerumu.backups.models.Snapshot;
import ru.rerumu.backups.models.ZFSDataset;
import ru.rerumu.backups.models.zfs_dataset_properties.EncryptionProperty;
import ru.rerumu.backups.services.impl.SnapshotServiceImpl;
import ru.rerumu.backups.zfs_api.ZFSCommandFactory;
import ru.rerumu.backups.zfs_api.zfs.ListSnapshotsCommand;
import ru.rerumu.backups.zfs_api.zfs.SnapshotCommand;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class TestSnapshotServiceImpl {

    @Mock
    ZFSCommandFactory zfsCommandFactory = Mockito.mock(ZFSCommandFactory.class);
    @Mock
    SnapshotCommand snapshotCommand = mock(SnapshotCommand.class);
    @Mock
    ListSnapshotsCommand listSnapshotsCommand = mock(ListSnapshotsCommand.class);

    @Test
    void shouldCreateRecursive() throws Exception{
        String name="zfs-s3-snapshot";
        ZFSDataset dataset = new ZFSDataset("TestDataset",new ArrayList<>(), EncryptionProperty.OFF);
        List<Snapshot> snapshotList = new ArrayList<>();
        snapshotList.add(new Snapshot("TestDataset@tmp-01"));
        snapshotList.add(new Snapshot("TestDataset@tmp-02"));
        snapshotList.add(new Snapshot("TestDataset@tmp-03"));
        snapshotList.add(new Snapshot("TestDataset@zfs-s3-snapshot"));

        when(zfsCommandFactory.getSnapshotCommand(any(),anyString(),any())).thenReturn(snapshotCommand);
        when(zfsCommandFactory.getListSnapshotsCommand(any(),any())).thenReturn(listSnapshotsCommand);
        when(listSnapshotsCommand.execute()).thenReturn(snapshotList);

        SnapshotServiceImpl snapshotService = new SnapshotServiceImpl(zfsCommandFactory);
        Snapshot resultSnapshot = snapshotService.createRecursiveSnapshot(dataset,name);

        Assertions.assertEquals(resultSnapshot,new Snapshot("TestDataset@zfs-s3-snapshot"));
    }

    @Test
    void shouldNotCreate(){
        String name="zfs-s3-snapshot";
        ZFSDataset dataset = new ZFSDataset("TestDataset",new ArrayList<>(), EncryptionProperty.OFF);
        List<Snapshot> snapshotList = new ArrayList<>();
        snapshotList.add(new Snapshot("TestDataset@tmp-01"));
        snapshotList.add(new Snapshot("TestDataset@tmp-02"));
        snapshotList.add(new Snapshot("TestDataset@tmp-03"));

        when(zfsCommandFactory.getSnapshotCommand(any(),anyString(),any())).thenReturn(snapshotCommand);
        when(zfsCommandFactory.getListSnapshotsCommand(any(),any())).thenReturn(listSnapshotsCommand);
        when(listSnapshotsCommand.execute()).thenReturn(snapshotList);

        SnapshotServiceImpl snapshotService = new SnapshotServiceImpl(zfsCommandFactory);

        Assertions.assertThrows(NoSuchElementException.class,()->snapshotService.createRecursiveSnapshot(
                dataset,
                name
        ));
    }
}
