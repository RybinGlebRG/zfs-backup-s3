package ru.rerumu.backups;

import ru.rerumu.backups.factories.SnapshotSenderFactory;
import ru.rerumu.backups.factories.ZFSFileWriterFactory;
import ru.rerumu.backups.factories.ZFSProcessFactory;
import ru.rerumu.backups.factories.impl.*;
import ru.rerumu.backups.models.S3Storage;
import ru.rerumu.backups.repositories.FilePartRepository;
import ru.rerumu.backups.repositories.impl.FilePartRepositoryImpl;
import ru.rerumu.backups.repositories.impl.S3Repository;
import software.amazon.awssdk.regions.Region;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class EntityFactory {
    private final Configuration configuration = new Configuration();

    public EntityFactory() throws IOException {
    }

    public FilePartRepository getFilePartRepository(){
        return new FilePartRepositoryImpl(
                Paths.get(configuration.getProperty("backup.directory"))
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
                new S3ClientFactoryImpl(),
                Paths.get(configuration.getProperty("temp_dir"))
        );
    }

    public ZFSProcessFactory getZFSProcessFactory(){
        return new ZFSProcessFactoryImpl(
                new ProcessWrapperFactoryImpl()
        );
    }

    public ZFSFileWriterFactory getZFSFileWriterFactory(){
        return new ZFSFileWriterFactoryImpl(
                Integer.parseInt(configuration.getProperty("chunk.size")),
                Long.parseLong(configuration.getProperty("file.part.size")));
    }

    public SnapshotSenderFactory getSnapshotSenderFactory(
            FilePartRepository filePartRepository,
            S3Repository s3Repository,
            ZFSProcessFactory zfsProcessFactory,
            ZFSFileWriterFactory zfsFileWriterFactory){
        return new SnapshotSenderFactoryImpl(
                true,
                filePartRepository,
                s3Repository,
                zfsProcessFactory,
                zfsFileWriterFactory,
                Boolean.parseBoolean(configuration.getProperty("is.load.aws"))
        );
    }
}
