import org.example.dao.OrderDao;
import org.example.dao.OrderDaoImpl;
import org.example.exceptions.FlooringMasteryPersistanceException;
import org.example.models.Order;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.math.BigDecimal;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class OrderDaoImplTest {

    private OrderDao orderDao;
    private LocalDate testDate;

    //Uses 2 temp folders
    @BeforeEach
    void setUp(@TempDir Path tempDir) {
        Path ordersFolder = tempDir.resolve("Orders");
        Path backupFolder = tempDir.resolve("Backup");
        orderDao = new OrderDaoImpl(ordersFolder.toString(), backupFolder.toString());
        testDate = LocalDate.now().plusDays(30); // a real future date, no test should hardcode past tense
    }

    //A prebuilt, valid order for tests
    private Order buildSampleOrder(String customerName, BigDecimal area) {
        Order order = new Order();
        order.setOrderDate(testDate);
        order.setCustomerName(customerName);
        order.setState("TX");
        order.setTaxRate(new BigDecimal("4.45"));
        order.setProductType("Wood");
        order.setArea(area);
        order.setCostPerSquareFoot(new BigDecimal("5.15"));
        order.setLabourCostPerSquareFoot(new BigDecimal("4.75"));
        order.setMaterialCost(new BigDecimal("1030.00"));
        order.setLabourCost(new BigDecimal("950.00"));
        order.setTax(new BigDecimal("88.11"));
        order.setTotal(new BigDecimal("2068.11"));
        return order;
    }

    @Test
    void getAllOrders_returnsEmptyList_whenNoOrdersExistForDate() throws FlooringMasteryPersistanceException {
        List<Order> orders = orderDao.getAllOrders(testDate);

        assertNotNull(orders);
        assertTrue(orders.isEmpty());
    }

    @Test
    void addOrder_assignsOrderNumberOne_whenNoOrdersExistYet() throws FlooringMasteryPersistanceException {
        Order order = buildSampleOrder("Reece", new BigDecimal("200"));

        Order added = orderDao.addOrder(testDate, order);

        assertEquals(1, added.getOrderNumber());
    }

    //Test to see if the application can add the highest ordernumber available across all orders
    @Test
    void addOrder_assignsIncrementingOrderNumbers_acrossMultipleAdds() throws FlooringMasteryPersistanceException {
        Order first = orderDao.addOrder(testDate, buildSampleOrder("Reece", new BigDecimal("200")));
        Order second = orderDao.addOrder(testDate, buildSampleOrder("Ben", new BigDecimal("150")));

        assertEquals(1, first.getOrderNumber());
        assertEquals(2, second.getOrderNumber());
    }

    //Confirms an add can be retrieved afterwards
    @Test
    void addOrder_persistsOrder_soItCanBeRetrievedAfterward() throws FlooringMasteryPersistanceException {
        Order added = orderDao.addOrder(testDate, buildSampleOrder("Reece", new BigDecimal("200")));

        Order retrieved = orderDao.getOrder(testDate, added.getOrderNumber());

        assertNotNull(retrieved);
        assertEquals("Reece", retrieved.getCustomerName());
        assertEquals(new BigDecimal("200"), retrieved.getArea());
    }

    @Test
    void getOrder_returnsNull_whenOrderNumberDoesNotExistForDate() throws FlooringMasteryPersistanceException {
        Order result = orderDao.getOrder(testDate, 999);

        assertNull(result);
    }

    @Test
    void editOrder_updatesExistingOrder_whenOrderExists() throws FlooringMasteryPersistanceException {
        Order added = orderDao.addOrder(testDate, buildSampleOrder("Reece", new BigDecimal("200")));
        added.setCustomerName("Ben");

        Order edited = orderDao.editOrder(testDate, added);

        assertNotNull(edited);
        assertEquals("Ben", orderDao.getOrder(testDate, added.getOrderNumber()).getCustomerName());
    }

    @Test
    void editOrder_returnsNull_whenOrderNumberDoesNotExist() throws FlooringMasteryPersistanceException {
        Order fakeOrder = buildSampleOrder("Nobody", new BigDecimal("100"));
        fakeOrder.setOrderNumber(999);

        Order result = orderDao.editOrder(testDate, fakeOrder);

        assertNull(result);
    }

    @Test
    void removeOrder_deletesOrder_andReturnsIt() throws FlooringMasteryPersistanceException {
        Order added = orderDao.addOrder(testDate, buildSampleOrder("Reece", new BigDecimal("200")));

        Order removed = orderDao.removeOrder(testDate, added.getOrderNumber());

        assertNotNull(removed);
        assertNull(orderDao.getOrder(testDate, added.getOrderNumber()));
    }

    @Test
    void removeOrder_returnsNull_whenOrderNumberDoesNotExist() throws FlooringMasteryPersistanceException {
        Order result = orderDao.removeOrder(testDate, 999);

        assertNull(result);
    }

    @Test
    void getNextOrderNumber_returnsOne_whenNoOrdersExistAnywhere() throws FlooringMasteryPersistanceException {
        int next = orderDao.getNextOrder();

        assertEquals(1, next);
    }

    //Again ensures the highest ordernumber is being created
    @Test
    void getNextOrderNumber_isOneHigherThanHighestExistingOrder() throws FlooringMasteryPersistanceException {
        orderDao.addOrder(testDate, buildSampleOrder("Reece", new BigDecimal("200")));
        orderDao.addOrder(testDate, buildSampleOrder("Ben", new BigDecimal("150")));

        int next = orderDao.getNextOrder();

        assertEquals(3, next);
    }

    //Confirms the method is not throwing an exception but doesn't test if data is being sent a file
    @Test
    void exportAllData_doesNotThrow_whenOrdersExist() throws FlooringMasteryPersistanceException {
        orderDao.addOrder(testDate, buildSampleOrder("Reece", new BigDecimal("200")));

        assertDoesNotThrow(() -> orderDao.exportAllData());
    }
}
