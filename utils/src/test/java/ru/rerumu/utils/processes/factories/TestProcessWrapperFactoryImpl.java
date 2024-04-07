package ru.rerumu.utils.processes.factories;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.rerumu.zfs_backup_s3.utils.processes.StdProcessor4Mock;
import ru.rerumu.zfs_backup_s3.utils.processes.factories.impl.ProcessFactory4Mock;
import ru.rerumu.zfs_backup_s3.utils.processes.factories.impl.ProcessWrapperFactoryImpl;


@ExtendWith(MockitoExtension.class)
public class TestProcessWrapperFactoryImpl {

    @Mock
    ProcessFactory4Mock processFactory;

    @Mock
    StdProcessor4Mock stdProcessor;

    @Test
    void shouldCreate(){
        ProcessWrapperFactoryImpl factory =  new ProcessWrapperFactoryImpl(processFactory);
        factory.getProcessWrapper(null,stdProcessor);
    }
}
