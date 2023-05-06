package ru.rerumu.utils.processes.factories;

import ru.rerumu.utils.processes.StdProcessor;

import java.util.List;
import java.util.concurrent.Callable;

public interface ProcessWrapperFactory {

    Callable<Void> getProcessWrapper(
            List<String> args,
            StdProcessor stdProcessor
    );
}
