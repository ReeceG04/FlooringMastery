package org.example.dao;

import org.example.exceptions.FlooringMasteryPersistanceException;
import org.example.models.Product;

import java.util.List;

public interface ProductDao {

    //Interface that establishes that data will be read from the Products.txt

    //Returns a product entry based on its product type
    Product getProduct (String productType) throws FlooringMasteryPersistanceException;

    List<Product> getAllProducts() throws FlooringMasteryPersistanceException;
}
