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
import schedulemanager.DAO.SysOp.queryType;
import schedulemanager.model.Address;
import schedulemanager.model.City;
import schedulemanager.model.Country;
import schedulemanager.view_controller.Session;

/**
 * The AddressDaoImpl class implements CrudDao and handles data interaction with the MySQL database.
 * Singleton class
 * @author Tim Smith
 */
public class AddressDaoImpl implements CrudDao<Address>{
    private static volatile AddressDaoImpl addressDaoImpl;
    private SysOp sysOp;
    
    // Constructor
    private AddressDaoImpl() {
        // Prohibit multiple instances via reflection
        if (addressDaoImpl != null) {
            throw new RuntimeException("AddressDAO already implemented, use getInstance() of this class");
        }
        
        sysOp = SysOp.getSysOp();
    }

    /**
     * Method returns the only instance of AddressDaoImpl class, creating instance if not already created
     * @return
     */
    public static AddressDaoImpl getAddressDaoImpl() {
        // check for instance twice to ensure thread-safe
        if (addressDaoImpl == null) { 
            synchronized (AddressDaoImpl.class) {
                // Second check for instance, if there is no instance create it
                if (addressDaoImpl == null) {
                    addressDaoImpl = new AddressDaoImpl();
                }
            }
        }
        return addressDaoImpl;
    }

