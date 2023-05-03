package ru.rerumu.backups.services.zfs;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.rerumu.backups.services.zfs.models.Snapshot;
import ru.rerumu.backups.utils.processes.factories.ProcessWrapperFactory;
import ru.rerumu.backups.utils.processes.TriConsumer;

import java.io.BufferedInputStream;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class TestSendReplica {
    @Mock
    ProcessWrapperFactory processWrapperFactory;

    @Mock
    ExecutorService executorService;

    @Mock
    TriConsumer<BufferedInputStream,Runnable,Runnable> consumer;

    @Mock
    Callable<Void> processWrapper;


    @Test
    void shouldSend() throws Exception{
        Snapshot snapshot = new Snapshot("TestPool@zfs-backup-s3__2023-03-22T194000");

        when(processWrapperFactory.getProcessWrapper(any(),any(),any())).thenReturn(processWrapper);

        Callable<Void> sendReplica = new SendReplica(
                snapshot,
                processWrapperFactory,
                consumer,
                executorService
        );

        sendReplica.call();

        verify(processWrapperFactory).getProcessWrapper(eq(List.of(
                "zfs","send","-vpRPw","TestPool@zfs-backup-s3__2023-03-22T194000"
        )),any(),any());
    }
}
