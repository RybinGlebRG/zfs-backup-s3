package ru.rerumu.zfs_backup_s3.s3.services.impl;

import ru.rerumu.zfs_backup_s3.s3.services.S3RequestService;
import ru.rerumu.zfs_backup_s3.s3.services.impl.requests.models.ListObject;
import ru.rerumu.zfs_backup_s3.s3.services.impl.requests.models.UploadPartResult;
import ru.rerumu.zfs_backup_s3.utils.ByteArray;
import ru.rerumu.zfs_backup_s3.utils.ByteArrayList;
import ru.rerumu.zfs_backup_s3.utils.ImmutableList;
import software.amazon.awssdk.services.s3.model.CompletedPart;

import java.nio.file.Path;
import java.util.List;

public final class S3RequestServiceMock implements S3RequestService {
    @Override
    public UploadPartResult uploadPart(String key, String uploadId, Integer partNumber, ByteArray data) {
        return null;
    }

    @Override
    public String createMultipartUpload(String key) {
        return null;
    }

    @Override
    public void abortMultipartUpload(String key, String uploadId) {

    }

    @Override
    public void completeMultipartUpload(ImmutableList<CompletedPart> completedPartList, String key, String uploadId, ByteArrayList md5List) {

    }

    @Override
    public List<ListObject> listObjects(String key) {
        return null;
    }

    @Override
    public ListObject getMetadata(String key) {
        return null;
    }

    @Override
    public void putObject(Path sourcePath, String targetKey) {

    }

    @Override
    public byte[] getObjectRange(String key, Long startInclusive, Long endExclusive, Path targetPath) {
        return new byte[0];
    }
}
