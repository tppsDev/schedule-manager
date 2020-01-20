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
import schedulemanager.view_controller.DBServiceManager.InsertCustomerService;

/**
 * FXML Controller class
 *
 * @author Tim Smith
 */
public class AddCustomerController implements Initializable {
    @FXML private Label currentUserLabel;
    @FXML private Button selectAddrButton;
    @FXML private Button addCityButton;
    @FXML private Button cancelButton;
    @FXML private Button submitButton;
    @FXML private RadioButton addAddrRadioButton;
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
    
    private BooleanProperty customerNameValidated = new SimpleBooleanProperty(false);
    private BooleanProperty addressValidated = new SimpleBooleanProperty(false);
    private BooleanProperty newAddressValidated = new SimpleBooleanProperty(false);
    private BooleanProperty newCityValidated = new SimpleBooleanProperty(false);
    private BooleanProperty newPostalCodeValidated = new SimpleBooleanProperty(false);
    private BooleanProperty newPhoneValidated = new SimpleBooleanProperty(false);

    
    private Session session = Session.getSession();
    private Customer customer = new Customer();
    private DBServiceManager dbServiceMgr = new DBServiceManager();
    private GetAllCitiesService getAllCitiesService = dbServiceMgr.new GetAllCitiesService();
    private InsertCustomerService insertCustomerService = dbServiceMgr.new InsertCustomerService();
    private InsertAddressService insertAddressService = dbServiceMgr.new InsertAddressService();
    private GetJustAddedAddrService getJustAddedAddrService = dbServiceMgr.new GetJustAddedAddrService();
    private Address selectedAddress;
    private Address addedAddress;
    private final BooleanProperty selectedAddressExists = new SimpleBooleanProperty(false);
    private BooleanBinding allFieldsValidated;
    private boolean customerAdded = false;
    
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
        initInsertCustomerService();
        initGetJustAddedAddrService();
        runGetAllCitiesService();
        addAddressVBox.toFront();
        startChangeListeners();
        
    }
    
    /**
     *  Enables parent stage to see if a user was added so it can handle as needed
     * @return
     */
    public boolean wasCustomerAdded() {
        return customerAdded;
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
                .or(insertAddressService.runningProperty())
                .or(insertCustomerService.runningProperty())
                .or(getJustAddedAddrService.runningProperty());
                
        progressIndicator.visibleProperty().bind(serviceRunning);
    }
    
    private void populateSelectedAddrFields() {
        displayPhoneTextField.setText(selectedAddress.getPhone());
        displayAddressTextArea.setText(selectedAddress.toString());
    }
    
    private void createCustomer() {
        customer.setCustomerName(customerNameTextField.getText());
        customer.setActive(activeCheckBox.isSelected());
        if (selectAddrRadioButton.isSelected()) {
            customer.setAddress(selectedAddress);
            runInsertCustomerService();
        } else {
            addedAddress = new Address(
                                        addressTextField.getText(),
                                        address2TextField.getText(),
                                        cityChoiceBox.getValue(),
                                        postalCodeTextField.getText(),
                                        phoneTextField.getText());
            runInsertAddresservice();
        }
    }
    
// JavaFX Control Event Handlers
    @FXML
    private void handleAddOrSelectRadioButtons() {
        if (addAddrRadioButton.isSelected()) {
            addAddressVBox.toFront();
            runGetAllCitiesService();
        } else {
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
            // TODO Handle error
            System.out.println(ex.getMessage());
            ex.printStackTrace();
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
            // TODO Handle error
            System.out.println(ex.getMessage());
            ex.printStackTrace();
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
            createCustomer();
        }
    }

// Change Listeners
    private void startChangeListeners() {
        customerNameTextField.focusedProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue && !addCityButton.isFocused()) {
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
            if (!newValue && !selectAddrRadioButton.isSelected() && !addCityButton.isFocused()) {
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
            if (!newValue && !selectAddrRadioButton.isSelected() && !addCityButton.isFocused()) {
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
            if (!newValue && !selectAddrRadioButton.isSelected() && !addCityButton.isFocused()) {
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
            if (!newValue && !selectAddrRadioButton.isSelected() && !addCityButton.isFocused()) {
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
    
    private void initInsertCustomerService() {
        insertCustomerService.setOnSucceeded((event) -> {
            customerAdded = true;
            Stage stage = (Stage) currentUserLabel.getScene().getWindow();
            stage.close();
        });
        
        insertCustomerService.setOnFailed((event) -> {
            System.out.println(event.getSource().getException().getMessage());
        });
    }
    
    private void initGetJustAddedAddrService() {
        getJustAddedAddrService.setOnSucceeded((event) -> {
            addedAddress = getJustAddedAddrService.getValue();
            customer.setAddress(addedAddress);
            runInsertCustomerService();
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
    
    private void runInsertCustomerService() {
        if (!insertCustomerService.isRunning()) {
            insertCustomerService.reset();
            insertCustomerService.setCustomer(customer);
            insertCustomerService.start();
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
