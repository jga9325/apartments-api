package com.auger.apartments.exceptions;

public class ApplicationNotFoundException extends RuntimeException {

    public ApplicationNotFoundException(String message) {
        super(message);
    }
}
