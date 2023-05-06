package ru.rerumu.backups.utils.processes;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.rerumu.backups.utils.processes.impl.StdProcessorImpl;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.util.function.Consumer;

import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
public class TestStdProcessorImpl {
    @Mock
    Consumer<BufferedInputStream> stderrConsumer;
    @Mock
    Consumer<BufferedInputStream> stdoutConsumer;
    @Mock
    Consumer<BufferedOutputStream> stdinConsumer;
    @Mock
    BufferedInputStream stderr;
    @Mock
    BufferedInputStream stdout;
    @Mock
    BufferedOutputStream stdin;


    @Test
    void shouldProcess()throws Exception{
        StdProcessorImpl stdProcessor = new StdProcessorImpl(stderrConsumer,stdoutConsumer,stdinConsumer);

        stdProcessor.processStd(stderr,stdout,stdin);

        verify(stderrConsumer).accept(stderr);
        verify(stdoutConsumer).accept(stdout);
        verify(stdinConsumer).accept(stdin);
    }

    @Test
    void shouldProcessNoStdin()throws Exception{
        StdProcessorImpl stdProcessor = new StdProcessorImpl(stderrConsumer,stdoutConsumer,null);

        stdProcessor.processStd(stderr,stdout,null);

        verify(stderrConsumer).accept(stderr);
        verify(stdoutConsumer).accept(stdout);
        verify(stdinConsumer,never()).accept(stdin);
    }

    @Test
    void shouldThrowException1()throws Exception{
        StdProcessorImpl stdProcessor = new StdProcessorImpl(stderrConsumer,stdoutConsumer,null);

        Assertions.assertThrows(IllegalArgumentException.class,()->stdProcessor.processStd(stderr,stdout,stdin));
    }

    @Test
    void shouldThrowException2()throws Exception{
        StdProcessorImpl stdProcessor = new StdProcessorImpl(stderrConsumer,stdoutConsumer,stdinConsumer);

        Assertions.assertThrows(IllegalArgumentException.class,()->stdProcessor.processStd(stderr,stdout,null));
    }
}
