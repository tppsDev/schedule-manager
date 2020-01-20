/*
 * Project written by: Tim Smith
 * 
 */
package schedulemanager.DAO;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * @author Tim Smith
 * 
 * The SysOp class handles all system interactions with the database. In the first phase on the project it will be
 * implemented as a Singleton, ensuring only one instance.
 */

public class SysOp {
    private static volatile SysOp sysOp;
    private static final String DB = "U05EX0";
    private static final String URL = "jdbc:mysql://52.206.157.109/" + DB;
    private static final String DB_USER = "U05EX0";
    private static final String DB_PASSWORD = "53688479232";
    
    private static boolean activeConn;
    
    // Enum to represent query types when selecting execution function
    public static enum queryType {
        SELECT,
        INSERT,
        UPDATE,
        DELETE;
    }
    
    public static Connection conn;
    
    // Constructor
    private SysOp() {
        
        // Prohibit multiple instances via reflection
        if (sysOp != null) {
            throw new RuntimeException("Session already created, use getInstance() of this class");
        }
    }
    
    // Method returns the only instance of SysOp class, creating instance if not already created
    public static SysOp getSysOp() {
        
        // check for instance twice to ensure thread-safe
        if (sysOp == null) { 
            synchronized (SysOp.class) {
                // Second check for instance, if there is no instance create it
                if (sysOp == null) {
                    sysOp = new SysOp();
                }
            }
        }
        return sysOp;
    }
    
    // Check connection status
    public static boolean isActive() {
        return activeConn;
    }        

    // Open database connection
    public void openConnection() {
       
        try {
            conn = DriverManager.getConnection(URL, DB_USER, DB_PASSWORD);
            activeConn = true;
            System.out.println("Connected to " + DB + " successfully.");
        } catch(SQLException e) {
            System.out.println("Failed to connect: " + e.getMessage());
            activeConn = false;
        }
    }
    
    // Close database connection
    public void closeConnection() {
        
        try {
            conn.close();
            System.out.println("Connection to " + DB + " closed.");
            activeConn = false;
        } catch(SQLException e) {
            System.out.println("Failed to close connection: " + e.getMessage());
            activeConn = true;
        }
    }
    
    /*
     * Run Query
     * Accepts String containing an SQL command, and an enum representing query type
     */
    public ResultSet executeStatement(PreparedStatement statement, queryType type) throws SysOpException, SQLException {

        ResultSet result;        
        
        switch (type) {
            case SELECT:
                result = statement.executeQuery();
                return result;
            case INSERT:
            case UPDATE:
            case DELETE:
                statement.executeUpdate();
                return null;
            default:
                throw new SysOpException("Invalid queryType");
        }
        
    }
    
    public PreparedStatement generateStatement(String query) throws SysOpException {
        
        try {
            PreparedStatement statement = conn.prepareStatement(query);
            return statement;
        } catch (SQLException e) {
            throw new SysOpException("There was an error creating your statement, please try again.");
        }
    }

}