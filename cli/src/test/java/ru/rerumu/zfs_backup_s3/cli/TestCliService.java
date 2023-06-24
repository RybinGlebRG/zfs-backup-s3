package ru.rerumu.zfs_backup_s3.cli;


import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.rerumu.zfs_backup_s3.backups.services.ReceiveService;
import ru.rerumu.zfs_backup_s3.backups.services.ReceiveServiceMock;
import ru.rerumu.zfs_backup_s3.backups.services.SendService;
import ru.rerumu.zfs_backup_s3.backups.services.SendServiceMock;

import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
public class TestCliService {

    @Mock
    SendServiceMock sendService;

    @Mock
    ReceiveServiceMock receiveService;

    @Test
    void shouldSendFull()throws Exception{
        CliService cliService = new CliService(sendService,receiveService);
        cliService.run(new String[]{"--backupFull", "TestPool","TestBucket"});

        verify(sendService).send("TestPool","TestBucket");
    }

    @Test
    void shouldRestore()throws Exception{
        CliService cliService = new CliService(sendService,receiveService);
        cliService.run(new String[]{"--restore","TestBucket", "TestPool"});

        verify(receiveService).receive("TestBucket", "TestPool");
    }

    @Test
    void shouldThrowException()throws Exception{
        CliService cliService = new CliService(sendService,receiveService);

        Assertions.assertThrows(IllegalArgumentException.class,()->cliService.run(new String[]{"TestBucket", "TestPool"}));
    }

    @Test
    void shouldThrowException1()throws Exception{
        CliService cliService = new CliService(sendService,receiveService);

        Assertions.assertThrows(IllegalArgumentException.class,()->cliService.run(new String[]{"--restore","--backupFull","TestBucket", "TestPool"}));
    }

    @Test
    void shouldThrowException2()throws Exception{
        CliService cliService = new CliService(sendService,receiveService);

        Assertions.assertThrows(IllegalArgumentException.class,()->cliService.run(new String[]{"--backupFull"}));
    }

    @Test
    void shouldPrint()throws Exception{
        CliService cliService = new CliService(sendService,receiveService);
        cliService.run(new String[]{"-h"});
    }
}
