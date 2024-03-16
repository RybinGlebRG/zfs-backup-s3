package ru.rerumu.zfs_backup_s3.local_storage.services.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.rerumu.zfs_backup_s3.local_storage.exceptions.FileHitSizeLimitException;
import ru.rerumu.zfs_backup_s3.local_storage.exceptions.ZFSStreamEndedException;
import ru.rerumu.zfs_backup_s3.local_storage.factories.ZFSFileReaderFactory;
import ru.rerumu.zfs_backup_s3.local_storage.factories.ZFSFileWriterFactory;
import ru.rerumu.zfs_backup_s3.local_storage.services.LocalStorageService;
import ru.rerumu.zfs_backup_s3.local_storage.services.ZFSFileReader;
import ru.rerumu.zfs_backup_s3.local_storage.services.ZFSFileWriter;
import ru.rerumu.zfs_backup_s3.utils.FileManager;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Supplier;

public final class ConsecutiveLocalStorageService implements LocalStorageService {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private final ZFSFileWriterFactory zfsFileWriterFactory;
    private final ZFSFileReaderFactory zfsFileReaderFactory;
    private final FileManager fileManager;

    public ConsecutiveLocalStorageService(
            ZFSFileReaderFactory zfsFileReaderFactory,
            ZFSFileWriterFactory zfsFileWriterFactory,
            FileManager fileManager
    ) {
        this.zfsFileReaderFactory = zfsFileReaderFactory;
        this.zfsFileWriterFactory = zfsFileWriterFactory;
        this.fileManager = fileManager;
    }

    /**
     * First generate all files, then send them
     */
    @Override
    public void send(BufferedInputStream bufferedInputStream, Consumer<Path> fileConsumer) {
        try {
            List<Path> generatedFiles = new ArrayList<>();
            int n = 0;

            //File generation
            while (true) {
                Path newFilePath = fileManager.getNew(null, ".part" + n++);
                try (ZFSFileWriter zfsFileWriter = zfsFileWriterFactory.getZFSFileWriter(newFilePath)) {
                    zfsFileWriter.write(bufferedInputStream);
                } catch (FileHitSizeLimitException e) {
                    generatedFiles.add(newFilePath);
                    logger.debug(String.format("File '%s' added to generated", newFilePath));
                } catch (ZFSStreamEndedException e) {
                    generatedFiles.add(newFilePath);
                    logger.debug(String.format("File '%s' added to generated", newFilePath));
                    logger.info("End of stream. Exiting");
                    break;
                }
            }

            // File sending
            for (Path file : generatedFiles) {
                fileConsumer.accept(file);
                fileManager.delete(file);
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

    /**
     * First receive all files, then process
     */
    public void receive(List<String> keys, BiConsumer<String, Path> fileDownloader, BufferedOutputStream bufferedOutputStream) {
        try {

            List<Path> files = new ArrayList<>();

            for (String key : keys) {
                // Calc path from key
                Path path = fileManager.getNew(null, "-" + Paths.get(key).getFileName());
                files.add(path);

                fileDownloader.accept(key, path);
            }

            // Send to ZFS
            for (Path file : files) {
                ZFSFileReader zfsFileReader = zfsFileReaderFactory.getZFSFileReader(
                        bufferedOutputStream, file
                );
                zfsFileReader.read();
            }

            // Delete all files
            for (Path file : files) {
                fileManager.delete(file);
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }
}
