package ru.rerumu.backups.services.zfs;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.rerumu.backups.services.zfs.models.Dataset;
import ru.rerumu.backups.utils.processes.factories.ProcessWrapperFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;

import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
public class TestCreateSnapshot {
    @Mock
    ProcessWrapperFactory processWrapperFactory;

    @Mock
    ExecutorService executorService;

    @Mock
    Callable<Void> processWrapper;

    @Test
    void shouldCall() throws Exception{
        Dataset dataset = new Dataset("TestDataset",new ArrayList<>());

        when(processWrapperFactory.getProcessWrapper(any(),any(),any())).thenReturn(processWrapper);


        Callable<Void> createSnapshot = new CreateSnapshot(
                dataset,
                "zfs-backup-s3__2023-03-22T194000",
                false,
                processWrapperFactory,
                executorService
        );

        createSnapshot.call();



        verify(processWrapperFactory).getProcessWrapper(eq(List.of(
                "zfs","snapshot","TestDataset@zfs-backup-s3__2023-03-22T194000"
        )),any(),any());
    }

    @Test
    void shouldCallRecursive() throws Exception{
        Dataset dataset = new Dataset("TestDataset",new ArrayList<>());

        when(processWrapperFactory.getProcessWrapper(any(),any(),any())).thenReturn(processWrapper);


        Callable<Void> createSnapshot = new CreateSnapshot(
                dataset,
                "zfs-backup-s3__2023-03-22T194000",
                true,
                processWrapperFactory,
                executorService
        );

        createSnapshot.call();



        verify(processWrapperFactory).getProcessWrapper(eq(List.of(
                "zfs","snapshot","-r","TestDataset@zfs-backup-s3__2023-03-22T194000"
        )),any(),any());
    }
}
