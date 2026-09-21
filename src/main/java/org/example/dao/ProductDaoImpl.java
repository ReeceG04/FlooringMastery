package org.example.dao;

import org.example.exceptions.FlooringMasteryPersistanceException;
import org.example.models.Product;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ProductDaoImpl implements ProductDao {

    //Declaring Variables
    private static final String PRODUCTS_FILE = "Data" + File.separator + "Products.txt";
    private static final String DELIMITER = "::";

    //Variable for testing purposes
    private final String productsFilePath;

    private final Map<String, Product> products = new HashMap<>();
    private boolean loaded = false;

    //Testing Constructors
    public ProductDaoImpl(){
        this(PRODUCTS_FILE);
    }

    public ProductDaoImpl(String productsFilePath){
        this.productsFilePath = productsFilePath;
    }

    //Gets a project object based on its product type
    @Override
    public Product getProduct(String productType) throws FlooringMasteryPersistanceException {
        ensureLoaded();
        return products.get(productType);
    }

    //Lists all products
    @Override
    public List<Product> getAllProducts() throws FlooringMasteryPersistanceException {
        ensureLoaded();
        return new ArrayList<>(products.values());
    }

    //Checks that there is data to show to the user
    private void ensureLoaded() throws  FlooringMasteryPersistanceException{
        if(!loaded){
            loadProducts();
            loaded = true;
        }
    }

    //Reads Product objects from the .txt file
    private void loadProducts() throws FlooringMasteryPersistanceException{
        products.clear();
        try (BufferedReader reader = new BufferedReader(new FileReader(productsFilePath))){
            reader.readLine(); //Ignores headers
            String line;
            while ((line = reader.readLine()) != null){
                if (line.trim().isEmpty()){
                    continue;
                }
                Product product = unmarshallProduct(line);
                products.put(product.getProductType(), product);
            }
        }catch (IOException e){
            throw new FlooringMasteryPersistanceException("Could not load product types ", e);
        }
    }

    //Splits the .txt file using :: delimiter
    private Product unmarshallProduct(String line) {
        String[] tokens = line.split(DELIMITER);
        return new Product(tokens[0], new BigDecimal(tokens[1]), new BigDecimal(tokens[2]));
    }
}
