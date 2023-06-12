package ru.rerumu.zfs;

import org.junit.jupiter.api.Test;
import ru.rerumu.zfs_backup_s3.zfs.ZFSServiceFactoryImpl;

public class TestZFSServiceFactoryImpl {

    @Test
    void shouldCreate(){
        ZFSServiceFactoryImpl factory = new ZFSServiceFactoryImpl();
        factory.getZFSService();
    }
}
