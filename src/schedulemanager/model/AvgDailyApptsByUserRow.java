/*
 * Project written by: Tim Smith
 * 
 */
package schedulemanager.model;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

/**
 *
 * @author Tim Smith
 */
public class AvgDailyApptsByUserRow {

    private final StringProperty userName = new SimpleStringProperty();
    private final DoubleProperty dailyAvg = new SimpleDoubleProperty();

    public AvgDailyApptsByUserRow(String userName, double dailyAvg) {
        this.userName.set(userName);
        this.dailyAvg.set(dailyAvg);
    }

    public final String getUserName() {
        return userName.get();
    }

    public final void setUserName(String value) {
        userName.set(value);
    }

    public final StringProperty userNameProperty() {
        return userName;
    }

    public final double getDailyAvg() {
        return dailyAvg.get();
    }

    public final void setDailyAvg(double value) {
        dailyAvg.set(value);
    }

    public final DoubleProperty dailyAvgProperty() {
        return dailyAvg;
    }
    
}
