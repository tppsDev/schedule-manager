/*
 * Project written by: Tim Smith
 * 
 */
package schedulemanager.view_controller;

import java.io.IOException;
import java.net.URL;
import java.time.format.DateTimeFormatter;
import java.util.ResourceBundle;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.stage.Modality;
import javafx.stage.Stage;
import schedulemanager.model.Appointment;
import schedulemanager.view_controller.DBTask.DeleteSelectedAppointmentTask;

/**
 * FXML Controller class
 *
 * @author Tim Smith
 */
public class QuickViewAppointmentController implements Initializable {
    // Labels
    @FXML private Label qvTitleLabel;
    @FXML private Label qvDateTimeLabel;
    @FXML private Label qvLocationLabel;
    @FXML private Label qvContactLabel;
    @FXML private Label errorLabel;
    
    // Buttons
    @FXML private Button qvEditButton;
    @FXML private Button qvDeleteButton;
//    @FXML Button qvEmailButton; next version
    
    @FXML private ProgressIndicator qvProgressIndicator;
    
    //TODO refactor
    DateTimeFormatter qvStartDateTime = DateTimeFormatter.ofPattern("EEEE, MMMM d ~ h:mma - ");
    DateTimeFormatter qvEndDateTime = DateTimeFormatter.ofPattern("h:mma");
    private Appointment appointment;
    private ExecutorService dbExecutor= Executors.newFixedThreadPool(1, new DBTask.DBThreadFactory());
    private DBTask dbTask = new DBTask();
    private boolean apptChanged = false;
    
    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // TODO
        qvProgressIndicator.setVisible(false);
    }

    @FXML
    public void loadAppointment(Appointment appointment) {
        this.appointment = appointment;
        qvTitleLabel.setText(appointment.getTitle());
        qvDateTimeLabel.setText(appointment.getStart().format(qvStartDateTime) 
                + appointment.getEnd().format(qvEndDateTime));
        qvLocationLabel.setText(appointment.getLocation());
        qvContactLabel.setText(appointment.getContact());
    }

    @FXML
    public void handleQvEditButton() throws IOException {
        FXMLLoader viewModifyLoader = new FXMLLoader(getClass().getResource("ViewModifyAppointment.fxml"));
        Scene viewModifyScene;
        viewModifyScene = new Scene(viewModifyLoader.load());
        // TODO remove after testing Stage currentStage = (Stage) qvEditButton.getScene().getWindow();
        //currentStage.close();
        Stage newStage = new Stage();
        newStage.setScene(viewModifyScene);
        newStage.initModality(Modality.APPLICATION_MODAL);
        newStage.setTitle("View / Edit Appointment");
        ViewModifyAppointmentController viewController = viewModifyLoader.getController();
        viewController.setAppointment(appointment, true);
        newStage.showAndWait();
        apptChanged = viewController.isApptChanged();
    }
    
    @FXML
    public void handleQvDeleteButton() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirm deletion");
        alert.setHeaderText("");
        alert.initModality(Modality.APPLICATION_MODAL);
        alert.setContentText("Are you sure you want to delete " + appointment.getTitle() + "?");
        alert.showAndWait();
        if (alert.getResult().equals(ButtonType.OK)) {
            DeleteSelectedAppointmentTask deleteApptTask = dbTask.new DeleteSelectedAppointmentTask(appointment);
            if (qvProgressIndicator.visibleProperty().isBound()) qvProgressIndicator.visibleProperty().unbind();
            qvProgressIndicator.visibleProperty().bind(deleteApptTask.runningProperty());
            deleteApptTask.setOnSucceeded((event) -> {
                apptChanged = true;
                Stage stage = (Stage) qvDeleteButton.getScene().getWindow();
                stage.close();
            });
            deleteApptTask.setOnFailed((event) -> {
                errorLabel.setText("Delete failed.");
            });
        dbExecutor.submit(deleteApptTask);
        }
        
        
    }
// TODO implement on next version after submitted to WGU    
//    @FXML
//    public void handleQvEmailButton() {
//        
//    }

    public boolean isApptChanged() {
        return apptChanged;
    }
    
    

}
