/*
 * Project written by: Tim Smith
 * 
 */
package schedulemanager.view_controller;

import java.net.URL;
import java.util.ResourceBundle;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;
import schedulemanager.model.Address;
import schedulemanager.view_controller.DBServiceManager.GetAllAddressesService;

/**
 * FXML Controller class
 *
 * @author Tim Smith
 */
public class SelectAddressController implements Initializable {
    @FXML private Label currentUserLabel;
    @FXML private ProgressIndicator progressIndicator;
    @FXML private TextField addrFilterTextField;
    @FXML private TextField cityFilterTextField;
    @FXML private TextField postalCodeFilterTextField;
    @FXML private TextField phoneFilterTextField;
    @FXML private ImageView clearAddrFilterImageView;
    @FXML private ImageView clearCityFilterImageView;
    @FXML private ImageView clearPostalCodeFilterImageView;
    @FXML private ImageView clearPhoneFilterImageView;
    @FXML private Button clearAllFiltersButton;
    @FXML private TableView<Address> addressTable;
    @FXML private TableColumn<Address, Address> selectButtonColumn;
    @FXML private TableColumn<Address, String> addrColumn;
    @FXML private TableColumn<Address, String> addr2Column;
    @FXML private TableColumn<Address, String> cityColumn;
    @FXML private TableColumn<Address, String> postalCodeColumn;
    @FXML private TableColumn<Address, String> phoneColumn;
    @FXML private Label statusLabel;
    @FXML private Button cancelButton;
    
    private Session session = Session.getSession();
    private DBServiceManager dbServiceMgr = new DBServiceManager();
    private GetAllAddressesService getAllAddressesService = dbServiceMgr.new GetAllAddressesService();
    
    private ObservableList<Address> allAddresses;
    private FilteredList<Address> filteredAddresses;
    private SortedList<Address> sortedAddresses;
    private Address selectedAddress;
    private boolean selectionMade = false;
    
