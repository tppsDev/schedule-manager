/*
 * Project written by: Tim Smith
 * 
 */
package schedulemanager.view_controller;

import java.net.URL;
import java.time.LocalDateTime;
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
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.RadioButton;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleGroup;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import schedulemanager.model.Appointment;
import schedulemanager.model.Customer;
import schedulemanager.model.User;
import schedulemanager.view_controller.DBTask.GetActiveCustomersTask;
import schedulemanager.view_controller.DBTask.GetActiveUsersTask;
import schedulemanager.view_controller.DBTask.GetUserOverlappingAppointmentsTask;
import schedulemanager.view_controller.DBTask.UpdateAppointmentTask;
import static schedulemanager.utility.DateTimeHandler.*;

/**
 * FXML ViewModifyAppointmentController class
 *
 * @author Tim Smith
 */
public class ViewModifyAppointmentController implements Initializable {
    // HBoxes
    @FXML private HBox startTimeHBox;
    @FXML private HBox endTimeHBox;
    
    // GridPanes
    @FXML private GridPane formGridPane;
    
    // TextFields
    @FXML private TextField titleField;
    @FXML private TextField contactField;
    @FXML private TextField locationField;
    @FXML private TextField typeField;
    @FXML private TextField urlField;
    @FXML private TextField customerField;
    @FXML private TextField consultantField;
    
    // ChoiceBoxes
    @FXML private ChoiceBox<Customer> customerChoiceBox;
    @FXML private ChoiceBox<User> consultantChoiceBox;
    
    // TextAreas
    @FXML private TextArea descriptionTextArea;
    
    // Buttons
    @FXML private Button cancelButton;
    @FXML private Button saveButton;
    
    // RadioButtons
    @FXML private RadioButton viewRadioButton;
    @FXML private RadioButton editRadioButton;
    
    // DatePickers
    @FXML private DatePicker startDatePicker;
    @FXML private DatePicker endDatePicker;
    
    // Spinners
    @FXML private Spinner<String> startHourSpinner;
    @FXML private Spinner<String> startMinuteSpinner;
    @FXML private Spinner<String> startAmPmSpinner;
    @FXML private Spinner<String> endHourSpinner;
    @FXML private Spinner<String> endMinuteSpinner;
    @FXML private Spinner<String> endAmPmSpinner;
    
    // Labels
    @FXML private Label titleErrorLabel;
    @FXML private Label startTimeErrorLabel;
    @FXML private Label endTimeErrorLabel;
    @FXML private Label customerErrorLabel;
    @FXML private Label consultantErrorLabel;
    @FXML private Label contactErrorLabel;
    @FXML private Label locationErrorLabel;
    @FXML private Label typeErrorLabel;
    @FXML private Label urlErrorLabel;
    @FXML private Label descriptionErrorLabel;
    @FXML private Label unsavedChangesLabel;
    @FXML private Label lastUpdatedLabel;
    @FXML private Label lastUpdatedByLabel;
    
    // Progress Indicator
    @FXML private ProgressIndicator progressIndicator;

    private Appointment appointment;
    private Customer formCustomer;
    private User formConsultant;

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
    
    private ExecutorService dbExecutor= Executors.newFixedThreadPool(1, new DBTask.DBThreadFactory());
    private DBTask dbTask = new DBTask();
    private GetActiveCustomersTask getActiveCustomersChoiceBoxTask;
    private GetActiveUsersTask getActiveConsultantsChoiceBoxTask;
    private GetUserOverlappingAppointmentsTask getUserOverlappingAppointmentsTask;
    private UpdateAppointmentTask updateAppointmentTask;
    private EditAppointmentInputManager apptForm;
    private boolean apptChanged = false;
    
