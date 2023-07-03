package ru.rerumu.zfs;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.rerumu.zfs_backup_s3.utils.processes.factories.ProcessWrapperFactory;
import ru.rerumu.zfs_backup_s3.utils.processes.factories.ProcessWrapperFactoryMock;
import ru.rerumu.zfs_backup_s3.zfs.callable.CreateSnapshot;
import ru.rerumu.zfs_backup_s3.zfs.models.Dataset;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
public class TestCreateSnapshot {
    @Mock
    ProcessWrapperFactoryMock processWrapperFactory;
    @Mock
    Callable<Void> processWrapper;

    @Test
    void shouldCall() throws Exception{
        Dataset dataset = new Dataset("TestDataset",new ArrayList<>());

        when(processWrapperFactory.getProcessWrapper(eq(List.of(
                "zfs","snapshot","TestDataset@zfs-backup-s3__2023-03-22T194000"
        )),eq(null),any(),any())).thenReturn(processWrapper);


        Callable<Void> createSnapshot = new CreateSnapshot(
                dataset,
                "zfs-backup-s3__2023-03-22T194000",
                false,
                processWrapperFactory
        );

        createSnapshot.call();

        verifyNoMoreInteractions(processWrapperFactory);
    }

    @Test
    void shouldCallRecursive() throws Exception{
        Dataset dataset = new Dataset("TestDataset",new ArrayList<>());

        when(processWrapperFactory.getProcessWrapper(eq(List.of(
                "zfs","snapshot","-r","TestDataset@zfs-backup-s3__2023-03-22T194000"
        )),eq(null),any(),any())).thenReturn(processWrapper);


        Callable<Void> createSnapshot = new CreateSnapshot(
                dataset,
                "zfs-backup-s3__2023-03-22T194000",
                true,
                processWrapperFactory
        );

        createSnapshot.call();

        verifyNoMoreInteractions(processWrapperFactory);
    }
}
