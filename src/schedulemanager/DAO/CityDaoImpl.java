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
import schedulemanager.model.City;
import schedulemanager.model.Country;
import schedulemanager.view_controller.Session;

/**
 *
 * @author Tim Smith
 */
public class CityDaoImpl implements CrudDao<City>{
    private static volatile CityDaoImpl cityDaoImpl;
    private SysOp sysOp;
    
    // Constructor
    private CityDaoImpl() {
        // Prohibit multiple instances via reflection
        if (cityDaoImpl != null) {
            throw new RuntimeException("City DAO already implemented, use getInstance() of this class");
        }
        
        sysOp = SysOp.getSysOp();
    }
    
    // Method returns the only instance of CityDaoImpl class, creating instance if not already created
    public static CityDaoImpl getCityDaoImpl() {
        // check for instance twice to ensure thread-safe
        if (cityDaoImpl == null) { 
            synchronized (CityDaoImpl.class) {
                // Second check for instance, if there is no instance create it
                if (cityDaoImpl == null) {
                    cityDaoImpl = new CityDaoImpl();
                }
            }
        }
        return cityDaoImpl;
    }

    @Override
    public ObservableList<City> getAll() throws SysOpException, Exception {
        City cityResult;
        String query = "SELECT * FROM city";
        sysOp.openConnection();
        PreparedStatement statement = sysOp.generateStatement(query);
        
        ObservableList<City> cityList = FXCollections.observableArrayList();
        ResultSet result = sysOp.executeStatement(statement, queryType.SELECT );
        
        while(result.next()){

            int cityId = result.getInt("cityId");
            String city = result.getString("city");
            int countryId = result.getInt("countryId");
            Country country =  CountryDaoImpl.getCountryDaoImpl().getById(countryId);
            LocalDateTime createDate = result.getTimestamp("createDate").toLocalDateTime();
            String createdBy = result.getString("createdBy");
            LocalDateTime lastUpdate = result.getTimestamp("lastUpdate").toLocalDateTime();
            String lastUpdateBy = result.getString("lastUpdateBy");
            cityResult = new City(cityId, city, country,createDate, createdBy, lastUpdate, lastUpdateBy);
            cityList.add(cityResult);
           }
        result.close();
        statement.close();
        sysOp.closeConnection();
        return cityList;
    }

    @Override
    public City getById(int cityId) throws SysOpException, Exception {
        City cityResult;

        String query = "SELECT * FROM city WHERE cityId = ?";
        sysOp.openConnection();
        PreparedStatement statement = sysOp.generateStatement(query);
        statement.setInt(1, cityId);
        ResultSet result = sysOp.executeStatement(statement, queryType.SELECT);
        if (result.next()) {
            boolean active;
            int resultCityId = result.getInt("cityId");
            String cityName = result.getString("city");
            int countryId = result.getInt("countryId");
            Country country =  CountryDaoImpl.getCountryDaoImpl().getById(countryId);            
            LocalDateTime createDate = result.getTimestamp("createDate").toLocalDateTime();
            String createdBy = result.getString("createdBy");
            LocalDateTime lastUpdate = result.getTimestamp("lastUpdate").toLocalDateTime();
            String lastUpdateBy = result.getString("lastUpdateBy");

            sysOp.closeConnection();
            cityResult = new City(resultCityId, cityName, country, createDate, createdBy, lastUpdate, lastUpdateBy);
            return cityResult;
        } else {
            sysOp.closeConnection();
            throw new SysOpException("No record found");
        }
    }

    @Override
    public void insert(City city) throws SysOpException, Exception {
        Timestamp timeStamp = new Timestamp(System.currentTimeMillis());
        int sessionUserId = Session.getSession().getSessionUser().getUserId();
        
        String query  = "INSERT INTO city (city, countryId, createDate, createdBy, lastUpdate, lastUpdateBy) "
                        +"VALUES (?, "
                        +"?, "
                        +"?, "
                        +"?, "
                        +"?, "
                        +"?)";
        
        sysOp.openConnection();
        PreparedStatement statement = sysOp.generateStatement(query);
        
        statement.setString(1, city.getCity());
        statement.setInt(2, city.getCountry().getCountryId());
        statement.setTimestamp(3, timeStamp);
        statement.setInt(4, sessionUserId);
        statement.setTimestamp(5, timeStamp);
        statement.setInt(6, sessionUserId);
        sysOp.executeStatement(statement, queryType.INSERT);
        sysOp.closeConnection();
    }

