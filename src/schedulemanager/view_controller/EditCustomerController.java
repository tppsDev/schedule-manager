/*
 * Project written by: Tim Smith
 * 
 */
package schedulemanager.view_controller;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import schedulemanager.model.Address;
import schedulemanager.model.City;
import schedulemanager.model.Customer;
import schedulemanager.view_controller.DBServiceManager.GetAllCitiesService;
import schedulemanager.view_controller.DBServiceManager.GetJustAddedAddrService;
import schedulemanager.view_controller.DBServiceManager.InsertAddressService;
import schedulemanager.view_controller.DBServiceManager.UpdateAddressService;
import schedulemanager.view_controller.DBServiceManager.UpdateCustomerService;

/**
 * FXML Controller class
 *
 * @author Tim Smith
 */
public class EditCustomerController implements Initializable {
    @FXML private Label currentUserLabel;
    @FXML private Button selectAddrButton;
    @FXML private Button addCityButton;
    @FXML private Button cancelButton;
    @FXML private Button submitButton;
    @FXML private RadioButton addAddrRadioButton;
    @FXML private RadioButton editAddrRadioButton;
    @FXML private RadioButton selectAddrRadioButton;
    @FXML private CheckBox activeCheckBox;
    @FXML private ChoiceBox<City> cityChoiceBox;
    @FXML private Label addr2ErrorLabel;
    @FXML private Label addrErrorLabel;
    @FXML private Label cityErrorLabel;
    @FXML private Label customerNameErrorLabel;
    @FXML private Label phoneErrorLabel;
    @FXML private Label postalCodeErrorLabel;
    @FXML private TextField address2TextField;
    @FXML private TextField addressTextField;
    @FXML private TextField customerNameTextField;
    @FXML private TextField phoneTextField;
    @FXML private TextField postalCodeTextField;
    @FXML private TextArea displayAddressTextArea;
    @FXML private TextField displayPhoneTextField;
    @FXML private VBox addAddressVBox;
    @FXML private VBox selectAddressVBox;
    @FXML private ToggleGroup addressToggleGroup;
    @FXML private ProgressIndicator progressIndicator;
    
    private BooleanProperty customerNameValidated = new SimpleBooleanProperty(true);
    private BooleanProperty addressValidated = new SimpleBooleanProperty(true);
    private BooleanProperty newAddressValidated = new SimpleBooleanProperty(true);
    private BooleanProperty newCityValidated = new SimpleBooleanProperty(true);
    private BooleanProperty newPostalCodeValidated = new SimpleBooleanProperty(true);
    private BooleanProperty newPhoneValidated = new SimpleBooleanProperty(true);

    
    private Session session = Session.getSession();
    private Customer customer;
    private DBServiceManager dbServiceMgr = new DBServiceManager();
    private GetAllCitiesService getAllCitiesService = dbServiceMgr.new GetAllCitiesService();
    private UpdateCustomerService updateCustomerService = dbServiceMgr.new UpdateCustomerService();
    private InsertAddressService insertAddressService = dbServiceMgr.new InsertAddressService();
    private UpdateAddressService updateAddressService = dbServiceMgr.new UpdateAddressService();
    private GetJustAddedAddrService getJustAddedAddrService = dbServiceMgr.new GetJustAddedAddrService();
    private Address selectedAddress;
    private Address addedAddress;
    private Address editedAddress;
    private final BooleanProperty selectedAddressExists = new SimpleBooleanProperty(false);
    private BooleanBinding allFieldsValidated;
    private boolean customerLoaded = false;
    private boolean customerChanged = false;
    private boolean stopListening = false;
    
    /**
     * Initializes the controller class.
     * @param url
     * @param rb
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        currentUserLabel.setText(session.getSessionUser().getUserName());
        establishBindings();
        initGetAllCitiesService();
        initInsertAddressService();
        initUpdateAddressService();
        initUpdateCustomerService();
        initGetJustAddedAddrService();
        runGetAllCitiesService();
        addAddressVBox.toFront();
        startChangeListeners();
        
    }
    
    /**
     *  Enables parent stage to see if a user was added so it can handle as needed
     * @return
     */
    public boolean wasCustomerChanged() {
        return customerChanged;
    }

    /**
     * Allow calling controller to retrieve customer that was edited
     * @return
     */
    public Customer getCustomer() {
        return customer;
    }

    /**
     * Allow calling controller to pass the customer to be edited
     * @param customer
     */
    public void setCustomer(Customer customer) {
        this.customer = customer;
        customerLoaded = true;
        loadCustomer();
    }
    
    
    
    private void establishBindings() {
        displayPhoneTextField.visibleProperty().bind(selectedAddressExists);
        displayAddressTextArea.visibleProperty().bind(selectedAddressExists);
        bindProgressIndicator();
        bindSubmitToValidatedFields();
    }
    
