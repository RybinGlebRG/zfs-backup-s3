package ru.rerumu.backups.controllers;

import org.junit.jupiter.api.Disabled;

@Disabled
class TestBackupController {

//    @Test
//    void shouldBackup()throws Exception{
//        ZFSBackupService zfsBackupService = Mockito.mock(ZFSBackupService.class);
//        BackupController backupController = new BackupController(zfsBackupService);
//        backupController.backupFull("Test@level0");
//
//        Mockito.verify(zfsBackupService).zfsBackupFull("level0","Test");
//    }
//
//    @Test
//    void shouldHandleException()throws Exception{
//        ZFSBackupService zfsBackupService = Mockito.mock(ZFSBackupService.class);
//        Mockito.doThrow(IOException.class).when(zfsBackupService).zfsBackupFull(Mockito.any(),Mockito.any());
//
//        BackupController backupController = new BackupController(zfsBackupService);
//        backupController.backupFull("Test@level0");
//
//        Mockito.verify(zfsBackupService).zfsBackupFull("level0","Test");
//    }
//
//    @Test
//    void shouldBackupIncremental()throws Exception{
//        ZFSBackupService zfsBackupService = Mockito.mock(ZFSBackupService.class);
//        BackupController backupController = new BackupController(zfsBackupService);
//        backupController.backupIncremental("Test@level0","Test@level1");
//
//        Mockito.verify(zfsBackupService).zfsBackupIncremental("Test","level0","level1");
//    }
//
//    @Test
//    void shouldHandleExceptionIncremental()throws Exception{
//        ZFSBackupService zfsBackupService = Mockito.mock(ZFSBackupService.class);
//        Mockito.doThrow(IOException.class).when(zfsBackupService).zfsBackupIncremental(Mockito.any(),Mockito.any(),Mockito.any());
//
//        BackupController backupController = new BackupController(zfsBackupService);
//        backupController.backupIncremental("Test@level0","Test@level1");
//
//        Mockito.verify(zfsBackupService).zfsBackupIncremental("Test","level0","level1");
//    }
}