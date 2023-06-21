package ru.rerumu.zfs_backup_s3.s3.services.impl.requests.models;

import org.apache.commons.lang3.StringUtils;

// TODO: Check thread safe
public record ListObject(String key, String eTag, Long size) {

    public String md5Hex(){
        return StringUtils.strip(eTag,"\"");
    }
}
