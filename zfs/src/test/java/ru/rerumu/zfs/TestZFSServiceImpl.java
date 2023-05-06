package ru.rerumu.zfs;


import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.rerumu.zfs.factories.ZFSCallableFactory;
import ru.rerumu.zfs.impl.ZFSServiceImpl;
import ru.rerumu.zfs.models.Pool;
import ru.rerumu.zfs.services.SnapshotService;

import java.util.concurrent.Callable;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class TestZFSServiceImpl {

    @Mock
    ZFSCallableFactory zfsCallableFactory;

    @Mock
    Callable<Pool> poolCallable;

    @Mock
    SnapshotService snapshotService;


    @Test
    void shouldGetPool() throws Exception{

        when(zfsCallableFactory.getPoolCallable(anyString())).thenReturn(poolCallable);

        ZFSServiceImpl zfsService = new ZFSServiceImpl(zfsCallableFactory,snapshotService);
        zfsService.getPool("TestPool");
    }

    @Test
    void shouldThrowExceptionWhileGetPool() throws Exception{
        when(zfsCallableFactory.getPoolCallable(anyString())).thenReturn(poolCallable);
        when(poolCallable.call()).thenThrow(RuntimeException.class);

        ZFSServiceImpl zfsService = new ZFSServiceImpl(zfsCallableFactory,snapshotService);

        Assertions.assertThrows(RuntimeException.class,()->zfsService.getPool("TestPool"));
    }
}
