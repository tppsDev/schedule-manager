/*
 * Project written by: Tim Smith
 * 
 */
package schedulemanager.view_controller;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.ImageView;
import javafx.stage.Modality;
import javafx.stage.Stage;
import schedulemanager.model.Address;
import schedulemanager.model.Customer;
import schedulemanager.view_controller.DBServiceManager.DeleteCustomerService;
import schedulemanager.view_controller.DBServiceManager.GetActiveCustomersService;
import schedulemanager.view_controller.DBServiceManager.GetAllCustomersService;

/**
 * FXML Controller class
 *
 * @author Tim Smith
 */
public class ManageCustomersController implements Initializable {
    @FXML private Label activeCustomerLabel;
    @FXML private Label currentUserLabel;
    @FXML private Label statusLabel;
    @FXML private Label totalCustomerLabel;
    
    @FXML private TextField cityFilterTextField;
    @FXML private TextField countryFilterTextField;
    @FXML private TextField customerFilterTextField;
    @FXML private TextField phoneFilterTextField;
    
    @FXML private CheckBox includeInactiveCheckBox;
    
    @FXML private Button addCustomerButton;
    @FXML private Button closeButton;
    
    @FXML private ImageView clearCityFilterImageView;
    @FXML private ImageView clearCountryFilterImageView;
    @FXML private ImageView clearCustomerFilterImageView;
    @FXML private ImageView clearPhoneFilterImageView;
    
    @FXML private TableView<Customer> customerTable;
    @FXML private TableColumn<Customer, Customer> editColumn;
    @FXML private TableColumn<Customer, Customer> deleteColumn;
    @FXML private TableColumn<Customer, String> customerNameColumn;
    @FXML private TableColumn<Customer, Address> phoneColumn;
    @FXML private TableColumn<Customer, Address> streetAddrColumn;
    @FXML private TableColumn<Customer, Address> cityColumn;
    @FXML private TableColumn<Customer, Address> countryColumn;
    
    @FXML private ProgressIndicator progressIndicator;
    
