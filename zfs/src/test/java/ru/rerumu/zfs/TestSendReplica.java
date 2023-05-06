package ru.rerumu.zfs;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.rerumu.utils.processes.factories.ProcessWrapperFactory;
import ru.rerumu.utils.processes.factories.StdProcessorFactory;
import ru.rerumu.zfs.models.Snapshot;

import java.io.BufferedInputStream;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.function.Consumer;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class TestSendReplica {
    @Mock
    ProcessWrapperFactory processWrapperFactory;

    @Mock
    ExecutorService executorService;

    @Mock
    Consumer<BufferedInputStream> consumer;

    @Mock
    Callable<Void> processWrapper;

    @Mock
    StdProcessorFactory stdProcessorFactory;


    @Test
    void shouldSend() throws Exception{
        Snapshot snapshot = new Snapshot("TestPool@zfs-backup-s3__2023-03-22T194000");

        when(processWrapperFactory.getProcessWrapper(any(),any())).thenReturn(processWrapper);

        Callable<Void> sendReplica = new SendReplica(
                snapshot,
                processWrapperFactory,
                consumer,
                stdProcessorFactory
        );

        sendReplica.call();

        verify(processWrapperFactory).getProcessWrapper(eq(List.of(
                "zfs","send","-vpRPw","TestPool@zfs-backup-s3__2023-03-22T194000"
        )),any());
    }
}
