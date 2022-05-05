package ru.rerumu.backups.zfs_api;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class ZFSListFilesystemsTest implements ProcessWrapper{

    private final List<String> filesystems;

    public ZFSListFilesystemsTest(List<String> filesystems){
        this.filesystems = filesystems;
    }

    @Override
    public BufferedInputStream getBufferedInputStream() {
        StringBuilder stringBuilder = new StringBuilder();
        for (String filesystem : filesystems){
            stringBuilder.append(filesystem).append("\n");
        }
        String tmp = stringBuilder.toString();
        byte[] buf = tmp.getBytes(StandardCharsets.UTF_8);
        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(buf);
        BufferedInputStream bufferedInputStream = new BufferedInputStream(byteArrayInputStream);
        return bufferedInputStream;
    }

    @Override
    public void close() {

    }
}
