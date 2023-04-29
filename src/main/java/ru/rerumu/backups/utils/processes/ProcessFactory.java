package ru.rerumu.backups.utils.processes;

import ru.rerumu.backups.utils.processes.impl.ProcessWrapperImpl;

import java.io.BufferedInputStream;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.function.Consumer;

public interface ProcessFactory {

    ProcessWrapperImpl getProcessWrapper(
            List<String> args,
            TriConsumer<BufferedInputStream,Runnable,Runnable> stderrProcessor,
            TriConsumer<BufferedInputStream,Runnable,Runnable> stdoutProcessor
    );
}
