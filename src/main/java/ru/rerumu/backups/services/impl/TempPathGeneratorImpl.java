package ru.rerumu.backups.services.impl;

import ru.rerumu.backups.services.TempPathGenerator;

import java.nio.file.Path;
import java.util.UUID;

public class TempPathGeneratorImpl implements TempPathGenerator {

    private final Path tempDir;
    private final String consistent;

    public TempPathGeneratorImpl(Path tempDir) {
        this.tempDir = tempDir;
        consistent = UUID.randomUUID().toString();
    }

    @Override
    public Path generateConsistent(String prefix, String postfix) {
        StringBuilder stringBuilder = new StringBuilder();
        if (prefix != null){
            stringBuilder.append(prefix);
        }
        stringBuilder.append(consistent);
        if (postfix != null){
            stringBuilder.append(postfix);
        }
        return tempDir.resolve(stringBuilder.toString());
    }
}
