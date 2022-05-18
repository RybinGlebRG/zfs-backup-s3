package ru.rerumu.backups.services.helpers;

import ru.rerumu.backups.models.S3Storage;
import ru.rerumu.backups.services.S3Loader;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class S3LoaderTest implements S3Loader {

    private final Path srcDir;
    private final Path dstDir;

    public S3LoaderTest(Path srcDir, Path dstDir){
        this.srcDir = srcDir;
        this.dstDir = dstDir;
    }

    @Override
    public void addStorage(S3Storage s3Storage) {

    }

    @Override
    public void upload(Path path) throws IOException, InterruptedException {
        while (true){
            boolean isFoundFile = true;
            try (DirectoryStream<Path> stream = Files.newDirectoryStream(dstDir)) {
                isFoundFile = false;
                for (Path item : stream) {
                    isFoundFile = true;
                }
            }
            if (!isFoundFile){
                break;
            }
            Thread.sleep(10000);
        }

        String fileName = path.getFileName().toString();
        Path target = dstDir.resolve(fileName);
        Files.copy(path,target);

        Path res = Paths.get(target.toString() + ".ready");
        Files.move(target,res);
    }
}
