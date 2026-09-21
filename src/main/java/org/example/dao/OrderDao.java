package org.example.dao;

import org.example.exceptions.FlooringMasteryPersistanceException;
import org.example.models.Order;

import java.time.LocalDate;
import java.util.List;

public interface OrderDao {


    //Allows user to add an order
    Order addOrder(LocalDate date, Order o) throws FlooringMasteryPersistanceException;

    //Returns the order based on date and order number
    Order getOrder(LocalDate date, int orderNumber) throws FlooringMasteryPersistanceException;

    //Returns all orders for a certain data
    List<Order> getAllOrders(LocalDate date) throws FlooringMasteryPersistanceException;

    //Returns the highest order number available
    int getNextOrder() throws FlooringMasteryPersistanceException;

    //Allows a user to edit an order
    Order editOrder(LocalDate date, Order o) throws FlooringMasteryPersistanceException;

    //Allows a user to delete an order
    Order removeOrder(LocalDate date, int orderNumber) throws FlooringMasteryPersistanceException;

    //Allows user to export all data outwidth the application
    void exportAllData() throws FlooringMasteryPersistanceException;


}
