package ru.rerumu.zfs_backup_s3.cli;


import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.rerumu.zfs_backup_s3.backups.EntityFactory;
import ru.rerumu.zfs_backup_s3.backups.services.ReceiveService4Mock;
import ru.rerumu.zfs_backup_s3.backups.services.SendService4Mock;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class TestCliService {

    @Mock
    SendService4Mock sendService;

    @Mock
    ReceiveService4Mock receiveService;

    @Mock
    EntityFactory entityFactory;

    private CliService cliService;

    @BeforeEach
    public void beforeEach(){
        cliService = new CliService(entityFactory);
    }

    @Test
    void shouldSendFull()throws Exception{
        when(entityFactory.getSendService("TestBucket")).thenReturn(sendService);

        CliService cliService = new CliService(entityFactory);
        cliService.run(new String[]{"--backupFull", "TestPool","TestBucket"});

        verify(sendService).send("TestPool","TestBucket", null);
    }

    @Test
    void shouldRestore()throws Exception{
        when(entityFactory.getReceiveService("TestBucket")).thenReturn(receiveService);

        CliService cliService = new CliService(entityFactory);
        cliService.run(new String[]{"--restore","TestBucket", "RestorePool"});

        verify(receiveService).receive("TestBucket","RestorePool");
    }

    @Test
    void shouldThrowException()throws Exception{

        CliService cliService = new CliService(entityFactory);

        Assertions.assertThrows(IllegalArgumentException.class,()->cliService.run(new String[]{"TestBucket", "TestPool"}));
    }

    @Test
    void shouldThrowException1()throws Exception{
        CliService cliService = new CliService(entityFactory);

        Assertions.assertThrows(IllegalArgumentException.class,()->cliService.run(new String[]{"--restore","--backupFull","TestBucket", "TestPool"}));
    }

    @Test
    void shouldThrowException2()throws Exception{
        CliService cliService = new CliService(entityFactory);

        Assertions.assertThrows(IllegalArgumentException.class,()->cliService.run(new String[]{"--backupFull"}));
    }

    @Test
    void shouldPrint()throws Exception{
        CliService cliService = new CliService(entityFactory);
        cliService.run(new String[]{"-h"});
    }

    @Test
    public void shouldContinue() throws Exception{
        when(entityFactory.getSendService("TestBucket")).thenReturn(sendService);

        cliService.run(new String[]{"--backupFull","--continue","test_snapshot", "TestPool","TestBucket"});

        verify(sendService).send("TestPool","TestBucket", "test_snapshot");
    }
}
