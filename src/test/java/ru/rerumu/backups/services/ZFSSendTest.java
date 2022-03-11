package ru.rerumu.backups.services;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Random;

public class ZFSSendTest implements ZFSSend{
    private BufferedInputStream bufferedInputStream;
    private byte[] src;

    public ZFSSendTest(int n){
        src = new byte[n];
        new Random().nextBytes(src);
        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(src);
        bufferedInputStream = new BufferedInputStream(byteArrayInputStream);
    }

    @Override
    public BufferedInputStream getBufferedInputStream() {
        return bufferedInputStream;
    }

    @Override
    public void close() throws InterruptedException, IOException {

    }

    public byte[] getSrc() {
        return src;
    }
}
