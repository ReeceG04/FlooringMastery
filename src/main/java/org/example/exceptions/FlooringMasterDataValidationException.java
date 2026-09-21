package org.example.exceptions;

public class FlooringMasterDataValidationException extends RuntimeException {

    public FlooringMasterDataValidationException(String message) {
        super(message);
    }

    public FlooringMasterDataValidationException(String message, Throwable cause){
        super(message, cause);
    }
}
