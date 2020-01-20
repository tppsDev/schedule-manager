/*
 * Project written by: Tim Smith
 * 
 */
package schedulemanager.view_controller;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import schedulemanager.model.Appointment;
import schedulemanager.model.Customer;
import schedulemanager.model.User;
import static schedulemanager.utility.DateTimeHandler.*;
import schedulemanager.view_controller.ApptFormException.InputField;

/**
 *
 * @author Tim Smith
 */
public class AddAppointmentInputManager extends InputManager {
    private Appointment appt = new Appointment();
    private Session session = Session.getSession();
    private boolean initialDateProvided = false;
    private LocalDate initialDate;
    
    // JavaFX Properties - Values
    private final StringProperty title = new SimpleStringProperty();
    private final ObjectProperty<LocalDate> startDate = new SimpleObjectProperty<>();
    private final StringProperty startHour = new SimpleStringProperty();
    private final StringProperty startMinute = new SimpleStringProperty();
    private final StringProperty startAmPm = new SimpleStringProperty();
    private final ObjectProperty<LocalDate> endDate = new SimpleObjectProperty<>();
    private final StringProperty endHour = new SimpleStringProperty();
    private final StringProperty endMinute = new SimpleStringProperty();
    private final StringProperty endAmPm = new SimpleStringProperty();
    private final ObjectProperty<Customer> customer = new SimpleObjectProperty<>();
    private final ObjectProperty<User> consultant = new SimpleObjectProperty<>();
    private final StringProperty contact = new SimpleStringProperty();
    private final StringProperty location = new SimpleStringProperty();
    private final StringProperty type = new SimpleStringProperty();
    private final StringProperty url = new SimpleStringProperty();
    private final StringProperty description = new SimpleStringProperty();
    
    // JavaFX Properties - Populated indicators
    private final BooleanProperty titlePopulated = new SimpleBooleanProperty(false);
    private final BooleanProperty startDatePopulated = new SimpleBooleanProperty(false);
    private final BooleanProperty startHourPopulated = new SimpleBooleanProperty(false);
    private final BooleanProperty startMinutePopulated = new SimpleBooleanProperty(false);
    private final BooleanProperty startAmPmPopulated = new SimpleBooleanProperty(false);
    private final BooleanProperty endDatePopulated = new SimpleBooleanProperty(false);
    private final BooleanProperty endHourPopulated = new SimpleBooleanProperty(false);
    private final BooleanProperty endMinutePopulated = new SimpleBooleanProperty(false);
    private final BooleanProperty endAmPmPopulated = new SimpleBooleanProperty(false);
    private final BooleanProperty customerPopulated = new SimpleBooleanProperty(false);
    private final BooleanProperty consultantPopulated = new SimpleBooleanProperty(false);
    private final BooleanProperty contactPopulated = new SimpleBooleanProperty(false);
    private final BooleanProperty locationPopulated = new SimpleBooleanProperty(false);
    private final BooleanProperty typePopulated = new SimpleBooleanProperty(false);
    private final BooleanProperty urlPopulated = new SimpleBooleanProperty(false);
    private final BooleanProperty descriptionPopulated = new SimpleBooleanProperty(false);
    private final ObservableList<BooleanProperty> fieldChangeList = FXCollections.observableArrayList();
    
// Constructor

    /**
     * Creates new instance of AddAppointmentInputManager.
     * @param appt 
     */
    public AddAppointmentInputManager(Appointment appt) {
        initialize();
    }
    
    /**
     *  Creates new instance of AddAppointmentInputManager.
     *  Accepts an appointment and an appointment date if it is known. 
     *  If date is not known use AddAppointmentInputManager(Appointment appt) constructor
     * 
     * @param appt - Appoint object passed from form controller
     * @param startDate - Appointment date to be set at initialization and form clearing
     */
    public AddAppointmentInputManager(Appointment appt, LocalDate startDate) {
        this(appt);
        initialDate = startDate;
        initialDateProvided = true;
    }
    
// Getters

    /**
     * Returns the current appointment being managed
     * @return
     */
    public Appointment getAppt() {
        return appt;
    }    
    
