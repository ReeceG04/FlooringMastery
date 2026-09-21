import org.example.dao.TaxDao;
import org.example.dao.TaxDaoImpl;
import org.example.exceptions.FlooringMasteryPersistanceException;
import org.example.models.Tax;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class TaxDaoImplTest {

    private TaxDao taxDao;

    //Creates a fake tax.txt file that each test will use
    @BeforeEach
    void setUp(@TempDir Path tempDir) throws IOException {
        Path taxesFile = tempDir.resolve("Taxes.txt");
        try (PrintWriter writer = new PrintWriter(taxesFile.toFile())) {
            writer.println("StateAbbreviation::StateName::TaxRate");
            writer.println("TX::Texas::4.45");
            writer.println("CA::California::25.00");
        }
        taxDao = new TaxDaoImpl(taxesFile.toString());
    }

    @Test
    void getAllTaxes_returnsEveryStateFromFile() throws FlooringMasteryPersistanceException {
        List<Tax> taxes = taxDao.getAllTaxes();

        assertEquals(2, taxes.size());
    }

    @Test
    void getTax_returnsCorrectTax_whenStateExists() throws FlooringMasteryPersistanceException {
        Tax tx = taxDao.getTax("TX");

        assertNotNull(tx);
        assertEquals("Texas", tx.getStateName());
        assertEquals(new BigDecimal("4.45"), tx.getTaxRate());
    }

    //Test to ensure that null is being return when an invalid state abr is input
    @Test
    void getTax_returnsNull_whenStateDoesNotExist() throws FlooringMasteryPersistanceException {
        Tax result = taxDao.getTax("ZZ");

        assertNull(result);
    }
}
