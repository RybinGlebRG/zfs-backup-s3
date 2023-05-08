package ru.rerumu.zfs.consumers;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class TestGetDatasetStringStdConsumer {

    @Test
    void shouldCall(){
        List<String> stringList =  new ArrayList<>();
        String str = """
                Test
                Test/inner
                Test/inner/nested
                """;
        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(str.getBytes(StandardCharsets.UTF_8));
        BufferedInputStream bufferedInputStream = new BufferedInputStream(byteArrayInputStream);

        GetDatasetStringStdConsumer getDatasetStringStdConsumer =  new GetDatasetStringStdConsumer(stringList);
        getDatasetStringStdConsumer.accept(bufferedInputStream);


        List<String> expected =  new ArrayList<>();
        expected.add("Test");
        expected.add("Test/inner");
        expected.add("Test/inner/nested");

        Assertions.assertEquals(expected,stringList);
    }

    @Test
    void shouldThrowException()throws Exception{
        List<String> stringList =  new ArrayList<>();
        BufferedInputStream bufferedInputStream = mock(BufferedInputStream.class);

        when(bufferedInputStream.readAllBytes()).thenThrow(IOException.class);

        GetDatasetStringStdConsumer getDatasetStringStdConsumer =  new GetDatasetStringStdConsumer(stringList);
        Assertions.assertThrows(RuntimeException.class,()->getDatasetStringStdConsumer.accept(bufferedInputStream));
    }

    @Test
    void shouldThrowValidationException(){
        List<String> stringList =  new ArrayList<>();
        String str = """
                Some kind of error
                """;
        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(str.getBytes(StandardCharsets.UTF_8));
        BufferedInputStream bufferedInputStream = new BufferedInputStream(byteArrayInputStream);

        GetDatasetStringStdConsumer getDatasetStringStdConsumer =  new GetDatasetStringStdConsumer(stringList);
        Assertions.assertThrows(RuntimeException.class,()->getDatasetStringStdConsumer.accept(bufferedInputStream));
    }
}
