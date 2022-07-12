package ru.rerumu.backups.zfs_api;

import org.junit.jupiter.api.Test;
import org.mockito.InOrder;
import org.mockito.Mockito;
import ru.rerumu.backups.factories.ProcessWrapperFactory;
import ru.rerumu.backups.zfs_api.ProcessWrapper;
import ru.rerumu.backups.zfs_api.ZFSListFilesystems;
import ru.rerumu.backups.zfs_api.impl.ZFSReceiveImpl;

import java.util.List;


class TestZFSReceiveImpl {

    @Test
    void shouldCreate() throws Exception{
        ProcessWrapperFactory processWrapperFactory = Mockito.mock(ProcessWrapperFactory.class);
        ProcessWrapper processWrapper = Mockito.mock(ProcessWrapper.class);

        Mockito.when(processWrapperFactory.getProcessWrapper(Mockito.any())).thenReturn(processWrapper);

        InOrder inOrder = Mockito.inOrder(processWrapperFactory,processWrapper);

        ZFSReceiveImpl zfsReceive = new ZFSReceiveImpl("Test",processWrapperFactory);

        inOrder.verify(processWrapperFactory)
                .getProcessWrapper(List.of(
                        "zfs", "receive", "-duvF","Test"
                ));

        inOrder.verify(processWrapper).setStderrProcessor(Mockito.any());
        inOrder.verify(processWrapper).setStdinProcessor(Mockito.any());

    }

}