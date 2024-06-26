package ru.rerumu.zfs_backup_s3.local_storage.integration;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import ru.rerumu.zfs_backup_s3.local_storage.exceptions.FileHitSizeLimitException;
import ru.rerumu.zfs_backup_s3.local_storage.exceptions.ZFSStreamEndedException;
import ru.rerumu.zfs_backup_s3.local_storage.services.ZFSFileReader;
import ru.rerumu.zfs_backup_s3.local_storage.services.ZFSFileWriter;
import ru.rerumu.zfs_backup_s3.local_storage.services.impl.ZFSFileReaderTrivial;
import ru.rerumu.zfs_backup_s3.local_storage.services.impl.ZFSFileWriterTrivial;

import java.io.*;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class ITWriteReadTrivial {

    @Test
    void shouldWriteReadSame(@TempDir Path tempDir) throws Exception {
        long filePartSize = 1000;
        Path path = tempDir.resolve("test");
        ZFSFileWriter zfsFileWriter = new ZFSFileWriterTrivial(filePartSize,path);
        byte[] srcBuf = new byte[700];
        new Random().nextBytes(srcBuf);


        try (ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(srcBuf);
             BufferedInputStream bufferedInputStream = new BufferedInputStream(byteArrayInputStream)) {
            zfsFileWriter.write(bufferedInputStream);
        } catch (ZFSStreamEndedException ignored) {

        }

        byte[] resBuf;
        try (ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
             BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(byteArrayOutputStream)) {
            ZFSFileReader zfsFileReader = new ZFSFileReaderTrivial(bufferedOutputStream,path);
            zfsFileReader.read();
            bufferedOutputStream.flush();
            resBuf = byteArrayOutputStream.toByteArray();
        }

        Assertions.assertArrayEquals(srcBuf, resBuf);

    }


    @Test
    void shouldWriteReadSameTwoFiles(@TempDir Path tempDir) throws Exception {
        long filePartSize = 1000;
//        ZFSFileWriter zfsFileWriter = new ZFSFileWriterTrivial(chunkSize,filePartSize);
        byte[] srcBuf = new byte[1100];
        new Random().nextBytes(srcBuf);
//        Path path = tempDir.resolve("test");

        List<Path> pathList = new ArrayList<>();
        pathList.add(tempDir.resolve("test1"));
        pathList.add(tempDir.resolve("test2"));

        List<ZFSFileWriter> zfsFileWriterList = new ArrayList<>();
        zfsFileWriterList.add(new ZFSFileWriterTrivial(filePartSize,pathList.get(0)));
        zfsFileWriterList.add(new ZFSFileWriterTrivial(filePartSize,pathList.get(1)));

        int n = 0;

        try (ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(srcBuf);
             BufferedInputStream bufferedInputStream = new BufferedInputStream(byteArrayInputStream)) {
            while (true) {
                try {
                    ZFSFileWriter zfsFileWriter = zfsFileWriterList.get(n);
                    zfsFileWriter.write(bufferedInputStream);
                } catch (ZFSStreamEndedException ignored) {
                    break;
                } catch (FileHitSizeLimitException e) {
                    n++;
                }
            }
        }


        byte[] resBuf;

        try (ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
             BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(byteArrayOutputStream)) {

            for (Path path : pathList) {
                ZFSFileReader zfsFileReader = new ZFSFileReaderTrivial(bufferedOutputStream,path);
                zfsFileReader.read();
            }
            bufferedOutputStream.flush();
            resBuf = byteArrayOutputStream.toByteArray();
        }

        Assertions.assertArrayEquals(srcBuf, resBuf);

    }
}
