/*
 * Project written by: Tim Smith
 * 
 */
package schedulemanager.view_controller;

import java.net.URL;
import java.time.LocalDateTime;
import java.util.ResourceBundle;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import schedulemanager.model.Appointment;
import schedulemanager.model.User;
import schedulemanager.utility.DateTimeHandler;
import schedulemanager.view_controller.DBServiceManager.GetUserScheduleService;

/**
 * FXML Controller class
 *
 * @author Tim Smith
 */
public class ConsultantScheduleController implements Initializable {
    @FXML private Label titleLabel;
    @FXML private VBox schedulePane;
    @FXML private Label reportDateLabel;
    
    private DBServiceManager dbServiceManager = new DBServiceManager();
    private GetUserScheduleService getUserScheduleService = dbServiceManager.new GetUserScheduleService();
    private ObservableList<Appointment> schedule;
    
    private User consultant;
    private LocalDateTime fromDate = LocalDateTime.now();

    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        reportDateLabel.setText(fromDate.format(DateTimeHandler.FULL_DATE_TIME));
        initGetUserScheduleService();
    }
    
    public void setUser(User consultant) {
        this.consultant = consultant;
        titleLabel.setText("Schedule for " + consultant.getUserName());
        runGetUserScheduleService();
    }
    
    private void initGetUserScheduleService() {
        getUserScheduleService.setOnSucceeded((event) -> {
            schedule = getUserScheduleService.getValue();
            schedule.forEach((appt) -> {
                schedulePane.getChildren().add(new Label(appt.toReportString()));
            });
        });
        
        getUserScheduleService.setOnFailed((event) -> {
            event.getSource().getException().printStackTrace();
        });
    }
    
    private void runGetUserScheduleService() {
        if (!getUserScheduleService.isRunning()) {
            getUserScheduleService.reset();
            getUserScheduleService.setUser(consultant);
            getUserScheduleService.setFromDate(fromDate);
            getUserScheduleService.start();
        }
    }
    
}
