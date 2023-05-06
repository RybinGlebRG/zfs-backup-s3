package ru.rerumu.backups.services.zfs;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.rerumu.backups.services.zfs.factories.StdConsumerFactory;
import ru.rerumu.backups.services.zfs.models.Dataset;
import ru.rerumu.backups.utils.processes.factories.ProcessWrapperFactory;
import ru.rerumu.backups.utils.processes.factories.StdProcessorFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class TestGetDataset {

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


        when(processWrapperFactory.getProcessWrapper(any(),any())).thenReturn(processWrapper);


        Callable<Dataset> getDataset = new GetDataset(
                "TestDataset",
                processWrapperFactory,
                stdProcessorFactory,
                stdConsumerFactory
        );

        Dataset res = getDataset.call();



        verify(processWrapperFactory).getProcessWrapper(eq(List.of(
                "zfs","list","-rH","-t","snapshot","-o","name","-s","creation","-d","1","TestDataset"
        )),any());

        Dataset shouldDataset = new Dataset("TestDataset",new ArrayList<>());
        Assertions.assertEquals(shouldDataset,res);
    }
}
