package ru.rerumu.zfs_backup_s3.s3.utils;

import ru.rerumu.zfs_backup_s3.utils.ByteArray;
import ru.rerumu.zfs_backup_s3.utils.NotThreadSafe;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Optional;

@NotThreadSafe
public class InputStreamUtils {

    public static Optional<ByteArray> readNext(InputStream inputStream, Integer n) throws IOException {
        byte[] tmp = new byte[n];
        int len;

        if((len = inputStream.read(tmp)) != -1){
            return Optional.of(new ByteArray(Arrays.copyOf(tmp, len)));
        } else {
            return Optional.empty();
        }
    }
}
