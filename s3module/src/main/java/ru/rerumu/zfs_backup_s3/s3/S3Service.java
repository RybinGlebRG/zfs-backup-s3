package ru.rerumu.zfs_backup_s3.s3;

import ru.rerumu.zfs_backup_s3.s3.impl.S3ServiceImpl;
import ru.rerumu.zfs_backup_s3.utils.NotThreadSafe;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.nio.file.Path;
import java.util.List;
import java.util.function.Consumer;

@NotThreadSafe
public interface S3Service {
    void upload(Path path, String prefix);
    void download(String prefix, Path targetPath);
    List<String> list(String prefix);

}
