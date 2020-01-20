/*
 * Project written by: Tim Smith
 * 
 */
package schedulemanager.view_controller;

import java.net.URL;
import java.util.ResourceBundle;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.value.ChangeListener;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import schedulemanager.model.Appointment;
import schedulemanager.model.Customer;
import schedulemanager.model.User;

/**
 * FXML Controller class
 *
 * @author Tim Smith
 */
public class AddAppointmentController implements Initializable {
// JavaFX Controls    
    // HBoxes
    @FXML HBox startTimeHBox;
    @FXML HBox endTimeHBox;
    
    // GridPanes
    @FXML GridPane formGridPane;
    
    // TextFields
    @FXML TextField titleField;
    @FXML TextField contactField;
    @FXML TextField locationField;
    @FXML TextField typeField;
    @FXML TextField urlField;

    
    // ChoiceBoxes
    @FXML ChoiceBox<Customer> customerChoiceBox;
    @FXML ChoiceBox<User> consultantChoiceBox;
    
    // TextAreas
    @FXML TextArea descriptionTextArea;
    
    // Buttons
    @FXML Button cancelButton;
    @FXML Button saveButton;
    @FXML Button clearFormButton;
    
    // DatePickers
    @FXML DatePicker startDatePicker;
    @FXML DatePicker endDatePicker;
    
    // Spinners
    @FXML Spinner<String> startHourSpinner;
    @FXML Spinner<String> startMinuteSpinner;
    @FXML Spinner<String> startAmPmSpinner;
    @FXML Spinner<String> endHourSpinner;
    @FXML Spinner<String> endMinuteSpinner;
    @FXML Spinner<String> endAmPmSpinner;
    
    // Labels
    @FXML Label titleErrorLabel;
    @FXML Label startTimeErrorLabel;
    @FXML Label endTimeErrorLabel;
    @FXML Label customerErrorLabel;
    @FXML Label consultantErrorLabel;
    @FXML Label contactErrorLabel;
    @FXML Label locationErrorLabel;
    @FXML Label typeErrorLabel;
    @FXML Label urlErrorLabel;
    @FXML Label descriptionErrorLabel;
    @FXML Label currentUserLabel;
    
    // Progress Indicator
    @FXML ProgressIndicator progressIndicator;

// Member variables   
    private ObservableList<String> hourList =FXCollections.observableArrayList();
    private ObservableList<String> minuteList =FXCollections.observableArrayList();
    private ObservableList<String> ampmList = FXCollections.observableArrayList();
    
    // ChangeListeners
    private ChangeListener<Boolean> titleFocusListener;
    private ChangeListener<Boolean> startDateFocusListener;
    private ChangeListener<Boolean> startHourFocusListener;
    private ChangeListener<Boolean> startMinuteFocusListener;
    private ChangeListener<Boolean> startAmPmFocusListener;
    private ChangeListener<Boolean> endDateFocusListener;
    private ChangeListener<Boolean> endHourFocusListener;
    private ChangeListener<Boolean> endMinuteFocusListener;
    private ChangeListener<Boolean> endAmPmFocusListener;
    private ChangeListener<Boolean> customerFocusListener;
    private ChangeListener<Boolean> consultantFocusListener;
    private ChangeListener<Boolean> contactFocusListener;
    private ChangeListener<Boolean> locationFocusListener;
    private ChangeListener<Boolean> typeFocusListener;
    private ChangeListener<Boolean> urlFocusListener;
    private ChangeListener<Boolean> descriptionFocusListener;
    
    private Session session = Session.getSession();
    
    private ExecutorService dbExecutor= Executors.newFixedThreadPool(1, new DBTask.DBThreadFactory());
    private DBTask dbTask = new DBTask();
    private DBTask.GetActiveCustomersTask getActiveCustomersChoiceBoxTask;
    private DBTask.GetActiveUsersTask getActiveConsultantsChoiceBoxTask;
    private DBTask.GetUserOverlappingAppointmentsTask getUserOverlappingAppointmentsTask;
    private DBTask.InsertAppointmentTask insertAppointmentTask;
    private AddAppointmentInputManager apptForm;
    private boolean apptAdded = false;
    private BooleanBinding anyFieldNotPopulated;
    
    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        currentUserLabel.setText(session.getSessionUser().getUserName());
        apptForm = new AddAppointmentInputManager(new Appointment());
        // Initialize tasks
        initializeGetActiveCustomersChoiceBoxTask();
        initializeGetActiveUsersChoiceBoxTask();
        initializeGetUserOverlappingAppointmentsTask();
        initializeInsertAppointmentTask();
        
        // Initialize Form Input Objects
        initializeSpinners();
        initializeCustomerChoiceBox();
        initializeConsultantChoiceBox();
        initializeDescriptionTextArea();

