package ru.rerumu.backups.zfs_api;

import org.apache.commons.lang3.RandomStringUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import ru.rerumu.backups.zfs_api.zfs.impl.StdProcessorCallable;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

class TestStdProcessorCallable {

    @Test
    void shouldProcess() throws ExecutionException, InterruptedException {
        String generatedString = RandomStringUtils.random(2300, true, true);
        byte[] buf = generatedString.getBytes(StandardCharsets.UTF_8);

        InputStream inputStream = new ByteArrayInputStream(buf);
        BufferedInputStream bufferedInputStream = new BufferedInputStream(inputStream);
        StdProcessor stdProcessor = Mockito.mock(StdProcessor.class);
        ArgumentCaptor<String> argument = ArgumentCaptor.forClass(String.class);

        ExecutorService executorService = Executors.newFixedThreadPool(1);
        Future<Integer> future = executorService.submit(new StdProcessorCallable(bufferedInputStream,stdProcessor));
        int threadRes = future.get();

        Mockito.verify(stdProcessor,Mockito.atLeastOnce()).process(argument.capture());
        List<String> arguments = argument.getAllValues();
        StringBuilder res = new StringBuilder();
        for (String item : arguments) {
            res.append(item);
        }
        Assertions.assertEquals(res.toString(),generatedString);

    }

    @Test
    void shouldThrowException() throws ExecutionException, InterruptedException {
        String generatedString = RandomStringUtils.random(2300, true, true);
        byte[] buf = generatedString.getBytes(StandardCharsets.UTF_8);

        InputStream inputStream = new ByteArrayInputStream(buf);
        BufferedInputStream bufferedInputStream = new BufferedInputStream(inputStream);
        StdProcessor stdProcessor = Mockito.mock(StdProcessor.class);
        Mockito.doThrow(UnsupportedOperationException.class).when(stdProcessor).process(Mockito.any());

        ArgumentCaptor<String> argument = ArgumentCaptor.forClass(String.class);

        ExecutorService executorService = Executors.newFixedThreadPool(1);
        Future<Integer> future = executorService.submit(new StdProcessorCallable(bufferedInputStream,stdProcessor));
        int threadRes = future.get();

        Assertions.assertEquals(1,threadRes);

        Mockito.verify(stdProcessor,Mockito.atLeastOnce()).process(argument.capture());
        List<String> arguments = argument.getAllValues();
        StringBuilder res = new StringBuilder();
        for (String item : arguments) {
            res.append(item);
        }
        Assertions.assertEquals(res.toString(),generatedString);

    }

}