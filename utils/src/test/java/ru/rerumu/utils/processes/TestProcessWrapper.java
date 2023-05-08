package ru.rerumu.utils.processes;


import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.rerumu.utils.processes.factories.ProcessFactory;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.ExecutionException;

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

    @Test
    void shouldDestroyProcess()throws Exception{
        when(processFactory.create(any())).thenReturn(process);
        doThrow(ExecutionException.class).when(stdProcessor).processStd(any(),any(),any());
        when(process.waitFor()).thenReturn(1);

        ProcessWrapper processWrapper = new ProcessWrapper(
                List.of(),
                processFactory,
                stdProcessor
        );

        Assertions.assertThrows(IOException.class, processWrapper::call);

        verify(process).destroy();
    }
}
