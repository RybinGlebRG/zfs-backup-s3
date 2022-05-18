package ru.rerumu.backups.services.helpers;

import java.util.Random;

public class ZFSStreamTest {

    private byte[] data;

    public ZFSStreamTest(int n){
        data = new byte[n];
        new Random().nextBytes(data);
    }

    public byte[] getData() {
        return data;
    }
}
