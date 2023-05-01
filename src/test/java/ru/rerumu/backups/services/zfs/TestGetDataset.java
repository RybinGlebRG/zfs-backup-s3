package ru.rerumu.backups.services.zfs;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.rerumu.backups.services.zfs.models.Dataset;
import ru.rerumu.backups.utils.processes.ProcessFactory;

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
    ProcessFactory processFactory;

    @Mock
    Callable<Void> processWrapper;

    @Test
    void shouldCall() throws Exception{


        when(processFactory.getProcessWrapper(any(),any(),any())).thenReturn(processWrapper);


        Callable<Dataset> getDataset = new GetDataset(
                "TestDataset",
                processFactory
        );

        Dataset res = getDataset.call();



        verify(processFactory).getProcessWrapper(eq(List.of(
                "zfs","list","-rH","-t","snapshot","-o","name","-s","creation","-d","1","TestDataset"
        )),any(),any());

        Dataset shouldDataset = new Dataset("TestDataset",new ArrayList<>());
        Assertions.assertEquals(shouldDataset,res);
    }
}