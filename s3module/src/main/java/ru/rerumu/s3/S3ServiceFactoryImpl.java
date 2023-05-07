package ru.rerumu.s3;

import ru.rerumu.s3.factories.S3CallableFactory;
import ru.rerumu.s3.factories.S3ClientFactory;
import ru.rerumu.s3.factories.impl.S3CallableFactoryImpl;
import ru.rerumu.s3.factories.impl.S3ClientFactoryImpl;
import ru.rerumu.s3.factories.impl.ZFSFileReaderFactoryImpl;
import ru.rerumu.s3.factories.impl.ZFSFileWriterFactoryImpl;
import ru.rerumu.s3.impl.S3ServiceImpl;
import ru.rerumu.s3.models.S3Storage;
import ru.rerumu.s3.repositories.S3Repository;
import ru.rerumu.s3.repositories.impl.S3RepositoryImpl;
import ru.rerumu.s3.repositories.impl.S3StreamRepository;
import ru.rerumu.s3.utils.impl.FileManagerImpl;
import ru.rerumu.utils.callables.CallableExecutor;
import ru.rerumu.utils.callables.impl.CallableExecutorImpl;

import java.nio.file.Path;
import java.util.List;
import java.util.UUID;

public class S3ServiceFactoryImpl implements S3ServiceFactory {
    @Override
    public S3Service getS3Service(
            S3Storage s3Storage,
            int maxPartSize,
            long filePartSize,
            Path tempDir
    ) {
        S3ClientFactory s3ClientFactory = new S3ClientFactoryImpl(List.of(s3Storage));
        S3CallableFactory s3CallableFactory = new S3CallableFactoryImpl(maxPartSize, s3Storage, s3ClientFactory);
        CallableExecutor callableExecutor = new CallableExecutorImpl();
        S3Repository s3Repository = new S3RepositoryImpl(s3CallableFactory,callableExecutor);
        S3StreamRepository s3StreamRepository = new S3StreamRepository(
                s3Repository,
                new ZFSFileWriterFactoryImpl(filePartSize),
                new ZFSFileReaderFactoryImpl(),
                new FileManagerImpl(
                        UUID.randomUUID().toString(),
                        tempDir
                )
        );
        return new S3ServiceImpl(s3Repository, s3StreamRepository);
    }
}