    /**
     * Initializes the controller class.
     * @param url
     * @param rb
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        currentUserLabel.setText(session.getSessionUser().getUserName());
        initTable();
        initGetAllAddressesService();
        establishBindings();
        runGetAllAddressesService();
        startChangeListeners();
    }
    
    public Address getSelectedAddress() {
        return selectedAddress;
    }
    
    public boolean wasSelectionMade() {
        return selectionMade;
    }

// TableView & TableColumn methods    
    private void initTable() {
        initSelectButtonColumn();
        initAddrColumn();
        initAddr2Column();
        initCityColumn();
        initPostalCodeColumn();
        initPhoneColumn();
    }
    
    private void initSelectButtonColumn() {
        selectButtonColumn.setCellValueFactory((param) -> new ReadOnlyObjectWrapper<>(param.getValue()));
        selectButtonColumn.setStyle("-fx-alignment: CENTER;");
        selectButtonColumn.setCellFactory(param -> new TableCell<Address, Address>(){
            private Button selectButton = new Button(null);

            @Override
            protected void updateItem(Address addr, boolean empty) {
                super.updateItem(addr, empty);
                
                if (addr == null) {
                    setGraphic(null);
                    return;
                }
                selectButton.getStyleClass().add("table-select-button");
                setGraphic(selectButton);
                selectButton.setOnAction((event) -> {
                    handleSelectButton(addr);
                });
            }
        });
    }
    
    private void initAddrColumn() {
        addrColumn.setCellValueFactory(new PropertyValueFactory<>("address"));
    }
    
    private void initAddr2Column() {
        addr2Column.setCellValueFactory(new PropertyValueFactory<>("address2"));
    }
    
    private void initCityColumn() {
        cityColumn.setCellValueFactory(new PropertyValueFactory<>("city"));
    }
    
    private void initPostalCodeColumn() {
        postalCodeColumn.setCellValueFactory(new PropertyValueFactory<>("postalCode"));
    }
    
    private void initPhoneColumn() {
        phoneColumn.setCellValueFactory(new PropertyValueFactory<>("phone"));
    }
    
    private void loadTable() {
        filteredAddresses = new FilteredList<>(allAddresses, pred -> true);
        sortedAddresses = new SortedList<>(filteredAddresses);
        sortedAddresses.comparatorProperty().bind(addressTable.comparatorProperty());
        applyFilters();
        addressTable.setItems(sortedAddresses);
    }
    
    private void applyFilters() {
        switch (retrieveFilterStatus()) {
            case "IIII":    // all filters on
                filteredAddresses.setPredicate(addr -> {
                    return (addr.getAddress().contains(addrFilterTextField.getText())
                            || addr.getAddress2().contains(addrFilterTextField.getText()))
                            && (addr.getCity().toString().contains(cityFilterTextField.getText()))
                            && (addr.getPostalCode().contains(postalCodeFilterTextField.getText()))
                            && (addr.getPhone().contains(phoneFilterTextField.getText()));
                });
                break;
            case "IIIO":    // addr, city, postal code on
                filteredAddresses.setPredicate(addr -> {
                    return (addr.getAddress().contains(addrFilterTextField.getText())
                            || addr.getAddress2().contains(addrFilterTextField.getText()))
                            && (addr.getCity().toString().contains(cityFilterTextField.getText()))
                            && (addr.getPostalCode().contains(postalCodeFilterTextField.getText()));
                });
                break;
            case "IIOI":    // addr, city, phone on
                filteredAddresses.setPredicate(addr -> {
                    return (addr.getAddress().contains(addrFilterTextField.getText())
                            || addr.getAddress2().contains(addrFilterTextField.getText()))
                            && (addr.getCity().toString().contains(cityFilterTextField.getText()))
                            && (addr.getPhone().contains(phoneFilterTextField.getText()));
                });
                break;
            case "IIOO":    // addr and city on
                filteredAddresses.setPredicate(addr -> {
                    return (addr.getAddress().contains(addrFilterTextField.getText())
                            || addr.getAddress2().contains(addrFilterTextField.getText()))
                            && (addr.getCity().toString().contains(cityFilterTextField.getText()));
                });
                break;
            case "IOII":    // addr, postal code, phone on
                filteredAddresses.setPredicate(addr -> {
                    return (addr.getAddress().contains(addrFilterTextField.getText())
                            || addr.getAddress2().contains(addrFilterTextField.getText()))
                            && (addr.getPostalCode().contains(postalCodeFilterTextField.getText()))
                            && (addr.getPhone().contains(phoneFilterTextField.getText()));
                });
                break;
            case "IOIO":    // addr and postal code on
                filteredAddresses.setPredicate(addr -> {
                    return (addr.getAddress().contains(addrFilterTextField.getText())
                            || addr.getAddress2().contains(addrFilterTextField.getText()))
                            && (addr.getPostalCode().contains(postalCodeFilterTextField.getText()));
                });
                break;
            case "IOOI":    // addr and phone on
                filteredAddresses.setPredicate(addr -> {
                    return (addr.getAddress().contains(addrFilterTextField.getText())
                            || addr.getAddress2().contains(addrFilterTextField.getText()))
                            && (addr.getPhone().contains(phoneFilterTextField.getText()));
                });
                break;
            case "IOOO":    // addr on
                filteredAddresses.setPredicate(addr -> {
                    return (addr.getAddress().contains(addrFilterTextField.getText())
                            || addr.getAddress2().contains(addrFilterTextField.getText()));
                });
                break;
            case "OIII":    // city, postal code, phone on
                filteredAddresses.setPredicate(addr -> {
                    return (addr.getCity().toString().contains(cityFilterTextField.getText()))
                            && (addr.getPostalCode().contains(postalCodeFilterTextField.getText()))
                            && (addr.getPhone().contains(phoneFilterTextField.getText()));
                });
                break;
            case "OIIO":    // city and postal code on
                filteredAddresses.setPredicate(addr -> {
                    return (addr.getCity().toString().contains(cityFilterTextField.getText()))
                            && (addr.getPostalCode().contains(postalCodeFilterTextField.getText()));
                });
                break;
            case "OIOI":    // city and phone on
                filteredAddresses.setPredicate(addr -> {
                    return (addr.getCity().toString().contains(cityFilterTextField.getText()))
                            && (addr.getPhone().contains(phoneFilterTextField.getText()));
                });
                break;
            case "OIOO":    // city on
                filteredAddresses.setPredicate(addr -> {
                    return (addr.getCity().toString().contains(cityFilterTextField.getText()));
                });
                break;
            case "OOII":    // postal code and phone on
                filteredAddresses.setPredicate(addr -> {
                    return (addr.getPostalCode().contains(postalCodeFilterTextField.getText()))
                            && (addr.getPhone().contains(phoneFilterTextField.getText()));
                });
                break;
            case "OOIO":    // postal code on
                filteredAddresses.setPredicate(addr -> {
                    return (addr.getPostalCode().contains(postalCodeFilterTextField.getText()));
                });
                break;
            case "OOOI":    // phone on
                filteredAddresses.setPredicate(addr -> {
                    return (addr.getPhone().contains(phoneFilterTextField.getText()));
                });
                break;
            case "OOOO":    // no filters
                filteredAddresses.setPredicate(pred -> true);
                break;
            default:
                statusLabel.setText("System error: Invalid filter response");
        }
    }
    
    private String retrieveFilterStatus() {
        String addressFiltered = "O";
        String cityFiltered = "O";
        String postalCodeFiltered = "O";
        String phoneFiltered = "O";
        
        if (addrFilterTextField.getText() != null && !addrFilterTextField.getText().isEmpty()) {
            addressFiltered = "I";
        }
        
        if (cityFilterTextField.getText() != null && !cityFilterTextField.getText().isEmpty()) {
            cityFiltered = "I";
        }
        
        if (postalCodeFilterTextField.getText() != null && !postalCodeFilterTextField.getText().isEmpty()) {
            postalCodeFiltered = "I";
        }
        
        if (phoneFilterTextField.getText() != null && !phoneFilterTextField.getText().isEmpty()) {
            phoneFiltered = "I";
        }
        
        return addressFiltered + cityFiltered + postalCodeFiltered + phoneFiltered;
        
    }
    
// Bindings
    private void establishBindings() {
        progressIndicator.visibleProperty().bind(getAllAddressesService.runningProperty());
    }
    
// JavaFX Control Event Handlers
    private void handleSelectButton(Address selectedAddress) {
        this.selectedAddress = selectedAddress;
        selectionMade = true;
        
        Stage stage = (Stage) currentUserLabel.getScene().getWindow();
        stage.close();
    }
    
    @FXML
    private void handleCancelButton() {
        Stage stage = (Stage) currentUserLabel.getScene().getWindow();
        stage.close();
    }
    
    @FXML
    private void handleClearAddrFilter() {
        addrFilterTextField.setText("");
    }
    
    @FXML
    private void handleClearCityFilter() {
        cityFilterTextField.setText("");
    }
    
    @FXML
    private void handleClearPostalCodeFilter() {
        postalCodeFilterTextField.setText("");
    }
    
    @FXML
    private void handleClearPhoneFilter() {
        phoneFilterTextField.setText("");
    }
    
    @FXML
    private void handleClearAllFilters() {
        handleClearAddrFilter();
        handleClearCityFilter();
        handleClearPostalCodeFilter();
        handleClearPhoneFilter();
    }

// JavaFX ChangeListeners
    private void startChangeListeners() {
        addrFilterTextField.textProperty().addListener((observable) -> {
            applyFilters();
        });
        
        cityFilterTextField.textProperty().addListener((observable) -> {
            applyFilters();
        });
        
        postalCodeFilterTextField.textProperty().addListener((observable) -> {
            applyFilters();
        });
        
        phoneFilterTextField.textProperty().addListener((observable) -> {
            applyFilters();
        });
    }
    
// Service Methods
    private void initGetAllAddressesService() {
        getAllAddressesService.setOnSucceeded((event) -> {
            allAddresses = getAllAddressesService.getValue();
            loadTable();
        });
        
        getAllAddressesService.setOnFailed((event) -> {
            statusLabel.setText("System Error loading addresses. Please try again later.");
        });
    }
    
    private void runGetAllAddressesService() {
        if (!getAllAddressesService.isRunning()) {
            getAllAddressesService.reset();
            getAllAddressesService.start();
        }
    }
}
