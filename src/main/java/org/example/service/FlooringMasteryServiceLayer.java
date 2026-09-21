package org.example.service;

import org.example.exceptions.FlooringMasterDataValidationException;
import org.example.exceptions.FlooringMasteryPersistanceException;
import org.example.models.Order;
import org.example.models.Product;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

//Establishes every business rule in the application
//Validation and cost calculation so the controller never had to enforce any of it itself
//Add and Edit are split into different methods so the controller can show a summary before writing anything
public interface FlooringMasteryServiceLayer {

    List<Order> getOrders(LocalDate date) throws FlooringMasteryPersistanceException;

    Order getOrder(LocalDate date, int orderNumber) throws FlooringMasteryPersistanceException;

    List<Product> getAllProducts() throws FlooringMasteryPersistanceException;

    //Validates everyfield and calculates costs/tax/total, but does not persist anything
    Order buildAddPreview(LocalDate date, String customerName, String state,
                          String productType, BigDecimal area)
            throws FlooringMasteryPersistanceException, FlooringMasterDataValidationException;

    //Persists a previous order, to be called after the user has confirmed
    Order saveNewOrder(LocalDate date, Order previewOrder) throws FlooringMasteryPersistanceException;

    //Builds and updated version without saving it
    //Returns null if no order exists for that date
    Order buildEditPreview(LocalDate date, int orderNumber, String newCustomerName,
                           String newState, String newProductType, BigDecimal newArea)
            throws FlooringMasteryPersistanceException, FlooringMasterDataValidationException;

    //Only called when the user wants to save and write to a file
    Order saveEditedOrder(LocalDate date, Order updatedOrder) throws FlooringMasteryPersistanceException;

    Order removeOrder(LocalDate date, int orderNumber) throws FlooringMasteryPersistanceException;

    void exportAllData() throws FlooringMasteryPersistanceException;
}
