package ru.rerumu.backups.services.zfs;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.rerumu.backups.services.zfs.models.Dataset;
import ru.rerumu.backups.services.zfs.models.Pool;
import ru.rerumu.backups.services.zfs.models.Snapshot;
import ru.rerumu.backups.utils.processes.ProcessFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class TestListSnapshots {

    @Mock
    ProcessFactory processFactory;

    @Mock
    Callable<Void> processWrapper;

    @Mock
    ExecutorService executorService;

    @Test
    void shouldCall() throws Exception{
        Dataset dataset = new Dataset("TestDataset", new ArrayList<>());

        when(processFactory.getProcessWrapper(any(),any(),any())).thenReturn(processWrapper);


        Callable<List<Snapshot>> listSnapshots = new ListSnapshots(processFactory,dataset,executorService);

        List<Snapshot> res = listSnapshots.call();



        verify(processFactory).getProcessWrapper(eq(List.of(
                "zfs","list","-rH","-t","snapshot","-o","name","-s","creation","-d","1","TestDataset"
        )),any(),any());

        List<Snapshot> expected = new ArrayList<>();
        Assertions.assertEquals(expected,res);
    }

}
