package ru.rerumu.utils.processes;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

public interface StdProcessor {

    void processStd(
            BufferedInputStream stderr,
            BufferedInputStream stdout,
            BufferedOutputStream stdin
    ) throws ExecutionException, InterruptedException, IOException;
}
