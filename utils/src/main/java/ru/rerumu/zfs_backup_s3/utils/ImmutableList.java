package ru.rerumu.zfs_backup_s3.utils;

import java.util.ArrayList;
import java.util.List;


public record ImmutableList<T>(List<T> list) {

    public ImmutableList{
        list = new ArrayList<>(list);
    }

    @Override
    public List<T> list() {
        return new ArrayList<>(list);
    }
}
