package ru.rerumu.backups.zfs_api.impl;

import org.junit.jupiter.api.Test;
import org.mockito.InOrder;
import org.mockito.Mockito;
import ru.rerumu.backups.factories.ProcessWrapperFactory;
import ru.rerumu.backups.models.Snapshot;
import ru.rerumu.backups.zfs_api.ProcessWrapper;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class TestZFSSendMultiIncrementalEncrypted {
    @Test
    void shouldRunProcess()throws Exception{
        ProcessWrapperFactory processWrapperFactory = Mockito.mock(ProcessWrapperFactory.class);
        ProcessWrapper processWrapper = Mockito.mock(ProcessWrapper.class);

        Mockito.when(processWrapperFactory.getProcessWrapper(Mockito.any())).thenReturn(processWrapper);

        InOrder inOrder = Mockito.inOrder(processWrapperFactory,processWrapper);

        ZFSSendMultiIncrementalEncrypted zfsSendMultiIncrementalEncrypted = new ZFSSendMultiIncrementalEncrypted(
                new Snapshot("Test@level0"),
                new Snapshot("Test@level1"),
                processWrapperFactory);
        zfsSendMultiIncrementalEncrypted.getBufferedInputStream();
        zfsSendMultiIncrementalEncrypted.close();
        zfsSendMultiIncrementalEncrypted.kill();

        inOrder.verify(processWrapperFactory)
                .getProcessWrapper(List.of(
                        "zfs", "send", "-vpPw","-I","Test@level0","Test@level1"
                ));

        inOrder.verify(processWrapper).setStderrProcessor(Mockito.any());
        inOrder.verify(processWrapper).getBufferedInputStream();
        inOrder.verify(processWrapper).close();
        inOrder.verify(processWrapper).kill();
    }

}