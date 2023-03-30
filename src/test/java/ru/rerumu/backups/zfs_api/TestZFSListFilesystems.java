package ru.rerumu.backups.zfs_api;

import org.junit.jupiter.api.Test;
import org.mockito.InOrder;
import org.mockito.Mockito;
import ru.rerumu.backups.factories.ProcessWrapperFactory;
import ru.rerumu.backups.zfs_api.zfs.ZFSListFilesystems;

import java.util.List;


class TestZFSListFilesystems {

    @Test
    void shouldCreate() throws Exception{
        ProcessWrapperFactory processWrapperFactory = Mockito.mock(ProcessWrapperFactory.class);
        ProcessWrapper processWrapper = Mockito.mock(ProcessWrapper.class);

        Mockito.when(processWrapperFactory.getProcessWrapper(Mockito.any())).thenReturn(processWrapper);

        InOrder inOrder = Mockito.inOrder(processWrapperFactory,processWrapper);

        ZFSListFilesystems zfsListFilesystems = new ZFSListFilesystems("Test/test1/test2",processWrapperFactory);
        zfsListFilesystems.getBufferedInputStream();
        zfsListFilesystems.close();

        inOrder.verify(processWrapperFactory)
                .getProcessWrapper(List.of(
                                "zfs","list","-rH","-t","filesystem,volume","-o","name","-s","name","Test/test1/test2"
                ));

        inOrder.verify(processWrapper).setStderrProcessor(Mockito.any());
        inOrder.verify(processWrapper).getBufferedInputStream();
        inOrder.verify(processWrapper).close();
    }

}