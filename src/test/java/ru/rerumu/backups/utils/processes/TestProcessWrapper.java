package ru.rerumu.backups.utils.processes;


import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.rerumu.backups.utils.processes.factories.ProcessFactory;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.util.List;
import java.util.concurrent.ExecutorService;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class TestProcessWrapper {


    @Mock
    ProcessFactory processFactory;

    @Mock
    Process process;

    @Mock
    StdProcessor stdProcessor;

    @Test
    void shouldBeCalled() throws Exception{


        when(processFactory.create(any())).thenReturn(process);
        when(process.waitFor()).thenReturn(0);

        ProcessWrapper processWrapper = new ProcessWrapper(
                List.of(),
                processFactory,
                stdProcessor
        );

        processWrapper.call();
    }
}
