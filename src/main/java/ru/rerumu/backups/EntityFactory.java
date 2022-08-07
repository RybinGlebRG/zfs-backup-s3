package ru.rerumu.backups;

import ru.rerumu.backups.exceptions.IncorrectHashException;
import ru.rerumu.backups.exceptions.NoDatasetMetaException;
import ru.rerumu.backups.factories.SnapshotSenderFactory;
import ru.rerumu.backups.factories.ZFSFileWriterFactory;
import ru.rerumu.backups.factories.ZFSProcessFactory;
import ru.rerumu.backups.factories.impl.*;
import ru.rerumu.backups.models.S3Storage;
import ru.rerumu.backups.repositories.LocalBackupRepository;
import ru.rerumu.backups.repositories.RemoteBackupRepository;
import ru.rerumu.backups.repositories.impl.LocalBackupRepositoryImpl;
import ru.rerumu.backups.repositories.impl.S3Repository;
import software.amazon.awssdk.regions.Region;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Paths;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

public class EntityFactory {
    private final Configuration configuration = new Configuration();

    public EntityFactory() throws IOException {
    }

    public LocalBackupRepository getLocalBackupRepository(RemoteBackupRepository remoteBackupRepository) throws IOException, NoSuchAlgorithmException, IncorrectHashException, NoDatasetMetaException {
        return new LocalBackupRepositoryImpl(
                Paths.get(configuration.getProperty("local_repository_dir")),
                remoteBackupRepository,
                Boolean.parseBoolean(configuration.getProperty("is.load.aws"))
        );
    }

    public List<S3Storage> getS3StorageList() throws URISyntaxException {
        List<S3Storage> s3StorageList = new ArrayList<>();
        s3StorageList.add(new S3Storage(
                Region.of(configuration.getProperty("s3.region_name")),
                configuration.getProperty("s3.full.s3_bucket"),
                configuration.getProperty("s3.access_key_id"),
                configuration.getProperty("s3.secret_access_key"),
                Paths.get(configuration.getProperty("s3.full.prefix")),
                new URI(configuration.getProperty("s3.endpoint_url")),
                configuration.getProperty("s3.full.storage_class")
        ));
        return s3StorageList;
    }

    public S3Repository getS3Repository(List<S3Storage> s3StorageList){
        return new S3Repository(
                s3StorageList,
                new S3ManagerFactoryImpl(
                        Integer.parseInt(configuration.getProperty("max_part_size"))
                ),
                new S3ClientFactoryImpl()
        );
    }

    public ZFSProcessFactory getZFSProcessFactory(){
        return new ZFSProcessFactoryImpl(
                new ProcessWrapperFactoryImpl()
        );
    }

    public ZFSFileWriterFactory getZFSFileWriterFactory(){
        return new ZFSFileWriterFactoryImpl(
                Long.parseLong(configuration.getProperty("max_file_size")));
    }

    public SnapshotSenderFactory getSnapshotSenderFactory(
            LocalBackupRepository localBackupRepository,
            ZFSProcessFactory zfsProcessFactory,
            ZFSFileWriterFactory zfsFileWriterFactory){
        return new SnapshotSenderFactoryImpl(
                localBackupRepository,
                zfsProcessFactory,
                zfsFileWriterFactory,
                Paths.get(configuration.getProperty("sender_temp_dir"))
        );
    }
}
