package ru.rerumu.s3.utils;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Optional;

public class InputStreamUtils {

    public static Optional<byte[]> readNext(InputStream inputStream, Integer n) throws IOException {
        byte[] tmp = new byte[n];
        int len;

        if((len = inputStream.read(tmp)) != -1){
            return Optional.of(Arrays.copyOf(tmp, len));
        } else {
            return Optional.empty();
        }
    }
}
