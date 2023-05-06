package ru.rerumu.s3;

import java.nio.file.Path;
import java.util.List;

public interface S3Service {

    void upload(Path sourcePath, String s3Key);

    void download(String s3Key, Path targetPath);

    List<String> list(String prefix);

}
