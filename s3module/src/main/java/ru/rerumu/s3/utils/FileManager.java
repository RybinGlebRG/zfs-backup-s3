package ru.rerumu.s3.utils;

import java.io.IOException;
import java.nio.file.Path;

public interface FileManager {

    Path getNew(String prefix, String postfix);

    void delete(Path path) throws IOException;
}