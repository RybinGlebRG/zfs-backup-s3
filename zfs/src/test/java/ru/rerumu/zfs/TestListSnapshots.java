package ru.rerumu.zfs;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.rerumu.zfs_backup_s3.utils.processes.factories.ProcessWrapperFactory4Mock;
import ru.rerumu.zfs_backup_s3.zfs.callable.ListSnapshots;
import ru.rerumu.zfs_backup_s3.zfs.factories.StdConsumerFactoryMock;
import ru.rerumu.zfs_backup_s3.zfs.models.Dataset;
import ru.rerumu.zfs_backup_s3.zfs.models.Snapshot;

import java.io.BufferedInputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.function.Consumer;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class TestListSnapshots {

    @Mock
    ProcessWrapperFactory4Mock processWrapperFactory;
    @Mock
    Callable<Void> processWrapper;
    @Mock
    StdConsumerFactoryMock stdConsumerFactory;

    @Test
    void shouldCall() throws Exception{
        Dataset dataset = new Dataset("TestDataset", new ArrayList<>());

        when(processWrapperFactory.getProcessWrapper(any(),any())).thenReturn(processWrapper);
        when(stdConsumerFactory.getSnapshotListStdConsumer(any())).thenReturn((Consumer<BufferedInputStream>) mock(Consumer.class));


        Callable<List<Snapshot>> listSnapshots = new ListSnapshots(processWrapperFactory,dataset,stdConsumerFactory);

        List<Snapshot> res = listSnapshots.call();



        verify(processWrapperFactory).getProcessWrapper(eq(List.of(
                "zfs","list","-rH","-t","snapshot","-o","name","-s","creation","-d","1","TestDataset"
        )),any());

        List<Snapshot> expected = new ArrayList<>();
        Assertions.assertEquals(expected,res);
    }

}
