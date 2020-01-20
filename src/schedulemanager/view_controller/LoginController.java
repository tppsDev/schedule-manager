/*
 * Project written by: Tim Smith
 * 
 */
package schedulemanager.view_controller;

import java.io.IOException;
import java.net.URL;
import java.util.Locale;

import java.util.ResourceBundle;
import javafx.concurrent.Service;
import javafx.concurrent.WorkerStateEvent;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.stage.Stage;

/**
 * FXML LoginController class
 *
 * @author Tim Smith
 */
public class LoginController implements Initializable {
    // Buttons
    @FXML private Button loginButton;
    
    // Text Fields
    @FXML private TextField usernameField;
    @FXML private TextField unmaskedPasswordField;
    @FXML private PasswordField maskedPasswordField;
    
    // Labels
    @FXML private Label loginErrorLabel;
    @FXML private Label usernameLabel;
    @FXML private Label passwordLabel;
    
    // Check Box
    @FXML private CheckBox showPasswordCheckBox;
    
    // Progress Indicator
    @FXML private ProgressIndicator loginProgressIndicator;
    
    private LoginService service;
    private Locale locale;
    private Session session = Session.getSession();
    /**
     * Initializes the controller class.
     * @param url
     * @param rb
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        locale = Locale.getDefault();
        if (locale.getLanguage().equals("fr")) {
            convertLabelToFrench();
        }
        // Create instance of the LoginService for UI thread management    
        service = new LoginService();
        
        // Establish bindings
        loginButton.disableProperty().bind(service.runningProperty());
        loginProgressIndicator.visibleProperty().bind(service.runningProperty());

        unmaskedPasswordField.visibleProperty().bind(showPasswordCheckBox.selectedProperty());
        maskedPasswordField.visibleProperty().bind(showPasswordCheckBox.selectedProperty().not());

        unmaskedPasswordField.textProperty().bindBidirectional(maskedPasswordField.textProperty());

        // Use lambda for cleaner code than annonymous inner Class
        service.setOnSucceeded((WorkerStateEvent workerStateEvent) -> {
            try {
                handleLoginResults();
            } catch (LoginFormException e) {
                loginErrorLabel.setText(e.getMessage());
            } catch (IOException e) {
                System.out.println("Some error: " + e.getMessage());
                e.printStackTrace();
            }
        });
        service.setOnFailed((event) -> {
            System.out.println(service.getException().getMessage());
            service.getException().printStackTrace();
        });
    }
    
// Form interaction methods
    /*
     * Retrieves login credentials from the UI, reporting error to UI if fields are not populated
    */
    @FXML
    public void getLoginCredentials() throws LoginFormException {
        // Verify both fields contain data before setting service parameters
        if ((!usernameField.getText().isEmpty() && usernameField.getText() != null)  
                    || (!maskedPasswordField.getText().isEmpty() && maskedPasswordField.getText() != null)) {
            service.setUsername(usernameField.getText());
            service.setPassword(maskedPasswordField.getText());
        } else {
            if (locale.getLanguage().equals("fr")) {
                    throw new LoginFormException("Nom d'utilisateur et mot de passe requis");
            }
            throw new LoginFormException("Username and password required");
        }
    }
    /*
     * Handles the results of the login service and manages UI accordingly.
    */
    @FXML
    public void handleLoginResults() throws LoginFormException, IOException {
        // Check returned success status of service
        if (service.getValue()) {
            // TODO goto main screen and remove text population
                        
            Stage window = (Stage) loginButton.getScene().getWindow();           
            Parent mainScreenParent;
            mainScreenParent = FXMLLoader.load(getClass().getResource("MainScreen.fxml"));
      
            Scene mainScreenScene = new Scene(mainScreenParent);
            
            window.setX(10);
            window.setY(10);
            window.setTitle("Schedule Manager 1.0");
            window.setScene(mainScreenScene);
            window.show();
                


        } else {
            // Handle failed login attempt
            int attemptsRemaining = 3 - Session.getSession().getFailedLoginAttempts();
            maskedPasswordField.clear();
            // TODO update logfile
            // Send error message to UI, after 3 attempts exit
            if (attemptsRemaining > 0) {
                if (locale.getLanguage().equals("fr")) {
                    throw new LoginFormException("Le nom d'utilisateur et le mot de passe ne correspondent pas. "
                            + attemptsRemaining
                            + " autres tentatives avant la sortie.");
                }
                throw new LoginFormException("The username and password do not match. "
                        + attemptsRemaining
                        + " more attempts before exit.");
            } else {
                System.exit(0);
            }
        }
    }
    
    // Button handlers
    /*
     * Handle loginButton onAction event
     * @param event
    */
    @FXML
    public void handleLoginButtonAction (ActionEvent event) {
        // Clear error message from any previous clicks
        loginErrorLabel.setText("");
        // Check login service status ignore all but SUCCEEDED or READY
        if (service.getState() == Service.State.SUCCEEDED || service.getState() == Service.State.READY)  {
            
            if (service.getState() == Service.State.SUCCEEDED) {
                service.reset();  // reset puts service in ready status after previous clicks
            }
            
            // Retrieve the login credentials, handle FormException which will contain message for user
            try {
                getLoginCredentials();
            } catch (LoginFormException e) {
                loginErrorLabel.setText(e.getMessage());
                return;
            }
            // Run the login service
            service.start();
            
        }
    }
    
    public void handleKeyPressed(KeyEvent event) {
        if (event.getCode() == KeyCode.ENTER) {
            loginButton.fire();
        }
    }
    
    private void convertLabelToFrench() {
        usernameLabel.setText("Nom d'utilisateur:");
        passwordLabel.setText("Mot de passe:");
        showPasswordCheckBox.setText("Montrer le mot de passe");
        loginButton.setText("Connexion");
        loginButton.setPrefWidth(90);
        
    }
    
}
