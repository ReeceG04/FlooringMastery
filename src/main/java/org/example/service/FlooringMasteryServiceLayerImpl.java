package org.example.service;

import org.example.dao.OrderDao;
import org.example.dao.ProductDao;
import org.example.dao.TaxDao;
import org.example.exceptions.FlooringMasterDataValidationException;
import org.example.exceptions.FlooringMasteryPersistanceException;
import org.example.models.Order;
import org.example.models.Product;
import org.example.models.Tax;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;
import java.util.regex.Pattern;

public class FlooringMasteryServiceLayerImpl implements FlooringMasteryServiceLayer {

    private static final BigDecimal MINIMUM_AREA = new BigDecimal("100");
    //Establishes valid characters
    private static final Pattern VALID_NAME_PATTERN = Pattern.compile("^[a-zA-Z0-9.,\\s]+$");

    //Depends on a variety of DAO interfaces
    private final OrderDao orderDao;
    private final ProductDao productDao;
    private final TaxDao taxDao;

    //Constructor
    public FlooringMasteryServiceLayerImpl(OrderDao orderDao, ProductDao productDao, TaxDao taxDao){
        this.orderDao = orderDao;
        this.productDao = productDao;
        this.taxDao = taxDao;
    }

    @Override
    public List<Order> getOrders(LocalDate date) throws FlooringMasteryPersistanceException {
        return orderDao.getAllOrders(date);
    }

    @Override
    public Order getOrder(LocalDate date, int orderNumber) throws FlooringMasteryPersistanceException {
        return orderDao.getOrder(date, orderNumber);
    }

    @Override
    public List<Product> getAllProducts() throws FlooringMasteryPersistanceException {
        return productDao.getAllProducts();
    }

    //Validates and makes a new calculation if necessary
    //Works on a previous build order and never saves in this method
    //returns the preview so the user can see if everything is correct
    @Override
    public Order buildEditPreview(LocalDate date, int orderNumber, String newCustomerName,
                                  String newState, String newProductType, BigDecimal newArea)
            throws FlooringMasteryPersistanceException, FlooringMasterDataValidationException {

        Order existing = orderDao.getOrder(date, orderNumber);
        if (existing == null) {
            return null;
        }

        // Work on a copy, never mutate the object the DAO is still holding
        // as the "saved" version until the user actually confirms.
        Order preview = copyOrder(existing);

        boolean needsRecalculation = false;

        if (newCustomerName != null && !newCustomerName.isBlank()) {
            validateCustomerName(newCustomerName);
            preview.setCustomerName(newCustomerName);
        }

        if (newState != null && !newState.isBlank()) {
            Tax tax = validateState(newState);
            preview.setState(tax.getStateAbbreviation());
            preview.setTaxRate(tax.getTaxRate());
            needsRecalculation = true;
        }

        if (newProductType != null && !newProductType.isBlank()) {
            Product product = validateProductType(newProductType);
            preview.setProductType(product.getProductType());
            preview.setCostPerSquareFoot(product.getCostPerSquareFoot());
            preview.setLabourCostPerSquareFoot(product.getLabourCostPerSquareFoot());
            needsRecalculation = true;
        }

        if (newArea != null) {
            validateArea(newArea);
            preview.setArea(newArea);
            needsRecalculation = true;
        }

        //Recalculates the entire order not just the altered field
        //As all the variables depend on each other
        if (needsRecalculation) {
            calculateOrderCosts(preview);
        }

        return preview;
    }

    //The ONLY method that actually saves the edited data
    //Called after the user confirms
    @Override
    public Order saveEditedOrder(LocalDate date, Order updatedOrder) throws FlooringMasteryPersistanceException {
        return orderDao.editOrder(date, updatedOrder);
    }

    //Validates every field, then builds and calculates a brand-new Order
    //But does not call .addOrder to avoid duplication
    @Override
    public Order buildAddPreview(LocalDate date, String customerName, String state,
                                 String productType, BigDecimal area)
            throws FlooringMasteryPersistanceException, FlooringMasterDataValidationException {

        validateOrderDate(date);
        validateCustomerName(customerName);
        Tax tax = validateState(state);
        Product product = validateProductType(productType);
        validateArea(area);

        Order preview = new Order();
        preview.setOrderDate(date);
        preview.setCustomerName(customerName);
        preview.setState(tax.getStateAbbreviation());
        preview.setTaxRate(tax.getTaxRate());
        preview.setProductType(product.getProductType());
        preview.setArea(area);
        preview.setCostPerSquareFoot(product.getCostPerSquareFoot());
        preview.setLabourCostPerSquareFoot(product.getLabourCostPerSquareFoot());
        calculateOrderCosts(preview);

        return preview;
    }

