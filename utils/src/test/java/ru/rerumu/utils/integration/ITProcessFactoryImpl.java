package ru.rerumu.utils.integration;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import ru.rerumu.zfs_backup_s3.utils.processes.factories.impl.ProcessFactoryImpl;

import java.util.List;

@Disabled
public class ITProcessFactoryImpl {

    @Test
    void shouldCreate() throws Exception{
        ProcessFactoryImpl processFactory = new ProcessFactoryImpl();
        processFactory.create(List.of("pwd"));
    }
}
