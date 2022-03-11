package ru.rerumu.backups.services;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.OutputStream;

public class ZFSReceiveTest implements ZFSReceive{

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
