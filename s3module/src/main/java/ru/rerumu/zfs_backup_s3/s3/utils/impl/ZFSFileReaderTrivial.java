package ru.rerumu.zfs_backup_s3.s3.utils.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.rerumu.zfs_backup_s3.s3.utils.ZFSFileReader;
import ru.rerumu.zfs_backup_s3.utils.NotThreadSafe;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;

@NotThreadSafe
public final class ZFSFileReaderTrivial implements ZFSFileReader {
    private final Logger logger = LoggerFactory.getLogger(ZFSFileReaderTrivial.class);
    private final BufferedOutputStream bufferedOutputStream;
    private final Path path;

    public ZFSFileReaderTrivial(BufferedOutputStream bufferedOutputStream, Path path){
        this.bufferedOutputStream = bufferedOutputStream;
        this.path = path;
    }

    @Override
    public void read() throws IOException {
        logger.info(String.format("Starting reading from file '%s'", path.toString()));
        logger.debug(String.format("File size = %d",Files.size(path)));
        long w=0;
        try(InputStream inputStream = Files.newInputStream(path);
            BufferedInputStream bufferedInputStream = new BufferedInputStream(inputStream)) {
            int len;
            byte[] buf = new byte[8192];
            while((len=bufferedInputStream.read(buf))!=-1){
                bufferedOutputStream.write(buf,0,len);
                w+=len;
                logger.trace(String.format("Writing size = %d",len));
            }
        }
        bufferedOutputStream.flush();
        logger.debug(String.format("Written size = %d",w));
    }
}