    private Session session = Session.getSession();
    private DBServiceManager dbServiceMgr = new DBServiceManager();
    private GetAllCustomersService getAllCustomersService = dbServiceMgr.new GetAllCustomersService();
    private GetActiveCustomersService getActiveCustomersService = dbServiceMgr.new GetActiveCustomersService();
    private DeleteCustomerService deleteCustomerService = dbServiceMgr.new DeleteCustomerService();
    private ObservableList<Customer> allCustomerList = FXCollections.observableArrayList();
    private ObservableList<Customer> activeCustomerList = FXCollections.observableArrayList();
    private FilteredList<Customer> filteredCustomers;
    private SortedList<Customer> sortedCustomers;
    
    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        includeInactiveCheckBox.setSelected(false);
        currentUserLabel.setText(session.getSessionUser().getUserName());
        establishBindings();
        initTable();
        initGetAllCustomersService();
        initGetActiveCustomersService();
        initDeleteCustomerService();
        runGetAllCustomersService();
        startFilterChangeListeners();
    }
    
    private void establishBindings() {
        BooleanBinding servicesRunning = getAllCustomersService.runningProperty()
                                     .or(getActiveCustomersService.runningProperty());
        
        progressIndicator.visibleProperty().bind(servicesRunning);
    }
    
    private void initTable() {
        initEditColumn();
        initDeletColumn();
        initCustomerNameColumn();
        initPhoneColumn();
        initStreetAddrColumn();
        initCityColumn();
        initCountryColumn();
    }
    
    private void initEditColumn() {
        editColumn.setCellValueFactory((param) -> new ReadOnlyObjectWrapper<>(param.getValue()));
        editColumn.setStyle("-fx-alignment: CENTER;");
        editColumn.setCellFactory(param -> new TableCell<Customer, Customer>(){
            private Button editButton = new Button(null);

            @Override
            protected void updateItem(Customer customer, boolean empty) {
                super.updateItem(customer, empty);
                
                if (customer == null) {
                    setGraphic(null);
                    return;
                }
                editButton.getStyleClass().add("table-edit-button");
                setGraphic(editButton);
                editButton.setOnAction((event) -> {
                    handleEditButton(customer);
                });
            }
        });
    }

    private void initDeletColumn() {
        deleteColumn.setCellValueFactory((param) -> new ReadOnlyObjectWrapper<>(param.getValue()));
        deleteColumn.setStyle("-fx-alignment: CENTER;");
        deleteColumn.setCellFactory(param -> new TableCell<Customer, Customer>(){
            private final Button deleteButton = new Button();

            @Override
            protected void updateItem(Customer customer, boolean empty) {
                super.updateItem(customer, empty);
                
                if (customer == null) {
                    setGraphic(null);
                    return;
                }
                deleteButton.getStyleClass().add("table-delete-button");
                setGraphic(deleteButton);
                
                deleteButton.setOnAction((event) -> {
                    handleDeleteButton(customer);
                });
            }
        });
    }

    private void initCustomerNameColumn() {
        customerNameColumn.setCellValueFactory(new PropertyValueFactory<>("customerName"));
    }

    private void initPhoneColumn() {
        phoneColumn.setCellValueFactory(new PropertyValueFactory<>("address"));
        phoneColumn.setCellFactory(cust -> new TableCell<Customer, Address>(){
            @Override
            protected void updateItem(Address addr, boolean empty) {
                super.updateItem(addr, empty);
                if (empty) {
                    setText("");
                } else {
                    setText(addr.getPhone());
                }
            }
            
        });
    }

    private void initStreetAddrColumn() {
        streetAddrColumn.setCellValueFactory(new PropertyValueFactory<>("address"));
        streetAddrColumn.setCellFactory(cust -> new TableCell<Customer, Address>(){
            @Override
            protected void updateItem(Address addr, boolean empty) {
                super.updateItem(addr, empty);
                if (empty) {
                    setText("");
                } else {
                    setText(addr.getAddress() + "\n" + addr.getAddress2());
                }
            }
            
        });
    }

    private void initCityColumn() {
        cityColumn.setCellValueFactory(new PropertyValueFactory<>("address"));
        cityColumn.setCellFactory(cust -> new TableCell<Customer, Address>(){
            @Override
            protected void updateItem(Address addr, boolean empty) {
                super.updateItem(addr, empty);
                if (empty) {
                    setText("");
                } else {
                    setText(addr.getCity().getCity());
                }
            }
            
        });
    }

    private void initCountryColumn() {
        countryColumn.setCellValueFactory(new PropertyValueFactory<>("address"));
        countryColumn.setCellFactory(cust -> new TableCell<Customer, Address>(){
            @Override
            protected void updateItem(Address addr, boolean empty) {
                super.updateItem(addr, empty);
                if (empty) {
                    setText("");
                } else {
                    setText(addr.getCity().getCountry().getCountry());
                }
            }
        });
    }
    
    private void loadTable() {
        ObservableList<Customer> customerList;
        
        if (includeInactiveCheckBox.isSelected()) {
            customerList = allCustomerList;
        } else {
            customerList = activeCustomerList;
        }
        
        filteredCustomers = new FilteredList<>(customerList,(pred) -> true);
        sortedCustomers = new SortedList<>(filteredCustomers);
        sortedCustomers.comparatorProperty().bind(customerTable.comparatorProperty());
        applyFilters();
        customerTable.setItems(sortedCustomers);
        customerTable.getColumns().get(0).setVisible(false);
        customerTable.getColumns().get(0).setVisible(true);
    }
    
    private void applyFilters() {
        switch (retrieveFilterStatus()) {
            case "IIII":    // all filters on
                filteredCustomers.setPredicate(cust -> {
                    return cust.getCustomerName().toLowerCase().contains(customerFilterTextField.getText().toLowerCase())
                            && cust.getAddress().getPhone().toLowerCase().contains(phoneFilterTextField.getText().toLowerCase())
                            && cust.getAddress().getCity().getCity().toLowerCase().contains(cityFilterTextField.getText().toLowerCase())
                            && cust.getAddress().getCity().getCountry().getCountry().toLowerCase().contains(countryFilterTextField.getText().toLowerCase());
                });
                break;
            case "IIIO":    // customer, phone, city on
                filteredCustomers.setPredicate(cust -> {
                    return cust.getCustomerName().toLowerCase().toLowerCase().contains(customerFilterTextField.getText().toLowerCase())
                            && cust.getAddress().getPhone().toLowerCase().contains(phoneFilterTextField.getText().toLowerCase())
                            && cust.getAddress().getCity().getCity().toLowerCase().contains(cityFilterTextField.getText().toLowerCase());
                });
                break;
            case "IIOI":    // customer, phone, country on
                filteredCustomers.setPredicate(cust -> {
                    return cust.getCustomerName().toLowerCase().toLowerCase().contains(customerFilterTextField.getText().toLowerCase())
                            && cust.getAddress().getPhone().toLowerCase().contains(phoneFilterTextField.getText().toLowerCase())
                            && cust.getAddress().getCity().getCountry().getCountry().toLowerCase().contains(countryFilterTextField.getText().toLowerCase());
                });
                break;
            case "IIOO":    // customer and phone on
                filteredCustomers.setPredicate(cust -> {
                    return cust.getCustomerName().toLowerCase().toLowerCase().contains(customerFilterTextField.getText().toLowerCase())
                            && cust.getAddress().getPhone().toLowerCase().contains(phoneFilterTextField.getText().toLowerCase());
                });
                break;
            case "IOII":    // customer, city, country on
                filteredCustomers.setPredicate(cust -> {
                    return cust.getCustomerName().toLowerCase().toLowerCase().contains(customerFilterTextField.getText().toLowerCase())
                            && cust.getAddress().getCity().getCity().toLowerCase().contains(cityFilterTextField.getText().toLowerCase())
                            && cust.getAddress().getCity().getCountry().getCountry().toLowerCase().contains(countryFilterTextField.getText().toLowerCase());
                });
                break;
            case "IOIO":    // customer and city on
                filteredCustomers.setPredicate(cust -> {
                    return cust.getCustomerName().toLowerCase().toLowerCase().contains(customerFilterTextField.getText().toLowerCase())
                            && cust.getAddress().getCity().getCity().toLowerCase().contains(cityFilterTextField.getText().toLowerCase());
                });
                break;
            case "IOOI":    // customer and country on
                filteredCustomers.setPredicate(cust -> {
                    return cust.getCustomerName().toLowerCase().toLowerCase().contains(customerFilterTextField.getText().toLowerCase())
                            && cust.getAddress().getCity().getCountry().getCountry().toLowerCase().contains(countryFilterTextField.getText().toLowerCase());
                });
                break;
            case "IOOO":    // customer on
                filteredCustomers.setPredicate(cust -> {
                    return cust.getCustomerName().toLowerCase().toLowerCase().contains(customerFilterTextField.getText().toLowerCase());
                });
                break;
            case "OIII":    // phone, city, country on
                filteredCustomers.setPredicate(cust -> {
                    return cust.getAddress().getPhone().toLowerCase().contains(phoneFilterTextField.getText().toLowerCase())
                            && cust.getAddress().getCity().getCity().toLowerCase().contains(cityFilterTextField.getText().toLowerCase())
                            && cust.getAddress().getCity().getCountry().getCountry().toLowerCase().contains(countryFilterTextField.getText().toLowerCase());
                });
                break;
            case "OIIO":    // phone and city on
                filteredCustomers.setPredicate(cust -> {
                    return cust.getAddress().getPhone().toLowerCase().contains(phoneFilterTextField.getText().toLowerCase())
                            && cust.getAddress().getCity().getCity().toLowerCase().contains(cityFilterTextField.getText().toLowerCase());
                });
                break;
            case "OIOI":    // phone and country on
                filteredCustomers.setPredicate(cust -> {
                    return cust.getAddress().getPhone().toLowerCase().contains(phoneFilterTextField.getText().toLowerCase())
                            && cust.getAddress().getCity().getCountry().getCountry().toLowerCase().contains(countryFilterTextField.getText().toLowerCase());
                });
                break;
            case "OIOO":    // phone on
                filteredCustomers.setPredicate(cust -> {
                    return cust.getAddress().getPhone().toLowerCase().contains(phoneFilterTextField.getText().toLowerCase());
                });
                break;
            case "OOII":    // city and country on
                filteredCustomers.setPredicate(cust -> {
                    return cust.getAddress().getCity().getCity().toLowerCase().contains(cityFilterTextField.getText().toLowerCase())
                            && cust.getAddress().getCity().getCountry().getCountry().toLowerCase().contains(countryFilterTextField.getText().toLowerCase());
                });
                break;
            case "OOIO":    // city on
                filteredCustomers.setPredicate(cust -> {
                    return cust.getAddress().getCity().getCity().toLowerCase().contains(cityFilterTextField.getText().toLowerCase());
                });
                break;
            case "OOOI":    // country on
                filteredCustomers.setPredicate(cust -> {
                    return cust.getAddress().getCity().getCountry().getCountry().toLowerCase().contains(countryFilterTextField.getText().toLowerCase());
                });
                break;
            case "OOOO":    // no filters
                filteredCustomers.setPredicate(t -> true);
                break;
            default:
                statusLabel.setText("System error: Invalid filter response");
        }
    }
    
    private String retrieveFilterStatus() {
        String customerFiltered = "O";
        String phoneFiltered = "O";
        String cityFiltered = "O";
        String countryFiltered = "O";

        if (customerFilterTextField.getText() != null && !customerFilterTextField.getText().isEmpty()) {
            customerFiltered = "I";
        }

        if (phoneFilterTextField.getText() != null && !phoneFilterTextField.getText().isEmpty()) {
            phoneFiltered = "I";
        }
        
        if (cityFilterTextField.getText() != null && !cityFilterTextField.getText().isEmpty()) {
            cityFiltered = "I";
        }

        if (countryFilterTextField.getText() != null && !countryFilterTextField.getText().isEmpty()) {
            countryFiltered = "I";
        }
        
        return customerFiltered + phoneFiltered + cityFiltered + countryFiltered;
    }
    
