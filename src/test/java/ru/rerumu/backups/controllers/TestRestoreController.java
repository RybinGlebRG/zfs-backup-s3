package ru.rerumu.backups.controllers;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import ru.rerumu.backups.services.ZFSRestoreService;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

class TestRestoreController {
    @Test
    void shouldRestore()throws Exception{
        ZFSRestoreService zfsRestoreService = Mockito.mock(ZFSRestoreService.class);
        RestoreController restoreController = new RestoreController(zfsRestoreService);
        restoreController.restore();

        Mockito.verify(zfsRestoreService).zfsReceive();
    }

    @Test
    void shouldHandleException()throws Exception{
        ZFSRestoreService zfsRestoreService = Mockito.mock(ZFSRestoreService.class);
        Mockito.doThrow(IOException.class).when(zfsRestoreService).zfsReceive();

        RestoreController restoreController = new RestoreController(zfsRestoreService);
        restoreController.restore();

        Mockito.verify(zfsRestoreService).zfsReceive();
    }

}