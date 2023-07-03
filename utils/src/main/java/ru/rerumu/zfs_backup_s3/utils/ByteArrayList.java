package ru.rerumu.zfs_backup_s3.utils;

import java.util.ArrayList;
import java.util.List;

@ThreadSafe
public record ByteArrayList(List<ByteArray> list) {

    public ByteArrayList{
        list = new ArrayList<>(list);
    }

    @Override
    public List<ByteArray> list() {
        return new ArrayList<>(list);
    }
}
