package ru.rerumu.backups.services.zfs;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.rerumu.backups.services.zfs.factories.StdConsumerFactory;
import ru.rerumu.backups.services.zfs.factories.ZFSCallableFactory;
import ru.rerumu.backups.services.zfs.models.Dataset;
import ru.rerumu.backups.services.zfs.models.Pool;
import ru.rerumu.backups.utils.processes.factories.ProcessWrapperFactory;
import ru.rerumu.backups.utils.processes.factories.StdProcessorFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class TestGetPool {

    @Mock
    ProcessWrapperFactory processWrapperFactory;

    @Mock
    Callable<Void> processWrapper;

    @Mock
    ZFSService zfsService;

    @Mock
    StdConsumerFactory stdConsumerFactory;

    @Mock
    StdProcessorFactory stdProcessorFactory;

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
            return null;
        });
        when(zfsCallableFactory.getDatasetCallable(anyString()))
                .thenReturn(callable1)
                .thenReturn(callable2)
                .thenReturn(callable3)
        ;
        when(callable1.call()).thenReturn(dataset1);
        when(callable2.call()).thenReturn(dataset2);
        when(callable3.call()).thenReturn(dataset3);



        Callable<Pool> getPool = new GetPool("TestPool", processWrapperFactory,zfsCallableFactory,stdConsumerFactory, stdProcessorFactory);

        Pool res = getPool.call();



        verify(processWrapperFactory).getProcessWrapper(eq(List.of(
                "zfs","list","-rH","-o","name","-s","name","TestPool"
        )),any());

        Pool shouldPool = new Pool("TestPool", List.of(dataset1,dataset2,dataset3));
        Assertions.assertEquals(shouldPool,res);
    }
}
