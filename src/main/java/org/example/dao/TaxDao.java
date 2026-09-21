package org.example.dao;

import org.example.exceptions.FlooringMasteryPersistanceException;
import org.example.models.Tax;

import java.util.List;

public interface TaxDao {

    //Interface that establishes how data will be used when accessing the Tax Object

    //Returns the matching state abr entry or throws exception if not found
    Tax getTax(String stateAbbreviation) throws FlooringMasteryPersistanceException;

    List<Tax> getAllTaxes() throws FlooringMasteryPersistanceException;
}
