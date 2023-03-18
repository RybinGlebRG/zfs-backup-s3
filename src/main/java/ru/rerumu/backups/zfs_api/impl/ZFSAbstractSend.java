package ru.rerumu.backups.zfs_api.impl;

import ru.rerumu.backups.exceptions.ThreadCloseError;
import ru.rerumu.backups.exceptions.ThreadKillError;
import ru.rerumu.backups.zfs_api.ProcessWrapper;
import ru.rerumu.backups.zfs_api.ZFSSend;

import java.io.BufferedInputStream;

public abstract class ZFSAbstractSend implements ZFSSend {

    protected final ProcessWrapper processWrapper;


    public ZFSAbstractSend(ProcessWrapper processWrapper) {
        this.processWrapper = processWrapper;
    }

    @Override
    public BufferedInputStream getBufferedInputStream() {
        return processWrapper.getBufferedInputStream();
    }

    @Override
    public void close()  {
        try {
            processWrapper.close();
        } catch (Exception e){
            kill();
            throw new ThreadCloseError(e);
        }
    }

    @Override
    public void kill()  {
        try {
            processWrapper.kill();
        } catch (Exception e){
            throw new ThreadKillError(e);
        }
    }
}
