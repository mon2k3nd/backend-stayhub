package com.stayhub.api.exception;

public class DuplicateRoomException extends RuntimeException {

    public DuplicateRoomException(String message) {
        super(message);
    }
}