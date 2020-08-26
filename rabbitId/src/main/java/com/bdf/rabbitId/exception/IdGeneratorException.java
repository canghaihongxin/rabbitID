package com.bdf.rabbitId.exception;

/**
 * author: 田培融
 */
public class IdGeneratorException extends Exception {
    private String message;

    public IdGeneratorException() {
        super();
    }

    public IdGeneratorException(String message){
        super(message);
        this.message = message;
    }

    public IdGeneratorException(String message, Throwable cause) {
        super(message, cause);
    }

    @Override
    public String getMessage() {
        return message;
    }
}
