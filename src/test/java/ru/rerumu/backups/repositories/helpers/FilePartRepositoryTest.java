package ru.rerumu.backups.repositories.helpers;

import org.apache.commons.lang3.RandomStringUtils;
import ru.rerumu.backups.exceptions.FinishedFlagException;
import ru.rerumu.backups.exceptions.NoMorePartsException;
import ru.rerumu.backups.exceptions.TooManyPartsException;
import ru.rerumu.backups.repositories.FilePartRepository;

import java.io.*;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class FilePartRepositoryTest  implements FilePartRepository {

    private final List<ByteArrayInputStream> byteArrayInputStreamList = new ArrayList<>();
    private int n=0;

    private List<ByteArrayOutputStream> streamList = new ArrayList<>();
    private List<Path> pathList = new ArrayList<>();

    public FilePartRepositoryTest(List<ByteArrayOutputStream> streamList){
        for (ByteArrayOutputStream item:streamList ) {
            byteArrayInputStreamList.add(new ByteArrayInputStream(item.toByteArray()));
        }
    }

    public BufferedInputStream getNextInputStream() throws NoMorePartsException, FinishedFlagException, IOException, TooManyPartsException {
        try {
            ByteArrayInputStream byteArrayInputStream = byteArrayInputStreamList.get(n);
            n++;
            pathList.add(Paths.get(
                    RandomStringUtils.random(5, true, true),
                    RandomStringUtils.random(7, true, true),
                    RandomStringUtils.random(4, true, true)
            ));
            return new BufferedInputStream(byteArrayInputStream);
        } catch (IndexOutOfBoundsException e){
            throw new FinishedFlagException();
        }
    }

    @Override
    public Path getNextInputPath() throws NoMorePartsException, FinishedFlagException, IOException, TooManyPartsException {
        return null;
    }

//    @Override
//    public void deleteLastPart() throws IOException {
//
//    }

    @Override
    public void delete(Path path) throws IOException {

    }

//    @Override
//    public void markReceivedLastPart() {
//
//    }

    @Override
    public Path markReceived(Path path) throws IOException {
        return null;
    }

    @Override
    public Path markReady(Path path) throws IOException {
        return null;
    }

    public void markReadyLastPart() throws IOException {

    }

    public BufferedOutputStream newPart() throws IOException {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        streamList.add(byteArrayOutputStream);
        String generatedString = RandomStringUtils.random(10, true, true);
        pathList.add(Paths.get(
                RandomStringUtils.random(5, true, true),
                RandomStringUtils.random(7, true, true),
                RandomStringUtils.random(4, true, true)
        ));
        return new BufferedOutputStream(byteArrayOutputStream);
    }

//    @Override
//    public Path getLastPart() {
//        return pathList.get(pathList.size()-1);
//    }

//    @Override
//    public boolean isExists(Path path) {
//        return false;
//    }

    @Override
    public Path createNewFilePath(String template, int partNumber) {
        return null;
    }

//    @Override
//    public OutputStream createNewOutputStream(Path path) throws IOException {
//        return null;
//    }
//
//    @Override
//    public InputStream createNewInputStream(Path path) throws IOException {
//        return null;
//    }

    public List<ByteArrayOutputStream> getStreams(){
        return streamList;
    }

    public List<Path> getPathList() {
        return pathList;
    }
}