// FXML Control Handlers
    @FXML
    private void handleAddCustomerButton() {
        FXMLLoader acLoader = new FXMLLoader(getClass().getResource("AddCustomer.fxml"));
        Scene acScene;
        try {
            acScene = new Scene(acLoader.load());
        } catch (IOException ex) {

            return;
        }
        Stage acStage = new Stage();
        acStage.initOwner(currentUserLabel.getScene().getWindow());
        acStage.initModality(Modality.APPLICATION_MODAL);
        acStage.setTitle("Schedule Manager 1.0 - Add Customer");
        acStage.setScene(acScene);
        AddCustomerController acController = acLoader.getController();
        acStage.showAndWait();
        if (acController.wasCustomerAdded()) {
            runGetAllCustomersService();
        }
    }
    
    @FXML
    private void handleCloseButton() {
        Stage stage = (Stage) closeButton.getScene().getWindow();
        stage.close();
    }
    
    @FXML
    private void handleClearCustomerFilterImageView() {
        customerFilterTextField.setText("");
    }
    
    @FXML
    private void handleClearPhoneFilterImageView() {
        phoneFilterTextField.setText("");
    }
    
    @FXML
    private void handleClearCityFilterImageView() {
        cityFilterTextField.setText("");
    }
    
    @FXML
    private void handleClearCountryFilterImageView() {
        countryFilterTextField.setText("");
    }
    
    @FXML
    private void handleEditButton(Customer selectedCustomer) {
        FXMLLoader ecLoader = new FXMLLoader(getClass().getResource("EditCustomer.fxml"));
        Scene ecScene;
        try {
            ecScene = new Scene(ecLoader.load());
        } catch (IOException ex) {

            return;
        }
        Stage ecStage = new Stage();
        ecStage.initOwner(currentUserLabel.getScene().getWindow());
        ecStage.initModality(Modality.APPLICATION_MODAL);
        ecStage.setTitle("Schedule Manager 1.0 - Edit Customer");
        ecStage.setScene(ecScene);
        EditCustomerController ecController = ecLoader.getController();
        ecController.setCustomer(selectedCustomer);
        ecStage.showAndWait();
        if (ecController.wasCustomerChanged()) {
            runGetAllCustomersService();
        }
    }
    
    @FXML
    private void handleDeleteButton(Customer selectedCustomer) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirm deletion");
        alert.setHeaderText("");
        alert.initModality(Modality.APPLICATION_MODAL);
        alert.setContentText("Are you sure you want to delete " + selectedCustomer.getCustomerName() + "?");
        alert.showAndWait();
        if (alert.getResult().equals(ButtonType.OK)) {
            runDeleteCustomerService(selectedCustomer);
        }
    }
    
