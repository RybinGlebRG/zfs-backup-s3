package ru.rerumu.backups.utils.processes.factories.impl;

import ru.rerumu.backups.utils.processes.factories.ProcessFactory;

import java.io.IOException;
import java.util.List;

public class ProcessFactoryImpl implements ProcessFactory {
    @Override
    public Process create(List<String> args) throws IOException {
        ProcessBuilder pb = new ProcessBuilder(args);
        return pb.start();
    }
}