    //The ONLY method that saves a new order
    //Called when the user confirms
    @Override
    public Order saveNewOrder(LocalDate date, Order previewOrder) throws FlooringMasteryPersistanceException {
        return orderDao.addOrder(date, previewOrder);
    }

    @Override
    public Order removeOrder(LocalDate date, int orderNumber) throws FlooringMasteryPersistanceException {
        return orderDao.removeOrder(date, orderNumber);
    }

    @Override
    public void exportAllData() throws FlooringMasteryPersistanceException {
        orderDao.exportAllData();
    }

    // private methods

    //Field by field copy rather than relying on a default copy
    //To guarantee the returned order shares no state oth the one in the DAOs map
    private Order copyOrder(Order source) {
        Order copy = new Order();
        copy.setOrderNumber(source.getOrderNumber());
        copy.setOrderDate(source.getOrderDate());
        copy.setCustomerName(source.getCustomerName());
        copy.setState(source.getState());
        copy.setTaxRate(source.getTaxRate());
        copy.setProductType(source.getProductType());
        copy.setArea(source.getArea());
        copy.setCostPerSquareFoot(source.getCostPerSquareFoot());
        copy.setLabourCostPerSquareFoot(source.getLabourCostPerSquareFoot());
        copy.setMaterialCost(source.getMaterialCost());
        copy.setLabourCost(source.getLabourCost());
        copy.setTax(source.getTax());
        copy.setTotal(source.getTotal());
        return copy;
    }

    //Validation

    //Order date must be after today (Must be in the future by a day)
    private void validateOrderDate(LocalDate date) throws FlooringMasterDataValidationException{
        if(date == null || !date.isAfter(LocalDate.now())){
            throw new FlooringMasterDataValidationException(
                    "Order date must be in the future."
            );
        }
    }

    private void validateCustomerName(String name) throws FlooringMasterDataValidationException{
        if(name == null || name.isBlank()){
            throw new FlooringMasterDataValidationException(
                    "Customer name must not be blank. "
            );
        }
        if(!VALID_NAME_PATTERN.matcher(name).matches()){
            throw new FlooringMasterDataValidationException(
                    "Customer name has to be inside naming parameters (letters, numbers, periods and commas."
            );
        }
    }

    //Returns the matched Tax object so the caller gets the real tax rate
    //without having to look it up a second time.
    //Validating and fetching in one call
    private Tax validateState(String state) throws FlooringMasterDataValidationException, FlooringMasteryPersistanceException{
        if(state == null || state.isBlank()){
            throw new FlooringMasterDataValidationException("State must not be blank.");
        }
        Tax tax = taxDao.getTax(state.toUpperCase());
        if(tax == null){
            throw new FlooringMasterDataValidationException(
                    "We do not sell in the state of " + state + "."
            );
        }
        return tax;
    }

    //Same pattern as validateState, returns the product so methods can use its cost fields
    private Product validateProductType(String productType) throws FlooringMasteryPersistanceException, FlooringMasterDataValidationException{
        if (productType == null || productType.isBlank()){
            throw new FlooringMasterDataValidationException("Product type cannot be blank");
            }
        Product product = productDao.getProduct(productType);
        if (product == null){
            throw new FlooringMasterDataValidationException(
                    "\"" + productType + "\" is not a valid product type.");
        }
        return product;
    }

    private void validateArea(BigDecimal area) throws FlooringMasterDataValidationException{
        if (area == null || area.compareTo(BigDecimal.ZERO) <= 0){
            throw new FlooringMasterDataValidationException("Area must be a positive number.");
        }
        if (area.compareTo(MINIMUM_AREA) < 0) {
            throw new FlooringMasterDataValidationException(
                    "Minimum order size is 100 sq ft.");
        }
    }

    //Where thr calculations are made
    //setScale(2, HALF_UP) after every step as BigDecimal doesn't round on its own
    private void calculateOrderCosts(Order order){
        BigDecimal materialCost = order.getArea()
                .multiply(order.getCostPerSquareFoot())
                .setScale(2, RoundingMode.HALF_UP);

        BigDecimal labourCost = order.getArea()
                .multiply(order.getLabourCostPerSquareFoot())
                .setScale(2, RoundingMode.HALF_UP);

        BigDecimal tax = materialCost.add(labourCost)
                .multiply(order.getTaxRate())
                .divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);

        BigDecimal total = materialCost.add(labourCost).add(tax)
                .setScale(2, RoundingMode.HALF_UP);

        order.setMaterialCost(materialCost);
        order.setLabourCost(labourCost);
        order.setTax(tax);
        order.setTotal(total);
    }
}
