package org.example.view;

import org.example.models.Order;
import org.example.models.Product;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class FlooringMasteryView {

    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("MM/dd/yyyy");

    //Establishes Constructors
    private final UserIO io;

    public FlooringMasteryView(UserIO io){
        this.io = io;
    }

    //Prints the 6 choices the user can select
    public int printMenuAndGetSelection(){
        io.print("FLOORING MASTERY PROGRAM");
        io.print("");
        io.print("1. Display Orders");
        io.print("2. Add an Order");
        io.print("3. Edit an Order");
        io.print("4. Remove an Order");
        io.print("5. Export All Data");
        io.print("6. Quit");

        int selection;
        while (true){
            //Allows the user to input a number and loops until it is in range
            selection = io.readInt("Please Select and Option from 1 - 6");
            if(selection >= 1 && selection <= 6){
                return selection;
            }
            io.print("Please Select and Option from 1 - 6");
        }
    }

    //Display Orders

    public LocalDate getDateToDisplay(){
        return io.readLocalDate("Enter a date to display orders (MM/dd/yyyy): ");
    }

    public void displayOrders(LocalDate date, List<Order> orders){
        io.print("");
        io.print("Orders for " + date.format(DATE_TIME_FORMATTER) + ":");
        if(orders.isEmpty()){
            displayNoOrdersForDateBanner(date);
            return;
        }
        for(Order order : orders){
            printOrder(order);
        }
    }

    //Add an Order

    public LocalDate getNewOrderDate() {
        return io.readLocalDate("Enter order date (MM/dd/yyyy, must be a future date): ");
    }

    public String getNewOrderCustomerName() {
        return io.readString("Enter customer name: ");
    }

    public String getNewOrderState() {
        return io.readString("Enter state abbreviation: ");
    }

    public void displayProductList(List<Product> products) {
        io.print("");
        io.print("Available products:");
        io.print(String.format("%-15s %-12s %-20s", "Product", "Cost/sqft", "Labour Cost/sqft"));
        for (Product product : products) {
            io.print(String.format("%-15s $%-11s $%-19s",
                    product.getProductType(),
                    product.getCostPerSquareFoot(),
                    product.getLabourCostPerSquareFoot()));
        }
    }

    public String getNewOrderProductType() {
        return io.readString("Enter product type: ");
    }

    public BigDecimal getNewOrderArea() {
        return io.readBigDecimal("Enter area in square feet (minimum 100): ");
    }

    //Shows the summary of the add methods and prompts the user if they are happy
    //to save the order
    //No calculations or saves are done here
    public boolean confirmPlaceOrder(Order order) {
        io.print("");
        io.print("Order Summary:");
        printOrder(order);
        String response = io.readString("Place this order? (Y/N): ");
        return response.trim().equalsIgnoreCase("Y");
    }

    //Edit an Order

    public LocalDate getEditOrderDate() {
        return io.readLocalDate("Enter the date of the order to edit (MM/dd/yyyy): ");
    }

    public int getEditOrderNumber() {
        return io.readInt("Enter the order number to edit: ");
    }

    //These four edit methods all follow the same procedure
    //An empty respone returns null to keep the value the same
    //Any changes are sent to the service layer to conduct validation
    public String getEditedCustomerName(String currentValue) {
        String input = io.readString("Enter customer name (" + currentValue + "): ");
        return input.isBlank() ? null : input;
    }

    public String getEditedState(String currentValue) {
        String input = io.readString("Enter state (" + currentValue + "): ");
        return input.isBlank() ? null : input;
    }

    public String getEditedProductType(String currentValue) {
        String input = io.readString("Enter product type (" + currentValue + "): ");
        return input.isBlank() ? null : input;
    }

    //Can't delegate to io.readBigDecimal here the way getNewOrderArea()
    //As it cannot handle entering ""
    //So it implements a small parse loop to handle the ""
    public BigDecimal getEditedArea(String currentValue) {
        String input = io.readString("Enter area (" + currentValue + "): ");
        if (input.isBlank()) {
            return null;
        }
        while (true) {
            try {
                return new BigDecimal(input.trim());
            } catch (NumberFormatException e) {
                io.print("Please enter a valid number, or press Enter to keep the current value.");
                input = io.readString("Enter area (" + currentValue + "): ");
                if (input.isBlank()) {
                    return null;
                }
            }
        }
    }

    //This method only displays and askeds it does not save
    //It also prompts the user for a response
    public boolean confirmSaveEdit(Order updatedOrder) {
        io.print("");
        io.print("Updated Order Summary:");
        printOrder(updatedOrder);
        String response = io.readString("Save these changes? (Y/N): ");
        return response.trim().equalsIgnoreCase("Y");
    }

    //Remove an Order

    public LocalDate getRemoveOrderDate(){
        return io.readLocalDate("Enter the date of the order to remove: ");
    }

    public int getRemoveOrderNumber() {
        return io.readInt("Enter the order number to remove: ");
    }

    public boolean confirmRemoval(Order order) {
        io.print("");
        io.print("Order to remove:");
        printOrder(order);
        String response = io.readString("Are you sure you want to remove this order? (Y/N): ");
        return response.trim().equalsIgnoreCase("Y");
    }

    //Exporting Orders to file

    public void displayExportSuccessBanner() {
        io.print("All data was successfully exported to Backup/DataExport.txt.");
    }

    //Helpers

    public void displayOrderNotFoundBanner() {
        io.print("No order was found matching that date and order number.");
    }

    public void displayNoOrdersForDateBanner(LocalDate date) {
        io.print("No orders were found for " + date.format(DATE_TIME_FORMATTER) + ".");
    }

    public void displayErrorMessage(String message) {
        io.print("ERROR: " + message);
    }

    public void displayUnknownCommandBanner() {
        io.print("Unknown command.");
    }

    public void displayExitBanner() {
        io.print("Goodbye!");
    }

    //Private method that is used to display orders in the valid format
    //Allows the users to see a particular order
    private void printOrder(Order order) {
        io.print("-----------------------------------------------");
        io.print("Order #" + order.getOrderNumber());
        io.print("Customer Name:      " + order.getCustomerName());
        io.print("State:              " + order.getState());
        io.print("Tax Rate:           " + order.getTaxRate() + "%");
        io.print("Product Type:       " + order.getProductType());
        io.print("Area:               " + order.getArea() + " sq ft");
        io.print("Cost/sqft:          $" + order.getCostPerSquareFoot());
        io.print("Labour Cost/sqft:    $" + order.getLabourCostPerSquareFoot());
        io.print("Material Cost:      $" + order.getMaterialCost());
        io.print("Labour Cost:         $" + order.getLabourCost());
        io.print("Tax:                $" + order.getTax());
        io.print("Total:              $" + order.getTotal());
        io.print("-----------------------------------------------");
    }
}
