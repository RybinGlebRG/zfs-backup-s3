package ru.rerumu.backups.factories.impl;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import ru.rerumu.backups.factories.ZFSProcessFactory;
import ru.rerumu.backups.models.Snapshot;
import ru.rerumu.backups.zfs_api.ZFSSend;
import ru.rerumu.backups.zfs_api.impl.ZFSSendFullEncrypted;

class TestZFSProcessFactoryImpl {

//    @Test
//    void shouldGetSendFull() throws Exception{
//        ZFSProcessFactory zfsProcessFactory = new ZFSProcessFactoryImpl(false,false);
//        ZFSSend zfsSend = null;
//        try {
//            zfsSend = zfsProcessFactory.getZFSSendFull(Mockito.mock(Snapshot.class));
//        } catch (Exception ignored){}
//        Assertions.assertInstanceOf(ZFSSendFullEncrypted.class, zfsSend);
//    }
}