    @FXML private ToggleGroup viewEditToggleGroup = new ToggleGroup();
    /**
     * Initializes the controller class.
     * @param url
     * @param rb
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // Initialize tasks
        initializeGetActiveCustomersChoiceBoxTask();
        initializeGetActiveUsersChoiceBoxTask();
        initializeGetUserOverlappingAppointmentsTask();
        initializeUpdateAppointmentTask();
        
        // Set toggle group
        viewRadioButton.setToggleGroup(viewEditToggleGroup);
        editRadioButton.setToggleGroup(viewEditToggleGroup);
        viewEditToggleGroup.selectToggle(viewRadioButton);
        
        // Bindings
        cancelButton.visibleProperty().bind(editRadioButton.selectedProperty());
        saveButton.visibleProperty().bind(editRadioButton.selectedProperty());
        formGridPane.disableProperty().bind(viewRadioButton.selectedProperty());
        setProgressIndicatorBindings();
        
                
        // Initialize Form Input Objects
        initializeSpinners();
        initializeCustomerChoiceBox();
        initializeConsultantChoiceBox();
        initializeDescriptionTextArea();
    }

    /**
     *  Establishes appointment to be viewed or edited and initializes field values
     * @param appointment
     * @param initialRun
     */
    public void setAppointment(Appointment appointment, boolean initialRun) {
        this.appointment = appointment;
        if (!initialRun) {
            stopChangeListeners();
        }            

        customerField.toFront();
        consultantField.toFront();
        // Populate fields & footer labels
        titleField.setText(appointment.getTitle());
        
        formCustomer = appointment.getCustomer();
        formConsultant = appointment.getUser();
        customerField.setText(formCustomer.getCustomerName());
        consultantField.setText(formConsultant.getUserName());
        contactField.setText(appointment.getContact());
        locationField.setText(appointment.getLocation());
        typeField.setText(appointment.getType());
        urlField.setText(appointment.getUrl());
        descriptionTextArea.setText(appointment.getDescription());
        
        lastUpdatedLabel.setText(appointment.getLastUpdate().format(DATE_TIME_STAMP));
        lastUpdatedByLabel.setText(appointment.getLastUpdatedBy());
        
        // Handle start and end times
        startDatePicker.setValue(appointment.getStart().toLocalDate());
        endDatePicker.setValue(appointment.getEnd().toLocalDate());
        startHourSpinner.getValueFactory().setValue(appointment.getStart().format(HOUR_ONLY));
        startMinuteSpinner.getValueFactory().setValue(appointment.getStart().format(MINUTE_ONLY));
        startAmPmSpinner.getValueFactory().setValue(appointment.getStart().format(AMPM_ONLY));
        endHourSpinner.getValueFactory().setValue(appointment.getEnd().format(HOUR_ONLY));
        endMinuteSpinner.getValueFactory().setValue(appointment.getEnd().format(MINUTE_ONLY));
        endAmPmSpinner.getValueFactory().setValue(appointment.getEnd().format(AMPM_ONLY));
        
        if (initialRun) {
            apptForm = new EditAppointmentInputManager(appointment);
            saveButton.disableProperty().bind((apptForm.changedProperty()).not());
            unsavedChangesLabel.visibleProperty().bind(apptForm.changedProperty());
            bindFormFieldsToFormManager();
        }
        
        startChangeListeners();

    }
    
    public void initializeGetActiveCustomersChoiceBoxTask() {
        getActiveCustomersChoiceBoxTask = dbTask.new GetActiveCustomersTask();
        ObservableList<Customer> customerChoices = FXCollections.observableArrayList();
        getActiveCustomersChoiceBoxTask.setOnSucceeded((event) -> {
            customerChoices.setAll(getActiveCustomersChoiceBoxTask.getValue());
            customerChoiceBox.getItems().clear();
            customerChoiceBox.getItems().addAll(customerChoices);
            if (formCustomer.isActive()) {
                customerChoiceBox.getSelectionModel().select(formCustomer);
            }
        });
        getActiveCustomersChoiceBoxTask.setOnFailed((event) -> {
            System.out.println(getActiveCustomersChoiceBoxTask.getException().getMessage());
        });
    }
    
