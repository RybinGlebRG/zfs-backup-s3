package ru.rerumu.backups.zfs_api;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class ZFSListSnapshotsTest implements ProcessWrapper{

    private final List<String> snapshots;

    public ZFSListSnapshotsTest(List<String> snapshots){
        this.snapshots = snapshots;
    }

    @Override
    public BufferedInputStream getBufferedInputStream() {
        StringBuilder stringBuilder = new StringBuilder();
        for (String snapshot : snapshots){
            stringBuilder.append(snapshot).append("\n");
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
