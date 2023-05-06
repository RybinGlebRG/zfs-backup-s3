package ru.rerumu.backups.utils.processes.factories;

import ru.rerumu.backups.utils.processes.StdProcessor;

import java.util.List;
import java.util.concurrent.Callable;

public interface ProcessWrapperFactory {

    Callable<Void> getProcessWrapper(
            List<String> args,
            StdProcessor stdProcessor
    );
}
