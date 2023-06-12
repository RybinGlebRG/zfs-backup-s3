package ru.rerumu.zfs;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.rerumu.zfs_backup_s3.utils.processes.factories.ProcessWrapperFactory;
import ru.rerumu.zfs_backup_s3.zfs.callable.GetDataset;
import ru.rerumu.zfs_backup_s3.zfs.factories.StdConsumerFactory;
import ru.rerumu.zfs_backup_s3.zfs.models.Dataset;

import java.io.BufferedInputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.function.Consumer;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class TestGetDataset {

    @Mock
    ProcessWrapperFactory processWrapperFactory;

    @Mock
    Callable<Void> processWrapper;

    @Mock
    StdConsumerFactory stdConsumerFactory;

    @Test
    void shouldCall() throws Exception{


        when(processWrapperFactory.getProcessWrapper(any(),any())).thenReturn(processWrapper);
        when(stdConsumerFactory.getSnapshotListStdConsumer(any())).thenReturn((Consumer<BufferedInputStream>) mock(Consumer.class));


        Callable<Dataset> getDataset = new GetDataset(
                "TestDataset",
                processWrapperFactory,
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
