/*
 * Project written by: Tim Smith
 * 
 */
package schedulemanager.view_controller;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ChangeListener;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import schedulemanager.model.Appointment;
import schedulemanager.model.Customer;
import schedulemanager.model.User;
import static schedulemanager.utility.DateTimeHandler.*;
import static schedulemanager.view_controller.ApptFormException.*;

/**
 *
 * @author Tim Smith
 */
public class EditAppointmentInputManager extends InputManager{
    private Appointment appt;
    private Appointment originalAppt;
    
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
    
    // JavaFX Properties - Changed indicators
    private final BooleanProperty titleChanged = new SimpleBooleanProperty();
    private final BooleanProperty startDateChanged = new SimpleBooleanProperty();
    private final BooleanProperty startHourChanged = new SimpleBooleanProperty();
    private final BooleanProperty startMinuteChanged = new SimpleBooleanProperty();
    private final BooleanProperty startAmPmChanged = new SimpleBooleanProperty();
    private final BooleanProperty endDateChanged = new SimpleBooleanProperty();
    private final BooleanProperty endHourChanged = new SimpleBooleanProperty();
    private final BooleanProperty endMinuteChanged = new SimpleBooleanProperty();
    private final BooleanProperty endAmPmChanged = new SimpleBooleanProperty();
    private final BooleanProperty customerChanged = new SimpleBooleanProperty();
    private final BooleanProperty consultantChanged = new SimpleBooleanProperty();
    private final BooleanProperty contactChanged = new SimpleBooleanProperty();
    private final BooleanProperty locationChanged = new SimpleBooleanProperty();
    private final BooleanProperty typeChanged = new SimpleBooleanProperty();
    private final BooleanProperty urlChanged = new SimpleBooleanProperty();
    private final BooleanProperty descriptionChanged = new SimpleBooleanProperty();
    private final ObservableList<BooleanProperty> fieldChangeList = FXCollections.observableArrayList();
    private boolean startAlreadyChanged = false;
    private boolean endAlreadyChanged = false;
    
    // Change Listeners
    private ChangeListener<String> titleListener;
    private ChangeListener<LocalDate> startDateListener;
    private ChangeListener<String> startHourListener;
    private ChangeListener<String> startMinuteListener;
    private ChangeListener<String> startAmPmListener;
    private ChangeListener<LocalDate> endDateListener;
    private ChangeListener<String> endHourListener;
    private ChangeListener<String> endMinuteListener;
    private ChangeListener<String> endAmPmListener;
    private ChangeListener<Customer> customerListener;
    private ChangeListener<User> consultantListener;
    private ChangeListener<String> contactListener;
    private ChangeListener<String> locationListener;
    private ChangeListener<String> typeListener;
    private ChangeListener<String> urlListener;
    private ChangeListener<String> descriptionListener;

// Constructor    
    /**
     *  Creates an instance of EditAppointmentInputManager requiring an appointment as parameter.
     * @param appt
     */
    public EditAppointmentInputManager(Appointment appt) {
        this.appt = appt;
        initialize();
        setOriginalAppt();
    }

// Getters

    /**
     * Returns the current appointment being managed
     * @return
     */
    public Appointment getAppt() {
        return appt;
    }

// Setters    

    /**
     * Creates a duplicate of the appointment to allow for resetting of form changes
     */
    public final void setOriginalAppt() {
        originalAppt = new Appointment(appt.getAppointmentId(),
                                       appt.getCustomer(),
                                       appt.getUser(),
                                       appt.getTitle(),
                                       appt.getDescription(),
                                       appt.getLocation(),
                                       appt.getContact(),
                                       appt.getType(),
                                       appt.getUrl(),
                                       appt.getStart(),
                                       appt.getEnd(),
                                       appt.getCreateDate(),
                                       appt.getCreatedBy(),
                                       appt.getLastUpdate(),
                                       appt.getLastUpdatedBy());
    }
    
// Overridden Methods
    @Override
    public final void initialize() {
        
        title.set(appt.getTitle());
        startDate.set(appt.getStart().toLocalDate());
        startHour.set(appt.getStart().format(HOUR_ONLY));
        startMinute.set(appt.getStart().format(MINUTE_ONLY));
        startAmPm.set(appt.getStart().format(AMPM_ONLY));
        endDate.set(appt.getEnd().toLocalDate());
        endHour.set(appt.getEnd().format(HOUR_ONLY));
        endMinute.set(appt.getEnd().format(MINUTE_ONLY));
        endAmPm.set(appt.getEnd().format(AMPM_ONLY));
        customer.set(appt.getCustomer());
        consultant.set(appt.getUser());
        contact.set(appt.getContact());
        location.set(appt.getLocation());
        type.set(appt.getType());
        url.set(appt.getUrl());
        description.set(appt.getDescription());
        establishBindings();
        addAllChangeListeners();
        setFieldChangeList();
    }
    
