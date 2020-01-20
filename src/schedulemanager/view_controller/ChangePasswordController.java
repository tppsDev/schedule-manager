/*
 * Project written by: Tim Smith
 * 
 */
package schedulemanager.view_controller;

import java.net.URL;
import java.util.ResourceBundle;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import schedulemanager.model.User;

/**
 * FXML Controller class
 *
 * @author Tim Smith
 */
public class ChangePasswordController implements Initializable {
    @FXML private StackPane userStackPane;
    @FXML private AnchorPane currentUserPane;
    @FXML private AnchorPane otherUserPane;
    @FXML private Button cancelButton;
    @FXML private Button changePasswordButton;
    @FXML private Label currentUserConfirmPasswordErrorLabel;
    @FXML private Label currentUserNewPasswordErrorLabel;
    @FXML private Label oldPasswordErrorLabel;
    @FXML private Label otherUserConfirmPasswordErrorLabel;
    @FXML private Label otherUserNewPasswordErrorLabel;
    @FXML private Label updatePasswordErrorLabel;
    @FXML private PasswordField oldPasswordField;
    @FXML private PasswordField currentUserNewPasswordField;
    @FXML private PasswordField currentUserConfirmPasswordField;
    @FXML private PasswordField otherUserNewPasswordField;
    @FXML private PasswordField otherUserConfirmPasswordField;
    @FXML private TextField oldUnmaskedPasswordField;
    @FXML private TextField currentUserNewUnmaskedPasswordField;
    @FXML private TextField currentUserConfirmUnmaskedPasswordField;
    @FXML private TextField otherUserNewUnmaskedPasswordField;
    @FXML private TextField otherUserConfirmUnmaskedPasswordField;
    @FXML private CheckBox showPasswordCheckBox;
    @FXML private ProgressIndicator progressIndicator;
    
    private final BooleanProperty userChanged = new SimpleBooleanProperty(false);
    private final boolean currentUser;
    private final String originalPassword;
    private Session session = Session.getSession();
    private DBServiceManager dbServiceManager = new DBServiceManager();
    private DBServiceManager.UpdateUserService updateUserService;
    private User user;
    private Stage stage;

    public ChangePasswordController() {
        user = session.getSessionUser();
        currentUser = true;
        originalPassword = user.getPassword();
    }
    
    public ChangePasswordController(User user) {
        this.user = user;
        currentUser = false;
        originalPassword = user.getPassword();
    }
    
