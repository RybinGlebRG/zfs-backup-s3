package ru.rerumu.backups.services.zfs;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.rerumu.backups.services.zfs.consumers.GetDatasetStringStdConsumer;
import ru.rerumu.backups.services.zfs.factories.StdConsumerFactory;
import ru.rerumu.backups.services.zfs.models.Dataset;
import ru.rerumu.backups.services.zfs.models.Pool;
import ru.rerumu.backups.utils.processes.ProcessFactory;
import ru.rerumu.backups.utils.processes.TriConsumer;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class TestGetPool {

    @Mock
    ProcessFactory processFactory;

    @Mock
    Callable<Void> processWrapper;

    @Mock
    ZFSService zfsService;

    @Mock
    StdConsumerFactory stdConsumerFactory;


    @Test
    void shouldCall() throws Exception{
        Dataset dataset1 = new Dataset("TestDataset1",new ArrayList<>());
        Dataset dataset2 = new Dataset("TestDataset2",new ArrayList<>());
        Dataset dataset3 = new Dataset("TestDataset3",new ArrayList<>());

        when(processFactory.getProcessWrapper(any(),any(),any())).thenReturn(processWrapper);
        when(stdConsumerFactory.getDatasetStringStdConsumer(any())).thenAnswer(invocationOnMock -> {
            List<String> datasetStrings = invocationOnMock.getArgument(0);
            datasetStrings.add("TestDataset1");
            datasetStrings.add("TestDataset2");
            datasetStrings.add("TestDataset3");
            return null;
        });
        when(zfsService.getDataset(anyString()))
                .thenReturn(dataset1)
                .thenReturn(dataset2)
                .thenReturn(dataset3)
        ;


        Callable<Pool> getPool = new GetPool("TestPool",processFactory,zfsService,stdConsumerFactory);

        Pool res = getPool.call();



        verify(processFactory).getProcessWrapper(eq(List.of(
                "zfs","list","-rH","-o","name","-s","name","TestPool"
        )),any(),any());

        Pool shouldPool = new Pool("TestPool", List.of(dataset1,dataset2,dataset3));
        Assertions.assertEquals(shouldPool,res);
    }
}
