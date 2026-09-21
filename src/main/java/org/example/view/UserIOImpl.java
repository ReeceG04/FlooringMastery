package org.example.view;

import java.math.BigDecimal;
import java.time.DateTimeException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Scanner;
import java.time.format.DateTimeParseException;

public class UserIOImpl implements UserIO{

    //Formatting the date into the right formation for file naming
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("MM/dd/yyyy");

    //Creating a scanner to be used throughout the application
    private final Scanner scanner = new Scanner(System.in);

    @Override
    public void print(String message) {
        System.out.println(message);
    }


    @Override
    public String readString(String prompt) {
        //Prompts the user to input a value based on the "prompt" object
        System.out.println(prompt);
        return scanner.nextLine();
    }

    @Override
    public int readInt(String prompt) {
        //Loops until the input meets the validation as an Integer
        while (true){
            String input = readString(prompt);
            try {
                return Integer.parseInt(input.trim());
            } catch (NumberFormatException e){
                print("Please enter a valid whole number.");
            }
        }
    }

    @Override
    public BigDecimal readBigDecimal(String prompt) {
        //Same as read int
        while (true){
            String input = readString(prompt);
            try {
                return new BigDecimal(input.trim());
            } catch (NumberFormatException e){
                print("Please enter a valid number.");
            }
        }
    }

    @Override
    public LocalDate readLocalDate(String prompt) {
        //Same as the other methods
        //Checks if the date is within the valid parameters
        //Is caught by an exception if date does not align with the requested format
        while (true){
            String input = readString(prompt);
            try {
                return LocalDate.parse(input.trim(), DATE_TIME_FORMATTER);
            } catch (DateTimeParseException e){
                print("Please enter a valid date in MM/dd/yyyy formatting.");
            }
        }
    }
}
