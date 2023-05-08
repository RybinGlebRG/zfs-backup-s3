package ru.rerumu.zfs;

import org.junit.jupiter.api.Test;

public class TestZFSServiceFactoryImpl {

    @Test
    void shouldCreate(){
        ZFSServiceFactoryImpl factory = new ZFSServiceFactoryImpl();
        factory.getZFSService();
    }
}