// Change Listeners
    private void startFilterChangeListeners() {
        // lambda
        includeInactiveCheckBox.selectedProperty().addListener((observable) -> {
            // lambda
            loadTable();
        });
        
        customerFilterTextField.textProperty().addListener((observable) -> {
            applyFilters();
        });
        
        phoneFilterTextField.textProperty().addListener((observable) -> {
            applyFilters();
        });
        
        cityFilterTextField.textProperty().addListener((observable) -> {
            applyFilters();
        });
        
        countryFilterTextField.textProperty().addListener((observable) -> {
            applyFilters();
        });
    }

// Service Methods
    private void initGetAllCustomersService() {
        getAllCustomersService.setOnSucceeded((event) -> {
            allCustomerList = getAllCustomersService.getValue();
            totalCustomerLabel.setText(String.valueOf(allCustomerList.size()));
            runGetActiveCustomersService();
        });
        
        getAllCustomersService.setOnFailed((event) -> {
            System.out.println(event.getSource().getException().getMessage());
        });
    }
    
    private void initGetActiveCustomersService() {
        getActiveCustomersService.setOnSucceeded((event) -> {
            activeCustomerList = getActiveCustomersService.getValue();
            activeCustomerLabel.setText(String.valueOf(activeCustomerList.size()));
            loadTable();
        });
        
        getActiveCustomersService.setOnFailed((event) -> {
            System.out.println(event.getSource().getException().getMessage());
        });
    }
    
    public void initDeleteCustomerService() {
        deleteCustomerService.setOnSucceeded((event) -> {
            runGetAllCustomersService();
        });
        deleteCustomerService.setOnFailed((event) -> {
            if (event.getSource().getException().getMessage().startsWith("Cannot delete")) {
                statusLabel.setText(event.getSource().getException().getMessage() + " Remove active status instead.");
            } else {
                System.out.println(event.getSource().getException().getMessage());
            }
        });
    }
    
    private void runGetAllCustomersService() {
        if (!getAllCustomersService.isRunning()) {
            getAllCustomersService.reset();
            getAllCustomersService.start();
        }
    }
    
    private void runGetActiveCustomersService() {
        if (!getActiveCustomersService.isRunning()) {
            getActiveCustomersService.reset();
            getActiveCustomersService.start();
        }
    }
    
    private void runDeleteCustomerService(Customer customer) {
        if (!deleteCustomerService.isRunning()) {
            deleteCustomerService.reset();
            deleteCustomerService.setCustomer(customer);
            deleteCustomerService.start();
        }
    }

}
