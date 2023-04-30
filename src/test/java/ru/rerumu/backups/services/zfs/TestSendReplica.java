package ru.rerumu.backups.services.zfs;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.rerumu.backups.services.zfs.impl.SendReplica;
import ru.rerumu.backups.services.zfs.models.Snapshot;
import ru.rerumu.backups.utils.processes.ProcessFactory;
import ru.rerumu.backups.utils.processes.TriConsumer;

import java.io.BufferedInputStream;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class TestSendReplica {
    @Mock
    ProcessFactory processFactory;

    @Mock
    ExecutorService executorService;


    @Test
    void shouldSend() throws Exception{
        Snapshot snapshot = new Snapshot("TestPool@zfs-backup-s3__2023-03-22T194000");
        TriConsumer<BufferedInputStream,Runnable,Runnable> consumer =(TriConsumer<BufferedInputStream,Runnable,Runnable>) mock(TriConsumer.class);
        Callable<Void> processWrapper =(Callable<Void>)  mock(Callable.class);

        when(processFactory.getProcessWrapper(any(),any(),any())).thenReturn(processWrapper);

        Callable<Void> sendReplica = new SendReplica(
                snapshot,
                processFactory,
                consumer,
                executorService
        );

        sendReplica.call();
    }
}
