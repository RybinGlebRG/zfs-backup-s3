package ru.rerumu.zfs;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.rerumu.zfs_backup_s3.utils.processes.factories.ProcessWrapperFactory4Mock;
import ru.rerumu.zfs_backup_s3.zfs.callable.Receive;
import ru.rerumu.zfs_backup_s3.zfs.models.Pool;

import java.io.BufferedOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.function.Consumer;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


@ExtendWith(MockitoExtension.class)
public class TestReceive {

    @Mock
    ProcessWrapperFactory4Mock processWrapperFactory;
    @Mock
    Callable<Void> processWrapper;
    @Mock
    Consumer<BufferedOutputStream> consumer;

    @Test
    void shouldCall() throws Exception{
        Pool pool =new Pool("ReceivePool",new ArrayList<>());

        when(processWrapperFactory.getProcessWrapper(any(),any())).thenReturn(processWrapper);


        Callable<Void> receive = new Receive(pool, processWrapperFactory,consumer);

        receive.call();



        verify(processWrapperFactory).getProcessWrapper(eq(List.of(
                "zfs","receive","-duvF","ReceivePool"
        )),any());
    }
}
