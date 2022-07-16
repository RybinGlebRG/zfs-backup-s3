package ru.rerumu.backups.factories.impl;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import ru.rerumu.backups.factories.ProcessWrapperFactory;
import ru.rerumu.backups.factories.ZFSProcessFactory;
import ru.rerumu.backups.models.Snapshot;
import ru.rerumu.backups.zfs_api.ZFSSend;
import ru.rerumu.backups.zfs_api.impl.ZFSSendFullEncrypted;

class TestZFSProcessFactoryImpl {

//    @Test
//    void shouldGetSendFull() throws Exception{
//        ProcessWrapperFactory processWrapperFactory = Mockito.mock(ProcessWrapperFactory.class);
//        ZFSProcessFactory zfsProcessFactory = new ZFSProcessFactoryImpl(processWrapperFactory);
//
//        Assertions.assertInstanceOf(ZFSSendFullEncrypted.class, zfsProcessFactory.getZFSSendFull(new Snapshot("Test@level0")));
//    }
}