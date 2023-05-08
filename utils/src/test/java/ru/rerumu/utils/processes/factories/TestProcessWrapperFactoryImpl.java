package ru.rerumu.utils.processes.factories;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.rerumu.utils.processes.StdProcessor;
import ru.rerumu.utils.processes.factories.impl.ProcessWrapperFactoryImpl;


@ExtendWith(MockitoExtension.class)
public class TestProcessWrapperFactoryImpl {

    @Mock
    ProcessFactory processFactory;

    @Mock
    StdProcessor stdProcessor;

    @Test
    void shouldCreate(){
        ProcessWrapperFactoryImpl factory =  new ProcessWrapperFactoryImpl(processFactory);
        factory.getProcessWrapper(null,stdProcessor);
    }
}
