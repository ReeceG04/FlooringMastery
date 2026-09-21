import org.example.dao.OrderDao;
import org.example.dao.ProductDao;
import org.example.dao.TaxDao;
import org.example.exceptions.FlooringMasterDataValidationException;
import org.example.exceptions.FlooringMasteryPersistanceException;
import org.example.models.Order;
import org.example.models.Product;
import org.example.models.Tax;
import org.example.service.FlooringMasteryServiceLayer;
import org.example.service.FlooringMasteryServiceLayerImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

//A unit test of only the service layer logic
@ExtendWith(MockitoExtension.class)
class FlooringMasteryServiceLayerImplTest {

    //Mocks needed as each DAO is present in the class
    @Mock
    private OrderDao orderDao;

    @Mock
    private ProductDao productDao;

    @Mock
    private TaxDao taxDao;

    private FlooringMasteryServiceLayer service;

    @BeforeEach
    void setUp() {
        service = new FlooringMasteryServiceLayerImpl(orderDao, productDao, taxDao);
    }

    //validateOrderDate (buildAddPreview)
    //Must be in the future
    @Test
    void buildAddPreview_throwsValidationException_whenDateIsToday() {
        LocalDate today = LocalDate.now();

        assertThrows(FlooringMasterDataValidationException.class, () ->
                service.buildAddPreview(today, "Reece", "TX", "Wood", new BigDecimal("200")));
    }

    @Test
    void buildAddPreview_throwsValidationException_whenDateIsInThePast() {
        LocalDate yesterday = LocalDate.now().minusDays(1);

        assertThrows(FlooringMasterDataValidationException.class, () ->
                service.buildAddPreview(yesterday, "Reece", "TX", "Wood", new BigDecimal("200")));
    }

    // validateCustomerName

    @Test
    void buildAddPreview_throwsValidationException_whenCustomerNameIsBlank() {
        LocalDate futureDate = LocalDate.now().plusDays(10);

        assertThrows(FlooringMasterDataValidationException.class, () ->
                service.buildAddPreview(futureDate, "", "TX", "Wood", new BigDecimal("200")));
    }

    @Test
    void buildAddPreview_throwsValidationException_whenCustomerNameHasInvalidCharacters() {
        LocalDate futureDate = LocalDate.now().plusDays(10);

        assertThrows(FlooringMasterDataValidationException.class, () ->
                service.buildAddPreview(futureDate, "Reece@#$", "TX", "Wood", new BigDecimal("200")));
    }

    //Confirms that the application can handle companies with commas in their name
    @Test
    void buildAddPreview_allowsCustomerName_withPeriodsAndCommas() throws Exception {
        LocalDate futureDate = LocalDate.now().plusDays(10);
        Tax tax = new Tax("TX", "Texas", new BigDecimal("4.45"));
        Product product = new Product("Wood", new BigDecimal("5.15"), new BigDecimal("4.75"));

        when(taxDao.getTax("TX")).thenReturn(tax);
        when(productDao.getProduct("Wood")).thenReturn(product);

        Order preview = service.buildAddPreview(futureDate, "Acme, Inc.", "TX", "Wood", new BigDecimal("200"));

        assertEquals("Acme, Inc.", preview.getCustomerName());
    }

    // validateState

    @Test
    void buildAddPreview_throwsValidationException_whenStateNotFoundInTaxFile() throws Exception {
        LocalDate futureDate = LocalDate.now().plusDays(10);
        when(taxDao.getTax("ZZ")).thenReturn(null);

        assertThrows(FlooringMasterDataValidationException.class, () ->
                service.buildAddPreview(futureDate, "Reece", "ZZ", "Wood", new BigDecimal("200")));
    }

    // validateProductType

    @Test
    void buildAddPreview_throwsValidationException_whenProductTypeNotFound() throws Exception {
        LocalDate futureDate = LocalDate.now().plusDays(10);
        Tax tax = new Tax("TX", "Texas", new BigDecimal("4.45"));
        when(taxDao.getTax("TX")).thenReturn(tax);
        when(productDao.getProduct("Marble")).thenReturn(null);

        assertThrows(FlooringMasterDataValidationException.class, () ->
                service.buildAddPreview(futureDate, "Reece", "TX", "Marble", new BigDecimal("200")));
    }

    // validateArea