    @Override
    public final void initialize() {
        String ampm = "AM";
        int hour = LocalTime.now().plusHours(1).getHour();
        
        if (hour > 12) {
            hour = hour - 12;
            ampm = "PM";
        }
        
        if (initialDateProvided) {
            startDate.set(initialDate);
            endDate.set(initialDate);
        } else {
            startDate.set(LocalDate.now());
            endDate.set(LocalDate.now());
        }
        startDatePopulated.set(true);
        endDatePopulated.set(true);
        
        startHour.set(String.valueOf(hour));
        startMinute.set("00");
        startAmPm.set(ampm);
        endHour.set(String.valueOf(hour));
        endMinute.set("30");
        endAmPm.set(ampm);
        
        startHourPopulated.set(true);
        startMinutePopulated.set(true);
        startAmPmPopulated.set(true);
        endHourPopulated.set(true);
        endMinutePopulated.set(true);
        endAmPmPopulated.set(true);
        
    }

    @Override
    public void clearAllChanges() {
        int hour = LocalTime.now().plusHours(1).getHour();
        
        if (hour > 12) {
            hour = hour - 12;
        }

        title.set("");
        startHour.set(String.valueOf(hour));
        startMinute.set("00");
        startAmPm.set("AM");
        endHour.set(String.valueOf(hour));
        endMinute.set("30");
        endAmPm.set("AM");
//        customer.set(new Customer());
//        consultant.set(new User());
        contact.set("");
        location.set("");
        type.set("");
        url.set("");
        description.set("");
        
        if (initialDateProvided) {
            startDate.set(initialDate);
            endDate.set(initialDate);
        } else {
            startDate.set(LocalDate.now());
            endDate.set(LocalDate.now());
        }
    }

    @Override
    public ObservableList<ApptFormException> validateAll() {
        ObservableList<ApptFormException> errorList = FXCollections.observableArrayList();
        try {
            validateTitle();
        } catch (ApptFormException ex) {
            errorList.add(ex);
        }
        try {
            validateDateTimes();
        } catch (ApptFormException ex) {
            errorList.add(ex);
        }
        try {
            validateCustomer();
        } catch (ApptFormException ex) {
            errorList.add(ex);
        }
        try {
            validateConsultant();
        } catch (ApptFormException ex) {
            errorList.add(ex);
        }
        try {
            validateContact();
        } catch (ApptFormException ex) {
            errorList.add(ex);
        }
        try {
            validateLocation();
        } catch (ApptFormException ex) {
            errorList.add(ex);
        }
        try {
            validateType();
        } catch (ApptFormException ex) {
            errorList.add(ex);
        }
        try {
            validateUrl();
        } catch (ApptFormException ex) {
            errorList.add(ex);
        }
        try {
            validateDescription();
        } catch (ApptFormException ex) {
            errorList.add(ex);
        }
        
        if (errorList.isEmpty()) setValidated(true);
        
        return errorList;
    }

    @Override
    public void save() {
        if (isValidated()) {
            appt.setTitle(title.get());
            appt.setStart(buildStartDateTime());
            appt.setEnd(buildEndDateTime());
            appt.setCustomer(customer.get());
            appt.setUser(consultant.get());
            appt.setContact(contact.get());
            appt.setLocation(location.get());
            appt.setType(type.get());
            appt.setUrl(url.get());
            appt.setDescription(description.get());
            appt.setCreateDate(LocalDateTime.now());
            appt.setCreatedBy(session.getSessionUser().getUserName());
        }
    }
    
    /**
     * Create LocalStartDate from current form values for appointment start
     * @return
     */
    public LocalDateTime buildStartDateTime() {
        String timeString = startHour.get() + ":" +
                            startMinute.get() +
                            startAmPm.get();
        return LocalDateTime.of(startDate.get(), LocalTime.parse(timeString, DISPLAY_TIME));
    }
    
    /**
     * Create LocalStartDate from current form values for appointment end
     * @return
     */
    public LocalDateTime buildEndDateTime() {
        String timeString = endHour.get() + ":" +
                            endMinute.get() +
                            endAmPm.get();
        return LocalDateTime.of(endDate.get(), LocalTime.parse(timeString, DISPLAY_TIME));
    }

// Validation Methods

