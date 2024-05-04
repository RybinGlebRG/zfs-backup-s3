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
import ru.rerumu.zfs_backup_s3.s3.S3Service;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public final class ConsecutiveLocalStorageService implements LocalStorageService {
    private static final String PART_SUFFIX = ".part";
    private static final Comparator<Path> filesComparator = Comparator.comparing(path -> {
        String fileName = path.getFileName().toString();
        String fileNumberStr = fileName.substring(fileName.lastIndexOf(PART_SUFFIX) + PART_SUFFIX.length());
        return Integer.valueOf(fileNumberStr);
    });

    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private final ZFSFileWriterFactory zfsFileWriterFactory;
    private final ZFSFileReaderFactory zfsFileReaderFactory;
    private final S3Service s3Service;
    private final String unique;
    private final Path tempDir;

    public ConsecutiveLocalStorageService(
            ZFSFileReaderFactory zfsFileReaderFactory,
            ZFSFileWriterFactory zfsFileWriterFactory,
            S3Service s3Service,
            String unique,
            Path tempDir
    ) {
        this.zfsFileReaderFactory = zfsFileReaderFactory;
        this.zfsFileWriterFactory = zfsFileWriterFactory;
        this.s3Service = s3Service;
        this.unique = unique;
        this.tempDir = tempDir;
    }

    /**
     * First generate all files, then send them
     */
    @Override
    public void send(BufferedInputStream bufferedInputStream, String prefix) {
        try {
            List<Path> generatedFiles = new ArrayList<>();
            int n = 0;

            //File generation
            while (true) {
                Path newFilePath = tempDir.resolve(unique + ".part" + n++);
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
                s3Service.upload(file, prefix);
                Files.delete(file);
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

    @Override
    public void sendExisting(String prefix) {
        logger.info(String.format("Sending existing files. Prefix='%s'", prefix));
        try {
            List<Path> presentFiles = getPresentFiles();
            // TODO: Sort files
            // File sending
            for (Path file : presentFiles) {
                logger.debug(String.format("Uploading file '%s' with prefix '%s'", file.getFileName(), prefix));
                s3Service.upload(file, prefix);
                logger.debug(String.format("Deleting file '%s'", file.getFileName()));
                Files.delete(file);
            }
            logger.info("All files sent");
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

    @Override
    public boolean areFilesPresent() {
        try {
            return getPresentFiles().size() > 0;
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

    private List<Path> getPresentFiles() throws IOException {
        try (Stream<Path> pathStream = Files.list(tempDir)) {
            return pathStream.collect(Collectors.toCollection(ArrayList::new));
        }
    }

    /**
     * First receive all files, then process
     */
    @Override
    public void receive(String prefix, BufferedOutputStream bufferedOutputStream) {
        try {

            // Get S3 keys for stored files
            List<String> keys = s3Service.list(prefix);

            List<Path> files = new ArrayList<>();

            List<Path> alreadyPresent = getPresentFiles();

            for (String key : keys) {
                // Calc path from key
                Path path = tempDir.resolve(Paths.get(key).getFileName().toString());

                // Only load files if not already present
                if (!alreadyPresent.contains(path)) {
                    logger.info(String.format("File with key='%s' is not present. Downloading to file='%S'", key, path));
                    s3Service.download(key, path);
                }

                files.add(path);
            }

            files.sort(filesComparator);

            // Send to ZFS
            for (Path file : files) {
                ZFSFileReader zfsFileReader = zfsFileReaderFactory.getZFSFileReader(
                        bufferedOutputStream, file
                );
                zfsFileReader.read();
            }

            // Delete all files
            for (Path file : files) {
                Files.delete(file);
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }
}