    @Test
    void buildAddPreview_throwsValidationException_whenAreaIsNegative() {
        LocalDate futureDate = LocalDate.now().plusDays(10);

        assertThrows(FlooringMasterDataValidationException.class, () ->
                service.buildAddPreview(futureDate, "Reece", "TX", "Wood", new BigDecimal("-50")));
    }

    @Test
    void buildAddPreview_throwsValidationException_whenAreaIsBelowMinimum() {
        LocalDate futureDate = LocalDate.now().plusDays(10);

        assertThrows(FlooringMasterDataValidationException.class, () ->
                service.buildAddPreview(futureDate, "Reece", "TX", "Wood", new BigDecimal("99")));
    }

    //Tests the boundries
    @Test
    void buildAddPreview_allowsArea_whenExactlyAtMinimum() throws Exception {
        LocalDate futureDate = LocalDate.now().plusDays(10);
        Tax tax = new Tax("TX", "Texas", new BigDecimal("4.45"));
        Product product = new Product("Wood", new BigDecimal("5.15"), new BigDecimal("4.75"));
        when(taxDao.getTax("TX")).thenReturn(tax);
        when(productDao.getProduct("Wood")).thenReturn(product);

        Order preview = service.buildAddPreview(futureDate, "Reece", "TX", "Wood", new BigDecimal("100"));

        assertEquals(new BigDecimal("100"), preview.getArea());
    }

    // calculateOrderCosts
    //Expected values are computed from the spec's formulas
    //To ensure that they are correct
    @Test
    void buildAddPreview_calculatesCostsCorrectly() throws Exception {
        LocalDate futureDate = LocalDate.now().plusDays(10);
        Tax tax = new Tax("TX", "Texas", new BigDecimal("4.45"));
        Product product = new Product("Wood", new BigDecimal("5.15"), new BigDecimal("4.75"));
        when(taxDao.getTax("TX")).thenReturn(tax);
        when(productDao.getProduct("Wood")).thenReturn(product);

        Order preview = service.buildAddPreview(futureDate, "Reece", "TX", "Wood", new BigDecimal("200"));

        // MaterialCost = 200 * 5.15 = 1030.00
        assertEquals(new BigDecimal("1030.00"), preview.getMaterialCost());
        // LabourCost = 200 * 4.75 = 950.00
        assertEquals(new BigDecimal("950.00"), preview.getLabourCost());
        // Tax = (1030.00 + 950.00) * (4.45 / 100) = 88.11 (rounded)
        assertEquals(new BigDecimal("88.11"), preview.getTax());
        // Total = 1030.00 + 950.00 + 88.11 = 2068.11
        assertEquals(new BigDecimal("2068.11"), preview.getTotal());
    }

    // buildAddPreview never touches the DAO's save method

    //Tests that the preview/save split works by confirming orderdao/addOrder
    //is not called when building a preview.
    //verify() checks call history not a return value
    @Test
    void buildAddPreview_neverCallsOrderDaoAddOrder() throws Exception {
        LocalDate futureDate = LocalDate.now().plusDays(10);
        Tax tax = new Tax("TX", "Texas", new BigDecimal("4.45"));
        Product product = new Product("Wood", new BigDecimal("5.15"), new BigDecimal("4.75"));
        when(taxDao.getTax("TX")).thenReturn(tax);
        when(productDao.getProduct("Wood")).thenReturn(product);

        service.buildAddPreview(futureDate, "Reece", "TX", "Wood", new BigDecimal("200"));

        verify(orderDao, never()).addOrder(any(), any());
    }

    // saveNewOrder delegates to the DAO

    @Test
    void saveNewOrder_delegatesToOrderDao() throws FlooringMasteryPersistanceException {
        LocalDate futureDate = LocalDate.now().plusDays(10);
        Order order = new Order();
        order.setOrderDate(futureDate);
        when(orderDao.addOrder(futureDate, order)).thenReturn(order);

        Order result = service.saveNewOrder(futureDate, order);

        assertEquals(order, result);
        verify(orderDao, times(1)).addOrder(futureDate, order);
    }

    // buildEditPreview

    @Test
    void buildEditPreview_returnsNull_whenOrderDoesNotExist() throws Exception {
        LocalDate date = LocalDate.now().plusDays(10);
        when(orderDao.getOrder(date, 999)).thenReturn(null);

        Order result = service.buildEditPreview(date, 999, "NewName", null, null, null);

        assertNull(result);
    }

