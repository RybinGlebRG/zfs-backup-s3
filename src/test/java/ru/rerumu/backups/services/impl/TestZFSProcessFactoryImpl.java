package ru.rerumu.backups.services.impl;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import ru.rerumu.backups.factories.impl.ZFSProcessFactoryImpl;
import ru.rerumu.backups.models.Snapshot;
import ru.rerumu.backups.models.ZFSPool;
import ru.rerumu.backups.factories.ZFSProcessFactory;
import ru.rerumu.backups.zfs_api.*;
import ru.rerumu.backups.zfs_api.impl.ZFSReceiveImpl;
import ru.rerumu.backups.zfs_api.impl.ZFSSendFull;
import ru.rerumu.backups.zfs_api.impl.ZFSSendIncremental;

import java.io.IOException;

class TestZFSProcessFactoryImpl {

    @Disabled
    @Test
    void shouldCreateZFSSendFull() throws IOException {
        ZFSProcessFactory zfsProcessFactory = new ZFSProcessFactoryImpl();

        ZFSSend zfsSend = zfsProcessFactory.getZFSSendFull(new Snapshot("ExternalPool/Applications@auto-20220326-150000"));

        Assertions.assertInstanceOf(ZFSSendFull.class, zfsSend);
    }

    @Disabled
    @Test
    void shouldCreateZFSSendIncremental() throws IOException {
        ZFSProcessFactory zfsProcessFactory = new ZFSProcessFactoryImpl();

        ZFSSend zfsSend = zfsProcessFactory.getZFSSendIncremental(
                new Snapshot("ExternalPool/Applications@auto-20220326-150000"),
                new Snapshot("ExternalPool/Applications@auto-20220327-150000")
        );

        Assertions.assertInstanceOf(ZFSSendIncremental.class, zfsSend);
    }

    @Disabled
    @Test
    void shouldCreateZFSReceiveImpl() throws IOException {
        ZFSProcessFactory zfsProcessFactory = new ZFSProcessFactoryImpl();

        ZFSReceive zfsReceive = zfsProcessFactory.getZFSReceive(new ZFSPool("ExternalPool"));

        Assertions.assertInstanceOf(ZFSReceiveImpl.class, zfsReceive);
    }

    @Disabled
    @Test
    void shouldCreateZFSListFilesystems() throws IOException {
        ZFSProcessFactory zfsProcessFactory = new ZFSProcessFactoryImpl();

        ProcessWrapper processWrapper = zfsProcessFactory.getZFSListFilesystems("Test");

        Assertions.assertInstanceOf(ZFSListFilesystems.class, processWrapper);
    }

    @Disabled
    @Test
    void shouldCreateZFSListSnapshots() throws IOException {
        ZFSProcessFactory zfsProcessFactory = new ZFSProcessFactoryImpl();

        ProcessWrapper processWrapper = zfsProcessFactory.getZFSListSnapshots("Test");

        Assertions.assertInstanceOf(ZFSListSnapshots.class, processWrapper);
    }

}