/*
 * Project written by: Tim Smith
 * 
 */
package schedulemanager.view_controller;

import javafx.concurrent.Service;
import javafx.concurrent.Task;

/**
 * LoginService class to handle login process for JavaFX GU
 * Returns boolean based on login outcome
 * @author Tim Smith
 * 
 */
public class LoginService extends Service<Boolean> {
    private String username;
    private String password;
    
    // Getter Methods
    public final String getUsername() {
        return username;
    }

    public final String getPassword() {
        return password;
    }
 
    // Setter methods
    public final void setUsername(String username) {
        this.username = username;
    }

    public final void setPassword(String password) {
        this.password = password;
    }

    @Override
    protected Task<Boolean> createTask() {
        return new Task<Boolean>() {
            @Override
            protected Boolean call() {
               Session session;
               boolean success = false;   // default to false in case of thrown exception
                // Call Session login method
                try {
                    session = Session.getSession();
                    success = session.login(username, password);                    
                } catch (SessionException e) {
                    success = false;
                }
                return success;
            }
        };
    }
}
