package ru.rerumu.backups.services;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import ru.rerumu.backups.exceptions.FileHitSizeLimitException;
import ru.rerumu.backups.exceptions.ZFSStreamEndedException;
import ru.rerumu.backups.factories.ZFSFileReaderFactory;
import ru.rerumu.backups.factories.ZFSFileWriterFactory;
import ru.rerumu.backups.repositories.S3Repository;
import ru.rerumu.backups.repositories.impl.S3StreamRepositoryImpl;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class TestS3StreamRepositoryImpl {

    @Mock
    S3Repository s3Repository;

    @Mock
    ZFSFileWriterFactory zfsFileWriterFactory;

    @Mock
    ZFSFileReaderFactory zfsFileReaderFactory;

    @Mock
    TempPathGenerator tempPathGenerator;

    @Mock
    ZFSFileWriter zfsFileWriter;

    @Mock
    ZFSFileReader zfsFileReader;

    @Test
    void shouldAddOne(@TempDir Path tmpDir) throws Exception {
        BufferedInputStream bufferedInputStream = new BufferedInputStream(new ByteArrayInputStream(new byte[0]));

        when(zfsFileWriterFactory.getZFSFileWriter(any()))
                .thenReturn(zfsFileWriter);
        when(tempPathGenerator.generateConsistent(eq(null), anyString()))
                .thenReturn(tmpDir.resolve("test.part0"))
                .thenReturn(tmpDir.resolve("test.part1"));
        doAnswer(invocationOnMock -> {
            Files.createFile(tmpDir.resolve("test.part0"));
            throw new FileHitSizeLimitException();
        })
                .doAnswer(invocationOnMock -> {
                    Files.createFile(tmpDir.resolve("test.part1"));
                    throw new ZFSStreamEndedException();
                })
                .when(zfsFileWriter).write(any());

        InOrder inOrder = inOrder(s3Repository, zfsFileWriterFactory, tempPathGenerator, zfsFileWriter);

        S3StreamRepositoryImpl s3StreamRepository = new S3StreamRepositoryImpl(
                s3Repository,
                zfsFileWriterFactory,
                zfsFileReaderFactory,
                tempPathGenerator
        );

        s3StreamRepository.add("bucket/pool/level-0/zfs-backup-s3", bufferedInputStream);

        bufferedInputStream.close();

        inOrder.verify(tempPathGenerator).generateConsistent(null, ".part0");
        inOrder.verify(zfsFileWriterFactory).getZFSFileWriter(tmpDir.resolve("test.part0"));
        inOrder.verify(zfsFileWriter).write(any());
        inOrder.verify(s3Repository).add("bucket/pool/level-0/zfs-backup-s3", tmpDir.resolve("test.part0"));

        inOrder.verify(tempPathGenerator).generateConsistent(null, ".part1");
        inOrder.verify(zfsFileWriterFactory).getZFSFileWriter(tmpDir.resolve("test.part1"));
        inOrder.verify(zfsFileWriter).write(any());
        inOrder.verify(s3Repository).add("bucket/pool/level-0/zfs-backup-s3", tmpDir.resolve("test.part1"));

        inOrder.verifyNoMoreInteractions();
    }

    @Test
    void shouldGetAll(@TempDir Path tmpDir) throws Exception {
        BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(new ByteArrayOutputStream());

        when(s3Repository.listAll(anyString()))
                .thenReturn(List.of("test.part0", "test.part1"));

        when(s3Repository.getOne(anyString()))
                .thenReturn(tmpDir.resolve("test.part0"))
                .thenReturn(tmpDir.resolve("test.part1"));

        when(zfsFileReaderFactory.getZFSFileReader(any(), any()))
                .thenReturn(zfsFileReader);

        InOrder inOrder = inOrder(s3Repository, zfsFileReaderFactory);

        S3StreamRepositoryImpl s3StreamRepository = new S3StreamRepositoryImpl(
                s3Repository,
                zfsFileWriterFactory,
                zfsFileReaderFactory,
                tempPathGenerator
        );

        s3StreamRepository.getAll(bufferedOutputStream, "bucket/pool/level-0/zfs-backup-s3");


        inOrder.verify(s3Repository).listAll("bucket/pool/level-0/zfs-backup-s3");

        inOrder.verify(s3Repository).getOne("bucket/pool/level-0/zfs-backup-s3/test.part0");
        inOrder.verify(zfsFileReaderFactory).getZFSFileReader(bufferedOutputStream, tmpDir.resolve("test.part0"));

        inOrder.verify(s3Repository).getOne("bucket/pool/level-0/zfs-backup-s3/test.part1");
        inOrder.verify(zfsFileReaderFactory).getZFSFileReader(bufferedOutputStream, tmpDir.resolve("test.part1"));

    }
}
