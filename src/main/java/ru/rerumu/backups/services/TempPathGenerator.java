package ru.rerumu.backups.services;

import java.nio.file.Path;

@Deprecated
public interface TempPathGenerator {

    Path generateConsistent(String prefix, String postfix);
}
