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
import schedulemanager.model.Country;
import schedulemanager.view_controller.Session;

/**
 *
 * @author Tim Smith
 */
public class CountryDaoImpl implements CrudDao<Country>{
    private static volatile CountryDaoImpl countryDaoImpl;
    private SysOp sysOp;
    
    // Constructor
    private CountryDaoImpl() {
        // Prohibit multiple instances via reflection
        if (countryDaoImpl != null) {
            throw new RuntimeException("Country DAO already implemented, use getInstance() of this class");
        }
        
        sysOp = SysOp.getSysOp();
    }
    
    // Method returns the only instance of CountryDaoImpl class, creating instance if not already created
    public static CountryDaoImpl getCountryDaoImpl() {
        // check for instance twice to ensure thread-safe
        if (countryDaoImpl == null) { 
            synchronized (CountryDaoImpl.class) {
                // Second check for instance, if there is no instance create it
                if (countryDaoImpl == null) {
                    countryDaoImpl = new CountryDaoImpl();
                }
            }
        }
        return countryDaoImpl;
    }

    @Override
    public ObservableList<Country> getAll() throws SysOpException, Exception {
        Country countryResult;
        
        String query = "SELECT * FROM country ORDER BY country";
        sysOp.openConnection();
        PreparedStatement statement = sysOp.generateStatement(query);
        
        ObservableList<Country> countryList = FXCollections.observableArrayList();
        ResultSet result = sysOp.executeStatement(statement, queryType.SELECT );
        
        while(result.next()){
            boolean active;
            int countryId = result.getInt("countryId");
            String country = result.getString("country");
            LocalDateTime createDate = result.getTimestamp ("createDate").toLocalDateTime();
            String createdBy = result.getString("createdBy");
            LocalDateTime lastUpdate = result.getTimestamp("lastUpdate").toLocalDateTime();
            String lastUpdateBy = result.getString("lastUpdateBy");

            countryResult = new Country(countryId, country, createDate, createdBy, lastUpdate, lastUpdateBy);
            countryList.add(countryResult);
           }
        result.close();
        statement.close();
        sysOp.closeConnection();
        return countryList;
    }

    @Override
    public Country getById(int countryId) throws SysOpException, Exception {
        Country countryResult;

        String query = "SELECT * FROM country WHERE countryId = ?";
        sysOp.openConnection();
        PreparedStatement statement = sysOp.generateStatement(query);
        statement.setInt(1, countryId);
        ResultSet result = sysOp.executeStatement(statement, queryType.SELECT);
        if (result.next()) {
            int resultCountryId = result.getInt("countryId");
            String countryName = result.getString("country");
            LocalDateTime createDate = result.getTimestamp("createDate").toLocalDateTime();
            String createdBy = result.getString("createdBy");
            LocalDateTime lastUpdate = result.getTimestamp("lastUpdate").toLocalDateTime();
            String lastUpdateBy = result.getString("lastUpdateBy");
            sysOp.closeConnection();
            countryResult = new Country(resultCountryId, countryName, createDate, createdBy, lastUpdate, lastUpdateBy);
            return countryResult;
        } else {
            throw new SysOpException("No record found");
        }
    }

    @Override
    public void insert(Country country) throws SysOpException, Exception {
        Timestamp timeStamp = new Timestamp(System.currentTimeMillis());
        int sessionUserId = Session.getSession().getSessionUser().getUserId();
        
        String query  = "INSERT INTO country (country, createDate, createdBy, lastUpdate, lastUpdateBy) "
                        +"VALUES (?, "
                        +"?, "
                        +"?, "
                        +"?, "
                        +"?)";
        
        sysOp.openConnection();
        PreparedStatement statement = sysOp.generateStatement(query);
        
        statement.setString(1, country.getCountry());
        statement.setTimestamp(2, timeStamp);
        statement.setInt(3, sessionUserId);
        statement.setTimestamp(4, timeStamp);
        statement.setInt(5, sessionUserId);
        sysOp.executeStatement(statement, queryType.INSERT);
        sysOp.closeConnection();
    }

    @Override
    public void update(Country country) throws SysOpException, Exception {
        String query = "UPDATE country Set "
                        +"country = ?, "
                        +"lastUpdate = ?, "
                        +" lastUpdateBy = ? "
                        +"WHERE countryId = ?";
        
        sysOp.openConnection();
        PreparedStatement statement = sysOp.generateStatement(query);
        statement.setString(1, country.getCountry());
        statement.setTimestamp(2, new Timestamp(System.currentTimeMillis()));
        statement.setInt(3, Session.getSession().getSessionUser().getUserId());
        statement.setInt(4, country.getCountryId());
        sysOp.executeStatement(statement, queryType.UPDATE);
        sysOp.closeConnection();
    }

    @Override
    public void delete(Country country) throws SysOpException, Exception {
        String query = "DELETE FROM country WHERE countryId = ?";
        sysOp.openConnection();
        PreparedStatement statement = sysOp.generateStatement(query);
        

        try {
            statement.setInt(1, country.getCountryId());
            sysOp.executeStatement(statement, queryType.DELETE);            
        } catch (SQLException e) {
            if (e.getSQLState().startsWith("23")) {
                throw new SysOpException("Cannot delete, " + country.getCountry() + " assigned to 1 or more cities.");
            }
            throw new SysOpException("There was an error in request");
        }
        statement.close();
        sysOp.closeConnection();
    }
    
    public boolean checkIfExists(String country) throws SysOpException, Exception {
        boolean exists;
        
        String query = "SELECT country FROM country WHERE country = ?";
        
        sysOp.openConnection();
        PreparedStatement statement = sysOp.generateStatement(query);
        statement.setString(1, country);
        
        ResultSet result = sysOp.executeStatement(statement, queryType.SELECT);
        
        exists = result.next();
        
        result.close();
        statement.close();
        sysOp.closeConnection();
        
        return exists;
    }
    
}
