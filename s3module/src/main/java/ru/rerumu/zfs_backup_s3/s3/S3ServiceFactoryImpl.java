package ru.rerumu.zfs_backup_s3.s3;

import ru.rerumu.zfs_backup_s3.s3.impl.S3CallableFactory;
import ru.rerumu.zfs_backup_s3.s3.factories.S3ClientFactory;
import ru.rerumu.zfs_backup_s3.s3.impl.S3CallableFactoryImpl;
import ru.rerumu.zfs_backup_s3.s3.factories.impl.S3ClientFactoryImpl;
import ru.rerumu.zfs_backup_s3.s3.factories.impl.ZFSFileReaderFactoryImpl;
import ru.rerumu.zfs_backup_s3.s3.factories.impl.ZFSFileWriterFactoryImpl;
import ru.rerumu.zfs_backup_s3.s3.impl.S3ServiceImpl;
import ru.rerumu.zfs_backup_s3.s3.models.S3Storage;
import ru.rerumu.zfs_backup_s3.s3.services.S3RequestService;
import ru.rerumu.zfs_backup_s3.s3.services.impl.S3RequestServiceImpl;
import ru.rerumu.zfs_backup_s3.s3.services.impl.requests.CallableSupplierFactory;
import ru.rerumu.zfs_backup_s3.s3.utils.impl.FileManagerImpl;
import ru.rerumu.zfs_backup_s3.utils.callables.impl.CallableExecutorImpl;

import java.nio.file.Path;
import java.util.List;
import java.util.UUID;

public class S3ServiceFactoryImpl implements S3ServiceFactory {

    @Override
    public S3Service getS3Service(
            S3Storage s3Storage,
            int maxPartSize,
            long filePartSize,
            Path tempDir,
            UUID uuid
    ) {

        S3ClientFactory s3ClientFactory = new S3ClientFactoryImpl(List.of(s3Storage));
        S3RequestService s3RequestService = new S3RequestServiceImpl(
                new CallableExecutorImpl(),
                // TODO: Thread safe?
                new CallableSupplierFactory(
                        s3ClientFactory,
                        s3Storage
                ));
        S3CallableFactory s3CallableFactory = new S3CallableFactoryImpl(maxPartSize, s3Storage, s3ClientFactory, s3RequestService);
//        S3Repository s3Repository = new S3RepositoryImpl(s3CallableFactory);

        return new S3ServiceImpl(
                new FileManagerImpl(
                        uuid.toString(),
                        tempDir
                ),
                new ZFSFileWriterFactoryImpl(filePartSize),
                new ZFSFileReaderFactoryImpl(),
                s3CallableFactory
        );
    }
}
