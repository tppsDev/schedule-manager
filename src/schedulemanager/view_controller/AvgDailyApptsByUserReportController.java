/*
 * Project written by: Tim Smith
 * 
 */
package schedulemanager.view_controller;

import java.net.URL;
import java.util.ResourceBundle;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.layout.VBox;
import schedulemanager.model.AvgDailyApptsByUserRow;
import schedulemanager.view_controller.DBServiceManager.GetAvgDailyAppointmentCountByUserService;

/**
 * FXML Controller class
 *
 * @author Tim Smith
 */
public class AvgDailyApptsByUserReportController implements Initializable {
    @FXML VBox reportPane;
    
    private BarChart<Number, String> reportBarChart;
    
    private DBServiceManager dbServiceMgr = new DBServiceManager();
    private GetAvgDailyAppointmentCountByUserService getAvgDailyAppointmentCountByUserService 
            = dbServiceMgr.new GetAvgDailyAppointmentCountByUserService();
    
    private ObservableList<AvgDailyApptsByUserRow> reportList = FXCollections.observableArrayList();

    
    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        initGetAvgDailyAppointmentCountByUserService();
        runGetAvgDailyAppointmentCountByUserService();
    }

    private void initGetAvgDailyAppointmentCountByUserService() {
        getAvgDailyAppointmentCountByUserService.setOnSucceeded((event) -> {
            reportList = getAvgDailyAppointmentCountByUserService.getValue();
            NumberAxis xAxis = new NumberAxis(0, 7, 1);
            CategoryAxis yAxis   = new CategoryAxis();
            reportBarChart = new BarChart<>(xAxis,yAxis);
            reportBarChart.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
            reportBarChart.setMinHeight(535);
            reportBarChart.setHorizontalGridLinesVisible(false);

            final ObservableList<XYChart.Series> seriesList = FXCollections.observableArrayList();
            seriesList.add(new XYChart.Series());
            for (int i = 0; i < reportList.size(); i++) {
                
                seriesList.get(0).getData().add(new XYChart.Data(reportList.get(i).getDailyAvg(), reportList.get(i).getUserName()));

            }
            reportBarChart.legendVisibleProperty().set(false);
            reportBarChart.getData().add(seriesList.get(0));
            reportPane.getChildren().add(reportBarChart);
        });
        
        getAvgDailyAppointmentCountByUserService.setOnFailed((event) -> {
            System.out.println("System error running report");
        });
    }

    private void runGetAvgDailyAppointmentCountByUserService() {
        if (!getAvgDailyAppointmentCountByUserService.isRunning()) {
            getAvgDailyAppointmentCountByUserService.reset();
            getAvgDailyAppointmentCountByUserService.start();
        }
        
    }
    
    
    
}