    public void initializeGetActiveUsersChoiceBoxTask() {
        getActiveConsultantsChoiceBoxTask = dbTask.new GetActiveUsersTask();
        ObservableList<User> consultantChoices = FXCollections.observableArrayList();
        getActiveConsultantsChoiceBoxTask.setOnSucceeded((event) -> {
            consultantChoices.setAll(getActiveConsultantsChoiceBoxTask.getValue());
            consultantChoiceBox.getItems().clear();
            consultantChoiceBox.getItems().addAll(consultantChoices);
            if (formConsultant.isActive()) {
                consultantChoiceBox.getSelectionModel().select(formConsultant);
            }
        });
        getActiveConsultantsChoiceBoxTask.setOnFailed((event) -> {
            System.out.println(getActiveConsultantsChoiceBoxTask.getException().getMessage());
        });
    }
    
    public void initializeGetUserOverlappingAppointmentsTask() {
        getUserOverlappingAppointmentsTask = dbTask.new GetUserOverlappingAppointmentsTask();
        ObservableList<Appointment> overlappingAppts = FXCollections.observableArrayList();
        getUserOverlappingAppointmentsTask.setOnSucceeded((event) -> {
            apptForm.save();
            updateAppointmentTask.setAppt(appointment);
            dbExecutor.submit(updateAppointmentTask);
        });
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
    
    public void initializeUpdateAppointmentTask() {
        updateAppointmentTask = dbTask.new UpdateAppointmentTask();
        
        updateAppointmentTask.setOnSucceeded((event) -> {
            apptChanged = true;
            Stage window = (Stage) titleField.getScene().getWindow();
            window.close();
        });
        updateAppointmentTask.setOnFailed((event) -> {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Update Error");
            alert.setHeaderText(null);
            alert.setContentText("There was an error updating the record. Please try again later.\n" + 
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
    
    // JavaFX Event handlers
    @FXML
    public void handleViewEditRadioButtonChange() {
        if (viewRadioButton.isSelected()) {
            if (apptForm.isChanged()) {
                Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                alert.setTitle("Unsaved Changes Warning");
                alert.setHeaderText(null);
                alert.setContentText("Switching to View mode will diregard current changes to appointment data." 
                        + "\n Please select Ok to continue or select Cancel to return to Edit mode");
                alert.showAndWait();
                if (alert.getResult() == ButtonType.CANCEL) {
                    viewEditToggleGroup.selectToggle(editRadioButton);
                    editRadioButton.requestFocus();
                    return;
                }
            }
            // TODO fix this
            setAppointment(appointment, false);
        } else {
            StringBuilder errMessage = new StringBuilder("You are about to edit an appointment that will require " + 
                    "changing the following invalid data:\n\n");
            boolean errorFound = false;
            // TODO consider border color for alerting
            if (appointment.getStart().isBefore(LocalDateTime.now()) || 
                    appointment.getEnd().isBefore(LocalDateTime.now())) {
                errorFound = true;
                errMessage.append("\t* Appoint time has passed.\n");
            }

            if (!appointment.getCustomer().isActive()) {
                errorFound = true;
                errMessage.append("\t* Inactive Customer.\n");
            } else {
                customerChoiceBox.getSelectionModel()
                    .select(formCustomer);
            }
            
            if (!appointment.getUser().isActive()) {
                errorFound = true;
                errMessage.append("\t* Inactive Consultant.\n");
            } else {
                consultantChoiceBox.getSelectionModel()
                    .select(formConsultant);
            }
            errMessage.append("\nPlease select OK to continue, or select Cancel to return to View Mode.");
            if (errorFound) {
                Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                alert.setTitle("Invalid Appointment Data");
                alert.setHeaderText(null);
                alert.setContentText(errMessage.toString());
                alert.showAndWait();
                if (alert.getResult().equals(ButtonType.CANCEL)) {
                    viewEditToggleGroup.selectToggle(viewRadioButton);
                    viewRadioButton.requestFocus();
                }
                return;
            }
            customerChoiceBox.toFront();
            consultantChoiceBox.toFront();
            titleField.requestFocus();
        }
    }
    
    @FXML
    public void handleCancelButton(ActionEvent event) {
        Stage window = (Stage) ((Node)event.getSource()).getScene().getWindow();
        
        if (!apptForm.isChanged()) {
            // Stop form manager listeners
            apptForm.removeAllChangeListeners();
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
    public void handleSaveButton(ActionEvent event) {
        if (!apptForm.isChanged()) {
            apptChanged = false;
            Stage window = (Stage) titleField.getScene().getWindow();
            window.close();
            return;
        }
        
        ObservableList<ApptFormException> errorList = apptForm.validateAll();
        
        if (errorList.isEmpty()) {
            getUserOverlappingAppointmentsTask.setUser(formConsultant);
            getUserOverlappingAppointmentsTask.setFromDate(apptForm.buildStartDateTime());
            getUserOverlappingAppointmentsTask.setToDate(apptForm.buildEndDateTime());
            getUserOverlappingAppointmentsTask.setAppt(appointment);
            dbExecutor.submit(getUserOverlappingAppointmentsTask);
        } else {
            errorList.forEach((inputErr) -> {
                handleErrorListItem(inputErr);
            });
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
    
    public void setProgressIndicatorBindings() {
        BooleanBinding progressIndicatorBinding = getActiveCustomersChoiceBoxTask.runningProperty().or
                                                 (getActiveConsultantsChoiceBoxTask.runningProperty()).or
                                                 (getUserOverlappingAppointmentsTask.runningProperty()).or
                                                 (updateAppointmentTask.runningProperty());
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
        Scene scene = startDatePicker.getScene();
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
                    
                    if (!scene.getFocusOwner().getParent().getId().equals("startTimeHBox")){
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
                    if (!scene.getFocusOwner().getParent().getId().equals("endTimeHBox")){
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
        Scene scene = startHourSpinner.getScene();
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
                    if (!scene.getFocusOwner().getParent().getId().equals("startTimeHBox")){
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
                    if (!scene.getFocusOwner().getParent().getId().equals("endTimeHBox")){
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
        Scene scene = startMinuteSpinner.getScene();
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
                    if (!scene.getFocusOwner().getParent().getId().equals("startTimeHBox")){
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
                    if (!scene.getFocusOwner().getParent().getId().equals("endTimeHBox")){
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
        Scene scene = startAmPmSpinner.getScene();
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
                    if (!scene.getFocusOwner().getParent().getId().equals("startTimeHBox")){
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
                    if (!scene.getFocusOwner().getParent().getId().equals("endTimeHBox")){
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
        Scene scene = endDatePicker.getScene();
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
                    if (!scene.getFocusOwner().getParent().getId().equals("endTimeHBox")){
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
                    if (!scene.getFocusOwner().getParent().getId().equals("startTimeHBox")){
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
        Scene scene = endHourSpinner.getScene();
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
                    if (!scene.getFocusOwner().getParent().getId().equals("endTimeHBox")){
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
                    if (!scene.getFocusOwner().getParent().getId().equals("startTimeHBox")){
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
        Scene scene = endMinuteSpinner.getScene();
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
                    if (!scene.getFocusOwner().getParent().getId().equals("endTimeHBox")){
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
                    if (!scene.getFocusOwner().getParent().getId().equals("startTimeHBox")){
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
        Scene scene = endAmPmSpinner.getScene();
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
                    if (!scene.getFocusOwner().getParent().getId().equals("endTimeHBox")){
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
                    if (!scene.getFocusOwner().getParent().getId().equals("startTimeHBox")){
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
    
    public boolean isApptChanged() {
        return apptChanged;
    }
    
    
}
