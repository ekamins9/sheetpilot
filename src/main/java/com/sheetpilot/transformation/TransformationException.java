package com.sheetpilot.transformation;

/**
 * Exception thrown when transformation operations fail
 */
public class TransformationException extends Exception {

    public TransformationException(String message) {
        super(message);
    }

    public TransformationException(String message, Throwable cause) {
        super(message, cause);
    }
}
