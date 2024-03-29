package ru.rerumu.zfs_backup_s3.s3.services.impl.requests.models;

import org.apache.commons.lang3.StringUtils;
import ru.rerumu.zfs_backup_s3.utils.ThreadSafe;

@ThreadSafe
public record ListObject(String key, String eTag, Long size) {

    public String md5Hex(){
        return StringUtils.strip(eTag,"\"");
    }
}
