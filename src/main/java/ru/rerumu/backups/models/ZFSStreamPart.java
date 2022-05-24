package ru.rerumu.backups.models;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.rerumu.backups.exceptions.IncorrectFilePartNameException;

import java.nio.file.Path;


public class ZFSStreamPart {
    private static final String FILE_POSTFIX = ".part";

    private final Logger logger = LoggerFactory.getLogger(ZFSStreamPart.class);

    private final Path fullPath;
    private final Path filename;
    private final int partNumber;
    private final String streamName;

    public ZFSStreamPart(Path fullPath) throws IncorrectFilePartNameException {
        this.fullPath = fullPath;
        this.filename = fullPath.getFileName();
        this.partNumber = parsePartNumber(filename);
        this.streamName = parsePrefix(filename);
    }

    private int parsePartNumber(Path path) {
        String filename = path.getFileName().toString();
        String[] nameParts = filename.split("\\.");

        String numberPart;
        if (nameParts[nameParts.length - 1].contains("part")) {
            numberPart = nameParts[nameParts.length - 1];
        } else {
            numberPart = nameParts[nameParts.length - 2];
        }
        String number = numberPart.replace("part", "");
        int res = Integer.parseInt(number);
        logger.trace(String.format("parsePartNumber - '%d'", res));
        return res;
    }

    private String parsePrefix(Path path) throws IncorrectFilePartNameException {
        String filename = path.getFileName().toString();
        int ind = filename.lastIndexOf(".part");
        if (ind==-1){
            throw new IncorrectFilePartNameException();
        }
        String prefixPart = filename.substring(0,ind);
        logger.trace(String.format("parsePrefix - '%s'", prefixPart));
        return prefixPart;
    }

    public String getStreamName() {
        logger.trace(String.format("getStreamName - '%s'", streamName));
        return streamName;
    }

    public int getPartNumber() {
        logger.trace(String.format("getPartNumber - '%d'", partNumber));
        return partNumber;
    }

    public Path getFullPath() {
        logger.trace(String.format("getFullPath - '%s'", fullPath.toString()));
        return fullPath;
    }

    public Path getFilename() {
        logger.trace(String.format("getFilename - '%s'", filename.toString()));
        return filename;
    }

    @Override
    public String toString() {
        return "ZFSStreamPart{" +
                "fullPath='" + fullPath.toString() + '\'' +
                ", filename='" + filename.toString() + '\'' +
                ", partNumber=" + partNumber +
                ", streamName='" + streamName + '\'' +
                '}';
    }
}
