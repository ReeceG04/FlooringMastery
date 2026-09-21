import org.example.dao.ProductDao;
import org.example.dao.ProductDaoImpl;
import org.example.exceptions.FlooringMasteryPersistanceException;
import org.example.models.Product;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ProductDaoImplTest {

    //Fresh DAO instance per test
    private ProductDao productDao;

    //Creates a fake products.txt file that each test will use
    @BeforeEach
    void setUp(@TempDir Path tempDir) throws IOException {
        Path productsFile = tempDir.resolve("Products.txt");
        try (PrintWriter writer = new PrintWriter(productsFile.toFile())) {
            writer.println("ProductType::CostPerSquareFoot::LaborCostPerSquareFoot");
            writer.println("Tile::3.50::4.15");
            writer.println("Wood::5.15::4.75");
        }
        productDao = new ProductDaoImpl(productsFile.toString());
    }

    @Test
    void getAllProducts_returnsEveryProductFromFile() throws FlooringMasteryPersistanceException {
        List<Product> products = productDao.getAllProducts();

        assertEquals(2, products.size());
    }

    @Test
    void getProduct_returnsCorrectProduct_whenProductTypeExists() throws FlooringMasteryPersistanceException {
        Product tile = productDao.getProduct("Tile");

        assertNotNull(tile);
        assertEquals("Tile", tile.getProductType());
        assertEquals(new BigDecimal("3.50"), tile.getCostPerSquareFoot());
        assertEquals(new BigDecimal("4.15"), tile.getLabourCostPerSquareFoot());
    }

    //Confirms the DAOs not found catch returns null
    //as the service layers validateProductType relies on that behaviour to detect invalid
    @Test
    void getProduct_returnsNull_whenProductTypeDoesNotExist() throws FlooringMasteryPersistanceException {
        Product result = productDao.getProduct("Marble");

        assertNull(result);
    }

    @Test
    void getAllProducts_onlyReadsFileOnce_evenAcrossMultipleCalls() throws FlooringMasteryPersistanceException {
        // Calling twice shouldn't throw or behave differently, this indirectly
        // confirms the ensureLoaded() doesn't break repeat calls.
        List<Product> firstCall = productDao.getAllProducts();
        List<Product> secondCall = productDao.getAllProducts();

        assertEquals(firstCall.size(), secondCall.size());
    }
}