package ru.rerumu.backups.exceptions;

public class CompressorException extends Exception{

    public CompressorException(Throwable e){
        super(e);
    }

    public CompressorException(String message){
        super(message);
    }
}
