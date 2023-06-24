package ru.rerumu.zfs;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.rerumu.zfs_backup_s3.utils.processes.factories.ProcessWrapperFactory;
import ru.rerumu.zfs_backup_s3.utils.processes.factories.ProcessWrapperFactoryMock;
import ru.rerumu.zfs_backup_s3.zfs.callable.SendReplica;
import ru.rerumu.zfs_backup_s3.zfs.models.Snapshot;

import java.io.BufferedInputStream;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.function.Consumer;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class TestSendReplica {
    @Mock
    ProcessWrapperFactoryMock processWrapperFactory;

    @Mock
    Consumer<BufferedInputStream> consumer;

    @Mock
    Callable<Void> processWrapper;


    @Test
    void shouldSend() throws Exception{
        Snapshot snapshot = new Snapshot("TestPool@zfs-backup-s3__2023-03-22T194000");

        when(processWrapperFactory.getProcessWrapper(any(),any())).thenReturn(processWrapper);

        Callable<Void> sendReplica = new SendReplica(
                snapshot,
                processWrapperFactory,
                consumer
        );

        sendReplica.call();

        verify(processWrapperFactory).getProcessWrapper(eq(List.of(
                "zfs","send","-vpRPw","TestPool@zfs-backup-s3__2023-03-22T194000"
        )),any());
    }
}
