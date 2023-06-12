package ru.rerumu.zfs;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.rerumu.zfs_backup_s3.utils.processes.factories.ProcessWrapperFactory;
import ru.rerumu.zfs_backup_s3.zfs.callable.GetPool;
import ru.rerumu.zfs_backup_s3.zfs.factories.StdConsumerFactory;
import ru.rerumu.zfs_backup_s3.zfs.factories.ZFSCallableFactory;
import ru.rerumu.zfs_backup_s3.zfs.models.Dataset;
import ru.rerumu.zfs_backup_s3.zfs.models.Pool;

import java.io.BufferedInputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.function.Consumer;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class TestGetPool {

    @Mock
    ProcessWrapperFactory processWrapperFactory;
    @Mock
    Callable<Void> processWrapper;
    @Mock
    StdConsumerFactory stdConsumerFactory;
    @Mock
    ZFSCallableFactory zfsCallableFactory;



    @Test
    void shouldCall() throws Exception{
        Dataset dataset1 = new Dataset("TestDataset1",new ArrayList<>());
        Dataset dataset2 = new Dataset("TestDataset2",new ArrayList<>());
        Dataset dataset3 = new Dataset("TestDataset3",new ArrayList<>());

        Callable<Dataset> callable1 =(Callable<Dataset>) mock(Callable.class);
        Callable<Dataset> callable2 =(Callable<Dataset>) mock(Callable.class);
        Callable<Dataset> callable3 =(Callable<Dataset>) mock(Callable.class);

        when(processWrapperFactory.getProcessWrapper(any(),any())).thenReturn(processWrapper);
        when(stdConsumerFactory.getDatasetStringStdConsumer(any())).thenAnswer(invocationOnMock -> {
            List<String> datasetStrings = invocationOnMock.getArgument(0);
            datasetStrings.add("TestDataset1");
            datasetStrings.add("TestDataset2");
            datasetStrings.add("TestDataset3");
            return (Consumer<BufferedInputStream>)mock(Consumer.class);
        });
        when(zfsCallableFactory.getDatasetCallable(anyString()))
                .thenReturn(callable1)
                .thenReturn(callable2)
                .thenReturn(callable3)
        ;
        when(callable1.call()).thenReturn(dataset1);
        when(callable2.call()).thenReturn(dataset2);
        when(callable3.call()).thenReturn(dataset3);



        Callable<Pool> getPool = new GetPool("TestPool", processWrapperFactory,zfsCallableFactory,stdConsumerFactory);

        Pool res = getPool.call();



        verify(processWrapperFactory).getProcessWrapper(eq(List.of(
                "zfs","list","-rH","-o","name","-s","name","TestPool"
        )),any());

        Pool shouldPool = new Pool("TestPool", List.of(dataset1,dataset2,dataset3));
        Assertions.assertEquals(shouldPool,res);
    }
}
