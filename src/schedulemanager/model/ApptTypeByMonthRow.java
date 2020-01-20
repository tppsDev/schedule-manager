/*
 * Project written by: Tim Smith
 * 
 */
package schedulemanager.model;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

/**
 *
 * @author Tim Smith
 */
public class ApptTypeByMonthRow {

    private final StringProperty apptType = new SimpleStringProperty();
    private final IntegerProperty typeCount = new SimpleIntegerProperty();

    public ApptTypeByMonthRow(String apptType, int typeCount) {
        setApptType(apptType);
        setTypeCount(typeCount);
    }

    public final String getApptType() {
        return apptType.get();
    }

    public final void setApptType(String value) {
        apptType.set(value);
    }

    public final StringProperty apptTypeProperty() {
        return apptType;
    }

    public final int getTypeCount() {
        return typeCount.get();
    }

    public final void setTypeCount(int value) {
        typeCount.set(value);
    }

    public final IntegerProperty typeCountProperty() {
        return typeCount;
    }
    
    
}