    /**
     * Resets from to original appointment values
     */
    @Override
    public void clearAllChanges() {
        appt.setAppointmentId(originalAppt.getAppointmentId());
        appt.setCustomer(originalAppt.getCustomer());
        appt.setUser(originalAppt.getUser());
        appt.setTitle(originalAppt.getTitle());
        appt.setDescription(originalAppt.getDescription());
        appt.setLocation(originalAppt.getLocation());
        appt.setContact(originalAppt.getContact());
        appt.setType(originalAppt.getType());
        appt.setUrl(originalAppt.getUrl());
        appt.setStart(originalAppt.getStart());
        appt.setEnd(originalAppt.getEnd());
        appt.setCreateDate(originalAppt.getCreateDate());
        appt.setCreatedBy(originalAppt.getCreatedBy());
        appt.setLastUpdate(originalAppt.getLastUpdate());
        appt.setLastUpdatedBy(originalAppt.getLastUpdatedBy());
    }

    /**
     * Calls all the individual validators and returns a list of the ApptFormSceptions caught.
     * @return
     */
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
           for (int  i = 0; i < fieldChangeList.size(); i++) {
               if (fieldChangeList.get(i).get()) {
                   updateField(i);
               }
           }
        }
    }
    
    private void updateField(int fieldNo) {
        switch (fieldNo) {
            case 0:
                appt.setTitle(title.get());
                break;
            case 1:
            case 2:
            case 3:
            case 4:
                if (!startAlreadyChanged) {
                    appt.setStart(buildStartDateTime());
                    startAlreadyChanged = true;
                }
                break;
            case 5:
            case 6:
            case 7:
            case 8:
                if (!endAlreadyChanged) {
                    appt.setStart(buildStartDateTime());
                    endAlreadyChanged = true;
                }
                break;
            case 9:
                appt.setCustomer(customer.get());
                break;
            case 10:
                appt.setUser(consultant.get());
                break;
            case 11:
                appt.setContact(contact.get());
                break;
            case 12:
                appt.setLocation(location.get());
                break;
            case 13:
                appt.setType(type.get());
                break;
            case 14:
                appt.setUrl(url.get());
                break;
            case 15:
                appt.setDescription(description.get());
                break;
        }
    }
    
    /**
     * Creates compound binding for all value change properties.
     */
    public void establishBindings() {
        BooleanBinding anyFieldChanged = titleChanged
                                        .or(startDateChanged)
                                        .or(startHourChanged)
                                        .or(startMinuteChanged)
                                        .or(startAmPmChanged)
                                        .or(endDateChanged)
                                        .or(endHourChanged)
                                        .or(endMinuteChanged)
                                        .or(endAmPmChanged)
                                        .or(customerChanged)
                                        .or(consultantChanged)
                                        .or(contactChanged)
                                        .or(typeChanged)
                                        .or(locationChanged)
                                        .or(urlChanged)
                                        .or(descriptionChanged);
        changedProperty().bind(anyFieldChanged);
    }
    
    /**
     * Populates list of field changed indicators to loop through on save.
     * Changing this method will require changing the updateField method to altered indexes
     */
    public void setFieldChangeList() {
        fieldChangeList.add(titleChanged); // index 0
        fieldChangeList.add(startDateChanged); // index 1
        fieldChangeList.add(startHourChanged); // index 2
        fieldChangeList.add(startMinuteChanged); // index 3
        fieldChangeList.add(startAmPmChanged); // index 4
        fieldChangeList.add(endDateChanged); // index 5
        fieldChangeList.add(endHourChanged); // index 6
        fieldChangeList.add(endMinuteChanged); // index 7
        fieldChangeList.add(endAmPmChanged); // index 8
        fieldChangeList.add(customerChanged); // index 9
        fieldChangeList.add(consultantChanged); // index 10
        fieldChangeList.add(contactChanged); // index 11
        fieldChangeList.add(locationChanged); // index 12
        fieldChangeList.add(typeChanged); // index 13
        fieldChangeList.add(urlChanged); // index 14
        fieldChangeList.add(descriptionChanged); // index 15
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
    
// Validation Methods  TODO check string sizes

    /**
     * Validate title value for the following:
     *      -Not null
     *      -Does not begin with a space
     *      -Contains only letters, numbers, or white spaces
     * @throws ApptFormException
     */
    public void validateTitle() throws ApptFormException {
        
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
    }
    
    /**
     * Validate startDate for the following:
     *      -Not null
     *      -Not before current date (exclusive)
     * @throws ApptFormException
     */
    public void validateStartDate() throws ApptFormException {
        
        InputField inputField = InputField.START_TIME_DATE;
        // Check for empty
        if (startDate.get() == null) {
            throw new ApptFormException(inputField, "Start Date Required");
        }
        
        // Check for date prior to today
        if (startDate.get().isBefore(LocalDate.now())) {
            throw new ApptFormException(inputField, "Start Date cannot be before current date");
        }
    }
    
    public void validateStartHour() throws ApptFormException {
        
        InputField inputField = InputField.START_TIME_HOUR;
        // Check for empty
        if (startHour.get() == null) {
            throw new ApptFormException(inputField, "Start Hour Required");
        }
    }
    
    public void validateStartMinute() throws ApptFormException {
        
        InputField inputField = InputField.START_TIME_MIN;
        // Check for empty
        if (startMinute.get() == null) {
            throw new ApptFormException(inputField, "Start Minute Required");
        }
    }
    
    public void validateStartAmPm() throws ApptFormException {
        
        InputField inputField = InputField.START_TIME_AMPM;
        // Check for empty
        if (startAmPm.get() == null) {
            throw new ApptFormException(inputField, "Start AM/PM Required");
        }
    }
    
    public void validateEndDate() throws ApptFormException {
        
        InputField inputField = InputField.END_TIME_DATE;
        // Check for empty
        if (endDate.get() == null) {
            throw new ApptFormException(inputField, "End Date Required");
        }
        
        // Check for date prior to today
        if (endDate.get().isBefore(LocalDate.now())) {
            throw new ApptFormException(inputField, "End Date cannot be before current date");
        }
    }
    
    public void validateEndHour() throws ApptFormException {
        
        InputField inputField = InputField.END_TIME_HOUR;
        // Check for empty
        if (endHour.get() == null) {
            throw new ApptFormException(inputField, "Start Hour Required");
        }
    }
    
    public void validateEndMinute() throws ApptFormException {
        
        InputField inputField = InputField.END_TIME_MIN;
        // Check for empty
        if (endMinute.get() == null) {
            throw new ApptFormException(inputField, "Start Minute Required");
        }
    }
    
    public void validateEndAmPm() throws ApptFormException {
        
        InputField inputField = InputField.END_TIME_AMPM;
        // Check for empty
        if (endAmPm.get() == null) {
            throw new ApptFormException(inputField, "Start AM/PM Required");
        }
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
        
        InputField inputField = InputField.CUSTOMER_FIELD;
        if (customer.get() == null) {
            throw new ApptFormException(inputField, "Customer required");
        }
    }
    
    public void validateConsultant() throws ApptFormException {
        
        InputField inputField = InputField.CONSULTANT_FIELD;
        if (consultant.get() == null) {
            throw new ApptFormException(inputField, "Consultant required");
        }
    }
    
    public void validateContact() throws ApptFormException {
        
        InputField inputField = InputField.CONTACT_FIELD;
        if (contact.get() == null || contact.get().isEmpty()) {
            throw new ApptFormException(inputField, "Contact required");
        }
        if (contact.get().startsWith(" ")) {
            throw new ApptFormException(inputField, "Contact cannot begin with a space");
        }
    }
    
    public void validateLocation() throws ApptFormException {
        InputField inputField = InputField.LOCATION_FIELD;
        
        if (location.get() == null || location.get().isEmpty()) {
            throw new ApptFormException(inputField, "Location required");
        }
        if (location.get().startsWith(" ")) {
            throw new ApptFormException(inputField, "Location cannot begin with a space");
        }
    }
    
    public void validateType() throws ApptFormException {
        InputField inputField = InputField.TYPE_FIELD;
        
        if (type.get() == null || type.get().isEmpty()) {
            throw new ApptFormException(inputField, "Type required");
        }
        if (type.get().startsWith(" ")) {
            throw new ApptFormException(inputField, "Type cannot begin with a space");
        }
    }
    
    public void validateUrl() throws ApptFormException {
        InputField inputField = InputField.URL_FIELD;
        
        if (url.get() == null || url.get().isEmpty()) {
            throw new ApptFormException(inputField, "URL required");
        }
        if (url.get().startsWith(" ")) {
            throw new ApptFormException(inputField, "URL cannot begin with a space");
        }
    }
    
    public void validateDescription() throws ApptFormException {
        InputField inputField = InputField.DESCRIPTION_FIELD;
        
        if (description.get() == null || description.get().isEmpty()) {
            throw new ApptFormException(inputField, "Description required");
        }
        if (description.get().startsWith(" ")) {
            throw new ApptFormException(inputField, "Description cannot begin with a space");
        }
    }
    
    
            
// Change Listener Methods 
    public void addAllChangeListeners() {
        addTitleChangeListener();
        addStartDateChangeListener();
        addStartHourChangeListener();
        addStartMinuteChangeListener();
        addStartAmPmChangeListener();
        addEndDateChangeListener();
        addEndHourChangeListener();
        addEndMinuteChangeListener();
        addEndAmPmChangeListener();
        addCustomerChangeListener();
        addConsultantChangeListener();
        addContactChangeListener();
        addLocationChangeListener();
        addTypeChangeListener();
        addUrlChangeListener();
        addDescriptionChangeListener();
    }
    
    public void removeAllChangeListeners() {
        removeTitleChangeListener();
        removeStartDateChangeListener();
        removeStartHourChangeListener();
        removeStartMinuteChangeListener();
        removeStartAmPmChangeListener();
        removeEndDateChangeListener();
        removeEndHourChangeListener();
        removeEndMinuteChangeListener();
        removeEndAmPmChangeListener();
        removeCustomerChangeListener();
        removeConsultantChangeListener();
        removeContactChangeListener();
        removeLocationChangeListener();
        removeTypeChangeListener();
        removeUrlChangeListener();
        removeDescriptionChangeListener();
    }
    
    public void addTitleChangeListener() {
        if (titleListener == null) {
            titleListener = (observable, oldValue, newValue) -> {
                if (newValue.equals(originalAppt.getTitle())) {
                    setTitleChanged(false);
                } else {
                    setTitleChanged(true);
                }
            };
        }
        title.addListener(titleListener);
    }
    
    public void addStartDateChangeListener() {
        if (startDateListener == null) {
            startDateListener = (observable, oldValue, newValue) -> {
                if(newValue.isEqual(originalAppt.getStart().toLocalDate())) {
                    setStartDateChanged(false);
                } else {
                    setStartDateChanged(true);
                }
            };
        }
        startDate.addListener(startDateListener);
    }
    
    public void addStartHourChangeListener() {
        if (startHourListener == null) {
            startHourListener = (observable, oldValue, newValue) -> {
                if(newValue.equals(originalAppt.getStart().format(HOUR_ONLY))) {
                    setStartHourChanged(false);
                } else {
                    setStartHourChanged(true);
                }
            };
        }
        startHour.addListener(startHourListener);
    }
    
    public void addStartMinuteChangeListener() {
        if (startMinuteListener == null) {
            startMinuteListener = (observable, oldValue, newValue) -> {
                if(newValue.equals(originalAppt.getStart().format(MINUTE_ONLY))) {
                    setStartMinuteChanged(false);
                } else {
                    setStartMinuteChanged(true);
                }
            };
        }
        startMinute.addListener(startMinuteListener);
    }
    
    public void addStartAmPmChangeListener() {
        if (startAmPmListener == null) {
            startAmPmListener = (observable, oldValue, newValue) -> {
                if(newValue.equals(originalAppt.getStart().format(AMPM_ONLY))) {
                    setStartAmPmChanged(false);
                } else {
                    setStartAmPmChanged(true);
                }
            };
        }
        startAmPm.addListener(startAmPmListener);
    }
    
    public void addEndDateChangeListener() {
        if (endDateListener == null) {
            endDateListener = (observable, oldValue, newValue) -> {
                if(newValue.isEqual(originalAppt.getEnd().toLocalDate())) {
                    setEndDateChanged(false);
                } else {
                    setEndDateChanged(true);
                }
            };
        }
        endDate.addListener(endDateListener);
    }
    
    public void addEndHourChangeListener() {
        if (endHourListener == null) {
            endHourListener = (observable, oldValue, newValue) -> {
                if(newValue.equals(originalAppt.getEnd().format(HOUR_ONLY))) {
                    setEndHourChanged(false);
                } else {
                    setEndHourChanged(true);
                }
            };
        }
        endHour.addListener(endHourListener);
    }
    
    public void addEndMinuteChangeListener() {
        if (endMinuteListener == null) {
            endMinuteListener = (observable, oldValue, newValue) -> {
                if(newValue.equals(originalAppt.getEnd().format(MINUTE_ONLY))) {
                    setEndMinuteChanged(false);
                } else {
                    setEndMinuteChanged(true);
                }
            };
        }
        endMinute.addListener(endMinuteListener);
    }
    
    public void addEndAmPmChangeListener() {
        if (endAmPmListener == null) {
            endAmPmListener = (observable, oldValue, newValue) -> {
                if(newValue.equals(originalAppt.getEnd().format(AMPM_ONLY))) {
                    setEndAmPmChanged(false);
                } else {
                    setEndAmPmChanged(true);
                }
            };
        }
        endAmPm.addListener(endAmPmListener);
    }
    
    public void addCustomerChangeListener() {
        if (customerListener == null) {
            customerListener = (observable, oldValue, newValue) -> {
                if (newValue.equals(appt.getCustomer())) {
                    setCustomerChanged(false);
                } else {
                    setCustomerChanged(true);
                }
            };
        }
        customer.addListener(customerListener);
    }
    
    public void addConsultantChangeListener() {
        if (consultantListener == null) {
            consultantListener = (observable, oldValue, newValue) -> {
                if (newValue.equals(appt.getUser())) {
                    setConsultantChanged(false);
                } else {
                    setConsultantChanged(true);
                }
            };
        }
        consultant.addListener(consultantListener);
    }
    
    public void addContactChangeListener() {
        if (contactListener == null) {
            contactListener = (observable, oldValue, newValue) -> {
                if (newValue.equals(appt.getContact())) {
                    setContactChanged(false);
                } else {
                    setContactChanged(true);
                }
            };
        }
        contact.addListener(contactListener);
    }
    
    public void addLocationChangeListener() {
        if (locationListener == null) {
            locationListener = (observable, oldValue, newValue) -> {
                if (newValue.equals(appt.getLocation())) {
                    setLocationChanged(false);
                } else {
                    setLocationChanged(true);
                }
            };
        }
        location.addListener(locationListener);
    }
    
    public void addTypeChangeListener() {
        if (typeListener == null) {
            typeListener = (observable, oldValue, newValue) -> {
                if (newValue.equals(appt.getType())) {
                    setTypeChanged(false);
                } else {
                    setTypeChanged(true);
                }
            };
        }
        type.addListener(typeListener);
    }
    
    public void addUrlChangeListener() {
        if (urlListener == null) {
            urlListener = (observable, oldValue, newValue) -> {
                if (newValue.equals(appt.getUrl())) {
                    setUrlChanged(false);
                } else {
                    setUrlChanged(true);
                }
            };
        }
        url.addListener(urlListener);
    }
    
    public void addDescriptionChangeListener() {
        if (descriptionListener == null) {
            descriptionListener = (observable, oldValue, newValue) -> {
                if (newValue.equals(appt.getDescription())) {
                    setDescriptionChanged(false);
                } else {
                    setDescriptionChanged(true);
                }
            };
        }
        description.addListener(descriptionListener);
    }
    
    public void removeTitleChangeListener(){
        title.removeListener(titleListener);
    }
    
    public void removeStartDateChangeListener(){
        startDate.removeListener(startDateListener);
    }
    
    public void removeStartHourChangeListener(){
        startHour.removeListener(startHourListener);
    }
    
    public void removeStartMinuteChangeListener(){
        startMinute.removeListener(startMinuteListener);
    }
    
    public void removeStartAmPmChangeListener(){
        startAmPm.removeListener(startAmPmListener);
    }
    
    public void removeEndDateChangeListener(){
        endDate.removeListener(endDateListener);
    }
    
    public void removeEndHourChangeListener(){
        endHour.removeListener(endHourListener);
    }
    
    public void removeEndMinuteChangeListener(){
        endMinute.removeListener(endMinuteListener);
    }
    
    public void removeEndAmPmChangeListener(){
        endAmPm.removeListener(endAmPmListener);
    }
    
    public void removeCustomerChangeListener(){
        customer.removeListener(customerListener);
    }
    
    public void removeConsultantChangeListener(){
        consultant.removeListener(consultantListener);
    }
    
    public void removeContactChangeListener(){
        contact.removeListener(contactListener);
    }
    
    public void removeLocationChangeListener(){
        location.removeListener(locationListener);
    }
    
    public void removeTypeChangeListener(){
        type.removeListener(typeListener);
    }
    
    public void removeUrlChangeListener(){
        url.removeListener(urlListener);
    }
    
    public void removeDescriptionChangeListener(){
        description.removeListener(descriptionListener);
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

        public boolean isTitleChanged() {
        return titleChanged.get();
    }
    
    // Field Changed Properties
    public void setTitleChanged(boolean value) {
        titleChanged.set(value);
    }

    public BooleanProperty titleChangedProperty() {
        return titleChanged;
    }

    public boolean isStartDateChanged() {
        return startDateChanged.get();
    }

    public void setStartDateChanged(boolean value) {
        startDateChanged.set(value);
    }

    public BooleanProperty startDateChangedProperty() {
        return startDateChanged;
    }

    public boolean isStartHourChanged() {
        return startHourChanged.get();
    }

    public void setStartHourChanged(boolean value) {
        startHourChanged.set(value);
    }

    public BooleanProperty startHourChangedProperty() {
        return startHourChanged;
    }

    public boolean isStartMinuteChanged() {
        return startMinuteChanged.get();
    }

    public void setStartMinuteChanged(Boolean value) {
        startMinuteChanged.set(value);
    }

    public BooleanProperty startMinuteChangedProperty() {
        return startMinuteChanged;
    }

    public boolean isStartAmPmChanged() {
        return startAmPmChanged.get();
    }

    public void setStartAmPmChanged(boolean value) {
        startAmPmChanged.set(value);
    }

    public BooleanProperty startAmPmChangedProperty() {
        return startAmPmChanged;
    }
 
    public boolean isEndDateChanged() {
        return endDateChanged.get();
    }

    public void setEndDateChanged(boolean value) {
        endDateChanged.set(value);
    }

    public BooleanProperty endDateChangedProperty() {
        return endDateChanged;
    }

    public boolean isEndHourChanged() {
        return endHourChanged.get();
    }

    public void setEndHourChanged(boolean value) {
        endHourChanged.set(value);
    }

    public BooleanProperty endHourChangedProperty() {
        return endHourChanged;
    }

    public boolean isEndMinuteChanged() {
        return endMinuteChanged.get();
    }

    public void setEndMinuteChanged(boolean value) {
        endMinuteChanged.set(value);
    }

    public BooleanProperty endMinuteChangedProperty() {
        return endMinuteChanged;
    }

    public boolean isEndAmPmChanged() {
        return endAmPmChanged.get();
    }

    public void setEndAmPmChanged(boolean value) {
        endAmPmChanged.set(value);
    }

    public BooleanProperty endAmPmChangedProperty() {
        return endAmPmChanged;
    }

    public boolean isCustomerChanged() {
        return customerChanged.get();
    }

    public void setCustomerChanged(boolean value) {
        customerChanged.set(value);
    }

    public BooleanProperty customerChangedProperty() {
        return customerChanged;
    }

    public boolean isConsultantChanged() {
        return consultantChanged.get();
    }

    public void setConsultantChanged(boolean value) {
        consultantChanged.set(value);
    }

    public BooleanProperty consultantChangedProperty() {
        return consultantChanged;
    }

    public boolean isContactChanged() {
        return contactChanged.get();
    }

    public void setContactChanged(boolean value) {
        contactChanged.set(value);
    }

    public BooleanProperty contactChangedProperty() {
        return contactChanged;
    }

    public boolean isLocationChanged() {
        return locationChanged.get();
    }

    public void setLocationChanged(boolean value) {
        locationChanged.set(value);
    }

    public BooleanProperty locationChangedProperty() {
        return locationChanged;
    }
    
    public boolean isTypeChanged() {
        return typeChanged.get();
    }

    public void setTypeChanged(boolean value) {
        typeChanged.set(value);
    }

    public BooleanProperty typeChangedProperty() {
        return typeChanged;
    }

    public boolean isUrlChanged() {
        return urlChanged.get();
    }

    public void setUrlChanged(boolean value) {
        urlChanged.set(value);
    }

    public BooleanProperty urlChangedProperty() {
        return urlChanged;
    }

    public boolean isDescriptionChanged() {
        return descriptionChanged.get();
    }

    public void setDescriptionChanged(boolean value) {
        descriptionChanged.set(value);
    }

    public BooleanProperty descriptionChangedProperty() {
        return descriptionChanged;
    }

}
