package com.auger.apartments.exceptions;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class GlobalControllerExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(GlobalControllerExceptionHandler.class);

    @ExceptionHandler(DuplicateDataException.class)
    public ResponseEntity<String> handleDuplicateDataException(DuplicateDataException ex) {
        String line1 = "DuplicateDataException occurred while attempting to create or update an object.";
        String line2 = "The object passed in has at least one field which does not meet the unique constraint.";
        logger.error("{} {}", line1, line2, ex);
        return new ResponseEntity<>(ex.getMessage(), HttpStatus.CONFLICT);
    }

    @ExceptionHandler(DatabaseException.class)
    public ResponseEntity<String> handleDatabaseException(DatabaseException ex) {
        logger.error("DatabaseException occurred while attempting to complete a database operation.", ex);
        return new ResponseEntity<>(ex.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<String> handleUserNotFoundException(UserNotFoundException ex) {
        logger.error("UserNotFoundException occurred. An invalid user id was provided.", ex);
        return new ResponseEntity<>(ex.getMessage(), HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(ApartmentNotFoundException.class)
    public ResponseEntity<String> handleApartmentNotFoundException(ApartmentNotFoundException ex) {
        logger.error("ApartmentNotFoundException occurred. An invalid apartment id was provided.", ex);
        return new ResponseEntity<>(ex.getMessage(), HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(ApplicationNotFoundException.class)
    public ResponseEntity<String> handleApplicationNotFoundException(ApplicationNotFoundException ex) {
        logger.error("ApplicationNotFoundException occurred. An invalid application id was provided.", ex);
        return new ResponseEntity<>(ex.getMessage(), HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(DeleteApartmentException.class)
    public ResponseEntity<String> handleDeleteApartmentException(DeleteApartmentException ex) {
        String line1 = "DeleteApartmentException occurred while attempting to delete an apartment.";
        String line2 = "The specified apartment does not meet the requirements for deletion.";
        logger.error("{} {}", line1, line2, ex);
        return new ResponseEntity<>(ex.getMessage(), HttpStatus.CONFLICT);
    }

    @ExceptionHandler(DeleteUserException.class)
    public ResponseEntity<String> handleDeleteUserException(DeleteUserException ex) {
        String line1 = "DeleteUserException occurred while attempting to delete a user.";
        String line2 = "The specified user does not meet the requirements for deletion.";
        logger.error("{} {}", line1, line2, ex);
        return new ResponseEntity<>(ex.getMessage(), HttpStatus.CONFLICT);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<String> handleHttpMessageNotReadableException(HttpMessageNotReadableException ex) {
        String[] messageParts = ex.getMessage().split(":");
        String responseMessage = messageParts[messageParts.length-1].strip();
        return new ResponseEntity<>(responseMessage, HttpStatus.BAD_REQUEST);
    }
}
