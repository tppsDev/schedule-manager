/*
 * Project written by: Tim Smith
 * 
 */
package schedulemanager.view_controller;

import java.net.URL;
import java.util.ResourceBundle;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.value.ChangeListener;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import schedulemanager.DAO.SysOp.queryType;
import static schedulemanager.DAO.SysOp.queryType.*;
import schedulemanager.model.City;
import schedulemanager.model.Country;
import schedulemanager.view_controller.DBServiceManager.CheckIfCityExistsService;
import schedulemanager.view_controller.DBServiceManager.CheckIfCountryExistsService;
import schedulemanager.view_controller.DBServiceManager.DeleteCityService;
import schedulemanager.view_controller.DBServiceManager.DeleteCountryService;
import schedulemanager.view_controller.DBServiceManager.GetAllCitiesService;
import schedulemanager.view_controller.DBServiceManager.GetAllCountriesService;
import schedulemanager.view_controller.DBServiceManager.InsertCityService;
import schedulemanager.view_controller.DBServiceManager.InsertCountryService;
import schedulemanager.view_controller.DBServiceManager.UpdateCityService;
import schedulemanager.view_controller.DBServiceManager.UpdateCountryService;

/**
 * FXML Controller class
 *
 * @author Tim Smith
 */
public class ManageCitiesCountriesController implements Initializable {
    
    @FXML private AnchorPane cityFormPane;
    @FXML private AnchorPane countryFormPane;
    
    @FXML private Button addCityButton;
    @FXML private Button cityFormCancelButton;
    @FXML private Button cityFormSubmitButton;
    @FXML private Button addCountryButton;
    @FXML private Button countryFormCancelButton;
    @FXML private Button countryFormSubmitButton;
    
    @FXML private ComboBox<Country> cityFormCountryComboBox;
    
    @FXML private ImageView clearCityFilterImageView;
    @FXML private ImageView clearCityCountryFilterImageView;
    @FXML private ImageView clearCountryFilterImageView;
    
    @FXML private Label cityFormCityErrorLabel;
    @FXML private Label cityFormCountryErrorLabel;
    @FXML private Label countryFormCountryErrorLabel;
    @FXML private Label currentUserLabel;
    @FXML private Label statusLabel;
    
    @FXML private TableView<City> cityTableView;
    @FXML private TableColumn<City, City> cityEditColumn;
    @FXML private TableColumn<City, City> cityDeleteColumn;
    @FXML private TableColumn<City, String> cityColumn;
    @FXML private TableColumn<City, Country> cityCountryColumn;
    @FXML private TableView<Country> countryTableView;
    @FXML private TableColumn<Country, Country> countryEditColumn;
    @FXML private TableColumn<Country, Country> countryDeleteColumn;
    @FXML private TableColumn<Country, String> countryColumn;
    
    @FXML private TextField cityFilterTextField;
    @FXML private TextField countryFilterCityTableTextField;
    @FXML private TextField countryFilterCountryTableTextField;
    @FXML private TextField cityFormCityTextField;
    @FXML private TextField countryFormCountryTextField;
    
    @FXML private ProgressIndicator progressIndicator;
    
    private DBServiceManager dbServiceMgr = new DBServiceManager();
    private GetAllCitiesService getAllCitiesService = dbServiceMgr.new GetAllCitiesService();
    private GetAllCountriesService getAllCountriesService = dbServiceMgr.new GetAllCountriesService();
    private InsertCityService insertCityService = dbServiceMgr.new InsertCityService();
    private InsertCountryService insertCountryService = dbServiceMgr.new InsertCountryService();
    private UpdateCityService updateCityService = dbServiceMgr.new UpdateCityService();
    private UpdateCountryService updateCountryService = dbServiceMgr.new UpdateCountryService();
    private DeleteCityService deleteCityService = dbServiceMgr.new DeleteCityService();
    private DeleteCountryService deleteCountryService = dbServiceMgr.new DeleteCountryService();
    private CheckIfCityExistsService checkIfCityExistsService = dbServiceMgr.new CheckIfCityExistsService();
    private CheckIfCountryExistsService checkIfCountryExistsService = dbServiceMgr.new CheckIfCountryExistsService();
    
