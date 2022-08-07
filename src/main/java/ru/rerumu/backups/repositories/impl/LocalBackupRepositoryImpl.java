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
import ru.rerumu.backups.utils.MD5;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class LocalBackupRepositoryImpl implements LocalBackupRepository {

    private final Logger logger = LoggerFactory.getLogger(LocalBackupRepositoryImpl.class);

    private final Path repositoryDir;

    private final RemoteBackupRepository remoteBackupRepository;

    private final boolean isUseS3;


    public LocalBackupRepositoryImpl(
            Path repositoryDir,
            RemoteBackupRepository remoteBackupRepository,
            boolean isUseS3) throws IOException, NoSuchAlgorithmException, IncorrectHashException, NoDatasetMetaException {

        this.repositoryDir = repositoryDir;
        this.remoteBackupRepository = remoteBackupRepository;
        this.isUseS3 = isUseS3;
        if (isUseS3){
            cloneRepository();
        }

    }

    private void clearClone() throws IOException {
        logger.info("Clearing clone directory");
        List<Path> filesToDelete;
        try(Stream<Path> pathStream = Files.walk(repositoryDir)) {
            filesToDelete = pathStream
                        .filter(path-> !path.equals(repositoryDir))
                        .sorted(Comparator.reverseOrder())
                        .collect(Collectors.toCollection(ArrayList::new));
        }

        for (Path path: filesToDelete){
            Files.delete(path);
        }
        logger.info("Clearing clone directory finished");
    }

    private void clearRepositoryOnlyParts() throws IOException {
        List<Path> filesToDelete;
        try(Stream<Path> pathStream = Files.walk(repositoryDir)) {
            filesToDelete = pathStream
                    .filter(path->!Files.isDirectory(path) && !path.getFileName().toString().equals("_meta.json"))
                    .sorted(Comparator.reverseOrder())
                    .collect(Collectors.toCollection(ArrayList::new));
        }

        for (Path path: filesToDelete){
            Files.delete(path);
        }
    }

    /**
     * Clones S3 repository metadata to local repository
     *
     */
    private void cloneRepository() throws IOException, NoSuchAlgorithmException, IncorrectHashException, NoDatasetMetaException {
        logger.info("Cloning repository");
        clearClone();
        try {
            Path backupMetaPath = remoteBackupRepository.getBackupMeta(repositoryDir);
            BackupMeta backupMeta = new BackupMeta(readJson(backupMetaPath));
            List<String> datasets = backupMeta.getDatasets();
            for (String dataset: datasets){
                Files.createDirectory(repositoryDir.resolve(dataset));
                Path datasetMetaPath = remoteBackupRepository.getDatasetMeta(dataset, repositoryDir.resolve(dataset));
            }
        } catch (NoBackupMetaException e){
            logger.warn("Remote repository is empty");
        }
        logger.info("Cloning repository finished");
    }

    private JSONObject readJson(Path path) throws IOException {
        String jsonString;
        try(InputStream inputStream = Files.newInputStream(path);
            BufferedInputStream bufferedInputStream = new BufferedInputStream(inputStream)){
            jsonString = new String(bufferedInputStream.readAllBytes(),StandardCharsets.UTF_8);
        }
        return new JSONObject(jsonString);
    }

    private void addDatasetMeta(String datasetName) throws IOException, NoSuchAlgorithmException, IncorrectHashException, S3MissesFileException {
        Path path = repositoryDir.resolve("_meta.json");
        BackupMeta backupMeta;

        if (!Files.exists(path)) {
            backupMeta = new BackupMeta();
        } else {
            backupMeta = new BackupMeta(readJson(path));
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

    private void addPartMeta(String datasetName, String partName, long partSize, String md5Hex)
            throws IOException, NoSuchAlgorithmException, IncorrectHashException {
        DatasetMeta datasetMeta;
        Path path = repositoryDir.resolve(datasetName).resolve("_meta.json");

        if (!Files.exists(path)){
            datasetMeta = new DatasetMeta();
        } else {
            datasetMeta = new DatasetMeta(readJson(path));
        }

        datasetMeta.addPart(new PartMeta(partName,partSize,datasetName,md5Hex));
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
        remoteBackupRepository.add(datasetName+"/", path);
        remoteBackupRepository.add(datasetName+"/", repositoryDir.resolve(datasetName).resolve("_meta.json"));
        remoteBackupRepository.add("", repositoryDir.resolve("_meta.json"));
    }

    private PartMeta getPartMeta(String datasetName, String partName) throws IOException {
        Path datasetMetaPath = repositoryDir.resolve(datasetName).resolve("_meta.json");
        DatasetMeta datasetMeta = new DatasetMeta(readJson(datasetMetaPath));
        return datasetMeta.getPartMeta(partName);
    }

    /**
     * Either loads file from S3 or resolves local file name. Local file have to exist in the moment of resolution.
     * Does not require locks, since remote repository case uses only one process and local repository case only
     * resolves name, doesn't modify repository contents
     */
    @Override
    public Path getPart(String datasetName, String partName)
            throws IOException, NoSuchAlgorithmException, IncorrectHashException, InterruptedException, NoPartFoundException {

        Path path;
        if (isUseS3) {
            // Clearing space
            clearRepositoryOnlyParts();

            PartMeta partMeta = getPartMeta(datasetName, partName);

            path = remoteBackupRepository.getPart(
                    datasetName,
                    partName,
                    repositoryDir.resolve(datasetName),
                    partMeta
            );

        } else {
            path = repositoryDir.resolve(datasetName).resolve(partName);
            if (!Files.exists(path)){
                throw new NoPartFoundException();
            }
        }

        return path;
    }

    /**
     * Gets datasets from local metadata
     */
    @Override
    public List<String> getDatasets() throws IOException {
        logger.info("Getting datasets from backup metadata");
        Path backupMetaPath = repositoryDir.resolve("_meta.json");
        BackupMeta backupMeta = new BackupMeta(readJson(backupMetaPath));
        logger.debug(String.format("Got datasets: %s",backupMeta.getDatasets()));
        return backupMeta.getDatasets();
    }

    /**
     * Gets parts from local metadata
     */
    @Override
    public List<String> getParts(String datasetName) throws IOException {
        Path datasetMetaPath = repositoryDir.resolve(datasetName).resolve("_meta.json");
        DatasetMeta datasetMeta = new DatasetMeta(readJson(datasetMetaPath));
        return datasetMeta.getParts();
    }

    @Override
    public void add(String datasetName, String partName, Path path)
            throws IOException, S3MissesFileException, NoSuchAlgorithmException, IncorrectHashException, InterruptedException {
//        waitLock();
//        if (isUseS3){
//            clearRepositoryOnlyParts();
//        }
        Path newPath = repositoryDir.resolve(datasetName).resolve(partName);
        if (!Files.exists(repositoryDir.resolve(datasetName))){
            Files.createDirectory(repositoryDir.resolve(datasetName));
        }
        Files.move(path,newPath);
        addDatasetMeta(datasetName);
        addPartMeta(datasetName,partName,Files.size(newPath),MD5.getMD5Hex(newPath));
        if (isUseS3){
            push(datasetName, newPath);
            clearRepositoryOnlyParts();
        }
    }
}
