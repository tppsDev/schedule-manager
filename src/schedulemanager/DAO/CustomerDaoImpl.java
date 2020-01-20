/*
 * Project written by: Tim Smith
 * 
 */
package schedulemanager.DAO;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import schedulemanager.model.Address;
import schedulemanager.model.City;
import schedulemanager.model.Country;
import schedulemanager.model.Customer;
import schedulemanager.view_controller.Session;

/**
 *
 * @author Tim Smith
 */
public class CustomerDaoImpl implements CrudDao<Customer>{
    private static volatile CustomerDaoImpl customerDaoImpl;
    private SysOp sysOp;
    private AddressDaoImpl addressDaoImpl = AddressDaoImpl.getAddressDaoImpl();
    
    // Constructor
    private CustomerDaoImpl() {
        // Prohibit multiple instances via reflection
        if (customerDaoImpl != null) {
            throw new RuntimeException("CustomerDAO already implemented, use getInstance() of this class");
        }
        
        sysOp = SysOp.getSysOp();
    }
    
    // Method returns the only instance of CustomerDaoImpl class, creating instance if not already created
    public static CustomerDaoImpl getCustomerDaoImpl() {
        // check for instance twice to ensure thread-safe
        if (customerDaoImpl == null) { 
            synchronized (CustomerDaoImpl.class) {
                // Second check for instance, if there is no instance create it
                if (customerDaoImpl == null) {
                    customerDaoImpl = new CustomerDaoImpl();
                }
            }
        }
        return customerDaoImpl;
    }

    @Override
    public ObservableList<Customer> getAll() throws SysOpException, Exception {
        Customer customerResult;
        String query = "SELECT * FROM customer";
        sysOp.openConnection();
        PreparedStatement statement = sysOp.generateStatement(query);
        
        ObservableList<Customer> customerList = FXCollections.observableArrayList();
        ResultSet result = sysOp.executeStatement(statement, SysOp.queryType.SELECT );
        
        while(result.next()){
            boolean active;
            int customerId = result.getInt("customerId");
            String customerName = result.getString("customerName");
            int addressId = result.getInt("addressId");
            Address address = addressDaoImpl.getById(addressId);
            active = (result.getInt("active") == 1);
            LocalDateTime createDate = result.getTimestamp("createDate").toLocalDateTime();
            String createdBy = result.getString("createdBy");
            LocalDateTime lastUpdate = result.getTimestamp("lastUpdate").toLocalDateTime();
            String lastUpdateBy = result.getString("lastUpdateBy");
                customerResult = new Customer(customerId, customerName, address, active, createDate, createdBy, 
                                              lastUpdate, lastUpdateBy);
                customerList.add(customerResult);
           }
        sysOp.closeConnection();
        return customerList;
    }

    @Override
    public Customer getById(int customerId) throws SysOpException, Exception {
        String query = "SELECT * FROM customer WHERE customerId = ?";
        sysOp.openConnection();
        PreparedStatement statement = sysOp.generateStatement(query);
        statement.setInt(1, customerId);
        ResultSet result = sysOp.executeStatement(statement, SysOp.queryType.SELECT);
        if (result.next()) {
            boolean active;
            int resultCustomerId = result.getInt("customerId");
            String customerName = result.getString("customerName");
            int addressId = result.getInt("addressId");
            Address address = addressDaoImpl.getById(addressId);
            active = (result.getInt("active") == 1);
            LocalDateTime createDate = result.getTimestamp("createDate").toLocalDateTime();
            String createdBy = result.getString("createdBy");
            LocalDateTime lastUpdate = result.getTimestamp("lastUpdate").toLocalDateTime();
            String lastUpdateBy = result.getString("lastUpdateBy");
            
            sysOp.closeConnection();
            
            return new Customer(resultCustomerId, customerName, address, active, createDate, createdBy, 
                                lastUpdate, lastUpdateBy);
        } else {
            sysOp.closeConnection();

            throw new SysOpException("No record found");
        }
    }

