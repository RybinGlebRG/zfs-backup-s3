package ru.rerumu.backups.services;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.rerumu.backups.exceptions.CompressorException;
import ru.rerumu.backups.exceptions.EncryptException;
import ru.rerumu.backups.exceptions.FileHitSizeLimitException;
import ru.rerumu.backups.exceptions.ZFSStreamEndedException;
import ru.rerumu.backups.io.S3Loader;
import ru.rerumu.backups.io.ZFSFileWriter;
import ru.rerumu.backups.io.ZFSFileWriterFactory;
import ru.rerumu.backups.models.Snapshot;
import ru.rerumu.backups.repositories.FilePartRepository;
import ru.rerumu.backups.services.impl.SnapshotSenderImpl;
import ru.rerumu.backups.zfs_api.ZFSSend;

import java.io.IOException;
import java.nio.file.Paths;

public class TestSnapshotSender {

    @Test
    void shouldSendOneFile() throws CompressorException, IOException, InterruptedException, EncryptException, FileHitSizeLimitException, ZFSStreamEndedException {
        FilePartRepository filePartRepository = Mockito.mock(FilePartRepository.class);
        S3Loader s3Loader = Mockito.mock(S3Loader.class);
        ZFSProcessFactory zfsProcessFactory = Mockito.mock(ZFSProcessFactory.class);
        ZFSFileWriterFactory zfsFileWriterFactory = Mockito.mock(ZFSFileWriterFactory.class);
        ZFSSend zfsSend = Mockito.mock(ZFSSend.class);
        ZFSFileWriter zfsFileWriter = Mockito.mock(ZFSFileWriter.class);

        Mockito.when(zfsProcessFactory.getZFSSendFull(Mockito.any())).thenReturn(zfsSend);
        Mockito.when(zfsFileWriterFactory.getZFSFileWriter()).thenReturn(zfsFileWriter);
        Mockito.when(filePartRepository.createNewFilePath(Mockito.any(), Mockito.anyInt())).thenReturn(
                Paths.get("ExternalPool-Applications@auto-20220326-150000.part0")
        );
        Mockito.doThrow(new ZFSStreamEndedException()).when(zfsFileWriter).write(Mockito.any(), Mockito.any());

        SnapshotSender snapshotSender = new SnapshotSenderImpl(filePartRepository, s3Loader, zfsProcessFactory, zfsFileWriterFactory);
        Snapshot baseSnapshot = new Snapshot("ExternalPool/Applications@auto-20220326-150000");
        snapshotSender.sendBaseSnapshot(baseSnapshot, s3Loader, true);


        InOrder inOrder = Mockito.inOrder(filePartRepository, zfsFileWriter, s3Loader, zfsSend);

        Mockito.verify(filePartRepository, Mockito.times(1)).createNewFilePath(Mockito.any(), Mockito.anyInt());
        inOrder.verify(filePartRepository).createNewFilePath("ExternalPool-Applications@auto-20220326-150000", 0);

        Mockito.verify(zfsFileWriter, Mockito.times(1)).write(Mockito.any(), Mockito.any());
        inOrder.verify(zfsFileWriter).write(Mockito.any(), Matchers.eq(Paths.get("ExternalPool-Applications@auto-20220326-150000.part0")));

        Mockito.verify(s3Loader, Mockito.times(1)).upload(Mockito.any(), Mockito.any());
        inOrder.verify(s3Loader).upload("ExternalPool-Applications", Paths.get("ExternalPool-Applications@auto-20220326-150000.part0"));

        Mockito.verify(filePartRepository, Mockito.times(1)).delete(Mockito.any());
        inOrder.verify(filePartRepository).delete(Paths.get("ExternalPool-Applications@auto-20220326-150000.part0"));

        Mockito.verify(zfsSend, Mockito.times(1)).close();
        inOrder.verify(zfsSend).close();
    }
}
