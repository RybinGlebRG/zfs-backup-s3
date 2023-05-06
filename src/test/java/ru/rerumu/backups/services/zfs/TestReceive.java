package ru.rerumu.backups.services.zfs;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.rerumu.backups.services.zfs.models.Pool;
import ru.rerumu.backups.utils.processes.factories.ProcessWrapperFactory;
import ru.rerumu.backups.utils.processes.factories.StdProcessorFactory;

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
    ProcessWrapperFactory processWrapperFactory;

    @Mock
    Callable<Void> processWrapper;

    @Mock
    Consumer<BufferedOutputStream> consumer;

    @Mock
    StdProcessorFactory stdProcessorFactory;

    @Test
    void shouldCall() throws Exception{
        Pool pool =new Pool("ReceivePool",new ArrayList<>());

        when(processWrapperFactory.getProcessWrapper(any(),any())).thenReturn(processWrapper);


        Callable<Void> receive = new Receive(pool, processWrapperFactory,consumer,stdProcessorFactory);

        receive.call();



        verify(processWrapperFactory).getProcessWrapper(eq(List.of(
                "zfs","receive","-duvF","ReceivePool"
        )),any());
    }
}
