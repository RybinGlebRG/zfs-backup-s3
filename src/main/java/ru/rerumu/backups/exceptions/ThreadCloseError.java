package ru.rerumu.backups.exceptions;

public class ThreadCloseError extends Error{


    public ThreadCloseError() {
    }

    public ThreadCloseError(Throwable cause) {
        super(cause);
    }
}
