/*
 * Project written by: Tim Smith
 * 
 */
package schedulemanager.view_controller;

/**
 *
 * @author Tim Smith
 */
public class ApptFormException extends Exception {
    public static enum InputField {
        TITLE_FIELD,
        ALL_TIME_FIELDS,
        START_TIME_FIELDS,
        START_TIME_DATE,
        START_TIME_HOUR,
        START_TIME_MIN,
        START_TIME_AMPM,
        END_TIME_FIELDS,
        END_TIME_DATE,
        END_TIME_HOUR,
        END_TIME_MIN,
        END_TIME_AMPM,
        CUSTOMER_FIELD,
        CONSULTANT_FIELD,
        CONTACT_FIELD,
        LOCATION_FIELD,
        TYPE_FIELD,
        URL_FIELD,
        DESCRIPTION_FIELD
    }
    
    private InputField inputField;
    
    public ApptFormException(InputField inputField, String message) {
        super(message);
        this.inputField = inputField;
    }

    public InputField getInputField() {
        return inputField;
    }
   
}
