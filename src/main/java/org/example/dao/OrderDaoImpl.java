package org.example.dao;

import org.example.exceptions.FlooringMasteryPersistanceException;
import org.example.models.Order;

import java.io.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class OrderDaoImpl implements OrderDao {

    //Sets up most of the logic to be used in the service layer

    //Declaring Variables
    //Variables created for testing
    private static final String ORDERS_FOLDER = "Orders";
    private static final String BACKUP_FOLDER = "Backup";

    //Defining file structure
    private static final String EXPORT_FILE_NAME = "DataExport.txt";
    private static final String ORDER_FILE_PREFIX = "Orders_";
    private static final String ORDER_FILE_SUFFIX = ".txt";

    //Defining layout of date
    private static final DateTimeFormatter FILE_DATE_FORMAT = DateTimeFormatter.ofPattern("MMddyyyy");
    private static final DateTimeFormatter EXPORT_DATE_FORMAT = DateTimeFormatter.ofPattern("MM-dd-yyyy");

    private static final String DELIMITER = "::";
    private static final String ORDER_HEADER =
            "OrderNumber::CustomerName::State::TaxRate::ProductType::Area::CostPerSquareFoot::"
                    + "LabourCostPerSquareFoot::MaterialCost::LabourCost::Tax::Total";
    private static final String EXPORT_HEADER = ORDER_HEADER + "::OrderDate";

    //Variables for testong
    private final String ordersFolderPath;
    private final String backupFoldersPath;

    // Per-date cache, only populated for dates someone has actually asked about this run.
    private final Map<LocalDate, Map<Integer, Order>> loadedOrders = new HashMap<>();

    // Computed once, from a lightweight scan of every file then incremented in memory.
    private int nextOrderNumber = -1;

    //Testing Constructors
    public OrderDaoImpl(){
        this(ORDERS_FOLDER, BACKUP_FOLDER);
    }

    public OrderDaoImpl(String ordersFolderPath, String backupFoldersPath){
        this.ordersFolderPath = ordersFolderPath;
        this.backupFoldersPath = backupFoldersPath;
    }

    //Creates a new arrayList so data cannot be altered
    @Override
    public List<Order> getAllOrders(LocalDate orderDate) throws FlooringMasteryPersistanceException {
        return new ArrayList<>(getOrdersForDate(orderDate).values());
    }

    @Override
    public Order getOrder(LocalDate orderDate, int orderNumber) throws FlooringMasteryPersistanceException {
        //Throws exception if not found
        return getOrdersForDate(orderDate).get(orderNumber);
    }

    @Override
    public int getNextOrder() throws FlooringMasteryPersistanceException {
        ensureNextOrderNumberComputed();
        return nextOrderNumber;
    }

    @Override
    public Order addOrder(LocalDate orderDate, Order order) throws FlooringMasteryPersistanceException {
        ensureNextOrderNumberComputed();
        // the Dao assigns the ordernumber as only thr dao can see everydate
        // to correctly identify the next available number
        order.setOrderNumber(nextOrderNumber);

        Map<Integer, Order> forDate = getOrdersForDate(orderDate);
        forDate.put(order.getOrderNumber(), order);
        writeOrderFile(orderDate, forDate);

        //Only increments after the write method succeeds
        nextOrderNumber++;
        return order;
    }

    @Override
    public Order editOrder(LocalDate orderDate, Order order) throws FlooringMasteryPersistanceException {
        Map<Integer, Order> forDate = getOrdersForDate(orderDate);
        if (!forDate.containsKey(order.getOrderNumber())) {
            return null;
        }
        forDate.put(order.getOrderNumber(), order);
        writeOrderFile(orderDate, forDate);
        return order;
    }

    @Override
    public Order removeOrder(LocalDate orderDate, int orderNumber) throws FlooringMasteryPersistanceException {
        Map<Integer, Order> forDate = getOrdersForDate(orderDate);
        Order removed = forDate.remove(orderNumber);
        // Only rewrites the file if something was actually removed
        if (removed != null) {
            writeOrderFile(orderDate, forDate);
        }
        return removed;
    }

    @Override
    public void exportAllData() throws FlooringMasteryPersistanceException {
        File folder = new File(ordersFolderPath);
        File backupFolder = new File(backupFoldersPath);
        if (!backupFolder.exists()) {
            backupFolder.mkdirs();
        }
        File exportFile = new File(backupFolder, EXPORT_FILE_NAME);

        File[] orderFiles = folder.exists()
                ? folder.listFiles((dir, name) ->
                name.startsWith(ORDER_FILE_PREFIX) && name.endsWith(ORDER_FILE_SUFFIX))
                : null;
        //Converts filenames to Localdates and sorts that way instead of doing it as a string
        //As they are sorted as a calendar
        List<LocalDate> datesWithOrders = new ArrayList<>();
        if (orderFiles != null) {
            for (File file : orderFiles) {
                datesWithOrders.add(parseDateFromFileName(file.getName()));
            }
        }
        Collections.sort(datesWithOrders);
        // Overwrites the file everytime
        try (PrintWriter writer = new PrintWriter(new FileWriter(exportFile))) {
            writer.println(EXPORT_HEADER);

            for (LocalDate date : datesWithOrders) {
                //Goes through getOrdersForDate() instead of reading the over file again
                List<Order> ordersForDate = new ArrayList<>(getOrdersForDate(date).values());
                ordersForDate.sort(Comparator.comparing(Order::getOrderNumber));
                for (Order order : ordersForDate) {
                    writer.println(marshallOrderForExport(order));
                }
            }
        } catch (IOException e) {
            throw new FlooringMasteryPersistanceException("Could not export order data.", e);
        }
    }


    // Private Methods

    //Every public method makes used to check the cache first for the
    //required date
    private Map<Integer, Order> getOrdersForDate(LocalDate date) throws FlooringMasteryPersistanceException {
        if (!loadedOrders.containsKey(date)) {
            loadedOrders.put(date, readOrderFileIfExists(date));
        }
        return loadedOrders.get(date);
    }

    private Map<Integer, Order> readOrderFileIfExists(LocalDate date) throws FlooringMasteryPersistanceException {
        Map<Integer, Order> forDate = new HashMap<>();
        File file = orderFileFor(date);
        //When requesting a new date or an invalid date
        if (!file.exists()) {
            return forDate;
        }
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            reader.readLine(); // header row — discard
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.trim().isEmpty()) {
                    continue;
                }
                Order order = unmarshallOrder(line, date);
                forDate.put(order.getOrderNumber(), order);
            }
        } catch (IOException e) {
            throw new FlooringMasteryPersistanceException("Could not read order file: " + file.getName(), e);
        }
        return forDate;
    }

    //Created for the last resort full folder scan when checking the highest available ordernumber
    private void ensureNextOrderNumberComputed() throws FlooringMasteryPersistanceException {
        if (nextOrderNumber != -1) {
            return;
        }
        File folder = new File(ordersFolderPath);
        if (!folder.exists()) {
            folder.mkdirs();
            nextOrderNumber = 1;
            return;
        }
        File[] orderFiles = folder.listFiles((dir, name) ->
                name.startsWith(ORDER_FILE_PREFIX) && name.endsWith(ORDER_FILE_SUFFIX));
        int max = 0;
        if (orderFiles != null) {
            for (File file : orderFiles) {
                max = Math.max(max, highestOrderNumberIn(file));
            }
        }
        nextOrderNumber = max + 1;
    }

    // only parses the first token of each line, not a full Order object,
    // since this only runs to find one number per file.
    private int highestOrderNumberIn(File file) throws FlooringMasteryPersistanceException {
        int max = 0;
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            reader.readLine(); // header row
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.trim().isEmpty()) {
                    continue;
                }
                int orderNumber = Integer.parseInt(line.split(DELIMITER)[0]);
                max = Math.max(max, orderNumber);
            }
        } catch (IOException e) {
            throw new FlooringMasteryPersistanceException("Could not read order file: " + file.getName(), e);
        }
        return max;
    }

    //Rewrites the entire file from the in-memory map
    //Allows edit/remove to work
    private void writeOrderFile(LocalDate orderDate, Map<Integer, Order> forDate)
            throws FlooringMasteryPersistanceException {
        File folder = new File(ordersFolderPath);
        if (!folder.exists()) {
            folder.mkdirs();
        }
        File file = orderFileFor(orderDate);
        try (PrintWriter writer = new PrintWriter(new FileWriter(file))) {
            writer.println(ORDER_HEADER);
            for (Order order : forDate.values()) {
                writer.println(marshallOrder(order));
            }
        } catch (IOException e) {
            throw new FlooringMasteryPersistanceException("Could not write order file: " + file.getName(), e);
        }
    }

    private File orderFileFor(LocalDate date) {
        String fileName = ORDER_FILE_PREFIX + date.format(FILE_DATE_FORMAT) + ORDER_FILE_SUFFIX;
        return new File(ordersFolderPath, fileName);
    }

    private LocalDate parseDateFromFileName(String fileName) {
        String datePart = fileName.substring(
                ORDER_FILE_PREFIX.length(), fileName.length() - ORDER_FILE_SUFFIX.length());
        return LocalDate.parse(datePart, FILE_DATE_FORMAT);
    }

    //Sets the parameters for each object in an order
    private Order unmarshallOrder(String line, LocalDate date) {
        String[] tokens = line.split(DELIMITER);
        Order order = new Order();
        order.setOrderNumber(Integer.parseInt(tokens[0]));
        order.setCustomerName(tokens[1]);
        order.setState(tokens[2]);
        order.setTaxRate(new BigDecimal(tokens[3]));
        order.setProductType(tokens[4]);
        order.setArea(new BigDecimal(tokens[5]));
        order.setCostPerSquareFoot(new BigDecimal(tokens[6]));
        order.setLabourCostPerSquareFoot(new BigDecimal(tokens[7]));
        order.setMaterialCost(new BigDecimal(tokens[8]));
        order.setLabourCost(new BigDecimal(tokens[9]));
        order.setTax(new BigDecimal(tokens[10]));
        order.setTotal(new BigDecimal(tokens[11]));
        order.setOrderDate(date);
        return order;
    }

    //Organises the data
    private String marshallOrder(Order order) {
        return String.join(DELIMITER,
                String.valueOf(order.getOrderNumber()),
                order.getCustomerName(),
                order.getState(),
                order.getTaxRate().toString(),
                order.getProductType(),
                order.getArea().toString(),
                order.getCostPerSquareFoot().toString(),
                order.getLabourCostPerSquareFoot().toString(),
                order.getMaterialCost().toString(),
                order.getLabourCost().toString(),
                order.getTax().toString(),
                order.getTotal().toString());
    }

    //Adds the orderdate for exporting the data outwidth the application
    private String marshallOrderForExport(Order order) {
        return marshallOrder(order) + DELIMITER + order.getOrderDate().format(EXPORT_DATE_FORMAT);
    }
}
