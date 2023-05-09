package ru.rerumu.s3.impl.helper.factories;

import ru.rerumu.s3.impl.helper.CreateMultipartUploadCallable;
import ru.rerumu.s3.impl.helper.UploadPartCallable;

public interface HelperCallableFactory {

    CreateMultipartUploadCallable getCreateMultipartUploadCallable(String key);

    UploadPartCallable getUploadPartCallable(String key,String uploadId, Integer partNumber, byte[] data);
}
