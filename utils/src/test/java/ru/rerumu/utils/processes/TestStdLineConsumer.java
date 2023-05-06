package ru.rerumu.utils.processes;


import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.function.Consumer;

import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
public class TestStdLineConsumer {

    @Mock
    Consumer<String> consumer;

    @Test
    void shouldCall() throws Exception{
        String str = """
                Line1
                Line2
                Line3
                """;
        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(str.getBytes(StandardCharsets.UTF_8));
        BufferedInputStream bufferedInputStream = new BufferedInputStream(byteArrayInputStream);

        StdLineConsumer stdLineConsumer = new StdLineConsumer(consumer);
        stdLineConsumer.accept(bufferedInputStream);

        InOrder inOrder = inOrder(consumer);

        inOrder.verify(consumer).accept("Line1");
        inOrder.verify(consumer).accept("Line2");
        inOrder.verify(consumer).accept("Line3");
        inOrder.verifyNoMoreInteractions();
    }

    @Test
    void shouldThrowException() throws Exception{
        String str = """
                Line1
                Line2
                Line3
                """;
        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(str.getBytes(StandardCharsets.UTF_8));
        BufferedInputStream bufferedInputStream = new BufferedInputStream(byteArrayInputStream);

        doThrow(RuntimeException.class).when(consumer).accept(anyString());

        StdLineConsumer stdLineConsumer = new StdLineConsumer(consumer);


        Assertions.assertThrows(RuntimeException.class,()-> stdLineConsumer.accept(bufferedInputStream));

    }
}
