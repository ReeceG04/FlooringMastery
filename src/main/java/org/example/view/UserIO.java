package org.example.view;

import java.math.BigDecimal;
import java.time.LocalDate;
//Interface that establishes the methods that allows a user to interact with the system
public interface UserIO {

    void print(String message);

    String readString(String prompt);

    int readInt(String prompt);

    BigDecimal readBigDecimal(String prompt);

    LocalDate readLocalDate(String prompt);
}
