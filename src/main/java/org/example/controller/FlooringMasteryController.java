package org.example.controller;

import org.example.service.FlooringMasteryServiceLayer;
import org.example.exceptions.FlooringMasterDataValidationException;
import org.example.exceptions.FlooringMasteryPersistanceException;
import org.example.models.Order;
import org.example.view.FlooringMasteryView;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public class FlooringMasteryController {

    //Depends on the service layer and view
    //Never depends on a DAO or impl file

    private final FlooringMasteryView view;
    private final FlooringMasteryServiceLayer service;

    //Constructor
    public FlooringMasteryController(FlooringMasteryView view, FlooringMasteryServiceLayer service){
        this.view = view;
        this.service = service;
    }

    //Menu Loop
    //Calls a private method depending on user selection and runs until Quit is selected
    public void run(){

        boolean keepGoing = true;

        while(keepGoing){
            int selection = view.printMenuAndGetSelection();

            switch (selection) {
                case 1:
                    displayOrders();
                    break;
                case 2:
                    addOrder();
                    break;
                case 3:
                    editOrder();
                    break;
                case 4:
                    removeOrder();
                    break;
                case 5:
                    exportAllData();
                    break;
                case 6:
                    keepGoing = false;
                    view.displayExitBanner();
                    break;
                default:
                    view.displayUnknownCommandBanner();
            }
        }
    }

    //Display Orders

    private void displayOrders(){
        try {
            LocalDate date = view.getDateToDisplay();
            List<Order> orders = service.getOrders(date);
            view.displayOrders(date, orders);
        } catch (FlooringMasteryPersistanceException e) {
            view.displayErrorMessage(e.getMessage());
        }
    }

    //Add Order

    //Loops until an order is placed
    //Validation errors are present to ensure the correct data is being inserted into the method
    private void addOrder() {
        boolean orderPlaced = false;

        while (!orderPlaced) {
            LocalDate date = view.getNewOrderDate();
            String customerName = view.getNewOrderCustomerName();
            String state = view.getNewOrderState();

            try {
                view.displayProductList(service.getAllProducts());
            } catch (FlooringMasteryPersistanceException e) {
                view.displayErrorMessage(e.getMessage());
                return;
            }

            String productType = view.getNewOrderProductType();
            BigDecimal area = view.getNewOrderArea();

            //buildAddPreview validates and calculates
            Order preview;
            try {
                preview = service.buildAddPreview(date, customerName, state, productType, area);
            } catch (FlooringMasterDataValidationException e) {
                view.displayErrorMessage(e.getMessage());
                continue; // loop again — re-collect the whole order
            } catch (FlooringMasteryPersistanceException e) {
                view.displayErrorMessage(e.getMessage());
                return;
            }

            try {
                if (view.confirmPlaceOrder(preview)) {
                    //The only point in the method where anything is actually written
                    service.saveNewOrder(date, preview);
                    orderPlaced = true;
                } else {
                    return;
                }
            } catch (FlooringMasteryPersistanceException e) {
                view.displayErrorMessage(e.getMessage());
                return;
            }
        }
    }

    //Edit Order
    //Same setup as addOrder where a preview is build with validations and calculations
    private void editOrder() {
        try {
            LocalDate date = view.getEditOrderDate();
            int orderNumber = view.getEditOrderNumber();

            Order existing = service.getOrder(date, orderNumber);
            if (existing == null) {
                view.displayOrderNotFoundBanner();
                return;
            }

            //Each edit call retuns null if the user leaves it blank which is treated to keep the value the same
            String newCustomerName = view.getEditedCustomerName(existing.getCustomerName());
            String newState = view.getEditedState(existing.getState());
            String newProductType = view.getEditedProductType(existing.getProductType());
            BigDecimal newArea = view.getEditedArea(existing.getArea().toString());

            Order preview;
            try {
                preview = service.buildEditPreview(date, orderNumber, newCustomerName,
                        newState, newProductType, newArea);
            } catch (FlooringMasterDataValidationException e) {
                view.displayErrorMessage(e.getMessage());
                return;
            }

            //The only point in the method where anything is written
            if (view.confirmSaveEdit(preview)) {
                service.saveEditedOrder(date, preview);
            }

        } catch (FlooringMasteryPersistanceException e) {
            view.displayErrorMessage(e.getMessage());
        }
    }

    //Remove Order

    //Asks the user for orderdate and number
    //if the record exists they are asked for final confirmation
    private void removeOrder(){
        try{
            LocalDate date = view.getRemoveOrderDate();
            int orderNumber = view.getRemoveOrderNumber();

            Order existing = service.getOrder(date, orderNumber);
            if(existing == null){
                view.displayOrderNotFoundBanner();
                return;
            }

            //This is where data can be removed from the file
            if(view.confirmRemoval(existing)){
                service.removeOrder(date, orderNumber);
            }
        } catch (FlooringMasteryPersistanceException e){
            view.displayErrorMessage(e.getMessage());
        }
    }

    //Export

    private void exportAllData(){
        try{
            service.exportAllData();
            view.displayExportSuccessBanner();
        } catch (FlooringMasteryPersistanceException e){
            view.displayErrorMessage(e.getMessage());
        }
    }

}
