package com.db.awmd.challenge.exception;

public class InvalidAmountException extends  RuntimeException{

    public InvalidAmountException() {
        super("Amount must be greater than 0");
    }
}