    private ChangeListener<Boolean> cityFormCityListener;
    private ChangeListener<Boolean> cityFormCountryListener;
    private ChangeListener<Boolean> countryFormCountryListener;
    
    private ObservableList<City> allCities = FXCollections.observableArrayList();
    private FilteredList<City> filteredCities;
    private SortedList<City> sortedCities;
    private ObservableList<Country> allCountries = FXCollections.observableArrayList();
    private FilteredList<Country> filteredCountries;
    private SortedList<Country> sortedCountries;
    
    private boolean cityAdded = false;
    private queryType currentFormMode;
    private City editCity;
    private Country editCountry;
    
    /**
     * Initializes the controller class.
     * @param url
     * @param rb
     */
    @Override
    public final void initialize(URL url, ResourceBundle rb) {
        // TODO
        cityFormPane.setVisible(false);
        countryFormPane.setVisible(false);
        //cityFormCountryChoiceBox.setItems(allCountries);
        establishBindings();
        initTables();
        initGetAllCitiesService();
        initGetAllCountriesService();
        initInsertCityService();
        initInsertCountryService();
        initUpdateCityService();
        initUpdateCountryService();
        initDeleteCityService();
        initDeleteCountryService();
        initCheckIfCityExistsService();
        initCheckIfCountryExistsService();
        runGetAllCitiesService();
        runGetAllCountriesService();
        startTableFilterChangeListeners();
    }
    
    public boolean cityWasAdded() {
        return cityAdded;
    }

    private void establishBindings() {
        bindProgressIndicator();
        addCityButton.visibleProperty().bind(cityFormPane.visibleProperty().not()
                                        .and(countryFormPane.visibleProperty().not()));
        
        addCountryButton.visibleProperty().bind(cityFormPane.visibleProperty().not()
                                        .and(countryFormPane.visibleProperty().not()));
        
        cityTableView.disableProperty().bind(cityFormPane.visibleProperty()
                                        .or(countryFormPane.visibleProperty()));
        
        countryTableView.disableProperty().bind(cityFormPane.visibleProperty()
                                        .or(countryFormPane.visibleProperty()));
    
    }
    
    private void bindProgressIndicator() {
        BooleanBinding servicesRunning = getAllCitiesService.runningProperty()
                                        .or(getAllCountriesService.runningProperty())
                                        .or(insertCityService.runningProperty())
                                        .or(insertCountryService.runningProperty())
                                        .or(updateCityService.runningProperty())
                                        .or(updateCountryService.runningProperty())
                                        .or(deleteCityService.runningProperty())
                                        .or(deleteCountryService.runningProperty())
                                        .or(checkIfCityExistsService.runningProperty())
                                        .or(checkIfCountryExistsService.runningProperty());
        
        progressIndicator.visibleProperty().bind(servicesRunning);
    }

    private void initTables() {
        initCityEditColumn();
        initCityDeleteColumn();
        initCityColumn();
        initCityCountryColumn();
        initCountryEditColumn();
        initCountryDeleteColumn();
        initCountryColumn();
    }
    
    private void initCityEditColumn() {
        cityEditColumn.setCellValueFactory((param) -> new ReadOnlyObjectWrapper<>(param.getValue()));
        cityEditColumn.setStyle("-fx-alignment: CENTER;");
        // Lambda allows function to be defined simply in plave where it is set as cell factory
        cityEditColumn.setCellFactory(colVal-> new TableCell<City, City>(){
            private Button editCityButton = new Button(null);

            @Override
            protected void updateItem(City city, boolean empty) {
                super.updateItem(city, empty);
                
                if (city == null) {
                    setGraphic(null);
                    return;
                }
                editCityButton.getStyleClass().add("table-edit-button");
                setGraphic(editCityButton);
                editCityButton.setOnAction((event) -> {
                    handleEditCityButton(city);
                });
            }
        });
    }
    
