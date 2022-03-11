package ru.rerumu.backups.exceptions;

public class EncryptException extends Exception{

    public EncryptException(Throwable e){
        super(e);
    }

    public EncryptException(String message){
        super(message);
    }
}
