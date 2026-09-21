package org.example.exceptions;

public class FlooringMasteryPersistanceException extends Exception{

    public FlooringMasteryPersistanceException(String message){
        super(message);
    }

    public FlooringMasteryPersistanceException(String message, Throwable cause){
        super(message, cause);
    }
}