    /**
     * Validate title value for the following:
     *      -Not null
     *      -Does not begin with a space
     *      -Contains only letters, numbers, or white spaces
     * @throws ApptFormException
     */
    public void validateTitle() throws ApptFormException {
        titlePopulated.set(false);
        InputField inputField = InputField.TITLE_FIELD;
        // Check for empty, starting with a space, and non-alphanumeric characters
        if (title.get() == null || title.get().isEmpty()) {
            throw new ApptFormException(inputField, "Title required");
        }
        if (title.get().startsWith(" ")) {
            throw new ApptFormException(inputField, "Title cannot begin with a space");
        }
        if (!title.get().matches("[\\w\\s]+")) {
            throw new ApptFormException(inputField, "Title cannot include special characters or punctuation");
        }
        System.out.println("Title popultated set");
        titlePopulated.set(true);
    }
    
    /**
     * Validate startDate for the following:
     *      -Not null
     *      -Not before current date (exclusive)
     * @throws ApptFormException
     */
    public void validateStartDate() throws ApptFormException {
        startDatePopulated.set(false);
        InputField inputField = InputField.START_TIME_DATE;
        // Check for empty
        if (startDate.get() == null) {
            throw new ApptFormException(inputField, "Start Date Required");
        }
        
        // Check for date prior to today
        if (startDate.get().isBefore(LocalDate.now())) {
            throw new ApptFormException(inputField, "Start Date cannot be before current date");
        }
        System.out.println("startdate popultated set");
        startDatePopulated.set(true);
    }
    
    public void validateStartHour() throws ApptFormException {
        startHourPopulated.set(false);
        InputField inputField = InputField.START_TIME_HOUR;
        // Check for empty
        if (startHour.get() == null) {
            throw new ApptFormException(inputField, "Start Hour Required");
        }
        System.out.println("starthour popultated set");
        startHourPopulated.set(true);
    }
    
    public void validateStartMinute() throws ApptFormException {
        startMinutePopulated.set(false);
        InputField inputField = InputField.START_TIME_MIN;
        // Check for empty
        if (startMinute.get() == null) {
            throw new ApptFormException(inputField, "Start Minute Required");
        }
        System.out.println("startmin popultated set");
        startMinutePopulated.set(true);
    }
    
    public void validateStartAmPm() throws ApptFormException {
        startAmPmPopulated.set(false);
        InputField inputField = InputField.START_TIME_AMPM;
        // Check for empty
        if (startAmPm.get() == null) {
            throw new ApptFormException(inputField, "Start AM/PM Required");
        }
        System.out.println("startampm popultated set");
        startAmPmPopulated.set(true);
    }
    
    public void validateEndDate() throws ApptFormException {
        endDatePopulated.set(false);
        InputField inputField = InputField.END_TIME_DATE;
        // Check for empty
        if (endDate.get() == null) {
            throw new ApptFormException(inputField, "End Date Required");
        }
        
        // Check for date prior to today
        if (endDate.get().isBefore(LocalDate.now())) {
            throw new ApptFormException(inputField, "End Date cannot be before current date");
        }
        System.out.println("endDate popultated set");
        endDatePopulated.set(true);
    }
    
    public void validateEndHour() throws ApptFormException {
        endHourPopulated.set(false);
        InputField inputField = InputField.END_TIME_HOUR;
        // Check for empty
        if (endHour.get() == null) {
            throw new ApptFormException(inputField, "Start Hour Required");
        }
        System.out.println("endhour popultated set");
        endHourPopulated.set(true);
    }
    
    public void validateEndMinute() throws ApptFormException {
        endMinutePopulated.set(false);
        InputField inputField = InputField.END_TIME_MIN;
        // Check for empty
        if (endMinute.get() == null) {
            throw new ApptFormException(inputField, "Start Minute Required");
        }
        System.out.println("endmin popultated set");
        endMinutePopulated.set(true);
    }
    
    public void validateEndAmPm() throws ApptFormException {
        endAmPmPopulated.set(false);
        InputField inputField = InputField.END_TIME_AMPM;
        // Check for empty
        if (endAmPm.get() == null) {
            throw new ApptFormException(inputField, "Start AM/PM Required");
        }
        System.out.println("endampm popultated set");
        endAmPmPopulated.set(true);
    }
    
