package com.tankbattle.config;

public class LevelLoadException extends RuntimeException {
    public enum ErrorType { MISSING_FILE, INVALID_FORMAT }

    private final ErrorType errorType;

    public LevelLoadException(ErrorType errorType, String message) {
        super(message);
        this.errorType = errorType;
    }

    public ErrorType getErrorType() { return errorType; }
}