    private void initCityDeleteColumn() {
        cityDeleteColumn.setCellValueFactory((param) -> new ReadOnlyObjectWrapper<>(param.getValue()));
        cityDeleteColumn.setStyle("-fx-alignment: CENTER;");
        cityDeleteColumn.setCellFactory(colVal -> new TableCell<City, City>(){
            private Button deleteCityButton = new Button(null);

            @Override
            protected void updateItem(City city, boolean empty) {
                super.updateItem(city, empty);
                
                if (city == null) {
                    setGraphic(null);
                    return;
                }
                deleteCityButton.getStyleClass().add("table-delete-button");
                setGraphic(deleteCityButton);
                deleteCityButton.setOnAction((event) -> {
                    handleDeleteCityButton(city);
                });
            }
        });
    }
    
    private void initCityColumn() {
        cityColumn.setCellValueFactory(new PropertyValueFactory<>("city"));
    }
    
    private void initCityCountryColumn() {
        cityCountryColumn.setCellValueFactory(new PropertyValueFactory<>("country"));
    }
    
    private void initCountryEditColumn() {
        countryEditColumn.setCellValueFactory((param) -> new ReadOnlyObjectWrapper<>(param.getValue()));
        countryEditColumn.setStyle("-fx-alignment: CENTER;");
        countryEditColumn.setCellFactory(colVal-> new TableCell<Country, Country>(){
            private Button editCountryButton = new Button(null);

            @Override
            protected void updateItem(Country country, boolean empty) {
                super.updateItem(country, empty);
                
                if (country == null) {
                    setGraphic(null);
                    return;
                }
                editCountryButton.getStyleClass().add("table-edit-button");
                setGraphic(editCountryButton);
                editCountryButton.setOnAction((event) -> {
                    handleEditCountryButton(country);
                });
            }
        });
    }
    
    private void initCountryDeleteColumn() {
        countryDeleteColumn.setCellValueFactory((param) -> new ReadOnlyObjectWrapper<>(param.getValue()));
        countryDeleteColumn.setStyle("-fx-alignment: CENTER;");
        countryDeleteColumn.setCellFactory(colVal-> new TableCell<Country, Country>(){
            private Button deleteCountryButton = new Button(null);

            @Override
            protected void updateItem(Country country, boolean empty) {
                super.updateItem(country, empty);
                
                if (country == null) {
                    setGraphic(null);
                    return;
                }
                deleteCountryButton.getStyleClass().add("table-delete-button");
                setGraphic(deleteCountryButton);
                deleteCountryButton.setOnAction((event) -> {
                    handleDeleteCountryButton(country);
                });
            }
        });
    }
    
    private void initCountryColumn() {
        countryColumn.setCellValueFactory(new PropertyValueFactory<>("country"));
    }
    
    private void loadCityTable() {
        filteredCities = new FilteredList<>(allCities, pred -> true);
        sortedCities = new SortedList<>(filteredCities);
        sortedCities.comparatorProperty().bind(cityTableView.comparatorProperty());
        applyCityFilters();
        cityTableView.setItems(sortedCities);
    }
    
    private void loadCountryTable() {
        filteredCountries = new FilteredList<>(allCountries, pred -> true);
        sortedCountries = new SortedList<>(filteredCountries);
        sortedCountries.comparatorProperty().bind(countryTableView.comparatorProperty());

        if (countryFilterCountryTableTextField.getText() != null || !countryFilterCountryTableTextField.getText().isEmpty()) {
            filteredCountries.setPredicate(country -> {
                
                return country.getCountry().contains(countryFilterCountryTableTextField.getText());
            });
        }
        
        countryTableView.setItems(sortedCountries);
        countryTableView.getColumns().get(0).setVisible(false);
        countryTableView.getColumns().get(0).setVisible(true);
    }
    
    /*
    * Apply filters based on value return from cityFilterStatus()
    * 3 - both, 2 just country, 1 just city, 0 none
    */
    private void applyCityFilters() {
        switch (cityFilterStatus()) {
            case 3:
                filteredCities.setPredicate(city -> {

                    return city.getCity().contains(cityFilterTextField.getText())
                        && city.getCountry().getCountry().contains(countryFilterCityTableTextField.getText());
                });
                break;
            case 2:
                filteredCities.setPredicate(city -> {

                    return city.getCountry().getCountry().contains(countryFilterCityTableTextField.getText());
                });
                break;
            case 1:
                filteredCities.setPredicate(city -> {

                    return city.getCity().contains(cityFilterTextField.getText());
                });
                break;
            default:
                filteredCities.setPredicate(city -> {

                    return true;
                });
        }
    }

