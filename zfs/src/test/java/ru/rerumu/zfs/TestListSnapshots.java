package ru.rerumu.zfs;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.rerumu.utils.processes.factories.ProcessWrapperFactory;
import ru.rerumu.utils.processes.factories.StdProcessorFactory;
import ru.rerumu.zfs.callable.ListSnapshots;
import ru.rerumu.zfs.factories.StdConsumerFactory;
import ru.rerumu.zfs.models.Dataset;
import ru.rerumu.zfs.models.Snapshot;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class TestListSnapshots {

    @Mock
    ProcessWrapperFactory processWrapperFactory;

    @Mock
    Callable<Void> processWrapper;
    @Mock
    StdProcessorFactory stdProcessorFactory;

    @Mock
    StdConsumerFactory stdConsumerFactory;

    @Test
    void shouldCall() throws Exception{
        Dataset dataset = new Dataset("TestDataset", new ArrayList<>());

        when(processWrapperFactory.getProcessWrapper(any(),any())).thenReturn(processWrapper);


        Callable<List<Snapshot>> listSnapshots = new ListSnapshots(processWrapperFactory,dataset,stdProcessorFactory, stdConsumerFactory);

        List<Snapshot> res = listSnapshots.call();



        verify(processWrapperFactory).getProcessWrapper(eq(List.of(
                "zfs","list","-rH","-t","snapshot","-o","name","-s","creation","-d","1","TestDataset"
        )),any());

        List<Snapshot> expected = new ArrayList<>();
        Assertions.assertEquals(expected,res);
    }

}
