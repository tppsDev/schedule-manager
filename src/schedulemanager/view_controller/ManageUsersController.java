/*
 * Project written by: Tim Smith
 * 
 */
package schedulemanager.view_controller;

import java.io.IOException;
import java.net.URL;
import java.time.LocalDateTime;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.fxml.Initializable;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import schedulemanager.DAO.SysOp.queryType;
import schedulemanager.model.User;

/**
 * FXML Controller class
 *
 * @author Tim Smith
 */
public class ManageUsersController implements Initializable {
// JavaFX Controls
    // Panes
    @FXML private AnchorPane addUserPane;
    @FXML private AnchorPane editUserPane;
    
    // Labels
    @FXML private Label currentUserLabel;
    @FXML private Label totalUsersLabel;
    @FXML private Label totalActiveUsersLabel;
    @FXML private Label addFormUserNameErrorLabel;
    @FXML private Label addFormPasswordErrorLabel;
    @FXML private Label addFormConfirmPasswordErrorLabel;
    @FXML private Label statusLabel;
    @FXML private Label editFormUserNameErrorLabel;
    
    // TextFields
    @FXML private TextField userFilterTextBox;
    @FXML private TextField addFormUserNameTextField;
    @FXML private TextField editFormUserNameTextField;
    
    // PasswordFields
    @FXML private PasswordField addFormPasswordField;
    @FXML private PasswordField addFormConfirmPasswordField;

    
    // ImageViews
    @FXML private ImageView clearFilterImageView;

    // CheckBoxes
    @FXML private CheckBox includeInactiveCheckBox;
    @FXML private CheckBox addFormActiveCheckBox;
    @FXML private CheckBox editFormActiveCheckBox;

    // Buttons
    @FXML private Button addUserButton;
    @FXML private Button closeButton;
    @FXML private Button addFormSubmitButton;
    @FXML private Button addFormCancelButton;
    @FXML private Button editFormSaveButton;
    @FXML private Button editFormCancelButton;

    // TableView & TableColumns
    @FXML private TableView<User> userTableView;
    @FXML private TableColumn<User, User> editColumn;
    @FXML private TableColumn<User, User> deleteColumn;
    @FXML private TableColumn<User, User> changePasswordColumn;
    @FXML private TableColumn<User, String> userNameColumn;
    @FXML private TableColumn<User, Boolean> activeColumn;
    @FXML private TableColumn<User, LocalDateTime> createDateColumn;
    @FXML private TableColumn<User, String> createdByColumn;
    @FXML private TableColumn<User, LocalDateTime> lastUpdateColumn;
    @FXML private TableColumn<User, String> lastUpdateByColumn;

    // Progress Indicator
    @FXML private ProgressIndicator progressIndicator;

// Member properties
    private DBServiceManager dbServiceMgr = new DBServiceManager();
    private DBServiceManager.GetAllUsersService getAllUsersService = dbServiceMgr.new GetAllUsersService();
    private DBServiceManager.GetActiveUsersService getActiveUsersService = dbServiceMgr.new GetActiveUsersService();
    private DBServiceManager.GetIfUserNamerExistsService getIfUserNameExistsService 
            = dbServiceMgr.new GetIfUserNamerExistsService();
    private DBServiceManager.InsertUserService insertUserService = dbServiceMgr.new InsertUserService();
    private DBServiceManager.UpdateUserService updateUserService = dbServiceMgr.new UpdateUserService();
    private DBServiceManager.DeleteUserService deleteUserService = dbServiceMgr.new DeleteUserService();
    private ObservableList<User> allUsers = FXCollections.observableArrayList();
    ObservableList<User> activeUsers = FXCollections.observableArrayList();;
    private FilteredList<User> filteredUsers;
    private SortedList<User> sortedUsers;
    private User editUser;
    private User addUser;
    private Session session = Session.getSession();
    
