package ru.rerumu.s3.utils;

import java.io.IOException;
import java.io.InputStream;
import java.util.Optional;

public interface InputStreamUtilsI {

    Optional<byte[]> readNext(InputStream inputStream, Integer n) throws IOException;
}