    /**
     * Initializes the controller class.
     * @param url
     * @param rb
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
       
        establishBindings();
        initUpdateUserService();
        if (currentUser) {
            currentUserPane.toFront();
            oldPasswordField.requestFocus();
        } else {
            otherUserPane.toFront();
            otherUserNewPasswordField.requestFocus();
        }
        startFocusChangeListeners();
    }
    
// Buttons Handlers
    public void handleCancelButton() {
        closeStage();
    }
    
    public void handleChangePasswordButton() {
        if (currentUser) {
            if (!checkPassword(oldPasswordField.getText())) {
                oldPasswordErrorLabel.setText("Incorrect password");
                return;
            }
            if (!currentUserNewPasswordField.getText().equals(currentUserConfirmPasswordField.getText())) {
                currentUserNewPasswordErrorLabel.setText("Passwords do not match");
                currentUserConfirmPasswordErrorLabel.setText("Passwords do not match");
                return;
            }
            user.setPassword(currentUserNewPasswordField.getText());
        } else {
            if (!otherUserNewPasswordField.getText().equals(otherUserConfirmPasswordField.getText())) {
                otherUserNewPasswordErrorLabel.setText("Passwords do not match");
                otherUserConfirmPasswordErrorLabel.setText("Passwords do not match");
                return;
            }
            user.setPassword(otherUserNewPasswordField.getText());
        }
        runUpdateUserService();
    }
    
// Binding Methods    
    public void establishBindings() {
        setPasswordFieldBindings();
        setChangePasswordButtonBindings();
        setShowPasswordBindings();
    }
    
    public void setPasswordFieldBindings() {
        oldPasswordField.textProperty()
                .bindBidirectional(oldUnmaskedPasswordField.textProperty());
        
        currentUserNewPasswordField.textProperty()
                .bindBidirectional(currentUserNewUnmaskedPasswordField.textProperty());
        
        currentUserConfirmPasswordField.textProperty()
                .bindBidirectional(currentUserConfirmUnmaskedPasswordField.textProperty());
        
        otherUserNewPasswordField.textProperty()
                .bindBidirectional(otherUserNewUnmaskedPasswordField.textProperty());
        
        otherUserConfirmPasswordField.textProperty()
                .bindBidirectional(otherUserConfirmUnmaskedPasswordField.textProperty());
    }
    
    public void setShowPasswordBindings() {
        oldPasswordField.visibleProperty().bind(showPasswordCheckBox.selectedProperty().not());
        oldUnmaskedPasswordField.visibleProperty().bind(showPasswordCheckBox.selectedProperty().not());
        
        currentUserNewPasswordField.visibleProperty().bind(showPasswordCheckBox.selectedProperty().not());
        currentUserNewUnmaskedPasswordField.visibleProperty().bind(showPasswordCheckBox.selectedProperty());
        
        currentUserConfirmPasswordField.visibleProperty().bind(showPasswordCheckBox.selectedProperty().not());
        currentUserConfirmUnmaskedPasswordField.visibleProperty().bind(showPasswordCheckBox.selectedProperty());
        
        otherUserNewPasswordField.visibleProperty().bind(showPasswordCheckBox.selectedProperty().not());
        otherUserNewUnmaskedPasswordField.visibleProperty().bind(showPasswordCheckBox.selectedProperty());
        
        otherUserConfirmPasswordField.visibleProperty().bind(showPasswordCheckBox.selectedProperty().not());
        otherUserConfirmUnmaskedPasswordField.visibleProperty().bind(showPasswordCheckBox.selectedProperty());
    }
    
    public void setChangePasswordButtonBindings() {
        BooleanBinding changePasswordBinding;
        if (currentUser) {
            changePasswordBinding = oldPasswordField.textProperty().isNotEmpty()
                                .and(oldPasswordField.textProperty().isNotNull())
                                .and(currentUserNewPasswordField.textProperty().isNotEmpty())
                                .and(currentUserNewPasswordField.textProperty().isNotNull())
                                .and(currentUserConfirmPasswordField.textProperty().isNotEmpty())
                                .and(currentUserConfirmPasswordField.textProperty().isNotNull());
        } else {
            changePasswordBinding = otherUserNewPasswordField.textProperty().isNotEmpty()
                                .and(otherUserNewPasswordField.textProperty().isNotNull())
                                .and(otherUserConfirmPasswordField.textProperty().isNotEmpty())
                                .and(otherUserConfirmPasswordField.textProperty().isNotNull());
        }
        changePasswordButton.visibleProperty().bind(changePasswordBinding);
    }

// Utility methods    
    public void clearAllFields() {
        if (currentUser) {
            oldPasswordField.setText("");
            currentUserNewPasswordField.setText("");
            currentUserConfirmPasswordField.setText("");
            oldPasswordField.requestFocus();
        } else {
            otherUserNewPasswordField.setText("");
            otherUserConfirmPasswordField.setText("");
            otherUserConfirmPasswordErrorLabel.requestFocus();
        }
    }
    
    public boolean checkPassword(String password) {
        return password.equals(user.getPassword());
    }
    
    public void closeStage() {
         stage = (Stage) cancelButton.getScene().getWindow();
         stage.close();
    }
    
// Service Methods
    public void initUpdateUserService() {
        updateUserService = dbServiceManager.new UpdateUserService();
        updateUserService.setUser(user);
        progressIndicator.visibleProperty().bind(updateUserService.runningProperty());
        updateUserService.setOnSucceeded((event) -> {
            // TODO
            userChanged.set(true);
            closeStage();
        });
        updateUserService.setOnFailed((event) -> {
            user.setPassword(originalPassword);
            updatePasswordErrorLabel.setText("System error changing password, please try again.");
            clearAllFields();
        });
    }
    
    public void runUpdateUserService() {
        updateUserService.reset();
        updateUserService.start();
    }
 
// JavaFX Property Methods
    public boolean isUserChanged() {
        return userChanged.get();
    }

    public void setUserChanged(boolean value) {
        userChanged.set(value);
    }

    public BooleanProperty userChangedProperty() {
        return userChanged;
    }
    
// Focus Change Listeners
    public void startFocusChangeListeners() {
        oldPasswordField.focusedProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue) {
                if (oldPasswordField.getText() == null || oldPasswordField.getText().isEmpty()) {
                    oldPasswordErrorLabel.setText("Password required");
                } else {
                    oldPasswordErrorLabel.setText("");
                }
            }
        });
        
        oldUnmaskedPasswordField.focusedProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue) {
                if (oldPasswordField.getText() == null || oldPasswordField.getText().isEmpty()) {
                    oldPasswordErrorLabel.setText("Password required");
                } else {
                    oldPasswordErrorLabel.setText("");
                }
            }
        });
        
        currentUserNewPasswordField.focusedProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue) {
                if (currentUserNewPasswordField.getText() == null || currentUserNewPasswordField.getText().isEmpty()) {
                    currentUserNewPasswordErrorLabel.setText("New Password required");
                } else {
                    currentUserNewPasswordErrorLabel.setText("");
                }
            }
        });
        
        currentUserNewUnmaskedPasswordField.focusedProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue) {
                if (currentUserNewPasswordField.getText() == null || currentUserNewPasswordField.getText().isEmpty()) {
                    currentUserNewPasswordErrorLabel.setText("New Password required");
                } else {
                    currentUserNewPasswordErrorLabel.setText("");
                }
            }
        });
        
        currentUserConfirmPasswordField.focusedProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue) {
                if (currentUserConfirmPasswordField.getText() == null || currentUserConfirmPasswordField.getText().isEmpty()) {
                    currentUserConfirmPasswordErrorLabel.setText("Confirm New Password required");
                } else {
                    currentUserConfirmPasswordErrorLabel.setText("");
                }
            }
        });
        
        currentUserConfirmUnmaskedPasswordField.focusedProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue) {
                if (currentUserConfirmPasswordField.getText() == null || currentUserConfirmPasswordField.getText().isEmpty()) {
                    currentUserConfirmPasswordErrorLabel.setText("Confirm New Password required");
                } else {
                    currentUserConfirmPasswordErrorLabel.setText("");
                }
            }
        });
        
        otherUserNewPasswordField.focusedProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue) {
                if (otherUserNewPasswordField.getText() == null || otherUserNewPasswordField.getText().isEmpty()) {
                    otherUserNewPasswordErrorLabel.setText("New Password required");
                } else {
                    otherUserNewPasswordErrorLabel.setText("");
                }
            }
        });
        
        otherUserNewUnmaskedPasswordField.focusedProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue) {
                if (otherUserNewPasswordField.getText() == null || otherUserNewPasswordField.getText().isEmpty()) {
                    otherUserNewPasswordErrorLabel.setText("New Password required");
                } else {
                    otherUserNewPasswordErrorLabel.setText("");
                }
            }
        });
        
        otherUserConfirmPasswordField.focusedProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue) {
                if (otherUserConfirmPasswordField.getText() == null || otherUserConfirmPasswordField.getText().isEmpty()) {
                    otherUserConfirmPasswordErrorLabel.setText("Confirm New Password required");
                } else {
                    otherUserConfirmPasswordErrorLabel.setText("");
                }
            }
        });
        
        otherUserConfirmUnmaskedPasswordField.focusedProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue) {
                if (otherUserConfirmPasswordField.getText() == null || otherUserConfirmPasswordField.getText().isEmpty()) {
                    otherUserConfirmPasswordErrorLabel.setText("Confirm New Password required");
                } else {
                    otherUserConfirmPasswordErrorLabel.setText("");
                }
            }
        });
    }
}