    @Override
    public void insert(Customer customer) throws SysOpException, Exception {
        Timestamp timeStamp = new Timestamp(System.currentTimeMillis());
        int sessionUserId = Session.getSession().getSessionUser().getUserId();
        
        String query  = "INSERT INTO customer (customerName, addressId, active, createDate, createdBy, lastUpdate, "
                        +"lastUpdateBy) "
                        +"VALUES (?, "
                        +"?, "
                        +"?, "
                        +"?, "
                        +"?, "
                        +"?, "
                        +"?)";
        
        sysOp.openConnection();
        PreparedStatement statement = sysOp.generateStatement(query);
        
        statement.setString(1, customer.getCustomerName());
        statement.setInt(2, customer.getAddress().getAddressId());
        int active = customer.isActive() ? 1 : 0;
        statement.setInt(3, active);
        statement.setTimestamp(4, timeStamp);
        statement.setInt(5, sessionUserId);
        statement.setTimestamp(6, timeStamp);
        statement.setInt(7, sessionUserId);
        sysOp.executeStatement(statement, SysOp.queryType.INSERT);
        sysOp.closeConnection();
    }

    @Override
    public void update(Customer customer) throws SysOpException, Exception {
        String query = "UPDATE customer Set "
                        +"customerName = ?, "
                        +"addressId = ?, "
                        +"active = ?, "
                        +"lastUpdate = ?, "
                        +" lastUpdateBy = ? "
                        +"WHERE customerId = ?";
        
        sysOp.openConnection();
        PreparedStatement statement = sysOp.generateStatement(query);
        statement.setString(1, customer.getCustomerName());
        statement.setInt(2, customer.getAddress().getAddressId());
        int active = customer.isActive() ? 1 : 0;
        statement.setInt(3, active);        
        statement.setTimestamp(4, new Timestamp(System.currentTimeMillis()));
        statement.setInt(5, Session.getSession().getSessionUser().getUserId());
        statement.setInt(6, customer.getCustomerId());
        sysOp.executeStatement(statement, SysOp.queryType.UPDATE);
        sysOp.closeConnection();
    }

    @Override
    public void delete(Customer customer) throws SysOpException, Exception {
        String query = "DELETE FROM customer WHERE customerId = ?";
        sysOp.openConnection();
        PreparedStatement statement = sysOp.generateStatement(query);
        

        try {
            statement.setInt(1, customer.getCustomerId());
            sysOp.executeStatement(statement, SysOp.queryType.DELETE);            
        } catch (SQLException e) {
            if (e.getSQLState().startsWith("23")) {
                throw new SysOpException("Cannot delete, " + customer.getCustomerName() 
                                         + " assigned to 1 or more appointments.");
            }
            throw new SysOpException("There was an error in request");
        }
        sysOp.closeConnection();
    }
    
