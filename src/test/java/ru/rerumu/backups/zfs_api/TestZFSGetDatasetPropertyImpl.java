package ru.rerumu.backups.zfs_api;

import org.junit.jupiter.api.Test;
import org.mockito.InOrder;
import org.mockito.Mockito;
import ru.rerumu.backups.factories.ProcessWrapperFactory;
import ru.rerumu.backups.zfs_api.ProcessWrapper;
import ru.rerumu.backups.zfs_api.impl.ZFSGetDatasetPropertyImpl;

import java.util.List;


class TestZFSGetDatasetPropertyImpl {

    @Test
    void shouldCreate() throws Exception {
        ProcessWrapperFactory processWrapperFactory = Mockito.mock(ProcessWrapperFactory.class);
        ProcessWrapper processWrapper = Mockito.mock(ProcessWrapper.class);

        Mockito.when(processWrapperFactory.getProcessWrapper(Mockito.any())).thenReturn(processWrapper);

        InOrder inOrder = Mockito.inOrder(processWrapperFactory, processWrapper);

        ZFSGetDatasetPropertyImpl zfsGetDatasetProperty = new ZFSGetDatasetPropertyImpl(
                "prop",
                "Test/test1/test2",
                processWrapperFactory);

        inOrder.verify(processWrapperFactory)
                .getProcessWrapper(List.of(
                        "zfs", "get", "-Hp", "-d", "0", "-t", "filesystem,volume", "-o", "value", "prop", "Test/test1/test2"
                ));

        inOrder.verify(processWrapper).setStderrProcessor(Mockito.any());

    }

}