    /*
    * Check if city and/or country filter for the city table are populated.
    * Returns 0 for no filter, 1 for city only, 2 for country only and 3 for both
    */
    private int cityFilterStatus() {
        int status = 0;
        
        if (cityFilterTextField.getText() != null && !cityFilterTextField.getText().isEmpty()) {
            status = status + 1;
        }
        
        if (countryFilterCityTableTextField.getText() != null && !countryFilterCityTableTextField.getText().isEmpty()) {
            status = status + 2;
        }
        
        return status;
    }
    
    private void createNewCity() {
        City newCity = new City(cityFormCityTextField.getText(),cityFormCountryComboBox.getValue());
        runInsertCityService(newCity);
    }
    
    private void createNewCountry() {
        Country newCountry = new Country(countryFormCountryTextField.getText());
        runInsertCountryService(newCountry);
    }
    
    private void closeCityForm() {
        stopCityFormChangeListeners();
        cityFormCityTextField.setText("");
        cityFormCountryComboBox.getSelectionModel().clearSelection();
        cityFormCityTextField.getStyleClass().removeAll("error-border");
        cityFormCountryComboBox.getStyleClass().removeAll("error-border");
        cityFormCityErrorLabel.setText("");
        cityFormCountryErrorLabel.setText("");
        cityFormPane.setVisible(false);
    }
    
    private void closeCountryForm() {
        stopCountryFormChangeListeners();
        countryFormCountryTextField.setText("");
        countryFormCountryTextField.getStyleClass().removeAll("error-border");
        countryFormCountryErrorLabel.setText("");
        countryFormPane.setVisible(false);
    }
    
    public boolean canExit() {
        return addCityButton.isVisible();
    }
    
// Event handlers
    @FXML
    private void handleClearCityFilterImageView() {
        cityFilterTextField.setText("");
    }
    
    @FXML
    private void handleClearCityCountryFilterImageView() {
        countryFilterCityTableTextField.setText("");
    }
    @FXML
    private void clearCountryFilterImageView() {
        countryFilterCountryTableTextField.setText("");
    }
    
    @FXML
    private void handleAddCityButton() {
        currentFormMode = INSERT;
        cityFormPane.setVisible(true);
        startCityFormChangeListeners();
    }
    @FXML
    private void handleAddCityFormCancelButton() {
        closeCityForm();
    }
    @FXML
    private void handleAddCityFormSubmitButton() {
        runCheckIfCityExistsService(cityFormCityTextField.getText(), 
                                    cityFormCountryComboBox.getValue().getCountryId(), 
                                    currentFormMode);
    }
    @FXML
    private void handleAddCountryButton() {
        currentFormMode = INSERT;
        countryFormPane.setVisible(true);
        startCountryFormChangeListeners();
    }
    @FXML
    private void handleAddCountryFormCancelButton() {
        closeCountryForm();
    }
    @FXML
    private void handleAddCountryFormSubmitButton() {
        queryType requestType;
        runCheckIfCountryExistsService(countryFormCountryTextField.getText(), currentFormMode);
    }
    
    private void handleEditCityButton(City city) {
        currentFormMode = UPDATE;
        editCity = city.copy();
        cityFormPane.setVisible(true);
        cityFormCityTextField.setText(editCity.getCity());
        cityFormCountryComboBox.setValue(editCity.getCountry());
    }

