package ru.rerumu.zfs_backup_s3.zfs.models;


import ru.rerumu.zfs_backup_s3.utils.ThreadSafe;

@ThreadSafe
public record Snapshot(String dataset, String name) {
    private final static String DELIMITER ="@";

    public Snapshot(String fullName){
        this(
                fullName.substring(0,fullName.indexOf(DELIMITER)),
                fullName.substring(fullName.indexOf(DELIMITER)+1)
        );
    }

    public String getDataset() {
        return dataset;
    }

    public String getFullName() {
        return dataset+ DELIMITER +name;
    }

    public String getName() {
        return name;
    }
}
