package ru.rerumu.zfs;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.rerumu.utils.processes.factories.ProcessWrapperFactory;
import ru.rerumu.utils.processes.factories.StdProcessorFactory;
import ru.rerumu.zfs.callable.CreateSnapshot;
import ru.rerumu.zfs.models.Dataset;

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

    @Mock
    StdProcessorFactory stdProcessorFactory;

    @Test
    void shouldCall() throws Exception{
        Dataset dataset = new Dataset("TestDataset",new ArrayList<>());

        when(processWrapperFactory.getProcessWrapper(any(),any())).thenReturn(processWrapper);


        Callable<Void> createSnapshot = new CreateSnapshot(
                dataset,
                "zfs-backup-s3__2023-03-22T194000",
                false,
                processWrapperFactory,
                stdProcessorFactory
        );

        createSnapshot.call();



        verify(processWrapperFactory).getProcessWrapper(eq(List.of(
                "zfs","snapshot","TestDataset@zfs-backup-s3__2023-03-22T194000"
        )),any());
    }

    @Test
    void shouldCallRecursive() throws Exception{
        Dataset dataset = new Dataset("TestDataset",new ArrayList<>());

        when(processWrapperFactory.getProcessWrapper(any(),any())).thenReturn(processWrapper);


        Callable<Void> createSnapshot = new CreateSnapshot(
                dataset,
                "zfs-backup-s3__2023-03-22T194000",
                true,
                processWrapperFactory,
                stdProcessorFactory
        );

        createSnapshot.call();



        verify(processWrapperFactory).getProcessWrapper(eq(List.of(
                "zfs","snapshot","-r","TestDataset@zfs-backup-s3__2023-03-22T194000"
        )),any());
    }
}
