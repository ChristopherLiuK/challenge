package com.db.awmd.challenge.exception;

public class SameAccountTransferException extends RuntimeException{

    public SameAccountTransferException () {
        super("Sender and destinations accounts are the same");
    }
}
