package ru.rerumu.backups.zfs_api;

import org.junit.jupiter.api.Test;
import org.mockito.InOrder;
import org.mockito.Mockito;
import ru.rerumu.backups.factories.ProcessWrapperFactory;

import java.util.List;

class TestZFSListSnapshots {

    @Test
    void shouldCreate()throws Exception{
        ProcessWrapperFactory processWrapperFactory = Mockito.mock(ProcessWrapperFactory.class);
        ProcessWrapper processWrapper = Mockito.mock(ProcessWrapper.class);

        Mockito.when(processWrapperFactory.getProcessWrapper(Mockito.any())).thenReturn(processWrapper);

        InOrder inOrder = Mockito.inOrder(processWrapperFactory,processWrapper);

        ZFSListSnapshots zfsListSnapshots = new ZFSListSnapshots("Test/test1/test2",processWrapperFactory);

        inOrder.verify(processWrapperFactory)
                .getProcessWrapper(List.of(
                        "zfs","list","-rH","-t","snapshot","-o","name","-s","creation","-d","1","Test/test1/test2"
                ));

        inOrder.verify(processWrapper).setStderrProcessor(Mockito.any());
    }

}