    @Override
    public void update(City city) throws SysOpException, Exception {
        String query = "UPDATE city Set "
                        +"city = ?, "
                        +"countryId = ?, "
                        +"lastUpdate = ?, "
                        +" lastUpdateBy = ? "
                        +"WHERE cityId = ?";
        
        sysOp.openConnection();
        PreparedStatement statement = sysOp.generateStatement(query);
        statement.setString(1, city.getCity());
        statement.setInt(2, city.getCountry().getCountryId());
        statement.setTimestamp(3, new Timestamp(System.currentTimeMillis()));
        statement.setInt(4, Session.getSession().getSessionUser().getUserId());
        statement.setInt(5, city.getCityId());
        sysOp.executeStatement(statement, queryType.UPDATE);
        sysOp.closeConnection();
    }

    @Override
    public void delete(City city) throws SysOpException, Exception {
        String query = "DELETE FROM city WHERE cityId = ?";
        sysOp.openConnection();
        PreparedStatement statement = sysOp.generateStatement(query);
        

        try {
            statement.setInt(1, city.getCityId());
            sysOp.executeStatement(statement, queryType.DELETE);            
        } catch (SQLException e) {
            if (e.getSQLState().startsWith("23")) {
                throw new SysOpException("Cannot delete, " + city.getCity() + " assigned to 1 or more addresses.");
            }
            throw new SysOpException("There was an error in request");
        }
        sysOp.closeConnection();
    }
    
    public boolean checkIfExists(String city, int countryId) throws SysOpException, Exception {
        boolean exists;
        
        String query = "SELECT city FROM city WHERE (city = ?) AND countryId = ?";
        
        sysOp.openConnection();
        PreparedStatement statement = sysOp.generateStatement(query);
        statement.setString(1, city);
        statement.setInt(2, countryId);
        
        ResultSet result = sysOp.executeStatement(statement, queryType.SELECT);
        
        exists = result.next();
        
        result.close();
        statement.close();
        sysOp.closeConnection();
        
        return exists;
    }
    
    public ObservableList<City> getAllOrderByCityCountry() throws SysOpException, Exception {
        City cityResult;
        String query =  "SELECT city.cityId, "+
                            "city.city, "+
                            "city.createDate, "+
                            "city.createdBy, "+
                            "city.lastUpdate, "+
                            "city.lastUpdateBy, "+
                            "country.countryId, "+
                            "country.country, "+
                            "country.createDate, "+
                            "country.createdBy, "+
                            "country.lastUpdate, "+
                            "country.lastUpdateBy "+
                        "FROM city "+
                            "INNER JOIN country "+
                                "ON city.countryId = country.countryId "+
                        "ORDER BY city, country";
        sysOp.openConnection();
        PreparedStatement statement = sysOp.generateStatement(query);
        
        ObservableList<City> cityList = FXCollections.observableArrayList();
        ResultSet result = sysOp.executeStatement(statement, queryType.SELECT );
        
        while(result.next()){
                
            int cityId = result.getInt("city.cityId");
            String city = result.getString("city.city");
            LocalDateTime cityCreateDate = result.getTimestamp("city.createDate").toLocalDateTime();
            String cityCreatedBy = result.getString("city.createdBy");
            LocalDateTime cityLastUpdate = result.getTimestamp("city.lastUpdate").toLocalDateTime();
            String cityLastUpdateBy = result.getString("city.lastUpdateBy");
            
            int countryId = result.getInt("country.countryId");
            String countryName = result.getString("country.country");
            LocalDateTime countryCreateDate = result.getTimestamp("country.createDate").toLocalDateTime();
            String countryCreatedBy = result.getString("country.createdBy");
            LocalDateTime countryLastUpdate = result.getTimestamp("country.lastUpdate").toLocalDateTime();
            String countryLastUpdateBy = result.getString("country.lastUpdateBy");
            
            Country country = 
                    new Country(countryId, countryName, countryCreateDate, countryCreatedBy, 
                                countryLastUpdate, countryLastUpdateBy);
            
            cityResult = 
                    new City(cityId, city, country,cityCreateDate, cityCreatedBy, cityLastUpdate, cityLastUpdateBy);
            
            cityList.add(cityResult);
           }
        result.close();
        statement.close();
        sysOp.closeConnection();
        return cityList;
    }
    
    
}
