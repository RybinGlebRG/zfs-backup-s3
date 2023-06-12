package ru.rerumu.zfs_backup_s3.s3;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.nio.file.Path;
import java.util.List;

public interface S3Service {

    void upload(BufferedInputStream bufferedInputStream, String key);

    void download(String prefix, BufferedOutputStream bufferedOutputStream);

    List<String> list(String prefix);

}