    public void validateStartDateTime() throws ApptFormException {
        
        InputField inputField = InputField.START_TIME_FIELDS;
        // Verify all components exist before validating complete date & time
        try {
            validateStartDate();
            validateStartHour();
            validateStartMinute();
            validateStartAmPm();
        } catch (ApptFormException ex) {
            throw ex;
        }
        LocalDateTime newDateTime = buildStartDateTime();
        
        if (newDateTime.isBefore(LocalDateTime.now())) {
            throw new ApptFormException(inputField, "Start time cannot be before current time");
        }
        
        if (newDateTime.getHour() < 8) {
            throw new ApptFormException(inputField, "Start time cannot be before 8am");
        }
    }
    
    public void validateEndDateTime() throws ApptFormException {
        
        InputField inputField = InputField.END_TIME_FIELDS;
        // Verify all components exist before validating complete date & time
        try {
            validateEndDate();
            validateEndHour();
            validateEndMinute();
            validateEndAmPm();
        } catch (ApptFormException ex) {
            throw ex;
        }
        LocalDateTime newDateTime = buildEndDateTime();
        
        if (newDateTime.isBefore(LocalDateTime.now())) {
            throw new ApptFormException(inputField, "End time cannot be before current time");
        }
        
        if (newDateTime.toLocalTime().isAfter(LocalTime.of(17, 0))) {
            throw new ApptFormException(inputField, "End time cannot be after 5pm");
        }
    }
    
    public void validateDateTimes() throws ApptFormException {
        
        InputField inputField = InputField.ALL_TIME_FIELDS;
        // Verify start & end date and times before validating against each other
        try {
            validateStartDateTime();
            validateEndDateTime();
        } catch (ApptFormException ex) {
            throw ex;
        }
        LocalDateTime newStartDateTime = buildStartDateTime();
        LocalDateTime newEndDateTime = buildEndDateTime();
        if (newStartDateTime.isEqual(newEndDateTime) || newStartDateTime.isAfter(newEndDateTime)) {
            throw new ApptFormException(inputField, "Start time must be before end time");
        }
    }
    
    public void validateCustomer() throws ApptFormException {
        customerPopulated.set(false);
        InputField inputField = InputField.CUSTOMER_FIELD;
        if (customer.get() == null) {
            throw new ApptFormException(inputField, "Customer required");
        }
        System.out.println("customer popultated set");
        customerPopulated.set(true);
    }
    
    public void validateConsultant() throws ApptFormException {
        consultantPopulated.set(false);
        InputField inputField = InputField.CONSULTANT_FIELD;
        if (consultant.get() == null) {
            throw new ApptFormException(inputField, "Consultant required");
        }
        System.out.println("consultant popultated set");
        consultantPopulated.set(true);
    }
    
    public void validateContact() throws ApptFormException {
        contactPopulated.set(false);
        InputField inputField = InputField.CONTACT_FIELD;
        if (contact.get() == null || contact.get().isEmpty()) {
            throw new ApptFormException(inputField, "Contact required");
        }
        if (contact.get().startsWith(" ")) {
            throw new ApptFormException(inputField, "Contact cannot begin with a space");
        }
        System.out.println("contact popultated set");
        contactPopulated.set(true);
    }
    
    public void validateLocation() throws ApptFormException {
        locationPopulated.set(false);
        InputField inputField = InputField.LOCATION_FIELD;
        
        if (location.get() == null || location.get().isEmpty()) {
            throw new ApptFormException(inputField, "Location required");
        }
        if (location.get().startsWith(" ")) {
            throw new ApptFormException(inputField, "Location cannot begin with a space");
        }
        System.out.println("location popultated set");
        locationPopulated.set(true);
    }
    
    public void validateType() throws ApptFormException {
        typePopulated.set(false);
        InputField inputField = InputField.TYPE_FIELD;
        
        if (type.get() == null || type.get().isEmpty()) {
            throw new ApptFormException(inputField, "Type required");
        }
        if (type.get().startsWith(" ")) {
            throw new ApptFormException(inputField, "Type cannot begin with a space");
        }
        System.out.println("type popultated set");
        typePopulated.set(true);
    }
    
