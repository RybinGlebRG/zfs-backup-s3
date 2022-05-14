package ru.rerumu.backups.services.helpers;

import org.apache.commons.lang3.ArrayUtils;
import ru.rerumu.backups.zfs_api.ZFSReceive;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ZFSReceiveTest implements ZFSReceive {

//    private static byte[] result = new byte[0];

    private static final List<byte[]> resultList = new ArrayList<>();

    private final ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
    private final BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(byteArrayOutputStream);

//    public static byte[] getResult(){
//        return result;
//    }

    public static List<byte[]> getResultList(){
        return  resultList;
    }

    @Override
    public BufferedOutputStream getBufferedOutputStream() {
        return bufferedOutputStream;
    }

    public ByteArrayOutputStream getByteArrayOutputStream() {
        return byteArrayOutputStream;
    }

    public void close() throws IOException {
        bufferedOutputStream.flush();
//        result = ArrayUtils.addAll(result,byteArrayOutputStream.toByteArray());
        resultList.add(byteArrayOutputStream.toByteArray());
    }
}
