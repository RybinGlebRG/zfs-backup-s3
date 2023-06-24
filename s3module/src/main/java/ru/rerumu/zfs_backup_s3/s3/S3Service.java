package ru.rerumu.zfs_backup_s3.s3;

import ru.rerumu.zfs_backup_s3.s3.impl.S3ServiceImpl;
import ru.rerumu.zfs_backup_s3.utils.NotThreadSafe;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.nio.file.Path;
import java.util.List;

@NotThreadSafe
public sealed interface S3Service permits S3ServiceImpl {

    void upload(BufferedInputStream bufferedInputStream, String key);

    void download(String prefix, BufferedOutputStream bufferedOutputStream);

    List<String> list(String prefix);

}
