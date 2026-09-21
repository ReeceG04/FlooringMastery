package org.example.models;

import java.math.BigDecimal;
import java.util.Objects;

public class Tax {

    //Declaring Tax Variables
    private String stateAbbreviation;
    private String stateName;
    private BigDecimal taxRate;

    //Tax Constructors
    public Tax(){}

    public Tax(String stateAbbreviation, String stateName, BigDecimal taxRate) {
        this.stateAbbreviation = stateAbbreviation;
        this.stateName = stateName;
        this.taxRate = taxRate;
    }

    //Getters and Setters
    public String getStateAbbreviation() {
        return stateAbbreviation;
    }

    public void setStateAbbreviation(String stateAbbreviation) {
        this.stateAbbreviation = stateAbbreviation;
    }

    public String getStateName() {
        return stateName;
    }

    public void setStateName(String stateName) {
        this.stateName = stateName;
    }

    public BigDecimal getTaxRate() {
        return taxRate;
    }

    public void setTaxRate(BigDecimal taxRate) {
        this.taxRate = taxRate;
    }

    //Equals to check whether two variables point to the same object by using state abr
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Tax)) return false;
        Tax tax = (Tax) o;
        return Objects.equals(stateAbbreviation, tax.stateAbbreviation);
    }

    //Checks if objects match the same hashcode
    @Override
    public int hashCode() {
        return Objects.hash(stateAbbreviation);
    }

    //Prints Tax Object
    @Override
    public String toString() {
        return "Tax{" +
                "stateAbbreviation='" + stateAbbreviation + '\'' +
                ", stateName='" + stateName + '\'' +
                ", taxRate=" + taxRate +
                '}';
    }
}
