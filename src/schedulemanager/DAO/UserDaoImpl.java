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
import schedulemanager.model.User;
import schedulemanager.DAO.SysOp.queryType;
import schedulemanager.view_controller.Session;

/**
 *
 * @author Tim Smith
 */
public class UserDaoImpl implements CrudDao<User>{
    
    private static volatile UserDaoImpl userDaoImpl;
    private SysOp sysOp;
    
    // Constructor
    private UserDaoImpl() {
        // Prohibit multiple instances via reflection
        if (userDaoImpl != null) {
            throw new RuntimeException("User DAO already implemented, use getInstance() of this class");
        }
        
        sysOp = SysOp.getSysOp();
    }
    
    // Method returns the only instance of UserDaoImpl class, creating instance if not already created
    public static UserDaoImpl getUserDaoImpl() {
        // check for instance twice to ensure thread-safe
        if (userDaoImpl == null) { 
            synchronized (UserDaoImpl.class) {
                // Second check for instance, if there is no instance create it
                if (userDaoImpl == null) {
                    userDaoImpl = new UserDaoImpl();
                }
            }
        }
        return userDaoImpl;
    }

    @Override
    public ObservableList<User> getAll() throws SysOpException, Exception {
        User userResult;
        
        String query = "SELECT * FROM user";
        sysOp.openConnection();
        PreparedStatement statement = sysOp.generateStatement(query);
        
        ObservableList<User> userList = FXCollections.observableArrayList();
        ResultSet result = sysOp.executeStatement(statement, queryType.SELECT );
        
        while(result.next()){
                boolean active;
                int userId = result.getInt("userId");
                String userName = result.getString("userName");
                String password = result.getString("password");
                active = (result.getInt("active") == 1);
                LocalDateTime createDate = result.getTimestamp("createDate").toLocalDateTime();
                String createdBy = result.getString("createdBy");
                LocalDateTime lastUpdate = result.getTimestamp("lastUpdate").toLocalDateTime();
                String lastUpdateBy = result.getString("lastUpdateBy");

                userResult = new User(userId, userName, password, active, createDate, createdBy, lastUpdate, lastUpdateBy);
                userList.add(userResult);
           }
        sysOp.closeConnection();
        return userList;
    }

    @Override
    public User getById(int userId) throws SysOpException, Exception {
        User userResult;

        String query = "SELECT * FROM user WHERE userId = ?";
        sysOp.openConnection();
        PreparedStatement statement = sysOp.generateStatement(query);
        statement.setInt(1, userId);
        ResultSet result = sysOp.executeStatement(statement, queryType.SELECT);
        if (result.next()) {
            boolean active;
            int resultUserId = result.getInt("userId");
            String userName = result.getString("userName");
            String password = result.getString("password");
            active = (result.getInt("active") == 1);
            LocalDateTime createDate = result.getTimestamp("createDate").toLocalDateTime();
            String createdBy = result.getString("createdBy");
            LocalDateTime lastUpdate = result.getTimestamp("lastUpdate").toLocalDateTime();
            String lastUpdateBy = result.getString("lastUpdateBy");
            sysOp.closeConnection();
            userResult = new User(resultUserId, userName, password, active, createDate, createdBy, lastUpdate, lastUpdateBy);
            return userResult;
        } else {
            throw new SysOpException("No record found");
        }
    }

    @Override
    public void insert(User user) throws SysOpException, Exception {
        Timestamp timeStamp = new Timestamp(System.currentTimeMillis());
        String sessionUserName = Session.getSession().getSessionUser().getUserName();
        
        String query  = "INSERT INTO user (userName, password, active, createDate, createdBy, lastUpdate, lastUpdateBy) "
                        +"VALUES (?, "
                        +"?, "
                        +"?, "
                        +"?, "
                        +"?, "
                        +"?, "
                        +"?)";
        
        sysOp.openConnection();
        PreparedStatement statement = sysOp.generateStatement(query);
        
        statement.setString(1, user.getUserName().toLowerCase());
        statement.setString(2, user.getPassword());
        int active = user.isActive() ? 1 : 0;
        statement.setInt(3, active);
        statement.setTimestamp(4, timeStamp);
        statement.setString(5, sessionUserName.toLowerCase());
        statement.setTimestamp(6, timeStamp);
        statement.setString(7, sessionUserName.toLowerCase());
        sysOp.executeStatement(statement, queryType.INSERT);
        sysOp.closeConnection();
    }

    @Override
    public void update(User user) throws SysOpException, Exception {
        String query = "UPDATE user Set "
                        +"userName = ?, "
                        +"password = ?, "
                        +"active = ?, "
                        +"lastUpdate = ?, "
                        +" lastUpdateBy = ? "
                        +"WHERE userId = ?";
        
        sysOp.openConnection();
        PreparedStatement statement = sysOp.generateStatement(query);
        statement.setString(1, user.getUserName().toLowerCase());
        statement.setString(2, user.getPassword());
        int active = user.isActive() ? 1 : 0;
        statement.setInt(3, active);
        statement.setTimestamp(4, new Timestamp(System.currentTimeMillis()));
        statement.setString(5, Session.getSession().getSessionUser().getUserName().toLowerCase());
        statement.setInt(6, user.getUserId());
        sysOp.executeStatement(statement, queryType.UPDATE);
        sysOp.closeConnection();
    }
      
