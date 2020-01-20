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
import schedulemanager.model.Customer;
import schedulemanager.model.Appointment;
import schedulemanager.model.ApptTypeByMonthRow;
import schedulemanager.model.AvgDailyApptsByUserRow;
import schedulemanager.model.City;
import schedulemanager.model.Country;
import schedulemanager.model.User;
import schedulemanager.view_controller.Session;

/**
 *
 * @author Tim Smith
 */
public class AppointmentDaoImpl implements CrudDao<Appointment>{
    private static volatile AppointmentDaoImpl appointmentDaoImpl;
    private SysOp sysOp;
    private CustomerDaoImpl customerDaoImpl = CustomerDaoImpl.getCustomerDaoImpl();
    private UserDaoImpl userDaoImpl = UserDaoImpl.getUserDaoImpl();
    
    // Constructor
    private AppointmentDaoImpl() {
        // Prohibit multiple instances via reflection
        if (appointmentDaoImpl != null) {
            throw new RuntimeException("AppointmentDAO already implemented, use getInstance() of this class");
        }
        
        sysOp = SysOp.getSysOp();
    }
    
    // Method returns the only instance of AppointmentDaoImpl class, creating instance if not already created
    public static AppointmentDaoImpl getAppointmentDaoImpl() {
        // check for instance twice to ensure thread-safe
        if (appointmentDaoImpl == null) { 
            synchronized (AppointmentDaoImpl.class) {
                // Second check for instance, if there is no instance create it
                if (appointmentDaoImpl == null) {
                    appointmentDaoImpl = new AppointmentDaoImpl();
                }
            }
        }
        return appointmentDaoImpl;
    }
    // CrudDao interface implementations
    @Override
    public ObservableList<Appointment> getAll() throws SysOpException, Exception {
        Appointment appointment;
        String query = "SELECT appt.appointmentId, " +
                            "appt.title, " +
                            "appt.description, " +
                            "appt.location, " +
                            "appt.contact, " +
                            "appt.type, " +
                            "appt.url, " +
                            "appt.start, " +
                            "appt.end, " +
                            "appt.createDate, " +
                            "appt.createdBy, " +
                            "appt.lastUpdate, " +
                            "appt.lastUpdateBy, " +
                            "cust.customerId, " +
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
                            "user.userId, " +
                            "user.userName, " +
                            "user.password, " +
                            "user.active, " +
                            "user.createDate, " +
                            "user.createdBy, " +
                            "user.lastUpdate, " +
                            "user.lastUpdateBy, " +
                        "FROM appointment AS appt " +
                            "INNER JOIN user ON appt.userId = user.userId " +
                            "INNER JOIN customer AS cust ON appt.customerId = cust.customerId " +
                            "INNER JOIN address AS adr ON cust.addressId = adr.addressId " +
                            "INNER JOIN city ON adr.cityId = city.cityId " +
                            "INNER JOIN country AS ctry ON city.countryId = ctry.countryId " +
                        "ORDER BY appt.start";
        sysOp.openConnection();
        PreparedStatement statement = sysOp.generateStatement(query);
        
        ObservableList<Appointment> appointmentList = FXCollections.observableArrayList();
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
            boolean activeCust;
            int customerId = result.getInt("cust.customerId");
            String customerName = result.getString("cust.customerName");
            activeCust = (result.getInt("cust.active") == 1);
            LocalDateTime customerCreateDate = result.getTimestamp("cust.createDate").toLocalDateTime();
            String customerCreatedBy = result.getString("cust.createdBy");
            LocalDateTime customerLastUpdate = result.getTimestamp("cust.lastUpdate").toLocalDateTime();
            String customerLastUpdateBy = result.getString("cust.lastUpdateBy");
            
            Customer customer = 
                    new Customer(customerId, customerName, address, activeCust, customerCreateDate, 
                                 customerCreatedBy, customerLastUpdate, customerLastUpdateBy);
            // User
            boolean activeUser;
            int userId = result.getInt("user.userId");
            String userName = result.getString("user.userName");
            String password = result.getString("user.password");
            activeUser = (result.getInt("user.active") == 1);
            LocalDateTime userCreateDate = result.getTimestamp("user.createDate").toLocalDateTime();
            String userCreatedBy = result.getString("user.createdBy");
            LocalDateTime userLastUpdate = result.getTimestamp("user.lastUpdate").toLocalDateTime();
            String userLastUpdateBy = result.getString("user.lastUpdateBy");
            User user  = 
                    new User(userId, userName, password, activeUser, userCreateDate, userCreatedBy,
                             userLastUpdate, userLastUpdateBy);

            // Appointment
            int apptId = result.getInt("appt.appointmentId");
            String apptTitle = result.getString("appt.title");
            String apptDescription = result.getString("appt.description");
            String apptLocation = result.getString("appt.location");
            String apptContact = result.getString("appt.contact");
            String apptType = result.getString("appt.type");
            String apptUrl = result.getString("appt.url");
            LocalDateTime apptStart = result.getTimestamp("appt.start").toLocalDateTime();
            LocalDateTime apptEnd = result.getTimestamp("appt.end").toLocalDateTime();
            LocalDateTime apptCreateDate = result.getTimestamp("appt.createDate").toLocalDateTime();
            String apptCreatedBy = result.getString("appt.createdBy");
            LocalDateTime apptLastUpdate = result.getTimestamp("appt.lastUpdate").toLocalDateTime();
            String apptLastUpdateBy = result.getString("appt.lastUpdateBy");
            appointment = 
                    new Appointment(apptId, customer, user, apptTitle, apptDescription, apptLocation, apptContact, 
                                    apptType, apptUrl, apptStart, apptEnd, apptCreateDate, apptCreatedBy, 
                                    apptLastUpdate, apptLastUpdateBy);
            appointmentList.add(appointment);
       }
        sysOp.closeConnection();
        return appointmentList;
    }
    
    @Override
    public Appointment getById(int appointmentId) throws SysOpException, Exception {
        String query = "SELECT * FROM appointment WHERE appointmentId = ?";
        sysOp.openConnection();
        PreparedStatement statement = sysOp.generateStatement(query);
        statement.setInt(1, appointmentId);
        
        ResultSet result = sysOp.executeStatement(statement, SysOp.queryType.SELECT);

        if (result.next()) {
                int resultAppointmentId = result.getInt("appointmentId");
                int customerId = result.getInt("customerId");
                Customer customer = customerDaoImpl.getById(customerId);
                int userId = result.getInt("userId");
                User user = userDaoImpl.getById(userId);
                String title = result.getString("title");
                String description = result.getString("description");
                String location = result.getString("location");
                String contact = result.getString("contact");
                String type = result.getString("type");
                String url = result.getString("url");
                LocalDateTime start = result.getTimestamp("start").toLocalDateTime();
                LocalDateTime end = result.getTimestamp("end").toLocalDateTime();
                LocalDateTime createDate = result.getTimestamp("createDate").toLocalDateTime();
                String createdBy = result.getString("createdBy");
                LocalDateTime lastUpdate = result.getTimestamp("lastUpdate").toLocalDateTime();                
                String lastUpdateBy = result.getString("lastUpdateBy");
                sysOp.closeConnection();
                return new Appointment(resultAppointmentId, customer, user, title, description, location, contact, 
                                       type, url, start, end, createDate, createdBy, 
                                       lastUpdate, lastUpdateBy);
        } else {
            sysOp.closeConnection();
            throw new SysOpException("No record found");
        }
    }

    @Override
    public void insert(Appointment appointment) throws SysOpException, Exception {
        Timestamp timeStamp = new Timestamp(System.currentTimeMillis());
        Timestamp startTimeStamp = Timestamp.valueOf(appointment.getStart());
        Timestamp endTimeStamp = Timestamp.valueOf(appointment.getEnd());
        String sessionUserName = Session.getSession().getSessionUser().getUserName();
        
        String query  = "INSERT INTO appointment (customerId, userId, title, description, location, contact, type, "+
                                                 "url, start, end, createDate, createdBy, lastUpdate, lastUpdateBy) "
                        +"VALUES (?, "
                        +"?, "
                        +"?, "
                        +"?, "
                        +"?, "
                        +"?, "
                        +"?, "
                        +"?, "
                        +"?, "
                        +"?, "
                        +"?, "
                        +"?, "
                        +"?, "
                        +"?)";
        
        sysOp.openConnection();
        PreparedStatement statement = sysOp.generateStatement(query);
        
        statement.setInt(1, appointment.getCustomer().getCustomerId());
        statement.setInt(2, appointment.getUser().getUserId());
        statement.setString(3, appointment.getTitle());
        statement.setString(4, appointment.getDescription());
        statement.setString(5, appointment.getLocation());
        statement.setString(6, appointment.getContact());
        statement.setString(7, appointment.getType());
        statement.setString(8, appointment.getUrl());
        statement.setTimestamp(9, startTimeStamp);
        statement.setTimestamp(10, endTimeStamp);
        statement.setTimestamp(11, timeStamp);
        statement.setString(12, sessionUserName);
        statement.setTimestamp(13, timeStamp);
        statement.setString(14, sessionUserName);
        sysOp.executeStatement(statement, SysOp.queryType.INSERT);
        sysOp.closeConnection();
    }

    @Override
    public void update(Appointment appointment) throws SysOpException, Exception {
        Timestamp timeStamp = new Timestamp(System.currentTimeMillis());
        Timestamp startTimeStamp = Timestamp.valueOf(appointment.getStart());
        Timestamp endTimeStamp = Timestamp.valueOf(appointment.getEnd());
        String sessionUserName = Session.getSession().getSessionUser().getUserName();
        String query = "UPDATE appointment Set "
                        +"customerId = ?, "
                        +"userId = ?, "
                        +"title = ?, "
                        +"description = ?, "
                        +"location = ?, "
                        +"contact = ?, "
                        +"type = ?, "
                        +"url = ?, "
                        +"start = ?, "
                        +"end = ?, "
                        +"lastUpdate = ?, "
                        +"lastUpdateBy = ? "
                        +"WHERE appointmentId = ?";
        
        sysOp.openConnection();
        PreparedStatement statement = sysOp.generateStatement(query);
        statement.setInt(1, appointment.getCustomer().getCustomerId());
        statement.setInt(2, appointment.getUser().getUserId());
        statement.setString(3, appointment.getTitle());
        statement.setString(4, appointment.getDescription());
        statement.setString(5, appointment.getLocation());
        statement.setString(6, appointment.getContact());
        statement.setString(7, appointment.getType());
        statement.setString(8, appointment.getUrl());
        statement.setTimestamp(9, startTimeStamp);
        statement.setTimestamp(10, endTimeStamp);
        statement.setTimestamp(11, timeStamp);
        statement.setString(12, sessionUserName);
        statement.setInt(13, appointment.getAppointmentId());
        sysOp.executeStatement(statement, SysOp.queryType.UPDATE);
        sysOp.closeConnection();
    }

    @Override
    public void delete(Appointment appointment) throws SysOpException, Exception {
        String query = "DELETE FROM appointment WHERE appointmentId = ?";
        sysOp.openConnection();
        PreparedStatement statement = sysOp.generateStatement(query);
        

        try {
            statement.setInt(1, appointment.getAppointmentId());
            sysOp.executeStatement(statement, SysOp.queryType.DELETE);            
        } catch (SQLException e) {
            throw new SysOpException("There was an error in request");
        }
        sysOp.closeConnection();
    }
    
    // Other queries
    public ObservableList<Appointment> getAllByUser(User userToGet) throws SysOpException, Exception {
        Appointment appointmentResult;
        ObservableList<Appointment> appointmentList = FXCollections.observableArrayList();       
        
        String query = "SELECT * FROM appointment WHERE userId = ? ORDER BY start";
        sysOp.openConnection();
        PreparedStatement statement = sysOp.generateStatement(query);
        statement.setInt(1, userToGet.getUserId());
        ResultSet result = sysOp.executeStatement(statement, SysOp.queryType.SELECT );
        
        while(result.next()){
                int appointmentId = result.getInt("appointmentId");
                int customerId = result.getInt("customerId");
                Customer customer = customerDaoImpl.getById(customerId);
                int userId = result.getInt("userId");
                User user = userDaoImpl.getById(userId);
                String title = result.getString("title");
                String description = result.getString("description");
                String location = result.getString("location");
                String contact = result.getString("contact");
                String type = result.getString("type");
                String url = result.getString("url");
                LocalDateTime start = result.getTimestamp("start").toLocalDateTime();
                LocalDateTime end = result.getTimestamp("end").toLocalDateTime();
                LocalDateTime createDate = result.getTimestamp("createDate").toLocalDateTime();
                String createdBy = result.getString("createdBy");
                LocalDateTime lastUpdate = result.getTimestamp("lastUpdate").toLocalDateTime();
                String lastUpdateBy = result.getString("lastUpdateBy");
                appointmentResult = new Appointment(appointmentId, customer, user, title, description, location, contact, 
                                                    type, url, start, end, createDate, createdBy, lastUpdate, lastUpdateBy);
                appointmentList.add(appointmentResult);
           }
        sysOp.closeConnection();
        return appointmentList;
    }
    
    // Query for Consultant Schedule Report
    public ObservableList<Appointment> getUserSchedule(User userToGet, LocalDateTime fromDate) 
                throws SysOpException, Exception {
        Appointment appointment;
        ObservableList<Appointment> appointmentList = FXCollections.observableArrayList();
        Timestamp fromTimeStamp = Timestamp.valueOf(fromDate);

        
        String query = "SELECT appt.appointmentId, " +
                            "appt.title, " +
                            "appt.description, " +
                            "appt.location, " +
                            "appt.contact, " +
                            "appt.type, " +
                            "appt.url, " +
                            "appt.start, " +
                            "appt.end, " +
                            "appt.createDate, " +
                            "appt.createdBy, " +
                            "appt.lastUpdate, " +
                            "appt.lastUpdateBy, " +
                            "cust.customerId, " +
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
                            "ctry.lastUpdateBy, " +
                            "user.userId, " +
                            "user.userName, " +
                            "user.password, " +
                            "user.active, " +
                            "user.createDate, " +
                            "user.createdBy, " +
                            "user.lastUpdate, " +
                            "user.lastUpdateBy " +
                        "FROM appointment AS appt " +
                            "INNER JOIN user ON appt.userId = user.userId " +
                            "INNER JOIN customer AS cust ON appt.customerId = cust.customerId " +
                            "INNER JOIN address AS adr ON cust.addressId = adr.addressId " +
                            "INNER JOIN city ON adr.cityId = city.cityId " +
                            "INNER JOIN country AS ctry ON city.countryId = ctry.countryId " + 
                        "WHERE (appt.userId = ?) AND (appt.start >= ? ) " + 
                        "ORDER BY appt.start";
        sysOp.openConnection();
        PreparedStatement statement = sysOp.generateStatement(query);
        statement.setInt(1, userToGet.getUserId());
        statement.setTimestamp(2, fromTimeStamp);
        ResultSet result = sysOp.executeStatement(statement, SysOp.queryType.SELECT );
        if (result.next()) {
            do {
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
                boolean activeCust;
                int customerId = result.getInt("cust.customerId");
                String customerName = result.getString("cust.customerName");
                activeCust = (result.getInt("cust.active") == 1);
                LocalDateTime customerCreateDate = result.getTimestamp("cust.createDate").toLocalDateTime();
                String customerCreatedBy = result.getString("cust.createdBy");
                LocalDateTime customerLastUpdate = result.getTimestamp("cust.lastUpdate").toLocalDateTime();
                String customerLastUpdateBy = result.getString("cust.lastUpdateBy");

                Customer customer = 
                        new Customer(customerId, customerName, address, activeCust, customerCreateDate, 
                                     customerCreatedBy, customerLastUpdate, customerLastUpdateBy);
                // User
                boolean activeUser;
                int userId = result.getInt("user.userId");
                String userName = result.getString("user.userName");
                String password = result.getString("user.password");
                activeUser = (result.getInt("user.active") == 1);
                LocalDateTime userCreateDate = result.getTimestamp("user.createDate").toLocalDateTime();
                String userCreatedBy = result.getString("user.createdBy");
                LocalDateTime userLastUpdate = result.getTimestamp("user.lastUpdate").toLocalDateTime();
                String userLastUpdateBy = result.getString("user.lastUpdateBy");
                User user  = 
                        new User(userId, userName, password, activeUser, userCreateDate, userCreatedBy,
                                 userLastUpdate, userLastUpdateBy);

                // Appointment
                int apptId = result.getInt("appt.appointmentId");
                String apptTitle = result.getString("appt.title");
                String apptDescription = result.getString("appt.description");
                String apptLocation = result.getString("appt.location");
                String apptContact = result.getString("appt.contact");
                String apptType = result.getString("appt.type");
                String apptUrl = result.getString("appt.url");
                LocalDateTime apptStart = result.getTimestamp("appt.start").toLocalDateTime();
                LocalDateTime apptEnd = result.getTimestamp("appt.end").toLocalDateTime();
                LocalDateTime apptCreateDate = result.getTimestamp("appt.createDate").toLocalDateTime();
                String apptCreatedBy = result.getString("appt.createdBy");
                LocalDateTime apptLastUpdate = result.getTimestamp("appt.lastUpdate").toLocalDateTime();
                String apptLastUpdateBy = result.getString("appt.lastUpdateBy");
                appointment = 
                        new Appointment(apptId, customer, user, apptTitle, apptDescription, apptLocation, apptContact, 
                                        apptType, apptUrl, apptStart, apptEnd, apptCreateDate, apptCreatedBy, 
                                        apptLastUpdate, apptLastUpdateBy);
                appointmentList.add(appointment);
            } while(result.next());
        } else {
            result.close();
            statement.close();
            sysOp.closeConnection();
            throw new SysOpException("No records found");
        }
        result.close();
        statement.close();    
        sysOp.closeConnection();
        return appointmentList;
    }
    
    public ObservableList<Appointment> getUserAppointmentsByDateRange(User userToGet, LocalDateTime fromDate, LocalDateTime toDate) 
                throws SysOpException, Exception {
        Appointment appointment;
        ObservableList<Appointment> appointmentList = FXCollections.observableArrayList();
        Timestamp fromTimeStamp = Timestamp.valueOf(fromDate);
        Timestamp toTimeStamp = Timestamp.valueOf(toDate);

        
        String query = "SELECT appt.appointmentId, " +
                            "appt.title, " +
                            "appt.description, " +
                            "appt.location, " +
                            "appt.contact, " +
                            "appt.type, " +
                            "appt.url, " +
                            "appt.start, " +
                            "appt.end, " +
                            "appt.createDate, " +
                            "appt.createdBy, " +
                            "appt.lastUpdate, " +
                            "appt.lastUpdateBy, " +
                            "cust.customerId, " +
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
                            "ctry.lastUpdateBy, " +
                            "user.userId, " +
                            "user.userName, " +
                            "user.password, " +
                            "user.active, " +
                            "user.createDate, " +
                            "user.createdBy, " +
                            "user.lastUpdate, " +
                            "user.lastUpdateBy " +
                        "FROM appointment AS appt " +
                            "INNER JOIN user ON appt.userId = user.userId " +
                            "INNER JOIN customer AS cust ON appt.customerId = cust.customerId " +
                            "INNER JOIN address AS adr ON cust.addressId = adr.addressId " +
                            "INNER JOIN city ON adr.cityId = city.cityId " +
                            "INNER JOIN country AS ctry ON city.countryId = ctry.countryId " + 
                        "WHERE (appt.userId = ?) AND (appt.start BETWEEN ? AND ?) " + 
                        "ORDER BY appt.start";
        sysOp.openConnection();
        PreparedStatement statement = sysOp.generateStatement(query);
        statement.setInt(1, userToGet.getUserId());
        statement.setTimestamp(2, fromTimeStamp);
        statement.setTimestamp(3, toTimeStamp);
        ResultSet result = sysOp.executeStatement(statement, SysOp.queryType.SELECT );
        if (result.next()) {
            do {
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
                boolean activeCust;
                int customerId = result.getInt("cust.customerId");
                String customerName = result.getString("cust.customerName");
                activeCust = (result.getInt("cust.active") == 1);
                LocalDateTime customerCreateDate = result.getTimestamp("cust.createDate").toLocalDateTime();
                String customerCreatedBy = result.getString("cust.createdBy");
                LocalDateTime customerLastUpdate = result.getTimestamp("cust.lastUpdate").toLocalDateTime();
                String customerLastUpdateBy = result.getString("cust.lastUpdateBy");

                Customer customer = 
                        new Customer(customerId, customerName, address, activeCust, customerCreateDate, 
                                     customerCreatedBy, customerLastUpdate, customerLastUpdateBy);
                // User
                boolean activeUser;
                int userId = result.getInt("user.userId");
                String userName = result.getString("user.userName");
                String password = result.getString("user.password");
                activeUser = (result.getInt("user.active") == 1);
                LocalDateTime userCreateDate = result.getTimestamp("user.createDate").toLocalDateTime();
                String userCreatedBy = result.getString("user.createdBy");
                LocalDateTime userLastUpdate = result.getTimestamp("user.lastUpdate").toLocalDateTime();
                String userLastUpdateBy = result.getString("user.lastUpdateBy");
                User user  = 
                        new User(userId, userName, password, activeUser, userCreateDate, userCreatedBy,
                                 userLastUpdate, userLastUpdateBy);

                // Appointment
                int apptId = result.getInt("appt.appointmentId");
                String apptTitle = result.getString("appt.title");
                String apptDescription = result.getString("appt.description");
                String apptLocation = result.getString("appt.location");
                String apptContact = result.getString("appt.contact");
                String apptType = result.getString("appt.type");
                String apptUrl = result.getString("appt.url");
                LocalDateTime apptStart = result.getTimestamp("appt.start").toLocalDateTime();
                LocalDateTime apptEnd = result.getTimestamp("appt.end").toLocalDateTime();
                LocalDateTime apptCreateDate = result.getTimestamp("appt.createDate").toLocalDateTime();
                String apptCreatedBy = result.getString("appt.createdBy");
                LocalDateTime apptLastUpdate = result.getTimestamp("appt.lastUpdate").toLocalDateTime();
                String apptLastUpdateBy = result.getString("appt.lastUpdateBy");
                appointment = 
                        new Appointment(apptId, customer, user, apptTitle, apptDescription, apptLocation, apptContact, 
                                        apptType, apptUrl, apptStart, apptEnd, apptCreateDate, apptCreatedBy, 
                                        apptLastUpdate, apptLastUpdateBy);
                appointmentList.add(appointment);
            } while(result.next());
        } else {
            result.close();
            statement.close();
            sysOp.closeConnection();
            throw new SysOpException("No records found");
        }
        result.close();
        statement.close();    
        sysOp.closeConnection();
        return appointmentList;
    }
    
    public ObservableList<Appointment> getUserOverlappingAppointments(User userToGet, LocalDateTime fromDate, 
                                       LocalDateTime toDate, Appointment appt, boolean newAppt) throws SysOpException, Exception {
        Appointment appointment;
        ObservableList<Appointment> appointmentList = FXCollections.observableArrayList();
        Timestamp fromTimeStamp = Timestamp.valueOf(fromDate);
        Timestamp toTimeStamp = Timestamp.valueOf(toDate);
        // todo diesregard current app
        
        String query = "SELECT appt.appointmentId, " +
                            "appt.title, " +
                            "appt.description, " +
                            "appt.location, " +
                            "appt.contact, " +
                            "appt.type, " +
                            "appt.url, " +
                            "appt.start, " +
                            "appt.end, " +
                            "appt.createDate, " +
                            "appt.createdBy, " +
                            "appt.lastUpdate, " +
                            "appt.lastUpdateBy, " +
                            "cust.customerId, " +
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
                            "ctry.lastUpdateBy, " +
                            "user.userId, " +
                            "user.userName, " +
                            "user.password, " +
                            "user.active, " +
                            "user.createDate, " +
                            "user.createdBy, " +
                            "user.lastUpdate, " +
                            "user.lastUpdateBy " +
                        "FROM appointment AS appt " +
                            "INNER JOIN user ON appt.userId = user.userId " +
                            "INNER JOIN customer AS cust ON appt.customerId = cust.customerId " +
                            "INNER JOIN address AS adr ON cust.addressId = adr.addressId " +
                            "INNER JOIN city ON adr.cityId = city.cityId " +
                            "INNER JOIN country AS ctry ON city.countryId = ctry.countryId " + 
                        "WHERE (appt.userId = ?) " +
                            "AND " +
                                "((appt.start BETWEEN ? AND ?) " +
                                    "OR "+
                                "(appt.end BETWEEN ? AND ?)) " +
                            "AND " +
                                "(appt.appointmentId <> ?) " +
                        "ORDER BY appt.start";
        sysOp.openConnection();
        PreparedStatement statement = sysOp.generateStatement(query);
        statement.setInt(1, userToGet.getUserId());
        statement.setTimestamp(2, fromTimeStamp);
        statement.setTimestamp(3, toTimeStamp);
        statement.setTimestamp(4, fromTimeStamp);
        statement.setTimestamp(5, toTimeStamp);
        if (newAppt) {
            statement.setInt(6, -1);
        } else {
            statement.setInt(6, appt.getAppointmentId());
        }
        ResultSet result = sysOp.executeStatement(statement, SysOp.queryType.SELECT );
        if (result.next()) {
            do {
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
                boolean activeCust;
                int customerId = result.getInt("cust.customerId");
                String customerName = result.getString("cust.customerName");
                activeCust = (result.getInt("cust.active") == 1);
                LocalDateTime customerCreateDate = result.getTimestamp("cust.createDate").toLocalDateTime();
                String customerCreatedBy = result.getString("cust.createdBy");
                LocalDateTime customerLastUpdate = result.getTimestamp("cust.lastUpdate").toLocalDateTime();
                String customerLastUpdateBy = result.getString("cust.lastUpdateBy");

                Customer customer = 
                        new Customer(customerId, customerName, address, activeCust, customerCreateDate, 
                                     customerCreatedBy, customerLastUpdate, customerLastUpdateBy);
                // User
                boolean activeUser;
                int userId = result.getInt("user.userId");
                String userName = result.getString("user.userName");
                String password = result.getString("user.password");
                activeUser = (result.getInt("user.active") == 1);
                LocalDateTime userCreateDate = result.getTimestamp("user.createDate").toLocalDateTime();
                String userCreatedBy = result.getString("user.createdBy");
                LocalDateTime userLastUpdate = result.getTimestamp("user.lastUpdate").toLocalDateTime();
                String userLastUpdateBy = result.getString("user.lastUpdateBy");
                User user  = 
                        new User(userId, userName, password, activeUser, userCreateDate, userCreatedBy,
                                 userLastUpdate, userLastUpdateBy);

                // Appointment
                int apptId = result.getInt("appt.appointmentId");
                String apptTitle = result.getString("appt.title");
                String apptDescription = result.getString("appt.description");
                String apptLocation = result.getString("appt.location");
                String apptContact = result.getString("appt.contact");
                String apptType = result.getString("appt.type");
                String apptUrl = result.getString("appt.url");
                LocalDateTime apptStart = result.getTimestamp("appt.start").toLocalDateTime();
                LocalDateTime apptEnd = result.getTimestamp("appt.end").toLocalDateTime();
                LocalDateTime apptCreateDate = result.getTimestamp("appt.createDate").toLocalDateTime();
                String apptCreatedBy = result.getString("appt.createdBy");
                LocalDateTime apptLastUpdate = result.getTimestamp("appt.lastUpdate").toLocalDateTime();
                String apptLastUpdateBy = result.getString("appt.lastUpdateBy");
                appointment = 
                        new Appointment(apptId, customer, user, apptTitle, apptDescription, apptLocation, apptContact, 
                                        apptType, apptUrl, apptStart, apptEnd, apptCreateDate, apptCreatedBy, 
                                        apptLastUpdate, apptLastUpdateBy);
                appointmentList.add(appointment);
            } while(result.next());
        } else {
            result.close();
            statement.close();
            sysOp.closeConnection();
        }
        result.close();
        statement.close();    
        sysOp.closeConnection();
        return appointmentList;
    }
    
    public ObservableList<Appointment> getUserAppointmentsForDate(User userToGet, LocalDateTime fromDate) 
                throws SysOpException, Exception {
        Appointment appointmentResult;
        ObservableList<Appointment> appointmentList = FXCollections.observableArrayList();
        Timestamp fromTimeStamp = Timestamp.valueOf(fromDate);

        String query = "SELECT * FROM appointment WHERE (userId = ?) AND (date(start) = date(?)) ORDER BY start";
        sysOp.openConnection();
        PreparedStatement statement = sysOp.generateStatement(query);
        statement.setInt(1, userToGet.getUserId());
        statement.setTimestamp(2, fromTimeStamp);
        ResultSet result = sysOp.executeStatement(statement, SysOp.queryType.SELECT );
        if (result.next()) {
            do {
                int appointmentId = result.getInt("appointmentId");
                int customerId = result.getInt("customerId");
                Customer customer = customerDaoImpl.getById(customerId);
                int userId = result.getInt("userId");
                User user = userDaoImpl.getById(userId);
                String title = result.getString("title");
                String description = result.getString("description");
                String location = result.getString("location");
                String contact = result.getString("contact");
                String type = result.getString("type");
                String url = result.getString("url");
                LocalDateTime start = result.getTimestamp("start").toLocalDateTime();
                LocalDateTime end = result.getTimestamp("end").toLocalDateTime();
                LocalDateTime createDate = result.getTimestamp("createDate").toLocalDateTime();
                String createdBy = result.getString("createdBy");
                LocalDateTime lastUpdate = result.getTimestamp("lastUpdate").toLocalDateTime();              
                String lastUpdateBy = result.getString("lastUpdateBy");
                appointmentResult = new Appointment(appointmentId, customer, user, title, description, location, contact, 
                                                    type, url, start, end, createDate, createdBy, 
                                                    lastUpdate, lastUpdateBy);
                appointmentList.add(appointmentResult);
            } while(result.next());
        } else {
            sysOp.closeConnection();
            throw new SysOpException("No records found");
        }
            
        sysOp.closeConnection();
        return appointmentList;
    }
    
    // Query for Appointment Types by Month Report
    public ObservableList<ApptTypeByMonthRow> getCountOfApptTypesForMonth(int month) throws SysOpException, Exception {
        ObservableList<ApptTypeByMonthRow> apptTypeByMonthRows = FXCollections.observableArrayList();
        ApptTypeByMonthRow apptTypeByMonthRow;
        String query = "SELECT type AS apptType, COUNT(type) AS typeCount " +
                       "FROM appointment " +
                       "WHERE MONTH(start) = ? " +
                       "GROUP BY type " +
                       "ORDER BY typeCount DESC, apptType";
        
        sysOp.openConnection();
        
        PreparedStatement statement = sysOp.generateStatement(query);
        statement.setInt(1, month);
        
        ResultSet resultSet = sysOp.executeStatement(statement, SysOp.queryType.SELECT);
        
        if (resultSet.next()) {
            do {
                String apptType = resultSet.getString("apptType");
                int typeCount = resultSet.getInt("typeCount");
                
                apptTypeByMonthRow = new ApptTypeByMonthRow(apptType, typeCount);
                apptTypeByMonthRows.add(apptTypeByMonthRow);
                
            } while(resultSet.next());
        } else {
            resultSet.close();
            statement.close();
            sysOp.closeConnection();
            throw new SysOpException("No records found");
        }
        
        resultSet.close();
        statement.close();
        sysOp.closeConnection();
        
        return apptTypeByMonthRows;
    }
    
    public ObservableList<AvgDailyApptsByUserRow> getAvgDailyAppointmentCountByUser() throws SysOpException {
        ObservableList<AvgDailyApptsByUserRow> resultList = FXCollections.observableArrayList();
        AvgDailyApptsByUserRow avgDailyApptsByUserRow;
        
        String query = "SELECT User, AVG(Appts) AS dailyAverage " +
                        "FROM (SELECT user.userName AS User, DATE(appt.start) AS ApptDate, Count(appt.appointmentId) AS Appts " +
                            "FROM appointment AS appt " +
                                " INNER JOIN user ON appt.userId = user.userId " +
                                " GROUP BY ApptDate, User " +
                                " ORDER BY User, ApptDate) AS counts " +
                        "GROUP BY User " +
                        "ORDER BY AVG(Appts)";

        sysOp.openConnection();
        
        try (PreparedStatement statement = sysOp.generateStatement(query)){
            try (ResultSet resultSet = sysOp.executeStatement(statement, SysOp.queryType.SELECT )) {
                if (resultSet.next()){
                    do {
                        String userName = resultSet.getString("User");
                        double dailyAverage = resultSet.getDouble("dailyAverage");
                        
                        avgDailyApptsByUserRow = new AvgDailyApptsByUserRow(userName, dailyAverage);
                        resultList.add(avgDailyApptsByUserRow);
                        
                    } while (resultSet.next());
                } else {
                    sysOp.closeConnection();
                    throw new SysOpException("No records found");
                }
                    
            } catch (SQLException resultException) {
                // TODO handle exception more specifically
                sysOp.closeConnection();
                throw new SysOpException("System Error, please try again.");
            }
        } catch (SQLException statementException) {
            sysOp.closeConnection();
            // TODO handle exception more specifically
            throw new SysOpException("System Error, please try again.");
        }
        sysOp.closeConnection();

        return resultList;
    }
}
