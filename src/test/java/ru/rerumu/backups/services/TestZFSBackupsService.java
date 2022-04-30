package ru.rerumu.backups.services;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.rerumu.backups.exceptions.CompressorException;
import ru.rerumu.backups.exceptions.EncryptException;
import ru.rerumu.backups.exceptions.NoMorePartsException;
import ru.rerumu.backups.exceptions.TooManyPartsException;
import ru.rerumu.backups.repositories.FilePartRepository;
import ru.rerumu.backups.repositories.FilePartRepositoryTest;
import ru.rerumu.backups.repositories.impl.FilePartRepositoryImpl;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class TestZFSBackupsService {


    @Test
    void shouldSendS3() throws CompressorException, IOException, InterruptedException, EncryptException, NoMorePartsException, TooManyPartsException, ClassNotFoundException {
        ZFSBackupsService zfsBackupsServiceSend = new ZFSBackupsService(
                "gWR9IPAzbSaOfPp0"
        );

        ZFSSendTest zfsSendTest = new ZFSSendTest(100000);
        FilePartRepositoryTest filePartRepository = new FilePartRepositoryTest(new ArrayList<>());
        S3Loader s3LoaderMock = Mockito.mock(S3Loader.class);
        ArgumentCaptor<Path> argumentCaptorPath = ArgumentCaptor.forClass(Path.class);

        zfsBackupsServiceSend.zfsSendFull(
                zfsSendTest,
                40000,
                true,
                45000L,
                filePartRepository,
                false,
                s3LoaderMock
        );

        Mockito.verify(s3LoaderMock,Mockito.atLeastOnce()).upload(argumentCaptorPath.capture());
        List<Path> argumentsPaths = argumentCaptorPath.getAllValues();
        List<Path> pathList = filePartRepository.getPathList();
        for (int i=0;i<pathList.size();i++){
            Assertions.assertEquals(pathList.get(i).toString(),argumentsPaths.get(i).toString());
        }
    }

    @Test
    void shouldSendReceive(@TempDir Path tempDir) throws CompressorException, IOException, InterruptedException, EncryptException, NoMorePartsException, TooManyPartsException, ClassNotFoundException {
        ZFSBackupsService zfsBackupsServiceSend = new ZFSBackupsService(
                "gWR9IPAzbSaOfPp0"
        );

        ZFSSendTest zfsSendTest = new ZFSSendTest(100000);
        FilePartRepository filePartRepositorySend = new FilePartRepositoryImpl(tempDir,"MainPool@level0_25_02_2020__20_50");
        FilePartRepository filePartRepositoryReceive = new FilePartRepositoryImpl(tempDir,"MainPool@level0_25_02_2020__20_50");
        S3Loader s3LoaderMock = Mockito.mock(S3Loader.class);

        ZFSBackupsService zfsBackupsServiceReceive = new ZFSBackupsService(
                "gWR9IPAzbSaOfPp0"
        );
        ZFSReceiveTest zfsReceiveTest = new ZFSReceiveTest();

        Runnable runnableSend = ()->{
            Logger logger = LoggerFactory.getLogger("runnableSend");
            try {
                zfsBackupsServiceSend.zfsSendFull(
                        zfsSendTest,
                        40000,
                        false,
                        45000L,
                        filePartRepositorySend,
                        false,
                        s3LoaderMock
                );
            } catch (IOException | InterruptedException | CompressorException | EncryptException e) {
                logger.error(e.toString());
            }
        };
        Thread threadSend = new Thread(runnableSend);

        Runnable runnableReceive = ()->{
            Logger logger = LoggerFactory.getLogger("runnableReceive");
            try {
                zfsBackupsServiceReceive.zfsReceive(
                        zfsReceiveTest,
                        filePartRepositoryReceive,
                        false
                );
            } catch (IOException | TooManyPartsException | EncryptException | CompressorException | InterruptedException | ClassNotFoundException e) {
                logger.error(e.toString());
            }
        };
        Thread threadReceive = new Thread(runnableReceive);

        threadSend.start();
        threadReceive.start();

        threadSend.join();
        Files.createFile(tempDir.resolve("finished"));
        threadReceive.join();

        byte[] src = zfsSendTest.getSrc();
        zfsReceiveTest.getBufferedOutputStream().flush();
        byte[] dst = zfsReceiveTest.getByteArrayOutputStream().toByteArray();

//        Thread.sleep(120000);

        Assertions.assertArrayEquals(src,dst);
    }
}
