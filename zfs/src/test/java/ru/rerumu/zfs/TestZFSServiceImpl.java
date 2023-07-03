package ru.rerumu.zfs;


import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.rerumu.zfs_backup_s3.zfs.factories.ZFSCallableFactory;
import ru.rerumu.zfs_backup_s3.zfs.factories.ZFSCallableFactoryMock;
import ru.rerumu.zfs_backup_s3.zfs.impl.ZFSServiceImpl;
import ru.rerumu.zfs_backup_s3.zfs.models.Dataset;
import ru.rerumu.zfs_backup_s3.zfs.models.Pool;
import ru.rerumu.zfs_backup_s3.zfs.models.Snapshot;
import ru.rerumu.zfs_backup_s3.zfs.services.SnapshotService;
import ru.rerumu.zfs_backup_s3.zfs.services.SnapshotServiceMock;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.util.ArrayList;
import java.util.concurrent.Callable;
import java.util.function.Consumer;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class TestZFSServiceImpl {

    @Mock
    ZFSCallableFactoryMock zfsCallableFactory;

    @Mock
    Callable<Pool> poolCallable;

    @Mock
    Callable<Void> voidCallable;

    @Mock
    SnapshotServiceMock snapshotService;


    @Test
    void shouldGetPool() throws Exception{

        when(zfsCallableFactory.getPoolCallable("TestPool")).thenReturn(poolCallable);

        ZFSServiceImpl zfsService = new ZFSServiceImpl(zfsCallableFactory,snapshotService);
        zfsService.getPool("TestPool");
    }

//    @Test
//    void shouldThrowExceptionWhileGetPool() throws Exception{
//        when(zfsCallableFactory.getPoolCallable(anyString())).thenReturn(poolCallable);
//        when(poolCallable.call()).thenThrow(RuntimeException.class);
//
//        ZFSServiceImpl zfsService = new ZFSServiceImpl(zfsCallableFactory,snapshotService);
//
//        Assertions.assertThrows(RuntimeException.class,()->zfsService.getPool("TestPool"));
//    }

    @Test
    void shouldSend() throws Exception{
        Snapshot snapshot = new Snapshot("Test@tmp1");
        Consumer<BufferedInputStream> consumer =(Consumer<BufferedInputStream>) mock(Consumer.class);

        when(zfsCallableFactory.getSendReplica(snapshot,consumer)).thenReturn(voidCallable);

        ZFSServiceImpl zfsService = new ZFSServiceImpl(zfsCallableFactory,snapshotService);
        zfsService.send(snapshot,consumer);
    }

    @Test
    void shouldReceive() throws Exception{
        Pool pool = new Pool("TestPool",new ArrayList<>());
        Consumer<BufferedOutputStream> consumer =(Consumer<BufferedOutputStream>) mock(Consumer.class);

        when(zfsCallableFactory.getReceive(pool,consumer)).thenReturn(voidCallable);

        ZFSServiceImpl zfsService = new ZFSServiceImpl(zfsCallableFactory,snapshotService);
        zfsService.receive(pool,consumer);
    }

    @Test
    void shouldCreateRecursiveSnapshot() throws Exception{
        Dataset dataset = new Dataset("TestDataset",new ArrayList<>());

        ZFSServiceImpl zfsService = new ZFSServiceImpl(zfsCallableFactory,snapshotService);
        zfsService.createRecursiveSnapshot(dataset,"tmp1");

        verify(snapshotService).createRecursiveSnapshot(dataset,"tmp1");
    }
    @Test
    void shouldThrowExceptionWhileCreateRecursiveSnapshot() throws Exception{
        Dataset dataset = new Dataset("TestDataset",new ArrayList<>());

        ZFSServiceImpl zfsService = new ZFSServiceImpl(zfsCallableFactory,snapshotService);

        Assertions.assertThrows(IllegalArgumentException.class,()->zfsService.createRecursiveSnapshot(dataset,"tmp 1"));
    }
}
