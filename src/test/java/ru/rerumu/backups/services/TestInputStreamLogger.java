package ru.rerumu.backups.services;

import org.apache.commons.lang3.RandomStringUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.slf4j.Logger;
import ru.rerumu.backups.zfs_api.InputStreamLogger;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;


// TODO: Test stream read after closed
public class TestInputStreamLogger {

    @Test
    void shouldLog() throws InterruptedException {

        String generatedString = RandomStringUtils.random(2300, true, true);
        byte[] buf = generatedString.getBytes(StandardCharsets.UTF_8);

        InputStream inputStream = new ByteArrayInputStream(buf);
        BufferedInputStream bufferedInputStream = new BufferedInputStream(inputStream);
        Logger loggerMock = Mockito.mock(Logger.class);
        ArgumentCaptor<String> argument = ArgumentCaptor.forClass(String.class);
        Thread thread = new Thread(new InputStreamLogger(bufferedInputStream, loggerMock));
        thread.start();
        thread.join();
        Mockito.verify(loggerMock,Mockito.atLeastOnce()).debug(argument.capture());
        List<String> arguments = argument.getAllValues();
        StringBuilder res = new StringBuilder();
        for (String item : arguments) {
            res.append(item);
        }
        Assertions.assertEquals(res.toString(),generatedString);
    }
}
