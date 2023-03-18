package ru.rerumu.backups.exceptions;

public class ThreadKillError extends Error{
    public ThreadKillError() {
    }

    public ThreadKillError(Throwable cause) {
        super(cause);
    }
}
