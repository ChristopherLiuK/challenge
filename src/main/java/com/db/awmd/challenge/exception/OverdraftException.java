package com.db.awmd.challenge.exception;

public class OverdraftException extends RuntimeException{

    public OverdraftException() {
        super("Insufficient Balance");
    }
}