    @Override
    public void delete(User user) throws SysOpException {
        String query = "DELETE FROM user WHERE userId = ?";
        sysOp.openConnection();
        PreparedStatement statement = sysOp.generateStatement(query);
        

        try {
            statement.setInt(1, user.getUserId());
            sysOp.executeStatement(statement, queryType.DELETE);            
        } catch (SQLException e) {
            if (e.getSQLState().startsWith("23")) {
                throw new SysOpException("Cannot delete, " + user.getUserName() + " assigned to 1 or more appointments.");
            }
            throw new SysOpException("There was an error in request");
        }
        sysOp.closeConnection();
    }
    
    //User specific CRUD methods
    public User getUserByName(String userName) throws SysOpException, Exception {
        User userResult;
        String query = "SELECT * FROM user WHERE userName = ?";
        sysOp.openConnection();
        PreparedStatement statement = sysOp.generateStatement(query);
        statement.setString(1, userName.toLowerCase());
        ResultSet result = sysOp.executeStatement(statement, queryType.SELECT);
        if (result.next()) {
            boolean active;
            int userId = result.getInt("userId");
            String resultUserName = result.getString("userName");
            String password = result.getString("password");
            active = (result.getInt("active") == 1);
            LocalDateTime createDate = result.getTimestamp("createDate").toLocalDateTime();
            String createdBy = result.getString("createdBy");
            LocalDateTime lastUpdate = result.getTimestamp("lastUpdate").toLocalDateTime();
            String lastUpdateBy = result.getString("lastUpdateBy");
            sysOp.closeConnection();
            userResult = new User(userId, resultUserName, password, active, createDate, createdBy, lastUpdate, lastUpdateBy);
            return userResult;
        } else {
            throw new SysOpException("No record found");
        }
    }
    
    public boolean getIfUserNameExists(String userName) throws SysOpException, Exception {
        User userResult;
        String query = "SELECT * FROM user WHERE userName = ?";
        sysOp.openConnection();
        PreparedStatement statement = sysOp.generateStatement(query);
        statement.setString(1, userName.toLowerCase());
        ResultSet result = sysOp.executeStatement(statement, queryType.SELECT);
        return result.next();
    }
    
    public ObservableList<User> getAllOrderByUserName() throws SysOpException, Exception {
        User userResult;
        
        String query = "SELECT * FROM user ORDER BY userName";
        sysOp.openConnection();
        PreparedStatement statement = sysOp.generateStatement(query);
        
        ObservableList<User> userList = FXCollections.observableArrayList();
        ResultSet result = sysOp.executeStatement(statement, queryType.SELECT );
        
        while(result.next()){
                boolean active;
                int userId = result.getInt("userId");
                String userName = result.getString("userName");
                String password = result.getString("password");
                active = (result.getInt("active") == 1);
                LocalDateTime createDate = result.getTimestamp("createDate").toLocalDateTime();
                String createdBy = result.getString("createdBy");
                LocalDateTime lastUpdate = result.getTimestamp("lastUpdate").toLocalDateTime();
                String lastUpdateBy = result.getString("lastUpdateBy");

                userResult = new User(userId, userName, password, active, createDate, createdBy, lastUpdate, lastUpdateBy);
                userList.add(userResult);
           }
        sysOp.closeConnection();
        return userList;
    }
    
    public ObservableList<User> getAllActiveOrderByUserName() throws SysOpException, Exception {
        User userResult;
        
        String query = "SELECT * FROM user WHERE active = 1 ORDER BY userName";
        sysOp.openConnection();
        PreparedStatement statement = sysOp.generateStatement(query);
        
        ObservableList<User> userList = FXCollections.observableArrayList();
        ResultSet result = sysOp.executeStatement(statement, queryType.SELECT );
        
        while(result.next()){
                boolean active;
                int userId = result.getInt("userId");
                String userName = result.getString("userName");
                String password = result.getString("password");
                active = (result.getInt("active") == 1);
                LocalDateTime createDate = result.getTimestamp("createDate").toLocalDateTime();
                String createdBy = result.getString("createdBy");
                LocalDateTime lastUpdate = result.getTimestamp("lastUpdate").toLocalDateTime();
                String lastUpdateBy = result.getString("lastUpdateBy");

                userResult = new User(userId, userName, password, active, createDate, createdBy, lastUpdate, lastUpdateBy);
                userList.add(userResult);
           }
        sysOp.closeConnection();
        return userList;
    }
    
}
