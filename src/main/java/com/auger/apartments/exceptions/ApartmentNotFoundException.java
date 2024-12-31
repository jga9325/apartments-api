package com.auger.apartments.exceptions;

public class ApartmentNotFoundException extends RuntimeException {

    public ApartmentNotFoundException(String message) {
        super(message);
    }
}