    private void bindSubmitToValidatedFields() {
        BooleanBinding newAddressFieldsValidated = newAddressValidated
                                              .and(newCityValidated)
                                              .and(newPostalCodeValidated)
                                              .and(newPhoneValidated);
        
        allFieldsValidated = customerNameValidated
                                            .and(
                                                    (addressValidated)
                                                 .or
                                                    (newAddressFieldsValidated)
                                            );
    }
    
    private void bindProgressIndicator() {
        BooleanBinding serviceRunning = getAllCitiesService.runningProperty()
                .or(updateAddressService.runningProperty())
                .or(insertAddressService.runningProperty())
                .or(updateCustomerService.runningProperty())
                .or(getJustAddedAddrService.runningProperty());
                
        progressIndicator.visibleProperty().bind(serviceRunning);
    }
    
    private void populateSelectedAddrFields() {
        displayPhoneTextField.setText(selectedAddress.getPhone());
        displayAddressTextArea.setText(selectedAddress.toString());
    }
    
    private void updateCustomer() {
        customer.setCustomerName(customerNameTextField.getText());
        customer.setActive(activeCheckBox.isSelected());
        if (selectAddrRadioButton.isSelected()) {
            customer.setAddress(selectedAddress);
            runUpdateCustomerService();
        } 
        if (addAddrRadioButton.isSelected()) {
            addedAddress = new Address(
                                        addressTextField.getText(),
                                        address2TextField.getText(),
                                        cityChoiceBox.getValue(),
                                        postalCodeTextField.getText(),
                                        phoneTextField.getText());
            runInsertAddresservice();
        }
        if (editAddrRadioButton.isSelected()) {
            editedAddress.setAddress(addressTextField.getText());
            editedAddress.setAddress2(address2TextField.getText());
            editedAddress.setCity(cityChoiceBox.getValue());
            editedAddress.setPostalCode(postalCodeTextField.getText());
            editedAddress.setPhone(phoneTextField.getText());
            runUpdateAddresservice();
        }
    }
    
    private void loadCustomer() {
        if (customerLoaded) {
            customerNameTextField.setText(customer.getCustomerName());
            activeCheckBox.selectedProperty().set(customer.isActive());
            phoneTextField.setText(customer.getAddress().getPhone());
            addressTextField.setText(customer.getAddress().getAddress());
            address2TextField.setText(customer.getAddress().getAddress2());
            cityChoiceBox.setValue(customer.getAddress().getCity());
            postalCodeTextField.setText(customer.getAddress().getPostalCode());
            editedAddress = customer.getAddress();
        }
    }
    
    private void clearAddressFields() {
        stopListening = true;
        phoneTextField.setText("");
        addressTextField.setText("");
        address2TextField.setText("");
        cityChoiceBox.getSelectionModel().clearSelection();
        postalCodeTextField.setText("");
        phoneTextField.setText("");
        stopListening = false;
    }
    
    private void reloadInitialAddress() {
        phoneTextField.setText(customer.getAddress().getPhone());
        addressTextField.setText(customer.getAddress().getAddress());
        address2TextField.setText(customer.getAddress().getAddress2());
        cityChoiceBox.setValue(customer.getAddress().getCity());
        postalCodeTextField.setText(customer.getAddress().getPostalCode());
    }
    
// JavaFX Control Event Handlers
    @FXML
    private void handleAddOrSelectRadioButtons() {
        if (editAddrRadioButton.isSelected()) {
            addAddressVBox.toFront();
            reloadInitialAddress();
        }
        if (addAddrRadioButton.isSelected()) {
            addAddressVBox.toFront();
            clearAddressFields();
            runGetAllCitiesService();
        } 
        if (selectAddrRadioButton.isSelected()){
            selectAddressVBox.toFront();
        }
    }
    
    @FXML
    private void handleSelectAddrButton() {
        FXMLLoader selectAddressLoader = new FXMLLoader(getClass()
                            .getResource("SelectAddress.fxml"));
        Scene selectAddressScene;
        try {
            selectAddressScene = new Scene(selectAddressLoader.load());
        } catch (IOException ex) {
            System.out.println(ex.getMessage());
            return;
        }
        Stage selectAddressStage = new Stage();
        selectAddressStage.initOwner(currentUserLabel.getScene().getWindow());
        selectAddressStage.initModality(Modality.APPLICATION_MODAL);
        selectAddressStage.setTitle("Schedule Manager 1.0 - Select Address");
        SelectAddressController selectAddressController = selectAddressLoader.getController();
        selectAddressStage.setScene(selectAddressScene);
        selectAddressStage.showAndWait();
        if (selectAddressController.wasSelectionMade()) {
            selectedAddress = selectAddressController.getSelectedAddress();
            selectedAddressExists.set(true);
            addressValidated.set(true);
            populateSelectedAddrFields();
        }
    }
    
