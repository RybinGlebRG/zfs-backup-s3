package ru.rerumu.s3;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.nio.file.Path;
import java.util.List;

public interface S3Service {

//    void upload(Path sourcePath, String s3Key);

    void upload(Path path, String prefix);

    void upload(BufferedInputStream bufferedInputStream, String key);

//    void download(String s3Key, Path targetPath);

    void download(String prefix, Path targetPath);

    void download(String prefix, BufferedOutputStream bufferedOutputStream);

    List<String> list(String prefix);

}
