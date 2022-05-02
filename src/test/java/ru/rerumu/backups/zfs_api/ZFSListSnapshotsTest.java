package ru.rerumu.backups.zfs_api;

import java.io.BufferedInputStream;
import java.util.ArrayList;
import java.util.List;

public class ZFSListSnapshotsTest implements ProcessWrapper{
    @Override
    public BufferedInputStream getBufferedInputStream() {
        List<String> lines = new ArrayList<>();
        return null;
    }

    @Override
    public void close() {

    }
}