    @FXML
    private void handleAddCityButton() {
        FXMLLoader manageCityCountryLoader = new FXMLLoader(getClass()
                            .getResource("ManageCitiesCountries.fxml"));
        Scene manageCityCountryScene;
        try {
            manageCityCountryScene = new Scene(manageCityCountryLoader.load());
        } catch (IOException ex) {
            System.out.println(ex.getMessage());

            return;
        }
        Stage manageCityCountryStage = new Stage();
        manageCityCountryStage.initOwner(currentUserLabel.getScene().getWindow());
        manageCityCountryStage.initModality(Modality.APPLICATION_MODAL);
        manageCityCountryStage.setTitle("Schedule Manager 1.0 - Manage Cities & Countries");
        ManageCitiesCountriesController mccController = manageCityCountryLoader.getController();
        manageCityCountryStage.setOnCloseRequest(new EventHandler<WindowEvent>() {
            @Override
            public void handle(WindowEvent event) {
                if (!mccController.canExit()) {
                    event.consume();
                }
            }
        });
        manageCityCountryStage.setScene(manageCityCountryScene);
        manageCityCountryStage.showAndWait();
        if (mccController.cityWasAdded()) {
            runGetAllCitiesService();
        }
    }

    @FXML
    private void handleCancelButton() {
        Stage stage = (Stage) currentUserLabel.getScene().getWindow();
        stage.close();
    }
    
    @FXML
    private void handleSubmitButton() {
        if (allFieldsValidated.get()) {
            updateCustomer();
        }
    }

// Change Listeners
    private void startChangeListeners() {
        customerNameTextField.focusedProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue && !addCityButton.isFocused() && !stopListening) {
                if (customerNameTextField.getText() != null && !customerNameTextField.getText().isEmpty()) {
                    customerNameValidated.set(true);
                    customerNameErrorLabel.setText("");
                    customerNameTextField.getStyleClass().removeAll("error-border");
                } else {
                    customerNameValidated.set(false);
                    customerNameErrorLabel.setText("Customer name required");
                    
                    if (!customerNameTextField.getStyleClass().contains("error-border")) {
                        customerNameTextField.getStyleClass().add("error-border");
                    }
                }
            }
        });
        
        phoneTextField.focusedProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue && !selectAddrRadioButton.isSelected() && !addCityButton.isFocused() && !stopListening) {
                if (phoneTextField.getText() != null && !phoneTextField.getText().isEmpty()) {
                    newPhoneValidated.set(true);
                    phoneErrorLabel.setText("");
                    phoneTextField.getStyleClass().removeAll("error-border");
                } else {
                    newPhoneValidated.set(false);
                    phoneErrorLabel.setText("Phone number required");
                    
                    if (phoneTextField.getStyleClass().contains("error-border")) {
                        phoneTextField.getStyleClass().add("error-border");
                    }
                }
            }
        });
        
        addressTextField.focusedProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue && !selectAddrRadioButton.isSelected() && !addCityButton.isFocused() && !stopListening) {
                if (addressTextField.getText() != null && !addressTextField.getText().isEmpty()) {
                    newAddressValidated.set(true);
                    addrErrorLabel.setText("");
                    addressTextField.getStyleClass().removeAll("error-border");
                } else {
                    newAddressValidated.set(false);
                    addrErrorLabel.setText("Address is required");
                    
                    if (addressTextField.getStyleClass().contains("error-border")) {
                        addressTextField.getStyleClass().add("error-border");
                    }    
                }
            }
        });
        
        cityChoiceBox.focusedProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue && !selectAddrRadioButton.isSelected() && !addCityButton.isFocused() && !stopListening) {
                if (!cityChoiceBox.getSelectionModel().isEmpty()) {
                    newCityValidated.set(true);
                    cityErrorLabel.setText("");
                    cityChoiceBox.getStyleClass().removeAll("error-label");
                } else {
                    newCityValidated.set(false);
                    cityErrorLabel.setText("City required");
                    
                    if (cityChoiceBox.getStyleClass().contains("error-label")) {
                        cityChoiceBox.getStyleClass().removeAll("error-label");
                    }
                }
            }
        });
        
        postalCodeTextField.focusedProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue && !selectAddrRadioButton.isSelected() && !addCityButton.isFocused() && !stopListening) {
                if (postalCodeTextField.getText() != null && !postalCodeTextField.getText().isEmpty()) {
                    newPostalCodeValidated.set(true);
                    postalCodeErrorLabel.setText("");
                    postalCodeTextField.getStyleClass().removeAll("error-border");
                } else {
                    newPostalCodeValidated.set(false);
                    postalCodeErrorLabel.setText("Postal code is required");
                    
                    if (postalCodeTextField.getStyleClass().contains("error-border")) {
                        postalCodeTextField.getStyleClass().add("error-border");
                    }    
                }
            }
        });
    }
    