    public ObservableList<Customer> getAllOrderByCustName() throws SysOpException, Exception {
        Customer customer;
        String query = "SELECT cust.customerId, " +
                            "cust.customerName, " +
                            "cust.active, " +
                            "cust.createDate, " +
                            "cust.createdBy, " +
                            "cust.lastUpdate, " +
                            "cust.lastUpdateBy, " +
                            "adr.addressId, " +
                            "adr.address, " +
                            "adr.address2, " +
                            "adr.postalCode, " +
                            "adr.phone, " +
                            "adr.createDate, " +
                            "adr.createdBy, " +
                            "adr.lastUpdate, " +
                            "adr.lastUpdateBy, " +
                            "city.cityId, " +
                            "city.city, " +
                            "city.createDate, " +
                            "city.createdBy, " +
                            "city.lastUpdate, " +
                            "city.lastUpdateBy, " +
                            "ctry.countryId, " +
                            "ctry.country, " +
                            "ctry.createDate, " +
                            "ctry.createdBy, " +
                            "ctry.lastUpdate, " +
                            "ctry.lastUpdateBy " +
                        "FROM customer AS cust " +
                            "INNER JOIN address AS adr ON cust.addressId = adr.addressId " +
                            "INNER JOIN city ON adr.cityId = city.cityId " +
                            "INNER JOIN country AS ctry ON city.countryId = ctry.countryId " +
                        "ORDER BY cust.customerName";
        sysOp.openConnection();
        PreparedStatement statement = sysOp.generateStatement(query);
        
        ObservableList<Customer> customerList = FXCollections.observableArrayList();
        ResultSet result = sysOp.executeStatement(statement, SysOp.queryType.SELECT );
        
        while(result.next()){
            // Country
            int countryId = result.getInt("ctry.countryId");
            String countryName = result.getString("ctry.country");
            LocalDateTime countryCreateDate = result.getTimestamp("ctry.createDate").toLocalDateTime();
            String countryCreatedBy = result.getString("ctry.createdBy");
            LocalDateTime countryLastUpdate = result.getTimestamp("ctry.lastUpdate").toLocalDateTime();
            String countryLastUpdateBy = result.getString("ctry.lastUpdateBy");
            
            Country country = 
                    new Country(countryId, countryName, countryCreateDate, countryCreatedBy, countryLastUpdate, 
                                countryLastUpdateBy);
            // City
            int cityId = result.getInt("city.cityId");
            String cityName = result.getString("city.city");
            LocalDateTime cityCreateDate = result.getTimestamp("city.createDate").toLocalDateTime();
            String cityCreatedBy = result.getString("city.createdBy");
            LocalDateTime cityLastUpdate = result.getTimestamp("city.lastUpdate").toLocalDateTime();
            String cityLastUpdateBy = result.getString("city.lastUpdateBy");
            
            City city = 
                    new City(cityId, cityName, country, cityCreateDate, cityCreatedBy, cityLastUpdate, cityLastUpdateBy);
            // Address
            int addressId = result.getInt("adr.addressId");
            String streetAddr = result.getString("adr.address");
            String extAddr = result.getString("adr.address2");
            String postalCode = result.getString("adr.postalCode");
            String phone = result.getString("adr.phone");
            LocalDateTime addrCreateDate = result.getTimestamp("adr.createDate").toLocalDateTime();
            String addrCreatedBy = result.getString("adr.createdBy");
            LocalDateTime addrLastUpdate = result.getTimestamp("adr.lastUpdate").toLocalDateTime();
            String addrLastUpdateBy = result.getString("adr.lastUpdateBy");
            
            Address address = 
                    new Address(addressId, streetAddr, extAddr, city, postalCode, phone, addrCreateDate, addrCreatedBy, 
                                addrLastUpdate, addrLastUpdateBy);
            // Customer
            boolean active;
            int customerId = result.getInt("cust.customerId");
            String customerName = result.getString("cust.customerName");
            active = (result.getInt("cust.active") == 1);
            LocalDateTime customerCreateDate = result.getTimestamp("cust.createDate").toLocalDateTime();
            String customerCreatedBy = result.getString("cust.createdBy");
            LocalDateTime customerLastUpdate = result.getTimestamp("cust.lastUpdate").toLocalDateTime();
            String customerLastUpdateBy = result.getString("cust.lastUpdateBy");
            
            customer = new Customer(customerId, customerName, address, active, customerCreateDate, 
                                          customerCreatedBy, customerLastUpdate, customerLastUpdateBy);
            customerList.add(customer);
        }
        sysOp.closeConnection();
        return customerList;
    }
    