    public void validateUrl() throws ApptFormException {
        urlPopulated.set(false);
        InputField inputField = InputField.URL_FIELD;
        
        if (url.get() == null || url.get().isEmpty()) {
            throw new ApptFormException(inputField, "URL required");
        }
        if (url.get().startsWith(" ")) {
            throw new ApptFormException(inputField, "URL cannot begin with a space");
        }
        System.out.println("url popultated set");
        urlPopulated.set(true);
    }
    
    public void validateDescription() throws ApptFormException {
        descriptionPopulated.set(false);
        InputField inputField = InputField.DESCRIPTION_FIELD;
        
        if (description.get() == null || description.get().isEmpty()) {
            throw new ApptFormException(inputField, "Description required");
        }
        if (description.get().startsWith(" ")) {
            throw new ApptFormException(inputField, "Description cannot begin with a space");
        }
        System.out.println("description popultated set");
        descriptionPopulated.set(true);
    }
    
    // JavaFX property Setters/Getters
    public String getTitle() {
        return title.get();
    }
    
    public void setTitle(String value) {
        title.set(value);
    }

    public StringProperty titleProperty() {
        return title;
    }

    public LocalDate getStartDate() {
        return startDate.get();
    }

    public void setStartDate(LocalDate value) {
        startDate.set(value);
    }

    public ObjectProperty<LocalDate> startDateProperty() {
        return startDate;
    }

    public String getStartHour() {
        return startHour.get();
    }

    public void setStartHour(String value) {
        startHour.set(value);
    }

    public StringProperty startHourProperty() {
        return startHour;
    }

    public String getStartMinute() {
        return startMinute.get();
    }

    public void setStartMinute(String value) {
        startMinute.set(value);
    }

    public StringProperty startMinuteProperty() {
        return startMinute;
    }

    public String getStartAmPm() {
        return startAmPm.get();
    }

    public void setStartAmPm(String value) {
        startAmPm.set(value);
    }

    public StringProperty startAmPmProperty() {
        return startAmPm;
    }

    public LocalDate getEndDate() {
        return endDate.get();
    }

    public void setEndDate(LocalDate value) {
        endDate.set(value);
    }

    public ObjectProperty<LocalDate> endDateProperty() {
        return endDate;
    }

    public String getEndHour() {
        return endHour.get();
    }

    public void setEndHour(String value) {
        endHour.set(value);
    }

    public StringProperty endHourProperty() {
        return endHour;
    }

    public String getEndMinute() {
        return endMinute.get();
    }

    public void setEndMinute(String value) {
        endMinute.set(value);
    }

    public StringProperty endMinuteProperty() {
        return endMinute;
    }

    public String getEndAmPm() {
        return endAmPm.get();
    }

    public void setEndAmPm(String value) {
        endAmPm.set(value);
    }

    public StringProperty endAmPmProperty() {
        return endAmPm;
    }

    public Customer getCustomer() {
        return customer.get();
    }

    public void setCustomer(Customer value) {
        customer.set(value);
    }

    public ObjectProperty<Customer> customerProperty() {
        return customer;
    }

    public User getConsultant() {
        return consultant.get();
    }

    public void setConsultant(User value) {
        consultant.set(value);
    }

    public ObjectProperty<User> consultantProperty() {
        return consultant;
    }

    public String getContact() {
        return contact.get();
    }

    public void setContact(String value) {
        contact.set(value);
    }

    public StringProperty contactProperty() {
        return contact;
    }

    public String getLocation() {
        return location.get();
    }

    public void setLocation(String value) {
        location.set(value);
    }

    public StringProperty locationProperty() {
        return location;
    }

    public String getType() {
        return type.get();
    }

    public void setType(String value) {
        type.set(value);
    }

    public StringProperty typeProperty() {
        return type;
    }

    public String getUrl() {
        return url.get();
    }

    public void setUrl(String value) {
        url.set(value);
    }

    public StringProperty urlProperty() {
        return url;
    }

    public String getDescription() {
        return description.get();
    }

    public void setDescription(String value) {
        description.set(value);
    }

    public StringProperty descriptionProperty() {
        return description;
    }

    // Field Populated Properties
    public void setTitlePopulated(boolean value) {
        titlePopulated.set(value);
    }