    //Pressing enter for every "new" field should leave every value unchanged
    @Test
    void buildEditPreview_keepsExistingValues_whenAllNewValuesAreNull() throws Exception {
        LocalDate date = LocalDate.now().plusDays(10);
        Order existing = buildExistingOrder(date);
        when(orderDao.getOrder(date, 1)).thenReturn(existing);

        Order preview = service.buildEditPreview(date, 1, null, null, null, null);

        assertEquals("Reece", preview.getCustomerName());
        assertEquals("TX", preview.getState());
        assertEquals("Wood", preview.getProductType());
        assertEquals(new BigDecimal("200"), preview.getArea());
    }

    //Tests that when changing the previews name must not also change the orginal object
    //until after this method has passed
    @Test
    void buildEditPreview_doesNotMutateOriginalOrder_whenNameChanges() throws Exception {
        LocalDate date = LocalDate.now().plusDays(10);
        Order existing = buildExistingOrder(date);
        when(orderDao.getOrder(date, 1)).thenReturn(existing);

        Order preview = service.buildEditPreview(date, 1, "Ben", null, null, null);

        assertEquals("Ben", preview.getCustomerName());
        assertEquals("Reece", existing.getCustomerName()); // original untouched
    }

    @Test
    void buildEditPreview_recalculatesCosts_whenAreaChanges() throws Exception {
        LocalDate date = LocalDate.now().plusDays(10);
        Order existing = buildExistingOrder(date);
        when(orderDao.getOrder(date, 1)).thenReturn(existing);

        Order preview = service.buildEditPreview(date, 1, null, null, null, new BigDecimal("300"));

        // MaterialCost = 300 * 5.15 = 1545.00
        assertEquals(new BigDecimal("1545.00"), preview.getMaterialCost());
    }

    //Ensures that needsCalculation stays false when only the name changes
    @Test
    void buildEditPreview_doesNotRecalculate_whenOnlyNameChanges() throws Exception {
        LocalDate date = LocalDate.now().plusDays(10);
        Order existing = buildExistingOrder(date);
        when(orderDao.getOrder(date, 1)).thenReturn(existing);

        Order preview = service.buildEditPreview(date, 1, "Ben", null, null, null);

        // Costs should be untouched from the original
        assertEquals(existing.getMaterialCost(), preview.getMaterialCost());
        assertEquals(existing.getTotal(), preview.getTotal());
    }


    // saveEditedOrder delegates to the DAO

    @Test
    void saveEditedOrder_delegatesToOrderDao() throws FlooringMasteryPersistanceException {
        LocalDate date = LocalDate.now().plusDays(10);
        Order order = buildExistingOrder(date);
        when(orderDao.editOrder(date, order)).thenReturn(order);

        Order result = service.saveEditedOrder(date, order);

        assertEquals(order, result);
        verify(orderDao, times(1)).editOrder(date, order);
    }

    // removeOrder / getOrders / exportAllData just delegate

    //Tests that these methods have no business logic, just ensuring that they all call the right DAO method
    @Test
    void removeOrder_delegatesToOrderDao() throws FlooringMasteryPersistanceException {
        LocalDate date = LocalDate.now().plusDays(10);
        Order removed = buildExistingOrder(date);
        when(orderDao.removeOrder(date, 1)).thenReturn(removed);

        Order result = service.removeOrder(date, 1);

        assertEquals(removed, result);
    }

    @Test
    void exportAllData_delegatesToOrderDao() throws FlooringMasteryPersistanceException {
        service.exportAllData();

        verify(orderDao, times(1)).exportAllData();
    }

    // helper

    private Order buildExistingOrder(LocalDate date) {
        Order order = new Order();
        order.setOrderNumber(1);
        order.setOrderDate(date);
        order.setCustomerName("Reece");
        order.setState("TX");
        order.setTaxRate(new BigDecimal("4.45"));
        order.setProductType("Wood");
        order.setArea(new BigDecimal("200"));
        order.setCostPerSquareFoot(new BigDecimal("5.15"));
        order.setLabourCostPerSquareFoot(new BigDecimal("4.75"));
        order.setMaterialCost(new BigDecimal("1030.00"));
        order.setLabourCost(new BigDecimal("950.00"));
        order.setTax(new BigDecimal("88.11"));
        order.setTotal(new BigDecimal("2068.11"));
        return order;
    }
}