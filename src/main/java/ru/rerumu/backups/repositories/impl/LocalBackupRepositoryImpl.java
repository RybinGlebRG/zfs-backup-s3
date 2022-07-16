package ru.rerumu.backups.repositories.impl;

import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.rerumu.backups.exceptions.*;
import ru.rerumu.backups.models.meta.BackupMeta;
import ru.rerumu.backups.models.meta.DatasetMeta;
import ru.rerumu.backups.models.meta.PartMeta;
import ru.rerumu.backups.repositories.LocalBackupRepository;
import ru.rerumu.backups.repositories.RemoteBackupRepository;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;

public class LocalBackupRepositoryImpl implements LocalBackupRepository {

    private static final String filePostfix = ".part";
    private static final String FINISH_MARK = "finished";
    private final Path backupDirectory;
    private final Logger logger = LoggerFactory.getLogger(LocalBackupRepositoryImpl.class);

    private final Path repositoryDir;

    private final RemoteBackupRepository remoteBackupRepository;

    private final boolean isUseS3;


    public LocalBackupRepositoryImpl(
            Path backupDirectory,
            Path repositoryDir,
            RemoteBackupRepository remoteBackupRepository,
            boolean isUseS3) throws IOException, NoSuchAlgorithmException, IncorrectHashException {

        this.backupDirectory = backupDirectory;
        this.repositoryDir = repositoryDir;
        this.remoteBackupRepository = remoteBackupRepository;
        this.isUseS3 = isUseS3;
        if (isUseS3){
            cloneRepository();
        }

    }

    @Override
    public Path createNewFilePath(String prefix, int partNumber) {
        Path filePart = Paths.get(backupDirectory.toString(), prefix + filePostfix + partNumber);
        return filePart;
    }

    private void clearClone() throws IOException {
        List<Path> filesToDelete = new ArrayList<>();
        try(Stream<Path> pathStream = Files.walk(repositoryDir)) {
                pathStream
                        .sorted(Comparator.reverseOrder())
                        .forEach(filesToDelete::add);
        }

        for (Path path: filesToDelete){
            Files.delete(path);
        }
    }

    private void clearRepositoryOnlyParts() throws IOException {
        List<Path> filesToDelete = new ArrayList<>();
        try(Stream<Path> pathStream = Files.walk(repositoryDir)) {
            pathStream
                    .sorted(Comparator.reverseOrder())
                    .forEach(i->{
                        if (!Files.isDirectory(i) && !i.toString().equals("_meta.json")){
                            filesToDelete.add(i);
                        }
                    });
        }

        for (Path path: filesToDelete){
            Files.delete(path);
        }
    }
    private void cloneRepository() throws IOException, NoSuchAlgorithmException, IncorrectHashException {
        clearClone();

        Path backupMetaPath = remoteBackupRepository.getBackupMeta(repositoryDir);
        BackupMeta backupMeta = new BackupMeta(new JSONObject(backupMetaPath));
        List<String> datasets = backupMeta.getDatasets();
        for (String dataset: datasets){
            Path datasetMetaPath = remoteBackupRepository.getDatasetMeta(dataset, repositoryDir.resolve(dataset));
        }
    }

    private void addDatasetMeta(String datasetName) throws IOException, NoSuchAlgorithmException, IncorrectHashException, S3MissesFileException {
        Path path = repositoryDir.resolve("_meta.json");
        BackupMeta backupMeta;

        if (!Files.exists(path)) {
            backupMeta = new BackupMeta();
        } else {
            backupMeta = new BackupMeta(new JSONObject(path));
        }
        if (backupMeta.isAdded(datasetName)){
            return;
        }
        backupMeta.addDataset(datasetName);

        Files.writeString(
                path,
                backupMeta.toJSONObject().toString(),
                StandardCharsets.UTF_8,
                StandardOpenOption.CREATE,
                StandardOpenOption.TRUNCATE_EXISTING,
                StandardOpenOption.WRITE
        );
    }

    public void addPartMeta(String datasetName, String partName, long partSize)
            throws IOException, NoSuchAlgorithmException, IncorrectHashException {
        DatasetMeta datasetMeta;
        Path path = repositoryDir.resolve(datasetName).resolve("_meta.json");

        if (!Files.exists(path)){
            datasetMeta = new DatasetMeta();
        } else {
            datasetMeta = new DatasetMeta(new JSONObject(path));
        }

        datasetMeta.addPart(new PartMeta(partName,partSize));
        Files.writeString(
                path,
                datasetMeta.toJSONObject().toString(),
                StandardCharsets.UTF_8,
                StandardOpenOption.CREATE,
                StandardOpenOption.TRUNCATE_EXISTING,
                StandardOpenOption.WRITE
        );
    }

    private void push(String datasetName, Path path)
            throws S3MissesFileException, IOException, NoSuchAlgorithmException, IncorrectHashException {
        remoteBackupRepository.addPath(datasetName+"/", path);
        remoteBackupRepository.addPath(datasetName+"/", repositoryDir.resolve(datasetName).resolve("_meta.json"));
        remoteBackupRepository.addPath("", repositoryDir.resolve("_meta.json"));
    }

    private void setLock() throws IOException {
        Path path = repositoryDir.resolve("lock");
        Files.createFile(path);
    }

    private void releaseLock() throws IOException {
        Path path = repositoryDir.resolve("lock");
        Files.delete(path);
    }

