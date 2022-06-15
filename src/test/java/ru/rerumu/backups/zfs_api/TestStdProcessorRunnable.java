package ru.rerumu.backups.zfs_api;

import org.apache.commons.lang3.RandomStringUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static org.junit.jupiter.api.Assertions.*;

// TODO: Test stream read after closed
class TestStdProcessorRunnable {

    @Test
    void shouldProcess() throws ExecutionException, InterruptedException {
        String generatedString = RandomStringUtils.random(2300, true, true);
        byte[] buf = generatedString.getBytes(StandardCharsets.UTF_8);

        InputStream inputStream = new ByteArrayInputStream(buf);
        BufferedInputStream bufferedInputStream = new BufferedInputStream(inputStream);
        StdProcessor stdProcessor = Mockito.mock(StdProcessor.class);
        ArgumentCaptor<String> argument = ArgumentCaptor.forClass(String.class);

        ExecutorService executorService = Executors.newFixedThreadPool(1);
        Future<?> future = executorService.submit(new StdProcessorRunnable(bufferedInputStream,stdProcessor));
        future.get();

        Mockito.verify(stdProcessor,Mockito.atLeastOnce()).process(argument.capture());
        List<String> arguments = argument.getAllValues();
        StringBuilder res = new StringBuilder();
        for (String item : arguments) {
            res.append(item);
        }
        Assertions.assertEquals(res.toString(),generatedString);

    }

}