    private void handleDeleteCityButton(City city) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirm City Delete");
        alert.setHeaderText("");
        alert.setContentText("Are you sure you want to delete " + city + " from the database?\n\n" 
                + "Press OK to confirm delete or CANCEL to return.");
        alert.showAndWait();
        if (alert.getResult().equals(ButtonType.OK)) {
            runDeleteCityService(city);
        }
    }
    
    private void handleEditCountryButton(Country country) {
        currentFormMode = UPDATE;
        editCountry = country.copy();
        countryFormPane.setVisible(true);
        countryFormCountryTextField.setText(editCountry.getCountry());
    }
    
    private void handleDeleteCountryButton(Country country) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirm Country Delete");
        alert.setHeaderText("");
        alert.setContentText("Are you sure you want to delete " + country.getCountry() + " from the database?\n\n" 
                + "Press OK to confirm delete or CANCEL to return.");
        alert.showAndWait();
        if (alert.getResult().equals(ButtonType.OK)) {
            runDeleteCountryService(country);
        }
    }
    
// Change listeners
    private void startTableFilterChangeListeners() {
        // lambda
        cityFilterTextField.textProperty().addListener((observable) -> {
            applyCityFilters();
        });
        
        countryFilterCityTableTextField.textProperty().addListener((observable) -> {
            applyCityFilters();
        });
        
        countryFilterCountryTableTextField.textProperty().addListener((observable, oldValue, newValue) -> {
            filteredCountries.setPredicate(country -> {
                if (newValue == null || newValue.isEmpty()) {
                    return true;
                }
                
                String lowerCaseFilter = newValue.toLowerCase();
                
                return country.getCountry().contains(lowerCaseFilter);
            });
        });
    }
    
    private void startCityFormChangeListeners() {
        startCityFormCityListener();
        startCityFormCountryListener();
    }
    
    private void startCountryFormChangeListeners() {
        startCountryFormCountryListener();
    }
    
    private void stopCityFormChangeListeners() {
        stopCityFormCityListener();
        stopCityFormCountryListener();
    }
    
    private void stopCountryFormChangeListeners() {
        stopCountryFormCountryListener();
    }
    
    private void startCityFormCityListener() {
        if (cityFormCityListener == null) {
            cityFormCityListener = (observable, oldValue, newValue) -> {
                if(!newValue) {
                    if (cityFormCityTextField.getText() == null || cityFormCityTextField.getText().isEmpty()) {
                        cityFormCityErrorLabel.setText("City name required");
                        if (!cityFormCityTextField.getStyleClass().contains("error-border")) {
                            cityFormCityTextField.getStyleClass().add("error-border");
                        }
                        cityFormCityTextField.requestFocus();
                    } else {
                        cityFormCityErrorLabel.setText("");
                        cityFormCityTextField.getStyleClass().removeAll("error-border");
                    }
                }
            };
        }
        cityFormCityTextField.focusedProperty().addListener(cityFormCityListener);
    }
    
    private void startCityFormCountryListener() {
        if (cityFormCountryListener == null) {
            cityFormCountryListener = (observable, oldValue, newValue) -> {
                if (!newValue) {
                    if (cityFormCountryComboBox.getSelectionModel().isEmpty()) {
                        cityFormCountryErrorLabel.setText("Country required");
                        if (!cityFormCountryComboBox.getStyleClass().contains("error-border")) {
                            cityFormCountryComboBox.getStyleClass().add("error-border");
                        }
                        cityFormCountryComboBox.requestFocus();
                    } else {
                        cityFormCountryErrorLabel.setText("");
                        cityFormCountryComboBox.getStyleClass().removeAll("error-border");
                    }
                }
            };
        }
        cityFormCountryComboBox.focusedProperty().addListener(cityFormCountryListener);
    }
    
    private void startCountryFormCountryListener() {
        if (countryFormCountryListener == null) {
            countryFormCountryListener = (observable, oldValue, newValue) -> {
                if(!newValue) {
                    if (countryFormCountryTextField.getText() == null || countryFormCountryTextField.getText().isEmpty()) {
                        countryFormCountryErrorLabel.setText("Country name required");
                        if (!countryFormCountryTextField.getStyleClass().contains("error-border")) {
                            countryFormCountryTextField.getStyleClass().add("error-border");
                        }
                        countryFormCountryTextField.requestFocus();
                    } else {
                        countryFormCountryErrorLabel.setText("");
                        countryFormCountryTextField.getStyleClass().removeAll("error-border");
                    }
                }
            };
        }
        countryFormCountryTextField.focusedProperty().addListener(countryFormCountryListener);
    }
    
    private void stopCityFormCityListener() {
        cityFormCityTextField.focusedProperty().removeListener(cityFormCityListener);
    }
    
    private void stopCityFormCountryListener() {
        cityFormCountryComboBox.focusedProperty().removeListener(cityFormCountryListener);
    }
    
    private void stopCountryFormCountryListener() {
        countryFormCountryTextField.focusedProperty().removeListener(countryFormCountryListener);
    }