        // Set bindings
        setProgressIndicatorBindings();
        bindFormFieldsToFormManager();
        bindClearFormButtonDisabledToFormManager();
        bindSaveButtonDisabledToFormManager();
        startChangeListeners();

    }
    
    public void initializeGetActiveCustomersChoiceBoxTask() {
        getActiveCustomersChoiceBoxTask = dbTask.new GetActiveCustomersTask();
        ObservableList<Customer> customerChoices = FXCollections.observableArrayList();
        // Using lambda expression to provide code to run after the task succeeds. 
        // This simplifies the code and keeps it on location for debugging
        getActiveCustomersChoiceBoxTask.setOnSucceeded((event) -> {
            customerChoices.setAll(getActiveCustomersChoiceBoxTask.getValue());
            customerChoiceBox.setItems(customerChoices);
        });
        // Using lambda expression to provide code to run after the task fails. 
        // This simplifies the code and keeps it on location for debugging
        getActiveCustomersChoiceBoxTask.setOnFailed((event) -> {
            System.out.println(getActiveCustomersChoiceBoxTask.getException().getMessage());
        });
    }
    
    public void initializeGetActiveUsersChoiceBoxTask() {
        getActiveConsultantsChoiceBoxTask = dbTask.new GetActiveUsersTask();
        ObservableList<User> consultantChoices = FXCollections.observableArrayList();
        // Using lambda expression to provide code to run after the task succeeds. 
        // This simplifies the code and keeps it on location for debugging
        getActiveConsultantsChoiceBoxTask.setOnSucceeded((event) -> {
            consultantChoices.setAll(getActiveConsultantsChoiceBoxTask.getValue());
            consultantChoiceBox.setItems(consultantChoices);
            consultantChoiceBox.setValue(session.getSessionUser());
            apptForm.consultantPopulatedProperty().set(true);
        });
        // Using lambda expression to provide code to run after the task fails. 
        // This simplifies the code and keeps it on location for debugging
        getActiveConsultantsChoiceBoxTask.setOnFailed((event) -> {
            System.out.println(getActiveConsultantsChoiceBoxTask.getException().getMessage());
        });
    }
    
    public void initializeGetUserOverlappingAppointmentsTask() {
        getUserOverlappingAppointmentsTask = dbTask.new GetUserOverlappingAppointmentsTask();
        ObservableList<Appointment> overlappingAppts = FXCollections.observableArrayList();
        // Using lambda expression to provide code to run after the task succeeds. 
        // This simplifies the code and keeps it on location for debugging
        getUserOverlappingAppointmentsTask.setOnSucceeded((event) -> {
            apptForm.save();
            insertAppointmentTask.setAppt(apptForm.getAppt());
            dbExecutor.submit(insertAppointmentTask);
        });
        // Using lambda expression to provide code to run after the task fails. 
        // This simplifies the code and keeps it on location for debugging
        getUserOverlappingAppointmentsTask.setOnFailed((event) -> {
            overlappingAppts.setAll(getUserOverlappingAppointmentsTask.getOverlappingAppts());
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Overlapping Appointment");
            alert.setHeaderText("");
            alert.initModality(Modality.APPLICATION_MODAL);
            alert.setContentText("Submitted appointment times conflict with the following appointment.\n\n"
                                + overlappingAppts.get(0).toString() + "\n\n"
                                + "Please select different times or a different consultant.");
            alert.show();
        });
    }
    
    public void initializeInsertAppointmentTask() {
        insertAppointmentTask = dbTask.new InsertAppointmentTask();
        // Using lambda expression to provide code to run after the task succeeds. 
        // This simplifies the code and keeps it on location for debugging
        insertAppointmentTask.setOnSucceeded((event) -> {
            Stage window = (Stage) titleField.getScene().getWindow();
            apptAdded = true;
            window.close();
        });
        // Using lambda expression to provide code to run after the task fails. 
        // This simplifies the code and keeps it on location for debugging
        insertAppointmentTask.setOnFailed((event) -> {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Save Record Error");
            alert.setHeaderText(null);
            alert.setContentText("There was an error saving the record. Please try again later.\n" + 
                                 "If the problem persists please contact support.");
            alert.showAndWait();
        });
    }
    
    public void initializeSpinners() {
        hourList.addAll("8","9","10","11","12","1","2","3","4","5","6","7");
        SpinnerValueFactory<String> startHourValueFactory = new SpinnerValueFactory.ListSpinnerValueFactory<>(hourList);
        minuteList.addAll("00","15","30","45");
        SpinnerValueFactory<String> endHourValueFactory = new SpinnerValueFactory.ListSpinnerValueFactory<>(hourList);
        startHourValueFactory.setWrapAround(true);
        endHourValueFactory.setWrapAround(true);
        startHourSpinner.setValueFactory(startHourValueFactory);
        endHourSpinner.setValueFactory(endHourValueFactory);
        
        SpinnerValueFactory<String> startMinValueFactory = new SpinnerValueFactory.ListSpinnerValueFactory<>(minuteList);
        SpinnerValueFactory<String> endMinValueFactory = new SpinnerValueFactory.ListSpinnerValueFactory<>(minuteList);
        startMinValueFactory.setWrapAround(true);
        endMinValueFactory.setWrapAround(true);
        startMinuteSpinner.setValueFactory(startMinValueFactory);
        endMinuteSpinner.setValueFactory(endMinValueFactory);
        
        ampmList.add("AM");
        ampmList.add("PM");
        SpinnerValueFactory<String> startAmpmValueFactory = new SpinnerValueFactory.ListSpinnerValueFactory<>(ampmList);
        SpinnerValueFactory<String> endAmpmValueFactory = new SpinnerValueFactory.ListSpinnerValueFactory<>(ampmList);
        startAmpmValueFactory.setWrapAround(true);
        endAmpmValueFactory.setWrapAround(true);
        startAmPmSpinner.setValueFactory(startAmpmValueFactory);
        endAmPmSpinner.setValueFactory(endAmpmValueFactory);
    }
        
    public void initializeCustomerChoiceBox() {
        dbExecutor.submit(getActiveCustomersChoiceBoxTask);
    }
    
    public void initializeConsultantChoiceBox() {
        dbExecutor.submit(getActiveConsultantsChoiceBoxTask);
    }

    /**
     * Change behavior of tab key within description text area. Tab will focus next field, Ctrl+Tab will insert tab
     */
    public void initializeDescriptionTextArea() {
        // Using lambda to provide function to change effects of tab key on key event
        // Lambda simplifies the code rather than calling a function elsewhere.
        descriptionTextArea.addEventFilter(KeyEvent.KEY_PRESSED, (KeyEvent event) -> {
            KeyCode keyCode = event.getCode();
            
            if (keyCode == KeyCode.TAB && !event.isShiftDown() && !event.isControlDown()) {
                event.consume();
                Node node = (Node) event.getSource();
                KeyEvent newEvent = new KeyEvent(event.getSource(),
                        event.getTarget(), event.getEventType(),
                        event.getCharacter(), event.getText(),
                        event.getCode(), event.isShiftDown(),
                        true, event.isAltDown(),
                        event.isMetaDown());
                
                node.fireEvent(newEvent);
            }
        });
    }
    
    public boolean allStartFieldsPopulated() {
        return apptForm.isStartDatePopulated()
                && apptForm.isStartHourPopulated()
                && apptForm.isStartMinutePopulated()
                && apptForm.isStartAmPmPopulated();
    }
    
    public boolean allEndFieldsPopulated() {
        return apptForm.isEndDatePopulated()
                && apptForm.isEndHourPopulated()
                && apptForm.isEndMinutePopulated()
                && apptForm.isEndAmPmPopulated();
    }
    
    /**
     *  Returns true if form is state to be closed
     * @return
     */
    public boolean canExit() {
        return !apptForm.isChanged();
    }
    
    /**
     * Returns true if form added an appt
     * @return
     */
    public boolean wasApptAdded() {
        return apptAdded;
    }
    
    // JavaFX Event handlers
    @FXML
    public void handleCancelButton(ActionEvent event) {
        Stage window = (Stage) ((Node)event.getSource()).getScene().getWindow();
        
        if (!apptForm.isChanged()) {
            // Stop controller change listeners
            stopChangeListeners();
            window.close();
            return;
        }
        
        Alert alert = new  Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Unsaved Changes");
        alert.setHeaderText(null);
        alert.setContentText("There are unsaved changes, are you sure you want to exit without saving?\n" +
                             "Select Ok to exit or Cancel to return.");
        alert.showAndWait();
        if (alert.getResult().equals(ButtonType.OK)) {
            window.close();
        }
    }
    
    @FXML
    public void handleClearFormButton (ActionEvent event) {
        apptForm.clearAllChanges();
        customerChoiceBox.getSelectionModel().clearSelection();
        consultantChoiceBox.getSelectionModel().clearSelection();
    }
    
    @FXML
    public void handleSaveButton(ActionEvent event) {
        if (!anyFieldNotPopulated.get()) {
            ObservableList<ApptFormException> errorList = apptForm.validateAll();

            if (errorList.isEmpty()) {
                getUserOverlappingAppointmentsTask.setUser(apptForm.getConsultant());
                getUserOverlappingAppointmentsTask.setFromDate(apptForm.buildStartDateTime());
                getUserOverlappingAppointmentsTask.setToDate(apptForm.buildEndDateTime());
                getUserOverlappingAppointmentsTask.setNewAppt(true);
                dbExecutor.submit(getUserOverlappingAppointmentsTask);
            } else {
                // Using lambda expression to simplify the for loop iterating through the list
                errorList.forEach((inputErr) -> {
                    handleErrorListItem(inputErr);
                });
            }
        }
    }
    
    public void handleErrorListItem(ApptFormException inputErr) {
        switch (inputErr.getInputField()) {
            case TITLE_FIELD:
                if (!titleField.getStyleClass().contains("error-border")) {
                    titleField.getStyleClass().add("error-border");
                }
                titleErrorLabel.setText(inputErr.getMessage());
                break;
            case ALL_TIME_FIELDS:
                if (!startTimeHBox.getStyleClass().contains("error-border")) {
                    startTimeHBox.getStyleClass().add("error-border");
                }
                if (!endTimeHBox.getStyleClass().contains("error-border")) {
                    endTimeHBox.getStyleClass().add("error-border");
                }
                startTimeErrorLabel.setText(inputErr.getMessage());
                endTimeErrorLabel.setText(inputErr.getMessage());
                break;
            case START_TIME_FIELDS:
                if (!startTimeHBox.getStyleClass().contains("error-border")) {
                    startTimeHBox.getStyleClass().add("error-border");
                }
                startTimeErrorLabel.setText(inputErr.getMessage());
                break;
            case START_TIME_DATE:
                if (!startDatePicker.getStyleClass().contains("error-border")) {
                    startDatePicker.getStyleClass().add("error-border");
                }
                startTimeErrorLabel.setText(inputErr.getMessage());
                break;
            case START_TIME_HOUR:
                if (!startHourSpinner.getStyleClass().contains("error-border")) {
                    startHourSpinner.getStyleClass().add("error-border");
                }
                startTimeErrorLabel.setText(inputErr.getMessage());
                break;
            case START_TIME_MIN:
                if (!startMinuteSpinner.getStyleClass().contains("error-border")) {
                    startMinuteSpinner.getStyleClass().add("error-border");
                }
                startTimeErrorLabel.setText(inputErr.getMessage());
                break;
            case START_TIME_AMPM:
                if (!startAmPmSpinner.getStyleClass().contains("error-border")) {
                    startAmPmSpinner.getStyleClass().add("error-border");
                }
                startTimeErrorLabel.setText(inputErr.getMessage());
                break;
            case END_TIME_FIELDS:
                if (!endTimeHBox.getStyleClass().contains("error-border")) {
                    endTimeHBox.getStyleClass().add("error-border");
                }
                endTimeErrorLabel.setText(inputErr.getMessage());
                break;
            case END_TIME_DATE:
                if (!endDatePicker.getStyleClass().contains("error-border")) {
                    endDatePicker.getStyleClass().add("error-border");
                }
                endTimeErrorLabel.setText(inputErr.getMessage());
                break;
            case END_TIME_HOUR:
                if (!endHourSpinner.getStyleClass().contains("error-border")) {
                    endHourSpinner.getStyleClass().add("error-border");
                }
                endTimeErrorLabel.setText(inputErr.getMessage());
                break;
            case END_TIME_MIN:
                if (!endMinuteSpinner.getStyleClass().contains("error-border")) {
                    endMinuteSpinner.getStyleClass().add("error-border");
                }
                endTimeErrorLabel.setText(inputErr.getMessage());
                break;
            case END_TIME_AMPM:
                if (!endAmPmSpinner.getStyleClass().contains("error-border")) {
                    endAmPmSpinner.getStyleClass().add("error-border");
                }
                endTimeErrorLabel.setText(inputErr.getMessage());
                break;
            case CUSTOMER_FIELD:
                if (!customerChoiceBox.getStyleClass().contains("error-border")) {
                    customerChoiceBox.getStyleClass().add("error-border");
                }
                customerErrorLabel.setText(inputErr.getMessage());
                break;
            case CONSULTANT_FIELD:
                if (!consultantChoiceBox.getStyleClass().contains("error-border")) {
                    consultantChoiceBox.getStyleClass().add("error-border");
                }
                consultantErrorLabel.setText(inputErr.getMessage());
                break;
            case CONTACT_FIELD:
                if (!contactField.getStyleClass().contains("error-border")) {
                    contactField.getStyleClass().add("error-border");
                }
                contactErrorLabel.setText(inputErr.getMessage());
                break;
            case LOCATION_FIELD:
                if (!locationField.getStyleClass().contains("error-border")) {
                    locationField.getStyleClass().add("error-border");
                }
                locationErrorLabel.setText(inputErr.getMessage());
                break;
            case TYPE_FIELD:
                if (!typeField.getStyleClass().contains("error-border")) {
                    typeField.getStyleClass().add("error-border");
                }
                typeErrorLabel.setText(inputErr.getMessage());
                break;
            case URL_FIELD:
                if (!urlField.getStyleClass().contains("error-border")) {
                    urlField.getStyleClass().add("error-border");
                }
                urlErrorLabel.setText(inputErr.getMessage());
                break;
            case DESCRIPTION_FIELD:
                if (!descriptionTextArea.getStyleClass().contains("error-border")) {
                    descriptionTextArea.getStyleClass().add("error-border");
                }
                descriptionErrorLabel.setText(inputErr.getMessage());
                break;
        }
    }

    private void setProgressIndicatorBindings() {
        BooleanBinding progressIndicatorBinding = getActiveCustomersChoiceBoxTask.runningProperty()
                                                .or(getActiveConsultantsChoiceBoxTask.runningProperty())
                                                .or(getUserOverlappingAppointmentsTask.runningProperty())
                                                .or(insertAppointmentTask.runningProperty());
        
        progressIndicator.visibleProperty().bind(progressIndicatorBinding);
    }
    
    public void bindFormFieldsToFormManager() {
        titleField.textProperty().bindBidirectional(apptForm.titleProperty());
        startDatePicker.valueProperty().bindBidirectional(apptForm.startDateProperty());
        startHourSpinner.getValueFactory().valueProperty().bindBidirectional(apptForm.startHourProperty());
        startMinuteSpinner.getValueFactory().valueProperty().bindBidirectional(apptForm.startMinuteProperty());
        startAmPmSpinner.getValueFactory().valueProperty().bindBidirectional(apptForm.startAmPmProperty());
        endDatePicker.valueProperty().bindBidirectional(apptForm.endDateProperty());
        endHourSpinner.getValueFactory().valueProperty().bindBidirectional(apptForm.endHourProperty());
        endMinuteSpinner.getValueFactory().valueProperty().bindBidirectional(apptForm.endMinuteProperty());
        endAmPmSpinner.getValueFactory().valueProperty().bindBidirectional(apptForm.endAmPmProperty());
        customerChoiceBox.valueProperty().bindBidirectional(apptForm.customerProperty());
        consultantChoiceBox.valueProperty().bindBidirectional(apptForm.consultantProperty());
        contactField.textProperty().bindBidirectional(apptForm.contactProperty());
        locationField.textProperty().bindBidirectional(apptForm.locationProperty());
        typeField.textProperty().bindBidirectional(apptForm.typeProperty());
        urlField.textProperty().bindBidirectional(apptForm.urlProperty());
        descriptionTextArea.textProperty().bindBidirectional(apptForm.descriptionProperty());
    }
    
    public void bindClearFormButtonDisabledToFormManager() {
        BooleanBinding noFieldPopulated = apptForm.titlePopulatedProperty().not()
                                        .and(apptForm.startDatePopulatedProperty().not())
                                        .and(apptForm.startHourPopulatedProperty().not())
                                        .and(apptForm.startMinutePopulatedProperty().not())
                                        .and(apptForm.startAmPmPopulatedProperty().not())
                                        .and(apptForm.endDatePopulatedProperty().not())
                                        .and(apptForm.endHourPopulatedProperty().not())
                                        .and(apptForm.endMinutePopulatedProperty().not())
                                        .and(apptForm.endAmPmPopulatedProperty().not())
                                        .and(apptForm.customerPopulatedProperty().not())
                                        .and(apptForm.consultantPopulatedProperty().not())
                                        .and(apptForm.contactPopulatedProperty().not())
                                        .and(apptForm.typePopulatedProperty().not())
                                        .and(apptForm.locationPopulatedProperty().not())
                                        .and(apptForm.urlPopulatedProperty().not())
                                        .and(apptForm.descriptionPopulatedProperty().not());
        
        clearFormButton.disableProperty().bind(noFieldPopulated);
    }
    
    public void bindSaveButtonDisabledToFormManager() {
        anyFieldNotPopulated = apptForm.titlePopulatedProperty().not()
                                        .or(apptForm.startDatePopulatedProperty().not())
                                        .or(apptForm.startHourPopulatedProperty().not())
                                        .or(apptForm.startMinutePopulatedProperty().not())
                                        .or(apptForm.startAmPmPopulatedProperty().not())
                                        .or(apptForm.endDatePopulatedProperty().not())
                                        .or(apptForm.endHourPopulatedProperty().not())
                                        .or(apptForm.endMinutePopulatedProperty().not())
                                        .or(apptForm.endAmPmPopulatedProperty().not())
                                        .or(apptForm.customerPopulatedProperty().not())
                                        .or(apptForm.consultantPopulatedProperty().not())
                                        .or(apptForm.contactPopulatedProperty().not())
                                        .or(apptForm.typePopulatedProperty().not())
                                        .or(apptForm.locationPopulatedProperty().not())
                                        .or(apptForm.urlPopulatedProperty().not())
                                        .or(apptForm.descriptionPopulatedProperty().not());
    }
    
    public void startChangeListeners() {
        startTitleFocusListener();
        startStartDateFocusListener();
        startStartHourFocusListener();
        startStartMinuteFocusListener();
        startStartAmPmFocusListener();
        startEndDateFocusListener();
        startEndHourFocusListener();
        startEndMinuteFocusListener();
        startEndAmPmFocusListener();
        startCustomerFocusListener();
        startConsultantFocusListener();
        startContactFocusListener();
        startLocationFocusListener();
        startTypeFocusListener();
        startUrlFocusListener();
        startDescriptionFocusListener();
    }
    
    public void stopChangeListeners() {
        stopTitleFocusListener();
        stopStartDateFocusListener();
        stopStartHourFocusListener();
        stopStartMinuteFocusListener();
        stopStartAmPmFocusListener();
        stopEndDateFocusListener();
        stopEndHourFocusListener();
        stopEndMinuteFocusListener();
        stopEndAmPmFocusListener();
        stopCustomerFocusListener();
        stopConsultantFocusListener();
        stopContactFocusListener();
        stopLocationFocusListener();
        stopTypeFocusListener();
        stopUrlFocusListener();
        stopDescriptionFocusListener();
    }
    // Using lambda expressions in all the cange listeners. Makes the code easier to read and keeps the logic close
    // to the event that called it.
    public void startTitleFocusListener() {
        if (titleFocusListener == null) {
            titleFocusListener = (observable, oldValue, newValue) -> {
                if (!newValue) {
                    try {
                        apptForm.validateTitle();
                        titleErrorLabel.setText("");
                        titleField.getStyleClass().removeAll("error-border");
                    } catch (ApptFormException ex) {
                        handleErrorListItem(ex);
                        
                    }
                }
            };
        }
        titleField.focusedProperty().addListener(titleFocusListener);
    }
    
    public void startStartDateFocusListener() {
        if (startDateFocusListener == null) {
            startDateFocusListener = (observable, oldValue, newValue) -> {
                if (!newValue) {
                    try {
                        apptForm.validateStartDate();
                        startTimeErrorLabel.setText("");
                        startDatePicker.getStyleClass().removeAll("error-border");
                    } catch (ApptFormException ex) {
                        handleErrorListItem(ex);
                        return;
                    }

                    if (allStartFieldsPopulated()){
                        try {
                            apptForm.validateStartDateTime();
                            startHourSpinner.getStyleClass().removeAll("error-border");
                            startMinuteSpinner.getStyleClass().removeAll("error-border");
                            startAmPmSpinner.getStyleClass().removeAll("error-border");
                            startTimeHBox.getStyleClass().removeAll("error-border");
                            startTimeErrorLabel.setText("");
                        } catch (ApptFormException ex) {
                            handleErrorListItem(ex);
                            return;
                        }
                    }
                    if (allEndFieldsPopulated() && allStartFieldsPopulated()){
                        try {
                            apptForm.validateDateTimes();
                            startHourSpinner.getStyleClass().removeAll("error-border");
                            startMinuteSpinner.getStyleClass().removeAll("error-border");
                            startAmPmSpinner.getStyleClass().removeAll("error-border");
                            startTimeHBox.getStyleClass().removeAll("error-border");
                            endHourSpinner.getStyleClass().removeAll("error-border");
                            endMinuteSpinner.getStyleClass().removeAll("error-border");
                            endAmPmSpinner.getStyleClass().removeAll("error-border");
                            endTimeHBox.getStyleClass().removeAll("error-border");
                            startTimeErrorLabel.setText("");
                            endTimeErrorLabel.setText("");
                        } catch (ApptFormException ex) {
                            handleErrorListItem(ex);
                        }
                    }
                }
            };
        }
        startDatePicker.focusedProperty().addListener(startDateFocusListener);
    }
    
    public void startStartHourFocusListener() {
        if (startHourFocusListener == null) {
            startHourFocusListener = (observable, oldValue, newValue) -> {
                if (!newValue) {
                    try {
                        apptForm.validateStartHour();
                        startHourSpinner.getStyleClass().removeAll("error-border");
                        startTimeErrorLabel.setText("");
                    } catch (ApptFormException ex) {
                        handleErrorListItem(ex);
                        return;
                    }
                    if (allStartFieldsPopulated()){
                        try {
                            apptForm.validateStartDateTime();
                            startDatePicker.getStyleClass().removeAll("error-border");
                            startHourSpinner.getStyleClass().removeAll("error-border");
                            startMinuteSpinner.getStyleClass().removeAll("error-border");
                            startAmPmSpinner.getStyleClass().removeAll("error-border");
                            startTimeHBox.getStyleClass().removeAll("error-border");
                            startTimeErrorLabel.setText("");
                        } catch (ApptFormException ex) {
                            handleErrorListItem(ex);
                            return;
                        }
                    }
                    if (allEndFieldsPopulated() && allStartFieldsPopulated()){
                        try {
                            apptForm.validateDateTimes();
                            startDatePicker.getStyleClass().removeAll("error-border");
                            startHourSpinner.getStyleClass().removeAll("error-border");
                            startMinuteSpinner.getStyleClass().removeAll("error-border");
                            startAmPmSpinner.getStyleClass().removeAll("error-border");
                            startTimeHBox.getStyleClass().removeAll("error-border");
                            endHourSpinner.getStyleClass().removeAll("error-border");
                            endMinuteSpinner.getStyleClass().removeAll("error-border");
                            endAmPmSpinner.getStyleClass().removeAll("error-border");
                            endTimeHBox.getStyleClass().removeAll("error-border");
                            startTimeErrorLabel.setText("");
                            endTimeErrorLabel.setText("");
                        } catch (ApptFormException ex) {
                            handleErrorListItem(ex);
                        }
                    }
                }
            };
        }
        startHourSpinner.focusedProperty().addListener(startHourFocusListener);
    }
    
    public void startStartMinuteFocusListener() {
        if (startMinuteFocusListener == null) {
            startMinuteFocusListener = (observable, oldValue, newValue) -> {
                if (!newValue) {
                    try {
                        apptForm.validateStartMinute();
                        startMinuteSpinner.getStyleClass().removeAll("error-border");
                        startTimeErrorLabel.setText("");
                    } catch (ApptFormException ex) {
                        handleErrorListItem(ex);
                        return;
                    }
                    if (allStartFieldsPopulated()){
                        try {
                            apptForm.validateStartDateTime();
                            startDatePicker.getStyleClass().removeAll("error-border");
                            startHourSpinner.getStyleClass().removeAll("error-border");
                            startMinuteSpinner.getStyleClass().removeAll("error-border");
                            startAmPmSpinner.getStyleClass().removeAll("error-border");
                            startTimeHBox.getStyleClass().removeAll("error-border");
                            startTimeErrorLabel.setText("");
                        } catch (ApptFormException ex) {
                            handleErrorListItem(ex);
                            return;
                        }
                    }
                    if (allStartFieldsPopulated()  && allEndFieldsPopulated()){
                        try {
                            apptForm.validateDateTimes();
                            startDatePicker.getStyleClass().removeAll("error-border");
                            startHourSpinner.getStyleClass().removeAll("error-border");
                            startMinuteSpinner.getStyleClass().removeAll("error-border");
                            startAmPmSpinner.getStyleClass().removeAll("error-border");
                            startTimeHBox.getStyleClass().removeAll("error-border");
                            endHourSpinner.getStyleClass().removeAll("error-border");
                            endMinuteSpinner.getStyleClass().removeAll("error-border");
                            endAmPmSpinner.getStyleClass().removeAll("error-border");
                            endTimeHBox.getStyleClass().removeAll("error-border");
                            startTimeErrorLabel.setText("");
                            endTimeErrorLabel.setText("");
                        } catch (ApptFormException ex) {
                            handleErrorListItem(ex);
                        }
                    }
                }
            };
        }
        startMinuteSpinner.focusedProperty().addListener(startMinuteFocusListener);
    }
    
    public void startStartAmPmFocusListener() {
        if (startAmPmFocusListener == null) {
            startAmPmFocusListener = (observable, oldValue, newValue) -> {
                if (!newValue) {
                    try {
                        apptForm.validateStartAmPm();
                        startAmPmSpinner.getStyleClass().removeAll("error-border");
                        startTimeErrorLabel.setText("");
                    } catch (ApptFormException ex) {
                        handleErrorListItem(ex);
                        return;
                    }
                    if (allStartFieldsPopulated()){
                        try {
                            apptForm.validateStartDateTime();
                            startDatePicker.getStyleClass().removeAll("error-border");
                            startHourSpinner.getStyleClass().removeAll("error-border");
                            startMinuteSpinner.getStyleClass().removeAll("error-border");
                            startAmPmSpinner.getStyleClass().removeAll("error-border");
                            startTimeHBox.getStyleClass().removeAll("error-border");
                            startTimeErrorLabel.setText("");
                        } catch (ApptFormException ex) {
                            handleErrorListItem(ex);
                            return;
                        }
                    }
                    if (allStartFieldsPopulated()  && allEndFieldsPopulated()){
                        try {
                            apptForm.validateDateTimes();
                            startDatePicker.getStyleClass().removeAll("error-border");
                            startHourSpinner.getStyleClass().removeAll("error-border");
                            startMinuteSpinner.getStyleClass().removeAll("error-border");
                            startAmPmSpinner.getStyleClass().removeAll("error-border");
                            startTimeHBox.getStyleClass().removeAll("error-border");
                            endHourSpinner.getStyleClass().removeAll("error-border");
                            endMinuteSpinner.getStyleClass().removeAll("error-border");
                            endAmPmSpinner.getStyleClass().removeAll("error-border");
                            endTimeHBox.getStyleClass().removeAll("error-border");
                            startTimeErrorLabel.setText("");
                            endTimeErrorLabel.setText("");
                        } catch (ApptFormException ex) {
                            handleErrorListItem(ex);
                        }
                    }
                }
            };
        }
        startAmPmSpinner.focusedProperty().addListener(startAmPmFocusListener);
    }
    
    public void startEndDateFocusListener() {
        if (endDateFocusListener == null) {
            endDateFocusListener = (observable, oldValue, newValue) -> {
                if (!newValue) {
                    try {
                        apptForm.validateEndDate();
                        endDatePicker.getStyleClass().removeAll("error-border");
                        endTimeErrorLabel.setText("");
                    } catch (ApptFormException ex) {
                        handleErrorListItem(ex);
                        return;
                    }
                    if (allEndFieldsPopulated()){
                        try {
                            apptForm.validateEndDateTime();
                            endDatePicker.getStyleClass().removeAll("error-border");
                            endHourSpinner.getStyleClass().removeAll("error-border");
                            endMinuteSpinner.getStyleClass().removeAll("error-border");
                            endAmPmSpinner.getStyleClass().removeAll("error-border");
                            endTimeHBox.getStyleClass().removeAll("error-border");
                            endTimeErrorLabel.setText("");
                        } catch (ApptFormException ex) {
                            handleErrorListItem(ex);
                            return;
                        }
                    }
                    if (allStartFieldsPopulated()  && allEndFieldsPopulated()){
                        try {
                            apptForm.validateDateTimes();
                            startDatePicker.getStyleClass().removeAll("error-border");
                            startHourSpinner.getStyleClass().removeAll("error-border");
                            startMinuteSpinner.getStyleClass().removeAll("error-border");
                            startAmPmSpinner.getStyleClass().removeAll("error-border");
                            startTimeHBox.getStyleClass().removeAll("error-border");
                            endDatePicker.getStyleClass().removeAll("error-border");
                            endHourSpinner.getStyleClass().removeAll("error-border");
                            endMinuteSpinner.getStyleClass().removeAll("error-border");
                            endAmPmSpinner.getStyleClass().removeAll("error-border");
                            endTimeHBox.getStyleClass().removeAll("error-border");
                            startTimeErrorLabel.setText("");
                            endTimeErrorLabel.setText("");
                        } catch (ApptFormException ex) {
                            handleErrorListItem(ex);
                        }
                    }
                }
            };
        }
        endDatePicker.focusedProperty().addListener(endDateFocusListener);
    }
    
    public void startEndHourFocusListener() {
        if (endHourFocusListener == null) {
            endHourFocusListener = (observable, oldValue, newValue) -> {
                if (!newValue) {
                    try {
                        apptForm.validateEndHour();
                        endHourSpinner.getStyleClass().removeAll("error-border");
                        endTimeErrorLabel.setText("");
                    } catch (ApptFormException ex) {
                        handleErrorListItem(ex);
                        return;
                    }
                    if (allEndFieldsPopulated()){
                        try {
                            apptForm.validateEndDateTime();
                            endDatePicker.getStyleClass().removeAll("error-border");
                            endHourSpinner.getStyleClass().removeAll("error-border");
                            endMinuteSpinner.getStyleClass().removeAll("error-border");
                            endAmPmSpinner.getStyleClass().removeAll("error-border");
                            endTimeHBox.getStyleClass().removeAll("error-border");
                            endTimeErrorLabel.setText("");
                        } catch (ApptFormException ex) {
                            handleErrorListItem(ex);
                            return;
                        }
                    }
                    if (allStartFieldsPopulated()  && allEndFieldsPopulated()){
                        try {
                            apptForm.validateDateTimes();
                            startDatePicker.getStyleClass().removeAll("error-border");
                            startHourSpinner.getStyleClass().removeAll("error-border");
                            startMinuteSpinner.getStyleClass().removeAll("error-border");
                            startAmPmSpinner.getStyleClass().removeAll("error-border");
                            startTimeHBox.getStyleClass().removeAll("error-border");
                            endDatePicker.getStyleClass().removeAll("error-border");
                            endHourSpinner.getStyleClass().removeAll("error-border");
                            endMinuteSpinner.getStyleClass().removeAll("error-border");
                            endAmPmSpinner.getStyleClass().removeAll("error-border");
                            endTimeHBox.getStyleClass().removeAll("error-border");
                            startTimeErrorLabel.setText("");
                            endTimeErrorLabel.setText("");
                        } catch (ApptFormException ex) {
                            handleErrorListItem(ex);
                        }
                    }
                }
            };
        }
        endHourSpinner.focusedProperty().addListener(endHourFocusListener);
    }
    
    public void startEndMinuteFocusListener() {
        if (endMinuteFocusListener == null) {
            endMinuteFocusListener = (observable, oldValue, newValue) -> {
                if (!newValue) {
                    try {
                        apptForm.validateEndMinute();
                        endMinuteSpinner.getStyleClass().removeAll("error-border");
                        endTimeErrorLabel.setText("");
                    } catch (ApptFormException ex) {
                        handleErrorListItem(ex);
                        return;
                    }
                    if (allEndFieldsPopulated()){
                        try {
                            apptForm.validateEndDateTime();
                            endDatePicker.getStyleClass().removeAll("error-border");
                            endHourSpinner.getStyleClass().removeAll("error-border");
                            endMinuteSpinner.getStyleClass().removeAll("error-border");
                            endAmPmSpinner.getStyleClass().removeAll("error-border");
                            endTimeHBox.getStyleClass().removeAll("error-border");
                            endTimeErrorLabel.setText("");
                        } catch (ApptFormException ex) {
                            handleErrorListItem(ex);
                            return;
                        }
                    }
                    if (allStartFieldsPopulated()  && allEndFieldsPopulated()){
                        try {
                            apptForm.validateDateTimes();
                            startDatePicker.getStyleClass().removeAll("error-border");
                            startHourSpinner.getStyleClass().removeAll("error-border");
                            startMinuteSpinner.getStyleClass().removeAll("error-border");
                            startAmPmSpinner.getStyleClass().removeAll("error-border");
                            startTimeHBox.getStyleClass().removeAll("error-border");
                            endDatePicker.getStyleClass().removeAll("error-border");
                            endHourSpinner.getStyleClass().removeAll("error-border");
                            endMinuteSpinner.getStyleClass().removeAll("error-border");
                            endAmPmSpinner.getStyleClass().removeAll("error-border");
                            endTimeHBox.getStyleClass().removeAll("error-border");
                            startTimeErrorLabel.setText("");
                            endTimeErrorLabel.setText("");
                        } catch (ApptFormException ex) {
                            handleErrorListItem(ex);
                        }
                    }
                }
            };
        }
        endMinuteSpinner.focusedProperty().addListener(endMinuteFocusListener);
    }
    
    public void startEndAmPmFocusListener() {
        if (endAmPmFocusListener == null) {
            endAmPmFocusListener = (observable, oldValue, newValue) -> {
                if (!newValue) {
                    try {
                        apptForm.validateEndAmPm();
                        endAmPmSpinner.getStyleClass().removeAll("error-border");
                        endTimeErrorLabel.setText("");
                    } catch (ApptFormException ex) {
                        handleErrorListItem(ex);
                        return;
                    }
                    if (allEndFieldsPopulated()){
                        try {
                            apptForm.validateEndDateTime();
                            endDatePicker.getStyleClass().removeAll("error-border");
                            endHourSpinner.getStyleClass().removeAll("error-border");
                            endMinuteSpinner.getStyleClass().removeAll("error-border");
                            endAmPmSpinner.getStyleClass().removeAll("error-border");
                            endTimeHBox.getStyleClass().removeAll("error-border");
                            endTimeErrorLabel.setText("");
                        } catch (ApptFormException ex) {
                            handleErrorListItem(ex);
                            return;
                        }
                    }
                    if (allStartFieldsPopulated()  && allEndFieldsPopulated()){
                        try {
                            apptForm.validateDateTimes();
                            startDatePicker.getStyleClass().removeAll("error-border");
                            startHourSpinner.getStyleClass().removeAll("error-border");
                            startMinuteSpinner.getStyleClass().removeAll("error-border");
                            startAmPmSpinner.getStyleClass().removeAll("error-border");
                            startTimeHBox.getStyleClass().removeAll("error-border");
                            endDatePicker.getStyleClass().removeAll("error-border");
                            endHourSpinner.getStyleClass().removeAll("error-border");
                            endMinuteSpinner.getStyleClass().removeAll("error-border");
                            endAmPmSpinner.getStyleClass().removeAll("error-border");
                            endTimeHBox.getStyleClass().removeAll("error-border");
                            startTimeErrorLabel.setText("");
                            endTimeErrorLabel.setText("");
                        } catch (ApptFormException ex) {
                            handleErrorListItem(ex);
                        }
                    }
                }
            };
        }
        endAmPmSpinner.focusedProperty().addListener(endAmPmFocusListener);
    }
    
    public void startCustomerFocusListener() {
        if (customerFocusListener == null) {
            customerFocusListener = (observable, oldValue, newValue) -> {
                if (!newValue) {
                    try {
                        apptForm.validateCustomer();
                        customerChoiceBox.getStyleClass().removeAll("error-border");
                        customerErrorLabel.setText("");
                    } catch (ApptFormException ex) {
                        handleErrorListItem(ex);
                    }
                }
            };
        }
        customerChoiceBox.focusedProperty().addListener(customerFocusListener);
    }
    
    public void startConsultantFocusListener() {
        if (consultantFocusListener == null) {
            consultantFocusListener = (observable, oldValue, newValue) -> {
                if (!newValue) {
                    try {
                        apptForm.validateConsultant();
                        consultantChoiceBox.getStyleClass().removeAll("error-border");
                        consultantErrorLabel.setText("");
                    } catch (ApptFormException ex) {
                        handleErrorListItem(ex);
                    }
                }
            };
        }
        consultantChoiceBox.focusedProperty().addListener(consultantFocusListener);
    }
    
    public void startContactFocusListener() {
        if (contactFocusListener == null) {
            contactFocusListener = (observable, oldValue, newValue) -> {
                if (!newValue) {
                    try {
                        apptForm.validateContact();
                        contactField.getStyleClass().removeAll("error-border");
                        contactErrorLabel.setText("");
                    } catch (ApptFormException ex) {
                        handleErrorListItem(ex);
                    }
                }
            };
        }
        contactField.focusedProperty().addListener(contactFocusListener);
    }
    
    public void startLocationFocusListener() {
        if (locationFocusListener == null) {
            locationFocusListener = (observable, oldValue, newValue) -> {
                if (!newValue) {
                    try {
                        apptForm.validateLocation();
                        locationField.getStyleClass().removeAll("error-border");
                        locationErrorLabel.setText("");
                    } catch (ApptFormException ex) {
                        handleErrorListItem(ex);
                    }
                }
            };
        }
        locationField.focusedProperty().addListener(locationFocusListener);
    }
    
    public void startTypeFocusListener() {
        if (typeFocusListener == null) {
            typeFocusListener = (observable, oldValue, newValue) -> {
                if (!newValue) {
                    try {
                        apptForm.validateType();
                        typeField.getStyleClass().removeAll("error-border");
                        typeErrorLabel.setText("");
                    } catch (ApptFormException ex) {
                        handleErrorListItem(ex);
                    }
                }
            };
        }
        typeField.focusedProperty().addListener(typeFocusListener);
    }
    
    public void startUrlFocusListener() {
        if (urlFocusListener == null) {
            urlFocusListener = (observable, oldValue, newValue) -> {
                if (!newValue) {
                    try {
                        apptForm.validateUrl();
                        urlField.getStyleClass().removeAll("error-border");
                        urlErrorLabel.setText("");
                    } catch (ApptFormException ex) {
                        handleErrorListItem(ex);
                    }
                }
            };
        }
        urlField.focusedProperty().addListener(urlFocusListener);
    }
    
    public void startDescriptionFocusListener() {
        if (descriptionFocusListener == null) {
            descriptionFocusListener = (observable, oldValue, newValue) -> {
                if (!newValue) {
                    try {
                        apptForm.validateDescription();
                        descriptionTextArea.getStyleClass().removeAll("error-border");
                        descriptionErrorLabel.setText("");
                    } catch (ApptFormException ex) {
                        handleErrorListItem(ex);
                    }
                }
            };
        }
        descriptionTextArea.focusedProperty().addListener(descriptionFocusListener);
    }
    
    public void stopTitleFocusListener() {
        titleField.focusedProperty().removeListener(titleFocusListener);
    }
    
    public void stopStartDateFocusListener() {
        startDatePicker.focusedProperty().removeListener(startDateFocusListener);
    }
    
    public void stopStartHourFocusListener() {
        startHourSpinner.focusedProperty().removeListener(startHourFocusListener);
    }
    
    public void stopStartMinuteFocusListener() {
        startMinuteSpinner.focusedProperty().removeListener(startMinuteFocusListener);
    }
    
    public void stopStartAmPmFocusListener() {
        startAmPmSpinner.focusedProperty().removeListener(startAmPmFocusListener);
    }

    public void stopEndDateFocusListener() {
        endDatePicker.focusedProperty().removeListener(endDateFocusListener);
    }
    
    public void stopEndHourFocusListener() {
        endHourSpinner.focusedProperty().removeListener(endHourFocusListener);
    }
    
    public void stopEndMinuteFocusListener() {
        endMinuteSpinner.focusedProperty().removeListener(endMinuteFocusListener);
    }
    
    public void stopEndAmPmFocusListener() {
        endAmPmSpinner.focusedProperty().removeListener(endAmPmFocusListener);
    }
    
    public void stopCustomerFocusListener() {
        customerChoiceBox.focusedProperty().removeListener(customerFocusListener);
    }
    
    public void stopConsultantFocusListener() {
        consultantChoiceBox.focusedProperty().removeListener(consultantFocusListener);
    }
    
    public void stopContactFocusListener() {
        contactField.focusedProperty().removeListener(contactFocusListener);
    }
    
    public void stopLocationFocusListener() {
        locationField.focusedProperty().removeListener(locationFocusListener);
    }
    
    public void stopTypeFocusListener() {
        typeField.focusedProperty().removeListener(typeFocusListener);
    }
    
    public void stopUrlFocusListener() {
        urlField.focusedProperty().removeListener(urlFocusListener);
    }
    
    public void stopDescriptionFocusListener() {
        descriptionTextArea.focusedProperty().removeListener(descriptionFocusListener);
    }
    
}
