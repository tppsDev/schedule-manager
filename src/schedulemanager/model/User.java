/*
 * Project written by: Tim Smith
 * 
 */
package schedulemanager.model;

import java.time.LocalDateTime;
import java.util.Objects;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

/**
 * @author Tim Smith
 * User class is the data model for the user table in the U05EX0 database
 */
public class User {

    private final IntegerProperty userId = new SimpleIntegerProperty();
    private final StringProperty userName = new SimpleStringProperty();
    private final StringProperty password = new SimpleStringProperty();
    private final BooleanProperty active = new SimpleBooleanProperty();
    private final ObjectProperty<LocalDateTime> createDate = new SimpleObjectProperty<>();
    private final StringProperty createdBy = new SimpleStringProperty();
    private final ObjectProperty<LocalDateTime> lastUpdate = new SimpleObjectProperty<>();
    private final StringProperty lastUpdateBy = new SimpleStringProperty();

//  Constructor - all instance variable
    // remove this after testing
    public User() {
        
    }
    
    public User(int userId, String userName, String password, boolean active, LocalDateTime createDate, String createdBy, LocalDateTime lastUpdate, String lastUpdateBy) {
        this.userId.set(userId);
        this.userName.set(userName);
        this.password.set(password);
        this.active.set(active);
        this.createDate.set(createDate);
        this.createdBy.set(createdBy);
        this.lastUpdate.set(lastUpdate);
        this.lastUpdateBy.set(lastUpdateBy);
    }

//  Getter Methods
    public int getUserId() {
        return userId.get();
    }
    
    public String getUserName() {
        return userName.get();
    }
    
    public String getPassword() {
        return password.get();
    }
    
    public boolean getActive() {
        return active.get();
    }
    
    public boolean isActive() {
        return active.get();
    }
    
    public LocalDateTime getCreateDate() {
        return createDate.get();
    }

    public String getCreatedBy() {
        return createdBy.get();
    }
    
    public LocalDateTime getLastUpdate() {
        return lastUpdate.get();
    }

    public String getLastUpdateBy() {
        return lastUpdateBy.get();
    }
//  Setter Methods    
    public void setUserId(int value) {
        userId.set(value);
    }

    public void setUserName(String value) {
        userName.set(value);
    }

    public void setPassword(String value) {
        password.set(value);
    }

    public void setActive(boolean value) {
        active.set(value);
    }

    public void setCreateDate(LocalDateTime value) {
        createDate.set(value);
    }

    public void setCreatedBy(String value) {
        createdBy.set(value);
    }

    public void setLastUpdate(LocalDateTime value) {
        lastUpdate.set(value);
    }
    
    public void setLastUpdateBy(String value) {
        lastUpdateBy.set(value);
    }
    
    public User copy() {
        return new User(userId.get(), userName.get(), password.get(), active.get(), 
                        createDate.get(), createdBy.get(), lastUpdate.get(), lastUpdateBy.get());
    }
    
// Property methods
    public IntegerProperty userIdProperty() {
        return userId;
    }
    
    public StringProperty userNameProperty() {
        return userName;
    }

    public StringProperty passwordProperty() {
        return password;
    }

    public BooleanProperty activeProperty() {
        return active;
    }

    public ObjectProperty<LocalDateTime> createDateProperty() {
        return createDate;
    }
    
    public StringProperty createdByProperty() {
        return createdBy;
    }

    public ObjectProperty<LocalDateTime> lastUpdateProperty() {
        return lastUpdate;
    }

    public StringProperty lastUpdateByProperty() {
        return lastUpdateBy;
    }
    
    // Method to compare entered password to stored password for user
    public boolean checkPassword(String enteredPassword) {
        return (enteredPassword.equals(this.password.get()));
    }

    @Override
    public String toString() {
        return userName.get();
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 59 * hash + this.userId.get();
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final User other = (User) obj;
        return this.userId.get() == other.userId.get();
    }
}
