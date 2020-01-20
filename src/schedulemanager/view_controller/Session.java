/*
 * Project written by: Tim Smith
 * 
 */
package schedulemanager.view_controller;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Locale;
import java.util.Scanner;
import schedulemanager.DAO.SysOpException;
import schedulemanager.DAO.UserDaoImpl;
import schedulemanager.model.User;
import schedulemanager.utility.DateTimeHandler;

/**
 * @author Tim Smith
 * 
 * The Session class represents a user session. It handles login and log out and holds values of current session state.
 * The class will be expanded in the future to write to the user database when a user logs in and logs out cleanly. This
 * could be potentially used to restore state in future enhancements.
 */
public class Session {
    private static volatile Session session;
    private String sessionId;
    private LocalDateTime sessionOpened;
    private Locale sessionLocale;
    private User sessionUser;
    private LocalDateTime sessionClosed;
    private int failedLoginAttempts = 0;
    
    // Private constructor for Singleton class
    private Session() {
        
        // Prohibit multiple instances via reflection
        if (session != null) {
            throw new RuntimeException("Session already created, use getInstance() of this class");
        }
        // Set sessionOpened date & time
        sessionOpened = LocalDateTime.now();
        
        try {
            // Generate sessionId as timestamp concatenated with machinename
            sessionId = sessionOpened.format(DateTimeHandler.DATE_TIME_STAMP) + "-" + getMachineName();
            
            // Set failed login attempts to 0 for new session
        } catch (IOException ex) {
            sessionId = sessionOpened.format(DateTimeHandler.DATE_TIME_STAMP) + "-HOSTNAMEERROR";
        }
        sessionLocale = Locale.getDefault();
    }
    
    // Method returns the only instance of Session class, creating instance if not already created
    public static Session getSession() {
        
        // Check for instance twice to ensure thread-safe
        if (session == null) { 
            synchronized (Session.class) {
                // Second check for instance, if there is no instance create it
                if (session == null) {
                    session = new Session();
                }
            }
        }
        return session;
    }
    
    // Getter Methods
    public String getSessionId() {
        return sessionId;
    }

    public LocalDateTime getSessionOpened() {
        return sessionOpened;
    }

    public Locale getSessionLocale() {
        return sessionLocale;
    }
    
    public User getSessionUser() {
        return sessionUser;
    }

    public LocalDateTime getSessionClosed() {
        return sessionClosed;
    }

    public int getFailedLoginAttempts() {
        return failedLoginAttempts;
    }

    // Setter Methods
    public void setSessionUser(User user) {
        this.sessionUser = user;
    }
    
    public void setSessionClosed() {
        this.sessionClosed = LocalDateTime.now();
    }
    
    // Method to return current machine's hostname as a String
    public final String getMachineName() throws IOException {
            return execReadToString("hostname");
    }
    
    /* 
     * Method executes the command passed as a String returning the output as a String
     * Works on Windows, Mac, and LINUX
    */
    private String execReadToString(String execCommand) throws IOException {
        try (Scanner s = new Scanner(Runtime.getRuntime().exec(execCommand).getInputStream()).useDelimiter("\\A")) {
            return s.hasNext() ? s.next() : "";
        }
    }
    
    // Method accepts userName and password to verfiy against database. On success sessionUser is set to user logging in
    public boolean login(String username, String password) throws SessionException {
        UserDaoImpl userDaoImpl = UserDaoImpl.getUserDaoImpl();
        User user = new User();
        try {
            user = userDaoImpl.getUserByName(username);
            if (user.checkPassword(password)) {
                sessionUser = user;
                failedLoginAttempts = 0;
                // TODO update logfile
                File logFile = new File("userlog.txt");
                logFile.createNewFile();
                FileOutputStream fileOut = new FileOutputStream(logFile, true);
                String logEntry = "Login successful: " + session.getSessionUser().getUserName()
                                    + "@" + LocalDateTime.now().format(DateTimeHandler.DATE_TIME_STAMP) +"\n";
                byte[] logEntryBytes = logEntry.getBytes();
                fileOut.write(logEntryBytes);
                fileOut.close();
                return true;
            } else {
                // TODO log file
            }
        } catch (SysOpException e) {
            // TODO log
        } catch (Exception e) {
            throw new SessionException("Error logging in, please try again");
        }
        try {
            File logFile = new File("userlog.txt");
            logFile.createNewFile();
            FileOutputStream fileOut = new FileOutputStream(logFile, true);

            String logEntry = "Login failed: " + user.getUserName()
                                    + "@" + LocalDateTime.now().format(DateTimeHandler.DATE_TIME_STAMP) +"\n";
            byte[] logEntryBytes = logEntry.getBytes();
            fileOut.write(logEntryBytes);
            fileOut.close();
        } catch(IOException ex) {
            // Do nothing if the log write failed
        }
        failedLoginAttempts++;
        return false;
    }
    
    // Method to logout
    public void logout() {
        sessionUser = null;
    }
    
    public void closeSession() {
        // Capturing sessionClosed is for future implementation
        this.setSessionClosed();
      
        System.exit(0);
    }
}
