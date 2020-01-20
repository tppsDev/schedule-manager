/*
 * Project written by: Tim Smith
 * 
 */
package schedulemanager.view_controller;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.ObservableList;

/**
 *
 * @author Tim Smith
 */
public abstract class InputManager {

    private final BooleanProperty validated = new SimpleBooleanProperty(false);
    private final BooleanProperty changed = new SimpleBooleanProperty(false);
    private final BooleanProperty saved = new SimpleBooleanProperty();
    
    public final boolean isValidated() {
        return validated.get();
    }

    public final void setValidated(boolean value) {
        validated.set(value);
    }

    public final BooleanProperty validatedProperty() {
        return validated;
    }
    
    public final boolean isChanged() {
        return changed.get();
    }

    public final void setChanged(boolean value) {
        changed.set(value);
    }

    public final BooleanProperty changedProperty() {
        return changed;
    }
    
    public final boolean isSaved() {
        return saved.get();
    }

    public final void setSaved(boolean value) {
        saved.set(value);
    }

    public final BooleanProperty savedProperty() {
        return saved;
    }
    
    public abstract void initialize();
    public abstract void clearAllChanges();
    public abstract ObservableList<?> validateAll();
    public abstract void save();
}