    private void waitLock() throws InterruptedException, IOException {
        Path path = repositoryDir.resolve("lock");
        while (Files.exists(path)) {
            logger.debug("Repository locked. Waiting 1 second before retry");
            Thread.sleep(1000);
        }
        setLock();
    }

    @Override
    public Path getPart(String datasetName, String partName)
            throws IOException, NoSuchAlgorithmException, IncorrectHashException, InterruptedException {

        waitLock();
        Path path;
        if (isUseS3) {
            // Clearing space
            clearRepositoryOnlyParts();

            path = remoteBackupRepository.getPart(
                    datasetName,
                    partName,
                    repositoryDir.resolve(datasetName)
            );

        } else {
            path = repositoryDir.resolve(datasetName).resolve(partName);
        }

        releaseLock();
        return path;
    }

    @Override
    public Path getNextPart(String datasetName, String partName)
            throws IOException, NoSuchAlgorithmException, IncorrectHashException, InterruptedException, FinishedFlagException {
        String nextPart = null;

        while (true){
            if (Files.exists(repositoryDir.resolve("finished"))){
                throw new FinishedFlagException();
            }
            DatasetMeta datasetMeta = getDatasetMeta(datasetName);
            boolean isFoundCurrent = false;

            for (String part: datasetMeta.getParts()){

                if (!isFoundCurrent && part.equals(partName)){
                    isFoundCurrent=true;
                    continue;
                }

                nextPart = part;
                break;
            }
            if (nextPart!=null){
                break;
            } else {
                logger.debug("No acceptable files found. Waiting 1 second before retry");
                Thread.sleep(1000);
            }
        }

        return repositoryDir.resolve(datasetName).resolve(nextPart);
    }

    @Override
    public List<String> getDatasets() throws IOException, NoSuchAlgorithmException, IncorrectHashException {
        BackupMeta backupMeta = getBackupMeta();
        return backupMeta.getDatasets();
    }

    @Override
    public List<String> getParts(String datasetName) throws IOException, NoSuchAlgorithmException, IncorrectHashException {
        DatasetMeta datasetMeta = getDatasetMeta(datasetName);
        return datasetMeta.getParts();
    }

    @Override
    public void add(String datasetName, String partName, Path path)
            throws IOException, S3MissesFileException, NoSuchAlgorithmException, IncorrectHashException, InterruptedException {
        waitLock();
        clearRepositoryOnlyParts();
        Path newPath = repositoryDir.resolve(partName);
        Files.move(path,newPath);
        addDatasetMeta(datasetName);
        addPartMeta(datasetName,partName,Files.size(path));
        if (isUseS3){
            push(datasetName, newPath);
            clearRepositoryOnlyParts();
        }
        releaseLock();
    }

    private BackupMeta getBackupMeta() throws IOException, NoSuchAlgorithmException, IncorrectHashException {
        Path backupMetaPath = repositoryDir.resolve("_meta.json");
        return new BackupMeta(new JSONObject(backupMetaPath));
    }

    private DatasetMeta getDatasetMeta(String datasetName) throws IOException, NoSuchAlgorithmException, IncorrectHashException {
        Path datasetMetaPath = repositoryDir.resolve(datasetName).resolve("_meta.json");
        return new DatasetMeta(new JSONObject(datasetMetaPath));
    }

    @Override
    public void delete(Path path) throws IOException {
        logger.info(String.format("Deleting file '%s'",path.toString()));
        Files.delete(path);
        logger.info(String.format("File '%s' deleted",path.toString()));
    }

    @Override
    public Path markReady(Path path) throws IOException {
        logger.info(String.format("Marking file '%s' as 'ready'",path.toString()));
        Path res = Paths.get(path.toString() + ".ready");
        Files.move(path, res);
        logger.info(String.format("markReady - '%s'",res.toString()));
        return res;
    }

    @Override
    public Path markReceived(Path path) throws IOException {
        logger.info(String.format("Marking file '%s' as 'received'",path.toString()));
        String fileName = path.getFileName().toString();
        fileName = fileName.replace(".ready", ".received");
        Path res = Paths.get(path.getParent().toString(), fileName);
        Files.move(path, res);
        logger.info(String.format("markReceived - '%s'",res.toString()));
        return res;
    }

    @Override
    public Path getNextInputPath() throws NoMorePartsException, FinishedFlagException, IOException, TooManyPartsException {
        logger.info("Starting looking for next input path");
        List<Path> fileParts = new ArrayList<>();
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(backupDirectory)) {
            for (Path item : stream) {
                logger.info(String.format("Found file '%s'", item.toString()));
                if (item.toString().endsWith(".ready") || item.getFileName().toString().equals(FINISH_MARK)) {
                    logger.info(String.format("Accepted file '%s'", item.toString()));
                    fileParts.add(item);
                }
            }
        }

        if (fileParts.size() == 0) {
            logger.info("Did not find acceptable files");
            throw new NoMorePartsException();
        } else if (fileParts.size() > 1) {
            logger.info("Found too many files");
            throw new TooManyPartsException();
        } else {

            Path filePart = fileParts.get(0);
            if (filePart.getFileName().toString().equals(FINISH_MARK)) {
                logger.info("Found 'finished' flag");
                throw new FinishedFlagException();
            }
            logger.info(String.format("getNextInputPath - '%s'",filePart.toString()));
            return filePart;

        }
    }
}