// Service Methods
    private void initGetAllCitiesService() {
        getAllCitiesService.setOnSucceeded((event) -> {
            allCities = getAllCitiesService.getValue();
            loadCityTable();
        });
        
        getAllCitiesService.setOnFailed((event) -> {
            System.out.println("all cities failed: " + event.getSource().getException().getMessage());
        });
    }

    private void initGetAllCountriesService() {
        getAllCountriesService.setOnSucceeded((event) -> {
            allCountries = getAllCountriesService.getValue();
            cityFormCountryComboBox.setItems(allCountries);
            loadCountryTable();
        });
        
        getAllCountriesService.setOnFailed((event) -> {
            System.out.println("all countries failed: " + event.getSource().getException().getMessage());

        });
    }

    private void initInsertCityService() {
        insertCityService.setOnSucceeded((event) -> {
            statusLabel.setText("City added successfully");
            cityAdded = true;
            closeCityForm();
            runGetAllCitiesService();
        });
        
        insertCityService.setOnFailed((event) -> {
            statusLabel.setText("Adding of city failed");
            System.out.println(event.getSource().getException().getMessage());
        });
    }

    private void initInsertCountryService() {
        insertCountryService.setOnSucceeded((event) -> {
            statusLabel.setText("Country added successfully");
            closeCountryForm();
            runGetAllCountriesService();
        });
        
        insertCountryService.setOnFailed((event) -> {
            statusLabel.setText("Adding country failed");
            System.out.println(event.getSource().getException().getMessage());
        });
    }

    private void initUpdateCityService() {
        updateCityService.setOnSucceeded((event) -> {
            statusLabel.setText("Updated city successfully");
            closeCityForm();
            loadCityTable();
        });
        
        updateCityService.setOnFailed((event) -> {
            statusLabel.setText("Update of city failed");
            System.out.println(event.getSource().getException().getMessage());
        });
    }

    private void initUpdateCountryService() {
        updateCountryService.setOnSucceeded((event) -> {
            statusLabel.setText("Updated country successfully");
            closeCountryForm();
            loadCountryTable();
        });
        
        updateCountryService.setOnFailed((event) -> {
            statusLabel.setText("Update of country failed");
            System.out.println("Update country failed: " + event.getSource().getException().getMessage());
        });
    }
    
    private void initDeleteCityService() {
        deleteCityService.setOnSucceeded((event) -> {
            runGetAllCitiesService();
        });
        
        deleteCityService.setOnFailed((event) -> {
            if (event.getSource().getException().getMessage().startsWith("Cannot delete")) {
                statusLabel.setText(event.getSource().getException().getMessage());
            } else {
                System.out.println(event.getSource().getException().getMessage());
            }
        });
    }

    private void initDeleteCountryService() {
        deleteCountryService.setOnSucceeded((event) -> {
            runGetAllCountriesService();
        });
        
        deleteCountryService.setOnFailed((event) -> {
            if (event.getSource().getException().getMessage().startsWith("Cannot delete")) {
                statusLabel.setText(event.getSource().getException().getMessage());
            } else {
                System.out.println(event.getSource().getException().getMessage());
            }
        });
    }
    
    private void initCheckIfCityExistsService() {
        checkIfCityExistsService.setOnSucceeded((event) -> {
            if (checkIfCityExistsService.getRequestType().equals(INSERT)) {
                if (checkIfCityExistsService.getValue()) {
                    cityFormCityErrorLabel.setText("City already exists in country selected");
                    cityFormCityTextField.getStyleClass().add("error-border");
                    cityFormCityTextField.requestFocus();
                } else {
                    createNewCity();
                }
                return;
            }
            
            if (checkIfCityExistsService.getValue()) {
                cityFormCityErrorLabel.setText("City already exists in country selected");
                cityFormCityTextField.getStyleClass().add("error-border");
                cityFormCityTextField.requestFocus();
            } else {
               editCity.setCity(cityFormCityTextField.getText());
               editCity.setCountry(cityFormCountryComboBox.getValue());
               runUpdateCityService(editCity);
            }
        });
        
        checkIfCityExistsService.setOnFailed((event) -> {
            statusLabel.setText(event.getSource().getException().getMessage());
            System.out.println(event.getSource().getException().getMessage());
        });
    }
    
    private void initCheckIfCountryExistsService() {
        checkIfCountryExistsService.setOnSucceeded((event) -> {
            if (checkIfCountryExistsService.getRequestType().equals(INSERT)) {
                if (checkIfCountryExistsService.getValue()) {
                    countryFormCountryErrorLabel.setText("Country already exists");
                    countryFormCountryTextField.getStyleClass().add("error-border");
                    countryFormCountryTextField.requestFocus();
                } else {
                    createNewCountry();
                }
            }
            
            if (checkIfCountryExistsService.getValue()) {
                countryFormCountryErrorLabel.setText("Country already exists");
                countryFormCountryTextField.getStyleClass().add("error-border");
                countryFormCountryTextField.requestFocus();
            } else {
                editCountry.setCountry(countryFormCountryTextField.getText());
                runUpdateCountryService(editCountry);
            }
        });
        
        checkIfCountryExistsService.setOnFailed((event) -> {
            statusLabel.setText(event.getSource().getException().getMessage());
            System.out.println(event.getSource().getException().getMessage());
        });
    }

    private void runGetAllCitiesService() {
        if (!getAllCitiesService.isRunning()) {
            getAllCitiesService.reset();
            getAllCitiesService.start();
        }
    }

    private void runGetAllCountriesService() {
        if (!getAllCountriesService.isRunning()) {
            getAllCountriesService.reset();
            getAllCountriesService.start();
        }
    }

    private void runInsertCityService(City city) {
        if (!insertCityService.isRunning()) {
            insertCityService.reset();
            insertCityService.setCity(city);
            insertCityService.start();
        }
    }

    private void runInsertCountryService(Country country) {
        if (!insertCountryService.isRunning()) {
            insertCountryService.reset();
            insertCountryService.setCountry(country);
            insertCountryService.start();
        }
    }

    private void runUpdateCityService(City city) {
        if (!updateCityService.isRunning()) {
            updateCityService.reset();
            updateCityService.setCity(city);
            updateCityService.start();
        }
    }

    private void runUpdateCountryService(Country country) {
        if (!updateCountryService.isRunning()) {
            updateCountryService.reset();
            updateCountryService.setCountry(country);
            updateCountryService.start();
        }
    }
    
    private void runDeleteCityService(City city) {
        if (!deleteCityService.isRunning()) {
            deleteCityService.reset();
            deleteCityService.setCity(city);
            deleteCityService.start();
        }
    }

    private void runDeleteCountryService(Country country) {
        if (!deleteCountryService.isRunning()) {
            deleteCountryService.reset();
            deleteCountryService.setCountry(country);
            deleteCountryService.start();
        }
    }
    
    private void runCheckIfCityExistsService(String cityName, int countryId, queryType requestType) {
        if (!checkIfCityExistsService.isRunning()) {
            checkIfCityExistsService.reset();
            checkIfCityExistsService.setCityName(cityName);
            checkIfCityExistsService.setCountryId(countryId);
            checkIfCityExistsService.setRequestType(requestType);
            checkIfCityExistsService.start();
        }
    }
    
    private void runCheckIfCountryExistsService(String countryName, queryType requestType) {
        if (!checkIfCountryExistsService.isRunning()) {
            checkIfCountryExistsService.reset();
            checkIfCountryExistsService.setCountryName(countryName);
            checkIfCountryExistsService.setRequestType(requestType);
            checkIfCountryExistsService.start();
        }
    }

}
