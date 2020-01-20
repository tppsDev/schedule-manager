/*
 * TODO Fix SQL closes
 * TODO Double check opens and closes / consider try with resources
 *
 */
/*
 * Project written by: Tim Smith
 * 
 */
package schedulemanager;

import java.util.Locale;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import schedulemanager.view_controller.LoginController;
import schedulemanager.view_controller.Session;

/**
 *
 * @author Tim Smith
 */
public class ScheduleManager extends Application {
    
    @Override
    public void start(Stage primaryStage) throws Exception {
        Parent loginParent;
        FXMLLoader loader = new FXMLLoader(getClass().getResource("view_controller/Login.fxml"));
        loginParent = loader.load();
        Scene loginScene = new Scene(loginParent);
        LoginController controller = loader.getController();
        primaryStage.setTitle("Schedule Manager Login");
        primaryStage.setScene(loginScene);
        primaryStage.show();

    }

    @Override
    public void init() {
        Session currentSession;
        // Try to establish a session 3 times, terminate after 3rd unsuccessful attempt
        for (int i = 0; i < 3; i++) {
            currentSession = Session.getSession();
            System.out.println(currentSession.getSessionId());
            break;  // Session created succesfully, break loop
        }

    }
    
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        Session currentSession;
        launch(args);
        currentSession = Session.getSession();
        currentSession.closeSession();
    }
     
}
