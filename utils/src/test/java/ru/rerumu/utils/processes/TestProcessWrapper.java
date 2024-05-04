package ru.rerumu.utils.processes;


import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.rerumu.zfs_backup_s3.utils.processes.ProcessWrapper;
import ru.rerumu.zfs_backup_s3.utils.processes.StdProcessor4Mock;
import ru.rerumu.zfs_backup_s3.utils.processes.factories.impl.ProcessFactory4Mock;

import java.util.List;
import java.util.concurrent.ExecutionException;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class TestProcessWrapper {


    @Mock
    ProcessFactory4Mock processFactory;

    @Mock
    Process process;

    @Mock
    StdProcessor4Mock stdProcessor;

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

        ProcessWrapper processWrapper = new ProcessWrapper(
                List.of(),
                processFactory,
                stdProcessor
        );

        Assertions.assertThrows(ExecutionException.class, processWrapper::call);

        verify(process).destroy();
    }
}
