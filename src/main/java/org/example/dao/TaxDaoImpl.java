package org.example.dao;

import org.example.exceptions.FlooringMasteryPersistanceException;
import org.example.models.Tax;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TaxDaoImpl implements TaxDao {

    //Declaring final variables
    private static final String TAXES_FILE = "Data" + File.separator + "Taxes.txt";
    private static final String DELIMITER = "::";

    private final String taxesFilePath;

    //Ensures that data is loaded and adds it to a map
    private final Map<String, Tax> taxes = new HashMap<>();
    private boolean loaded = false;

    //Defining constructors so methods can be tested
    public TaxDaoImpl(){
        this(TAXES_FILE);
    }

    public TaxDaoImpl(String taxesFilePath){
        this.taxesFilePath = taxesFilePath;
    }

    //Returns the tax object based on its state abr
    @Override
    public Tax getTax(String stateAbbreviation) throws FlooringMasteryPersistanceException {
        ensureLoaded();
        return taxes.get(stateAbbreviation);
    }

    //Returns all the tax objects in the txt file
    @Override
    public List<Tax> getAllTaxes() throws FlooringMasteryPersistanceException {
        ensureLoaded();
        return new ArrayList<>(taxes.values());
    }

    //Again checking that data is correctly being read
    private void ensureLoaded() throws FlooringMasteryPersistanceException {
        if (!loaded) {
            loadTaxes();
            loaded = true;
        }
    }

    //Read taxes from .txt file
    private void loadTaxes() throws FlooringMasteryPersistanceException {
        taxes.clear();
        try (BufferedReader reader = new BufferedReader(new FileReader(taxesFilePath))) {
            reader.readLine(); // header row — discard
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.trim().isEmpty()) {
                    continue;
                }
                Tax tax = unmarshallTax(line);
                taxes.put(tax.getStateAbbreviation(), tax);
            }
        } catch (IOException e) {
            throw new FlooringMasteryPersistanceException("Could not load tax data.", e);
        }
    }

    //Splits the .txt file using :: delimiter
    private Tax unmarshallTax(String line) {
        String[] tokens = line.split(DELIMITER);
        return new Tax(tokens[0], tokens[1], new BigDecimal(tokens[2]));
    }
}