    public ObservableList<Customer> getAllActiveOrderByCustName() throws SysOpException, Exception {
        Customer customerResult;
        String query = "SELECT cust.customerId, " +
                            "cust.customerName, " +
                            "cust.active, " +
                            "cust.createDate, " +
                            "cust.createdBy, " +
                            "cust.lastUpdate, " +
                            "cust.lastUpdateBy, " +
                            "adr.addressId, " +
                            "adr.address, " +
                            "adr.address2, " +
                            "adr.postalCode, " +
                            "adr.phone, " +
                            "adr.createDate, " +
                            "adr.createdBy, " +
                            "adr.lastUpdate, " +
                            "adr.lastUpdateBy, " +
                            "city.cityId, " +
                            "city.city, " +
                            "city.createDate, " +
                            "city.createdBy, " +
                            "city.lastUpdate, " +
                            "city.lastUpdateBy, " +
                            "ctry.countryId, " +
                            "ctry.country, " +
                            "ctry.createDate, " +
                            "ctry.createdBy, " +
                            "ctry.lastUpdate, " +
                            "ctry.lastUpdateBy " +
                        "FROM customer AS cust " +
                            "INNER JOIN address AS adr ON cust.addressId = adr.addressId " +
                            "INNER JOIN city ON adr.cityId = city.cityId " +
                            "INNER JOIN country AS ctry ON city.countryId = ctry.countryId " +
                        "WHERE cust.active = 1 " +
                        "ORDER BY cust.customerName";
        sysOp.openConnection();
        PreparedStatement statement = sysOp.generateStatement(query);
        
        ObservableList<Customer> customerList = FXCollections.observableArrayList();
        ResultSet result = sysOp.executeStatement(statement, SysOp.queryType.SELECT );
        
        while(result.next()){
            // Country
            int countryId = result.getInt("ctry.countryId");
            String countryName = result.getString("ctry.country");
            LocalDateTime countryCreateDate = result.getTimestamp("ctry.createDate").toLocalDateTime();
            String countryCreatedBy = result.getString("ctry.createdBy");
            LocalDateTime countryLastUpdate = result.getTimestamp("ctry.lastUpdate").toLocalDateTime();
            String countryLastUpdateBy = result.getString("ctry.lastUpdateBy");
            
            Country country = 
                    new Country(countryId, countryName, countryCreateDate, countryCreatedBy, countryLastUpdate, 
                                countryLastUpdateBy);
            // City
            int cityId = result.getInt("city.cityId");
            String cityName = result.getString("city.city");
            LocalDateTime cityCreateDate = result.getTimestamp("city.createDate").toLocalDateTime();
            String cityCreatedBy = result.getString("city.createdBy");
            LocalDateTime cityLastUpdate = result.getTimestamp("city.lastUpdate").toLocalDateTime();
            String cityLastUpdateBy = result.getString("city.lastUpdateBy");
            
            City city = 
                    new City(cityId, cityName, country, cityCreateDate, cityCreatedBy, cityLastUpdate, cityLastUpdateBy);
            // Address
            int addressId = result.getInt("adr.addressId");
            String streetAddr = result.getString("adr.address");
            String extAddr = result.getString("adr.address2");
            String postalCode = result.getString("adr.postalCode");
            String phone = result.getString("adr.phone");
            LocalDateTime addrCreateDate = result.getTimestamp("adr.createDate").toLocalDateTime();
            String addrCreatedBy = result.getString("adr.createdBy");
            LocalDateTime addrLastUpdate = result.getTimestamp("adr.lastUpdate").toLocalDateTime();
            String addrLastUpdateBy = result.getString("adr.lastUpdateBy");
            
            Address address = 
                    new Address(addressId, streetAddr, extAddr, city, postalCode, phone, addrCreateDate, addrCreatedBy, 
                                addrLastUpdate, addrLastUpdateBy);
            // Customer
            boolean active;
            int customerId = result.getInt("cust.customerId");
            String customerName = result.getString("cust.customerName");
            active = (result.getInt("cust.active") == 1);
            LocalDateTime customerCreateDate = result.getTimestamp("cust.createDate").toLocalDateTime();
            String customerCreatedBy = result.getString("cust.createdBy");
            LocalDateTime customerLastUpdate = result.getTimestamp("cust.lastUpdate").toLocalDateTime();
            String customerLastUpdateBy = result.getString("cust.lastUpdateBy");
            
            customerResult = new Customer(customerId, customerName, address, active, customerCreateDate, 
                                          customerCreatedBy, customerLastUpdate, customerLastUpdateBy);
            customerList.add(customerResult);
        }
        sysOp.closeConnection();
        return customerList;
    }
}
