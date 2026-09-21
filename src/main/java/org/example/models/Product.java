package org.example.models;

import java.math.BigDecimal;
import java.util.Objects;

public class Product {

    //Declaring Variables
    private String productType;
    private BigDecimal costPerSquareFoot;
    private BigDecimal labourCostPerSquareFoot;

    //Defining Constructors
    public Product(){}

    public Product(String productType, BigDecimal costPerSquareFoot, BigDecimal labourCostPerSquareFoot){
        this.productType = productType;
        this.costPerSquareFoot = costPerSquareFoot;
        this.labourCostPerSquareFoot = labourCostPerSquareFoot;
    }

    //Getters and Setters
    public String getProductType(){
        return productType;
    }

    public void setProductType(String productType){
        this.productType = productType;
    }

    public BigDecimal getCostPerSquareFoot(){
        return costPerSquareFoot;
    }

    public void setCostPerSquareFoot(BigDecimal costPerSquareFoot){
        this.costPerSquareFoot = costPerSquareFoot;
    }

    public BigDecimal getLabourCostPerSquareFoot() {
        return labourCostPerSquareFoot;
    }

    public void setLabourCostPerSquareFoot(BigDecimal labourCostPerSquareFoot) {
        this.labourCostPerSquareFoot = labourCostPerSquareFoot;
    }

    //Equals to check whether two variables point to the same object by using product type
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Product)) return false;
        Product product = (Product) o;
        return Objects.equals(productType, product.productType);
    }

    //Checks if objects match the same hashcode
    @Override
    public int hashCode() {
        return Objects.hash(productType);
    }

    @Override
    public String toString(){
        return "Product{" + "Product Type= '" + productType + '\'' +
                "Cost Per Square Foot= '" + costPerSquareFoot + '\'' +
                "Labour Cost Per Square Foot= '" + labourCostPerSquareFoot + '\'' +
                '}';
    }
}
