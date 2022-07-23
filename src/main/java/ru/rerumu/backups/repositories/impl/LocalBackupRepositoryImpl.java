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

    private final Logger logger = LoggerFactory.getLogger(LocalBackupRepositoryImpl.class);

    private final Path repositoryDir;

    private final RemoteBackupRepository remoteBackupRepository;

    private final boolean isUseS3;


    public LocalBackupRepositoryImpl(
            Path repositoryDir,
            RemoteBackupRepository remoteBackupRepository,
            boolean isUseS3) throws IOException, NoSuchAlgorithmException, IncorrectHashException {

        this.repositoryDir = repositoryDir;
        this.remoteBackupRepository = remoteBackupRepository;
        this.isUseS3 = isUseS3;
        if (isUseS3){
            cloneRepository();
        }

    }

    private void clearClone() throws IOException {
        List<Path> filesToDelete = new ArrayList<>();
        try(Stream<Path> pathStream = Files.walk(repositoryDir)) {
                pathStream
                        .filter(path-> !path.equals(repositoryDir))
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
                    .filter(path->!Files.isDirectory(path) && !path.toString().equals("_meta.json"))
                    .sorted(Comparator.reverseOrder())
                    .forEach(filesToDelete::add);
        }

        for (Path path: filesToDelete){
            Files.delete(path);
        }
    }

    /**
     * Clones S3 repository metadata to local repository. Does not require lock, since does not share directory
     * with other processes
     *
     */
    private void cloneRepository() throws IOException, NoSuchAlgorithmException, IncorrectHashException {
        clearClone();

        Path backupMetaPath = remoteBackupRepository.getBackupMeta(repositoryDir);
        BackupMeta backupMeta = new BackupMeta(readJson(backupMetaPath));
        List<String> datasets = backupMeta.getDatasets();
        for (String dataset: datasets){
            Files.createDirectory(repositoryDir.resolve(dataset));
            Path datasetMetaPath = remoteBackupRepository.getDatasetMeta(dataset, repositoryDir.resolve(dataset));
        }
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

    private void addPartMeta(String datasetName, String partName, long partSize)
            throws IOException, NoSuchAlgorithmException, IncorrectHashException {
        DatasetMeta datasetMeta;
        Path path = repositoryDir.resolve(datasetName).resolve("_meta.json");

        if (!Files.exists(path)){
            datasetMeta = new DatasetMeta();
        } else {
            datasetMeta = new DatasetMeta(readJson(path));
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
        remoteBackupRepository.add(datasetName+"/", path);
        remoteBackupRepository.add(datasetName+"/", repositoryDir.resolve(datasetName).resolve("_meta.json"));
        remoteBackupRepository.add("", repositoryDir.resolve("_meta.json"));
    }

//    private void setLock() throws IOException {
//        Path path = repositoryDir.resolve("lock");
//        Files.createFile(path);
//    }
//
//    private void releaseLock() throws IOException {
//        Path path = repositoryDir.resolve("lock");
//        Files.delete(path);
//    }
//
//    private void waitLock() throws InterruptedException, IOException {
//        Path path = repositoryDir.resolve("lock");
//        while (Files.exists(path)) {
//            logger.debug("Repository locked. Waiting 1 second before retry");
//            Thread.sleep(1000);
//        }
//        setLock();
//    }


    // TODO: Check local exists?
    /**
     * Either loads file from S3 or resolves local file name. Local file have to exist in the moment of resolution.
     * Does not require locks, since remote repository case uses only one process and local repository case only
     * resolves name, doesn't modify repository contents
     */
    @Override
    public Path getPart(String datasetName, String partName)
            throws IOException, NoSuchAlgorithmException, IncorrectHashException, InterruptedException {

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

        return path;
    }

//    @Deprecated
//    @Override
//    public Path getNextPart(String datasetName, String partName)
//            throws IOException,
//            NoSuchAlgorithmException,
//            IncorrectHashException,
//            InterruptedException,
//            FinishedFlagException,
//            NoMorePartsException {
//        String nextPart = null;
//
//        if (Files.exists(repositoryDir.resolve("finished"))){
//            throw new FinishedFlagException();
//        }
//        DatasetMeta datasetMeta = getDatasetMeta(datasetName);
//        boolean isFoundCurrent = false;
//
//        for (String part: datasetMeta.getParts()){
//
//            if (!isFoundCurrent && part.equals(partName)){
//                isFoundCurrent=true;
//                continue;
//            }
//
//            nextPart = part;
//            break;
//        }
//        if (nextPart==null){
//            throw new NoMorePartsException();
//        }
//
//        Path path = getPart(datasetName, partName);
//
//        return path;
//    }

    /**
     * Gets datasets from local metadata
     */
    @Override
    public List<String> getDatasets() throws IOException {
        Path backupMetaPath = repositoryDir.resolve("_meta.json");
        BackupMeta backupMeta = new BackupMeta(readJson(backupMetaPath));
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
        if (isUseS3){
            clearRepositoryOnlyParts();
        }
        Path newPath = repositoryDir.resolve(partName);
        Files.move(path,newPath);
        addDatasetMeta(datasetName);
        addPartMeta(datasetName,partName,Files.size(path));
        if (isUseS3){
            push(datasetName, newPath);
            clearRepositoryOnlyParts();
        }
//        releaseLock();
    }



    @Override
    public void delete(Path path) throws IOException {
        logger.info(String.format("Deleting file '%s'",path.toString()));
        Files.delete(path);
        logger.info(String.format("File '%s' deleted",path.toString()));
    }

    @Override
    public void clear(String datasetName, String partName) throws IOException {
        Path path = repositoryDir.resolve(datasetName).resolve(partName);
        Files.delete(path);
    }
}
