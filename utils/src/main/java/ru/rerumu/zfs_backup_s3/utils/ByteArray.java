package ru.rerumu.zfs_backup_s3.utils;

import java.util.Arrays;

@ThreadSafe
public record ByteArray(byte[] array) {

    public ByteArray{
        array = Arrays.copyOf(array,array.length);
    }

    @Override
    public byte[] array() {
        return Arrays.copyOf(array,array.length);
    }
}
