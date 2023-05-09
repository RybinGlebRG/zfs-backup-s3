package ru.rerumu.utils.integration;

import org.junit.jupiter.api.Test;
import ru.rerumu.utils.processes.factories.impl.ProcessFactoryImpl;

import java.util.List;

public class ITProcessFactoryImpl {

    @Test
    void shouldCreate() throws Exception{
        ProcessFactoryImpl processFactory = new ProcessFactoryImpl();
        processFactory.create(List.of("pwd"));
    }
}
