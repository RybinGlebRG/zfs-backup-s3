package ru.rerumu.backups.utils.processes;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.util.List;
import java.util.concurrent.Callable;

public interface ProcessFactory {

    Callable<Void> getProcessWrapper(
            List<String> args,
            TriConsumer<BufferedInputStream,Runnable,Runnable> stderrProcessor,
            TriConsumer<BufferedInputStream,Runnable,Runnable> stdoutProcessor
    );

    Callable<Void> getProcessWrapper(
            List<String> args,
            TriConsumer<BufferedInputStream, Runnable, Runnable> stderrProcessor,
            TriConsumer<BufferedInputStream, Runnable, Runnable> stdoutProcessor,
            TriConsumer<BufferedOutputStream, Runnable, Runnable> stdinProcessor
    );
}
