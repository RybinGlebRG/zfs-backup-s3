package ru.rerumu.backups.models;

import java.nio.file.Path;


public class ZFSStreamPart {
    private static final String FILE_POSTFIX = ".part";
    private final Path fullPath;
    private final Path filename;
    private final int partNumber;
    private final String streamName;

    public ZFSStreamPart(Path fullPath){
        this.fullPath = fullPath;
        this.filename = fullPath.getFileName();
        this.partNumber = parsePartNumber(filename);
        this.streamName = parsePrefix(filename);
    }

    private int parsePartNumber(Path path){
        String filename = path.getFileName().toString();
        String[] nameParts = filename.split("\\.");
        String numberPart = nameParts[1];
        String number = numberPart.replace("part","");
        int res = Integer.parseInt(number);
        return res;
    }

    private String parsePrefix(Path path){
        String filename = path.getFileName().toString();
        String[] nameParts = filename.split("\\.");
        String prefixPart = nameParts[0];
        return prefixPart;
    }

    public String getStreamName() {
        return streamName;
    }

    public int getPartNumber() {
        return partNumber;
    }

    public Path getFullPath() {
        return fullPath;
    }

    public Path getFilename() {
        return filename;
    }
}
