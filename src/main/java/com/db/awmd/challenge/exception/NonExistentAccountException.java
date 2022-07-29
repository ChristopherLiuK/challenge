package com.db.awmd.challenge.exception;

public class NonExistentAccountException extends RuntimeException{

    public NonExistentAccountException() {
        super("One or both accounts entered don't exist");
    }
}
