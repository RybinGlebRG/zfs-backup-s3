package ru.rerumu.backups.services.zfs;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.rerumu.backups.services.zfs.models.Dataset;
import ru.rerumu.backups.services.zfs.models.Pool;
import ru.rerumu.backups.services.zfs.models.Snapshot;
import ru.rerumu.backups.utils.processes.ProcessFactory;
import ru.rerumu.backups.utils.processes.TriConsumer;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


@ExtendWith(MockitoExtension.class)
public class TestReceive {

    @Mock
    ProcessFactory processFactory;

    @Mock
    Callable<Void> processWrapper;

    @Mock
    TriConsumer<BufferedOutputStream,Runnable,Runnable> consumer;

    @Test
    void shouldCall() throws Exception{
        Pool pool =new Pool("ReceivePool",new ArrayList<>());

        when(processFactory.getProcessWrapper(any(),any(),any(),any())).thenReturn(processWrapper);


        Callable<Void> receive = new Receive(pool,processFactory,consumer);

        receive.call();



        verify(processFactory).getProcessWrapper(eq(List.of(
                "zfs","receive","-duvF","ReceivePool"
        )),any(),any(),any());
    }
}
