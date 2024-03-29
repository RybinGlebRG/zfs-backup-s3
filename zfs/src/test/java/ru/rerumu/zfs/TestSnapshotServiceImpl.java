package ru.rerumu.zfs;


import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.rerumu.zfs_backup_s3.zfs.callable.CreateSnapshot;
import ru.rerumu.zfs_backup_s3.zfs.callable.ListSnapshots;
import ru.rerumu.zfs_backup_s3.zfs.factories.ZFSCallableFactory;
import ru.rerumu.zfs_backup_s3.zfs.factories.ZFSCallableFactoryMock;
import ru.rerumu.zfs_backup_s3.zfs.services.impl.SnapshotServiceImpl;
import ru.rerumu.zfs_backup_s3.zfs.models.Dataset;
import ru.rerumu.zfs_backup_s3.zfs.models.Snapshot;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class TestSnapshotServiceImpl {

    @Mock
    CreateSnapshot createSnapshot;
    @Mock
    ListSnapshots listSnapshots;

    @Mock
    ZFSCallableFactoryMock zfsCallableFactory;

    @Test
    void shouldCreateRecursive() throws Exception{
        String name="zfs-s3-snapshot";
        Dataset dataset = new Dataset("TestDataset",new ArrayList<>());
        List<Snapshot> snapshotList = new ArrayList<>();
        snapshotList.add(new Snapshot("TestDataset@tmp-01"));
        snapshotList.add(new Snapshot("TestDataset@tmp-02"));
        snapshotList.add(new Snapshot("TestDataset@tmp-03"));
        snapshotList.add(new Snapshot("TestDataset@zfs-s3-snapshot"));

        when(zfsCallableFactory.getCreateSnapshotCallable(any(),anyString(),any())).thenReturn(createSnapshot);
        when(zfsCallableFactory.getListSnapshotsCallable(any())).thenReturn(listSnapshots);
        when(listSnapshots.call()).thenReturn(snapshotList);

        SnapshotServiceImpl snapshotService = new SnapshotServiceImpl(zfsCallableFactory);
        Snapshot resultSnapshot = snapshotService.createRecursiveSnapshot(dataset,name);

        Assertions.assertEquals(resultSnapshot,new Snapshot("TestDataset@zfs-s3-snapshot"));
    }

    @Test
    void shouldNotCreate() throws Exception {
        String name="zfs-s3-snapshot";
        Dataset dataset = new Dataset("TestDataset",new ArrayList<>());
        List<Snapshot> snapshotList = new ArrayList<>();
        snapshotList.add(new Snapshot("TestDataset@tmp-01"));
        snapshotList.add(new Snapshot("TestDataset@tmp-02"));
        snapshotList.add(new Snapshot("TestDataset@tmp-03"));

        when(zfsCallableFactory.getCreateSnapshotCallable(any(),anyString(),any())).thenReturn(createSnapshot);
        when(zfsCallableFactory.getListSnapshotsCallable(any())).thenReturn(listSnapshots);
        when(listSnapshots.call()).thenReturn(snapshotList);

        SnapshotServiceImpl snapshotService = new SnapshotServiceImpl(zfsCallableFactory);

        Assertions.assertThrows(NoSuchElementException.class,()->snapshotService.createRecursiveSnapshot(
                dataset,
                name
        ));
    }

    @Test
    void shouldThrowException() throws Exception {
        String name="zfs-s3-snapshot";
        Dataset dataset = new Dataset("TestDataset",new ArrayList<>());

        when(zfsCallableFactory.getCreateSnapshotCallable(any(),anyString(),any())).thenReturn(createSnapshot);
        when(zfsCallableFactory.getListSnapshotsCallable(any())).thenReturn(listSnapshots);
        when(createSnapshot.call()).thenThrow(Exception.class);

        SnapshotServiceImpl snapshotService = new SnapshotServiceImpl(zfsCallableFactory);

        Assertions.assertThrows(RuntimeException.class,()->snapshotService.createRecursiveSnapshot(
                dataset,
                name
        ));
    }
}