    /**
     * Method returns an {@code ObservbleList<Address>} containing all address records
     * @return {@code ObservbleList<Address>}
     * @throws SysOpException
     * @throws Exception
     */
    @Override
    public ObservableList<Address> getAll() throws SysOpException, Exception {
        Address addressResult;
        String query = "SELECT adr.addressId, " +
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
                        "FROM address AS adr " +
                            "INNER JOIN city ON adr.cityId = city.cityId " +
                            "INNER JOIN country AS ctry ON city.countryId = ctry.countryId ";
        sysOp.openConnection();
        PreparedStatement statement = sysOp.generateStatement(query);
        
        ObservableList<Address> addressList = FXCollections.observableArrayList();
        ResultSet result = sysOp.executeStatement(statement, queryType.SELECT );
        
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
            
            addressResult = 
                    new Address(addressId, streetAddr, extAddr, city, postalCode, phone, addrCreateDate, addrCreatedBy, 
                                addrLastUpdate, addrLastUpdateBy);
            addressList.add(addressResult);
           }
        result.close();
        statement.close();
        sysOp.closeConnection();
        return addressList;
    }

    /**
     *
     * @param addressId
     * @return
     * @throws SysOpException
     * @throws Exception
     */
    @Override
    public Address getById(int addressId) throws SysOpException, Exception {

        String query = "SELECT adr.addressId, " +
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
                        "FROM address AS adr " +
                            "INNER JOIN city ON adr.cityId = city.cityId " +
                            "INNER JOIN country AS ctry ON city.countryId = ctry.countryId " +
                        "WHERE addressId = ?";
        sysOp.openConnection();
        PreparedStatement statement = sysOp.generateStatement(query);
        statement.setInt(1, addressId);
        ResultSet result = sysOp.executeStatement(statement, queryType.SELECT);
        if (result.next()) {

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
            int resultAddressId = result.getInt("adr.addressId");
            String streetAddr = result.getString("adr.address");
            String extAddr = result.getString("adr.address2");
            String postalCode = result.getString("adr.postalCode");
            String phone = result.getString("adr.phone");
            LocalDateTime addrCreateDate = result.getTimestamp("adr.createDate").toLocalDateTime();
            String addrCreatedBy = result.getString("adr.createdBy");
            LocalDateTime addrLastUpdate = result.getTimestamp("adr.lastUpdate").toLocalDateTime();
            String addrLastUpdateBy = result.getString("adr.lastUpdateBy");
            result.close();
            statement.close();
            sysOp.closeConnection();
            return new Address(addressId, streetAddr, extAddr, city, postalCode, phone, addrCreateDate, addrCreatedBy, 
                                addrLastUpdate, addrLastUpdateBy);
        } else {
            result.close();
            statement.close();
            sysOp.closeConnection();
            throw new SysOpException("No record found");
        }
    }
    
    public Address getJustAddedAddr(Address addr) throws SysOpException, Exception {

        String query = "SELECT adr.addressId, " +
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
                        "FROM address AS adr " +
                            "INNER JOIN city ON adr.cityId = city.cityId " +
                            "INNER JOIN country AS ctry ON city.countryId = ctry.countryId " +
                        "WHERE address = ? " +
                            "AND adr.cityId = ? " +
                            "AND postalCode = ? " +
                            "AND phone = ?";
        sysOp.openConnection();
        PreparedStatement statement = sysOp.generateStatement(query);
        statement.setString(1, addr.getAddress());
        statement.setInt(2, addr.getCity().getCityId());
        statement.setString(3, addr.getPostalCode());
        statement.setString(4, addr.getPhone());
        ResultSet result = sysOp.executeStatement(statement, queryType.SELECT);
        if (result.next()) {

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
            result.close();
            statement.close();
            sysOp.closeConnection();
            return new Address(addressId, streetAddr, extAddr, city, postalCode, phone, addrCreateDate, addrCreatedBy, 
                                addrLastUpdate, addrLastUpdateBy);
        } else {
            result.close();
            statement.close();
            sysOp.closeConnection();
            throw new SysOpException("No record found");
        }
    }

    @Override
    public void insert(Address address) throws SysOpException, Exception {
        Timestamp timeStamp = new Timestamp(System.currentTimeMillis());
        int sessionUserId = Session.getSession().getSessionUser().getUserId();
        
        String query  = "INSERT INTO address (address, address2, cityId, postalCode, phone, createDate, createdBy, lastUpdate, lastUpdateBy) "
                        +"VALUES (?, "
                        +"?, "
                        +"?, "
                        +"?, "
                        +"?, "
                        +"?, "
                        +"?, "
                        +"?, "
                        +"?)";
        
        sysOp.openConnection();
        try (PreparedStatement statement = sysOp.generateStatement(query)) {
            statement.setString(1, address.getAddress());
            statement.setString(2, address.getAddress2());
            statement.setInt(3, address.getCity().getCityId());
            statement.setString(4, address.getPostalCode());
            statement.setString(5, address.getPhone());
            statement.setTimestamp(6, timeStamp);
            statement.setInt(7, sessionUserId);
            statement.setTimestamp(8, timeStamp);
            statement.setInt(9, sessionUserId);
            sysOp.executeStatement(statement, queryType.INSERT);
        } catch (SQLException ex) {
            throw new SysOpException(ex.getMessage());
        }
        sysOp.closeConnection();
    }

    @Override
    public void update(Address address) throws SysOpException, Exception {
        String query = "UPDATE address Set "
                        +"address = ?, "
                        +"address2 =?, "
                        +"cityId = ?, "
                        +"postalCode = ?, "
                        +"phone = ?, "
                        +"lastUpdate = ?, "
                        +"lastUpdateBy = ? "
                        +"WHERE addressId = ?";
        
        sysOp.openConnection();
        PreparedStatement statement = sysOp.generateStatement(query);
        statement.setString(1, address.getAddress());
        statement.setString(2, address.getAddress2());
        statement.setInt(3, address.getCity().getCityId());
        statement.setString(4, address.getPostalCode());
        statement.setString(5, address.getPhone());
        statement.setTimestamp(6, new Timestamp(System.currentTimeMillis()));
        statement.setInt(7, Session.getSession().getSessionUser().getUserId());
        statement.setInt(8, address.getAddressId());
        sysOp.executeStatement(statement, queryType.UPDATE);
        statement.close();
        sysOp.closeConnection();
    }

    @Override
    public void delete(Address address) throws SysOpException, Exception {
        String query = "DELETE FROM address WHERE addressId = ?";
        sysOp.openConnection();
        PreparedStatement statement = sysOp.generateStatement(query);
        

        try {
            statement.setInt(1, address.getAddressId());
            sysOp.executeStatement(statement, queryType.DELETE);            
        } catch (SQLException e) {
            if (e.getSQLState().startsWith("23")) {
                throw new SysOpException("Cannot delete, Address assigned to 1 or more contacts.");
            }
            throw new SysOpException("There was an error in request");
        } finally {
            statement.close();
            sysOp.closeConnection();
        }
    }
}