    public BooleanProperty titlePopulatedProperty() {
        return titlePopulated;
    }

    public boolean isStartDatePopulated() {
        return startDatePopulated.get();
    }

    public void setStartDatePopulated(boolean value) {
        startDatePopulated.set(value);
    }

    public BooleanProperty startDatePopulatedProperty() {
        return startDatePopulated;
    }

    public boolean isStartHourPopulated() {
        return startHourPopulated.get();
    }

    public void setStartHourPopulated(boolean value) {
        startHourPopulated.set(value);
    }

    public BooleanProperty startHourPopulatedProperty() {
        return startHourPopulated;
    }

    public boolean isStartMinutePopulated() {
        return startMinutePopulated.get();
    }

    public void setStartMinutePopulated(Boolean value) {
        startMinutePopulated.set(value);
    }

    public BooleanProperty startMinutePopulatedProperty() {
        return startMinutePopulated;
    }

    public boolean isStartAmPmPopulated() {
        return startAmPmPopulated.get();
    }

    public void setStartAmPmPopulated(boolean value) {
        startAmPmPopulated.set(value);
    }

    public BooleanProperty startAmPmPopulatedProperty() {
        return startAmPmPopulated;
    }
 
    public boolean isEndDatePopulated() {
        return endDatePopulated.get();
    }

    public void setEndDatePopulated(boolean value) {
        endDatePopulated.set(value);
    }

    public BooleanProperty endDatePopulatedProperty() {
        return endDatePopulated;
    }

    public boolean isEndHourPopulated() {
        return endHourPopulated.get();
    }

    public void setEndHourPopulated(boolean value) {
        endHourPopulated.set(value);
    }

    public BooleanProperty endHourPopulatedProperty() {
        return endHourPopulated;
    }

    public boolean isEndMinutePopulated() {
        return endMinutePopulated.get();
    }

    public void setEndMinutePopulated(boolean value) {
        endMinutePopulated.set(value);
    }

    public BooleanProperty endMinutePopulatedProperty() {
        return endMinutePopulated;
    }

    public boolean isEndAmPmPopulated() {
        return endAmPmPopulated.get();
    }

    public void setEndAmPmPopulated(boolean value) {
        endAmPmPopulated.set(value);
    }

    public BooleanProperty endAmPmPopulatedProperty() {
        return endAmPmPopulated;
    }

    public boolean isCustomerPopulated() {
        return customerPopulated.get();
    }

    public void setCustomerPopulated(boolean value) {
        customerPopulated.set(value);
    }

    public BooleanProperty customerPopulatedProperty() {
        return customerPopulated;
    }

    public boolean isConsultantPopulated() {
        return consultantPopulated.get();
    }

    public void setConsultantPopulated(boolean value) {
        consultantPopulated.set(value);
    }

    public BooleanProperty consultantPopulatedProperty() {
        return consultantPopulated;
    }

    public boolean isContactPopulated() {
        return contactPopulated.get();
    }

    public void setContactPopulated(boolean value) {
        contactPopulated.set(value);
    }

    public BooleanProperty contactPopulatedProperty() {
        return contactPopulated;
    }

    public boolean isLocationPopulated() {
        return locationPopulated.get();
    }

    public void setLocationPopulated(boolean value) {
        locationPopulated.set(value);
    }

    public BooleanProperty locationPopulatedProperty() {
        return locationPopulated;
    }
    
    public boolean isTypePopulated() {
        return typePopulated.get();
    }

    public void setTypePopulated(boolean value) {
        typePopulated.set(value);
    }

    public BooleanProperty typePopulatedProperty() {
        return typePopulated;
    }

    public boolean isUrlPopulated() {
        return urlPopulated.get();
    }

    public void setUrlPopulated(boolean value) {
        urlPopulated.set(value);
    }

    public BooleanProperty urlPopulatedProperty() {
        return urlPopulated;
    }

    public boolean isDescriptionPopulated() {
        return descriptionPopulated.get();
    }

    public void setDescriptionPopulated(boolean value) {
        descriptionPopulated.set(value);
    }

    public BooleanProperty descriptionPopulatedProperty() {
        return descriptionPopulated;
    }
}
