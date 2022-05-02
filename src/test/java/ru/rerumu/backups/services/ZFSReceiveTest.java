package ru.rerumu.backups.services;

import ru.rerumu.backups.zfs_api.ZFSReceive;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;

public class ZFSReceiveTest implements ZFSReceive {

    private final ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
    private final BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(byteArrayOutputStream);


    @Override
    public BufferedOutputStream getBufferedOutputStream() {
        return bufferedOutputStream;
    }

    public ByteArrayOutputStream getByteArrayOutputStream() {
        return byteArrayOutputStream;
    }

    public void close(){}
}