    /**
     * Initializes the controller class.
     * @param url
     * @param rb
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        addUserPane.setVisible(false);
        editUserPane.setVisible(false);
        includeInactiveCheckBox.setSelected(true);
        establishBindings();
        initTable();
        initGetAllUsersService();
        initGetActiveUsersService();
        initGetIfUserNameExistsService();
        initInsertUserService();
        initUpdateUserService();
        initDeleteUserService();
        runGetAllUsersService();
        startChangeListeners();
        currentUserLabel.setText(session.getSessionUser().getUserName());
    }    
    
// Initialization Methods
    public void initTable() {
        setEditColumn();
        setDeleteColumn();
        setChangePasswordColumn();
        setUserNameColumn();
        setActiveColumn();
        setCreateDateColumn();
        setCreatedByColumn();
        setLastUpdateColumn();
        setLastUpdateByColumn();
    }
    
// Bindings
    public void establishBindings() {
        setProgressIndicatorBindings();
        addUserButton.visibleProperty()
                .bind(addUserPane.visibleProperty().not()
                .and(editUserPane.visibleProperty().not()));
        
        closeButton.visibleProperty()
                .bind(addUserPane.visibleProperty().not()
                .and(editUserPane.visibleProperty().not()));
        
        userTableView.disableProperty()
                .bind(editUserPane.visibleProperty()
                .or(addUserPane.visibleProperty()));
    }
    
    public void setProgressIndicatorBindings() {
        BooleanBinding progIndBinding = getAllUsersService.runningProperty()
                                    .or(getActiveUsersService.runningProperty())
                                    .or(getIfUserNameExistsService.runningProperty())
                                    .or(insertUserService.runningProperty())
                                    .or(updateUserService.runningProperty())
                                    .or(deleteUserService.runningProperty());
        
        progressIndicator.visibleProperty().bind(progIndBinding);
    }

// TableView & Column Methods
    public void setEditColumn() {
        editColumn.setCellValueFactory((param) -> new ReadOnlyObjectWrapper<>(param.getValue()));
        editColumn.setStyle("-fx-alignment: CENTER;");
        editColumn.setCellFactory(param -> new TableCell<User, User>(){
            private Button editButton = new Button(null);

            @Override
            protected void updateItem(User user, boolean empty) {
                super.updateItem(user, empty);
                
                if (user == null) {
                    setGraphic(null);
                    return;
                }
                editButton.getStyleClass().add("table-edit-button");
                setGraphic(editButton);
                editButton.setOnAction((event) -> {
                    handleEditButton(user);
                });
            }
        });
    }
    
    public void setDeleteColumn() {
        deleteColumn.setCellValueFactory((param) -> new ReadOnlyObjectWrapper<>(param.getValue()));
        deleteColumn.setStyle("-fx-alignment: CENTER;");
        deleteColumn.setCellFactory(param -> new TableCell<User, User>(){
            private final Button deleteButton = new Button();

            @Override
            protected void updateItem(User user, boolean empty) {
                super.updateItem(user, empty);
                
                if (user == null) {
                    setGraphic(null);
                    return;
                }
                deleteButton.getStyleClass().add("table-delete-button");
                setGraphic(deleteButton);
                
                deleteButton.setOnAction((event) -> {
                    handleDeleteButton(user);
                });
            }
        });
    }
    
    public void setChangePasswordColumn() {
        changePasswordColumn.setCellValueFactory((param) -> new ReadOnlyObjectWrapper<>(param.getValue()));
        changePasswordColumn.setStyle("-fx-alignment: CENTER;");
        changePasswordColumn.setCellFactory(param -> new TableCell<User, User>(){
            private final Button changePasswordButton = new Button();

            @Override
            protected void updateItem(User user, boolean empty) {
                super.updateItem(user, empty);
                
                if (user == null) {
                    setGraphic(null);
                    return;
                }
                changePasswordButton.getStyleClass().add("table-change-password-button");
                setGraphic(changePasswordButton);
                
                changePasswordButton.setOnAction((event) -> {
                    handleChangePasswordButton(user);
                });
            }
        });
    }
    
    public void setUserNameColumn() {
        userNameColumn.setCellValueFactory(new PropertyValueFactory<>("userName"));
    }
    
    public void setActiveColumn() {
        activeColumn.setCellValueFactory(new PropertyValueFactory<>("active"));
        activeColumn.setStyle("-fx-alignment: CENTER;");
        activeColumn.setCellFactory(colVal -> new TableCell<User, Boolean>() {
            @Override
            protected void updateItem(Boolean active, boolean empty) {
                super.updateItem(active, empty);
                if (empty) {
                    setText(null);
                    return;
                }
                if (active) {
                    setText("Yes");
                } else {
                    setText("No");
                }
            }
        });
    }
    
    public void setCreateDateColumn() {
        createDateColumn.setCellValueFactory(new PropertyValueFactory<>("createDate"));
    }
    
    public void setCreatedByColumn() {
        createdByColumn.setCellValueFactory(new PropertyValueFactory<>("createdBy"));
    }
    
    public void setLastUpdateColumn() {
        lastUpdateColumn.setCellValueFactory(new PropertyValueFactory<>("lastUpdate"));
    }
    
    public void setLastUpdateByColumn() {
        lastUpdateByColumn.setCellValueFactory(new PropertyValueFactory<>("lastUpdateBy"));
    }
    
    public void loadTable() {
        ObservableList<User> userList;
        if (includeInactiveCheckBox.isSelected()) {
            userList = allUsers;
        } else {
            userList = activeUsers;
        }
        filteredUsers = new FilteredList<>(userList, pred -> true);
        sortedUsers = new SortedList<>(filteredUsers);
        if (userFilterTextBox.getText() != null || !userFilterTextBox.getText().isEmpty()) {
            String backup = userFilterTextBox.getText();
            userFilterTextBox.setText("");
            userFilterTextBox.setText(backup);
        }
        userTableView.setItems(sortedUsers);
        userTableView.getColumns().get(0).setVisible(false);
        userTableView.getColumns().get(0).setVisible(true);
    }
    
    public void createAddUser() {
        addUser = new User();
        addUser.setUserName(addFormUserNameTextField.getText());
        addUser.setPassword(addFormPasswordField.getText());
        addUser.setActive(addFormActiveCheckBox.isSelected());
        runInsertUserService(addUser);
    }
    
    public void clearAddForm() {
        addFormUserNameTextField.setText("");
        addFormPasswordField.setText("");
        addFormConfirmPasswordField.setText("");
    }
    
    public void clearEditForm() {
        editFormUserNameTextField.setText("");
    }
    
    public void copyUser(User user, User newUser) {
        newUser.setUserId(user.getUserId());
        newUser.setUserName(user.getUserName());
        newUser.setPassword(user.getPassword());
        newUser.setActive(user.getActive());
    }
    
    public boolean canExit() {
        return closeButton.isVisible();
    }
    
// Action/Event Handlers
    // General Buttons
    @FXML
    public void handleClearFilterImageView() {
        userFilterTextBox.setText("");
    }
    
    @FXML
    public void handleAddUserButton() {
        addUserPane.setVisible(true);
    }
    
    @FXML
    public void handleCloseButton() {
        Stage stage = (Stage) closeButton.getScene().getWindow();
        stage.close();
    }
    
    // Table Buttons
    @FXML
    public void handleEditButton(User user) {
        editUser = new User();
        copyUser(user, editUser);
        editFormUserNameTextField.setText(editUser.getUserName());
        editFormActiveCheckBox.setSelected(editUser.getActive());
        editUserPane.setVisible(true);
    }
    
    @FXML
    public void handleDeleteButton(User user) {
        runDeleteUserService(user);
    }
    
    @FXML
    public void handleChangePasswordButton(User user) {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("ChangePassword.fxml"));
        ChangePasswordController cpController = new ChangePasswordController(user);
        loader.setController(cpController);
        Scene cpScene;
        try {
            cpScene = new Scene(loader.load());
        } catch (IOException ex) {
            // TODO better handler
            Logger.getLogger(ManageUsersController.class.getName()).log(Level.SEVERE, null, ex);
            return;
        }
        Stage cpStage = new Stage();
        cpStage.initOwner(currentUserLabel.getScene().getWindow());
        cpStage.initModality(Modality.APPLICATION_MODAL);
        cpStage.setTitle("Schedule Manager 1.0 - Change Password");
        cpStage.setScene(cpScene);
        cpStage.showAndWait();
    }
    
    // Add Form Buttons
    @FXML
    public void handleAddFormSubmitButton() {
        if (addFormUserNameTextField.getText() == null || addFormUserNameTextField.getText().isEmpty()) {
            addFormUserNameErrorLabel.setText("Username required");
            addFormUserNameTextField.requestFocus();
            return;
        }
        
        if (addFormPasswordField.getText() == null || addFormPasswordField.getText().isEmpty()) {
            addFormPasswordErrorLabel.setText("Password required");
            return;
        }
        
        if (addFormConfirmPasswordField.getText() == null || addFormConfirmPasswordField.getText().isEmpty()) {
            addFormConfirmPasswordErrorLabel.setText("Password required");
            return;
        }
        
        if (!addFormPasswordField.getText().equals(addFormConfirmPasswordField.getText())) {
            addFormPasswordErrorLabel.setText("Passwords do not match");
            addFormConfirmPasswordErrorLabel.setText("Passwords do not match");
            return;
        }
        runGetIfUserNameExistsService(addFormUserNameTextField.getText().toLowerCase(), queryType.INSERT);
    }
    
    @FXML
    public void handleAddFormCancelButton() {
        addUserPane.setVisible(false);
        clearAddForm();
    }
    
    // Edit Form Buttons
    @FXML
    public void handleEditFormSubmitButton() {
        if (editFormUserNameTextField.getText() == null || editFormUserNameTextField.getText().isEmpty()) {
            editFormUserNameErrorLabel.setText("Username required");
            editFormUserNameTextField.requestFocus();
            return;
        }
        if (!editUser.getUserName().equals(editFormUserNameTextField.getText())) {
            runGetIfUserNameExistsService(editFormUserNameTextField.getText(), queryType.UPDATE);
            return;
        }
        if (editUser.isActive() == editFormActiveCheckBox.isSelected()) {
            editUserPane.setVisible(false);
            return;
        }
        editUser.setActive(editFormActiveCheckBox.isSelected());
        runUpdateUserService(editUser);
    }
    
    @FXML
    public void handleEditFormCancelButton() {
        editUserPane.setVisible(false);
        clearEditForm();
    }

// Change Listeners
    public void startChangeListeners() {
        // lambda
        userFilterTextBox.textProperty().addListener((observable, oldValue, newValue) -> {
            // lambda
            filteredUsers.setPredicate(user -> {
                if (newValue == null || newValue.isEmpty()) {
                    return true;
                }
                
                String lowerCaseFilter = newValue.toLowerCase();

                return user.getUserName().toLowerCase().contains(lowerCaseFilter);
            });
        });
        // lambda
        includeInactiveCheckBox.selectedProperty().addListener((observable) -> {
            // lambda
            loadTable();
        });
    }
    
// Service Initializers
    public void initGetAllUsersService() {
        // lambda
        getAllUsersService.setOnSucceeded((event) -> {
            allUsers = getAllUsersService.getValue();
            totalUsersLabel.setText(String.valueOf(allUsers.size()));
            runGetActiveUsersService();
            loadTable();
        });
        // lambda
        getAllUsersService.setOnFailed((event) -> {
            System.out.println(getAllUsersService.getException().getMessage());
        });
    }
    
    public void initGetActiveUsersService() {
        // lambda
        getActiveUsersService.setOnSucceeded((event) -> {
            activeUsers = getActiveUsersService.getValue();
            totalActiveUsersLabel.setText(String.valueOf(activeUsers.size()));
        });
        // lambda
        getActiveUsersService.setOnFailed((event) -> {
            System.out.println(getActiveUsersService.getException().getMessage());
        });
    }
    
    public void initGetIfUserNameExistsService() {
        getIfUserNameExistsService.setOnSucceeded((event) -> {
            if (getIfUserNameExistsService.getRequestType().equals(queryType.INSERT)) {
                if (getIfUserNameExistsService.getValue()) {
                    addFormUserNameErrorLabel.setText("User name already used");
                    addFormUserNameTextField.setText("");
                    addFormUserNameTextField.requestFocus();
                } else {
                    createAddUser();
                }
                return;
            }
            if (getIfUserNameExistsService.getValue()) {
                editFormUserNameErrorLabel.setText("User name already used, field reset");
                editFormUserNameTextField.setText(editUser.getUserName());
                editFormUserNameTextField.requestFocus();
            } else {
                if (!editUser.isActive() == editFormActiveCheckBox.isSelected()) {
                    editUser.setActive(editFormActiveCheckBox.isSelected()); 
                }
                editUser.setUserName(editFormUserNameTextField.getText().toLowerCase());              
                runUpdateUserService(editUser);
            }
        });
        getIfUserNameExistsService.setOnFailed((event) -> {
            System.out.println(getIfUserNameExistsService.getException().getMessage());
        });
    }
    
    public void initInsertUserService() {
        insertUserService.setOnSucceeded((event) -> {
            addUserPane.setVisible(false);
            clearAddForm();
            runGetAllUsersService();
        });
        insertUserService.setOnFailed((event) -> {
            System.out.println(insertUserService.getException().getMessage());
        });
    }
    
    public void initUpdateUserService() {
        updateUserService.setOnSucceeded((event) -> {
            runGetAllUsersService();
            clearEditForm();
            editUserPane.setVisible(false);
        });
        updateUserService.setOnFailed((event) -> {
            statusLabel.setText("Update failed: " + updateUserService.getException().getMessage());
        });
    }
    
    public void initDeleteUserService() {
        deleteUserService.setOnSucceeded((event) -> {
            runGetAllUsersService();
        });
        deleteUserService.setOnFailed((event) -> {
            if (event.getSource().getException().getMessage().startsWith("Cannot delete")) {
                statusLabel.setText(event.getSource().getException().getMessage() + " Remove active status instead.");
            } else {
                System.out.println(event.getSource().getException().getMessage());
            }
        });
    }
        
// Service Calls
    public void runGetAllUsersService() {
        if (!getAllUsersService.isRunning()) {
            getAllUsersService.reset();
            getAllUsersService.start();
        }
    }
    
    public void runGetActiveUsersService() {
        if (!getActiveUsersService.isRunning()) {
            getActiveUsersService.reset();
            getActiveUsersService.start();
        }
    }
    
    public void runGetIfUserNameExistsService(String userName, queryType requestType) {
        if (!getIfUserNameExistsService.isRunning()) {
            getIfUserNameExistsService.reset();
            getIfUserNameExistsService.setUser(userName);
            getIfUserNameExistsService.setRequestType(requestType);
            getIfUserNameExistsService.start();
        }
    }
    
    public void runInsertUserService(User user) {
        if (!insertUserService.isRunning()) {
            insertUserService.reset();
            insertUserService.setUser(user);
            insertUserService.start();
        }
    }
    
    public void runUpdateUserService(User updateUser) {
        if (!updateUserService.isRunning()) {
            updateUserService.reset();
            updateUserService.setUser(updateUser);
            updateUserService.start();
        }
    }
    
    public void runDeleteUserService(User user) {
        if (!deleteUserService.isRunning()) {
            deleteUserService.reset();
            deleteUserService.setUser(user);
            deleteUserService.start();
        }
    }
}
