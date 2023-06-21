package ru.rerumu.zfs_backup_s3.zfs.models;


// TODO: Check thread safe
public record Snapshot(String dataset, String name) {
    private final static String DELIMITER ="@";

//    public Snapshot(String dataset, String name){
//        this(dataset,name,dataset+ DELIMITER +name);
//    }

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