// Service Methods
    private void initGetAllCitiesService() {
        getAllCitiesService.setOnSucceeded((event) -> {
            cityChoiceBox.setItems(getAllCitiesService.getValue());
        });
        
        getAllCitiesService.setOnFailed((event) -> {
            System.out.println(event.getSource().getException().getMessage());
        });
    }
    
    private void initInsertAddressService() {
        insertAddressService.setOnSucceeded((event) -> {
            runGetJustAddedAddrService();
        });
        
        insertAddressService.setOnFailed((event) -> {
            System.out.println(event.getSource().getException().getMessage());
        });
    }
    
    private void initUpdateAddressService() {
        updateAddressService.setOnSucceeded((event) -> {
            runUpdateCustomerService();
        });
        
        updateAddressService.setOnFailed((event) -> {
            System.out.println(event.getSource().getException().getMessage());
        });
    }
    
    private void initUpdateCustomerService() {
        updateCustomerService.setOnSucceeded((event) -> {
            customerChanged = true;
            Stage stage = (Stage) currentUserLabel.getScene().getWindow();
            stage.close();
        });
        
        updateCustomerService.setOnFailed((event) -> {
            System.out.println(event.getSource().getException().getMessage());
        });
    }
    
    private void initGetJustAddedAddrService() {
        getJustAddedAddrService.setOnSucceeded((event) -> {
            addedAddress = getJustAddedAddrService.getValue();
            customer.setAddress(addedAddress);
            runUpdateCustomerService();
        });
        
        getJustAddedAddrService.setOnFailed((event) -> {
            System.out.println(event.getSource().getException().getMessage());
        });
    }
    
    private void runGetAllCitiesService() {
        if (!getAllCitiesService.isRunning()) {
            getAllCitiesService.reset();
            getAllCitiesService.start();
        }
    }
    
    private void runInsertAddresservice() {
        if (!insertAddressService.isRunning()) {
            insertAddressService.reset();
            insertAddressService.setAddress(addedAddress);
            insertAddressService.start();
        }
    }
    
    private void runUpdateAddresservice() {
        if (!updateAddressService.isRunning()) {
            updateAddressService.reset();
            updateAddressService.setAddress(editedAddress);
            updateAddressService.start();
        }
    }
    
    private void runUpdateCustomerService() {
        if (!updateCustomerService.isRunning()) {
            updateCustomerService.reset();
            updateCustomerService.setCustomer(customer);
            updateCustomerService.start();
        }
    }
    
    private void runGetJustAddedAddrService() {
        if (!getJustAddedAddrService.isRunning()) {
            getJustAddedAddrService.reset();
            getJustAddedAddrService.setAddr(addedAddress);
            getJustAddedAddrService.start();
        }
    }
    
// Form Property Getters & Setters
    private boolean isCustomerNameValidated() {
        return customerNameValidated.get();
    }

    private void setCustomerNameValidated(boolean value) {
        customerNameValidated.set(value);
    }

    private BooleanProperty customerNameValidatedProperty() {
        return customerNameValidated;
    }
    
    private boolean isAddressValidated() {
        return addressValidated.get();
    }

    private void setAddressValidated(boolean value) {
        addressValidated.set(value);
    }

    private BooleanProperty addressValidatedProperty() {
        return addressValidated;
    }
    
    private boolean isNewAddressValidated() {
        return newAddressValidated.get();
    }

    private void setNewAddressValidated(boolean value) {
        newAddressValidated.set(value);
    }

    private BooleanProperty newAddressValidatedProperty() {
        return newAddressValidated;
    }
    
    private boolean isNewCityValidated() {
        return newCityValidated.get();
    }

    private void setNewCityValidated(boolean value) {
        newCityValidated.set(value);
    }

    private BooleanProperty newCityValidatedProperty() {
        return newCityValidated;
    }
    
    private boolean isNewPostalCodeValidated() {
        return newPostalCodeValidated.get();
    }

    private void setNewPostalCodeValidated(boolean value) {
        newPostalCodeValidated.set(value);
    }

    private BooleanProperty newPostalCodeValidatedProperty() {
        return newPostalCodeValidated;
    }
    
    private boolean isNewPhoneValidated() {
        return newPhoneValidated.get();
    }

    private void setNewPhoneValidated(boolean value) {
        newPhoneValidated.set(value);
    }

    private BooleanProperty newPhoneValidatedProperty() {
        return newPhoneValidated;
    }
}
