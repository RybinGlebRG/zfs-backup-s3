package ru.rerumu.zfs_backup_s3.s3.services;

import ru.rerumu.zfs_backup_s3.s3.exceptions.PartNumberTooLargeException;
import ru.rerumu.zfs_backup_s3.utils.ImmutableMap;
import ru.rerumu.zfs_backup_s3.s3.services.impl.S3RequestServiceImpl;
import ru.rerumu.zfs_backup_s3.s3.services.impl.S3RequestServiceMock;
import ru.rerumu.zfs_backup_s3.s3.services.impl.requests.models.ListObject;
import ru.rerumu.zfs_backup_s3.s3.services.impl.requests.models.UploadPartResult;
import ru.rerumu.zfs_backup_s3.utils.ByteArray;
import ru.rerumu.zfs_backup_s3.utils.ByteArrayList;
import ru.rerumu.zfs_backup_s3.utils.ImmutableList;
import ru.rerumu.zfs_backup_s3.utils.ThreadSafe;
import software.amazon.awssdk.services.s3.model.*;

import java.nio.file.Path;
import java.util.List;

@ThreadSafe
public sealed interface S3RequestService permits S3RequestServiceMock, S3RequestServiceImpl {

    UploadPartResult uploadPart(
            String key, String uploadId, Integer partNumber, ByteArray data
    ) throws PartNumberTooLargeException;

    String createMultipartUpload(
            String key,
            ImmutableMap metadata
    );

    void abortMultipartUpload(
            String key, String uploadId
    );

    void completeMultipartUpload(
            ImmutableList<CompletedPart> completedPartList, String key, String uploadId, ByteArrayList md5List
    );

    List<ListObject> listObjects(
            String key
    );

    ListObject getMetadata(String key);


    void putObject(Path sourcePath, String targetKey);

    byte[] getObjectRange(String key,Long startInclusive, Long endExclusive, Path targetPath);

    ImmutableMap getObjectMetadata(String key);
}
