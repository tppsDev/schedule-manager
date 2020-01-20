/*
 * Project written by: Tim Smith
 * 
 */
package schedulemanager.view_controller;

import java.net.URL;
import java.util.ResourceBundle;
import javafx.beans.binding.Bindings;
import javafx.beans.value.ChangeListener;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.chart.PieChart;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import schedulemanager.model.ApptTypeByMonthRow;
import schedulemanager.view_controller.DBServiceManager.GetCountOfApptTypesForMonthService;

/**
 * FXML Controller class
 *
 * @author Tim Smith
 */
public class AppointmentTypesByMonthReportController implements Initializable {
    @FXML private ChoiceBox<String> monthChoiceBox;
    @FXML private Label emptyLabel;
    @FXML private PieChart reportChart;
    
    private DBServiceManager dbServiceManager = new DBServiceManager();
    private GetCountOfApptTypesForMonthService getCountOfApptTypesForMonthService 
            = dbServiceManager.new GetCountOfApptTypesForMonthService();
    
    private ObservableList<ApptTypeByMonthRow> apptTypeByMonthRows;
    private final ObservableList<String> monthList = FXCollections.observableArrayList();
    private int monthInt;
    
    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // TODO
        initMonthList();
        initGetCountOfApptTypesForMonthService();
        monthChoiceBox.valueProperty().addListener(monthChoiceBoxListener());
    }
    
    private void initMonthList() {
        monthList.add("January");
        monthList.add("February");
        monthList.add("March");
        monthList.add("April");
        monthList.add("May");
        monthList.add("June");
        monthList.add("July");
        monthList.add("August");
        monthList.add("September");
        monthList.add("October");
        monthList.add("November");
        monthList.add("December");
        monthChoiceBox.setItems(monthList);
    }
    
    private ChangeListener<String> monthChoiceBoxListener() {
        // Using lambda to simplify the code to catch choice box change. 
        //Also keeps the code close to where it is used for debugging
        return (observable, oldValue, newValue) -> {
            switch (newValue) {
                case "January":
                    monthInt = 1;
                    break;
                case "February":
                    monthInt = 2;
                    break;
                case "March":
                    monthInt = 3;
                    break;
                case "April":
                    monthInt = 4;
                    break;
                case "May":
                    monthInt = 5;
                    break;
                case "June":
                    monthInt = 6;
                    break;
                case "July":
                    monthInt = 7;
                    break;
                case "August":
                    monthInt = 8;
                    break;
                case "September":
                    monthInt = 9;
                    break;
                case "October":
                    monthInt = 10;
                    break;
                case "November":
                    monthInt = 11;
                    break;
                case "December":
                    monthInt = 12;
                    break;
            }
            
            runGetCountOfApptTypesForMonthService();
        };
    }
    
    private void loadChart() {
        ObservableList<PieChart.Data> chartData = FXCollections.observableArrayList();

        reportChart.setTitle(monthChoiceBox.getValue());
        // lambda simplifies the code to iterate though list and set each chart data series
        apptTypeByMonthRows.forEach((row) -> {
            chartData.add(new PieChart.Data(row.getApptType(), row.getTypeCount()));
        });
        reportChart.setData(chartData);
        reportChart.getData().forEach((data) -> {
            data.nameProperty().bind(Bindings.concat(data.getName()," - ", data.pieValueProperty().intValue(), " "));
        });
    }
    
    // Service Methods
    private void initGetCountOfApptTypesForMonthService() {
        getCountOfApptTypesForMonthService.setOnSucceeded((event) -> {
            apptTypeByMonthRows = getCountOfApptTypesForMonthService.getValue();
            reportChart.toFront();
            loadChart();
        });
        
        getCountOfApptTypesForMonthService.setOnFailed((event) -> {
            emptyLabel.setText("No data for " + monthChoiceBox.getValue());
            emptyLabel.toFront();
        });
    }
    
    private void runGetCountOfApptTypesForMonthService() {
        if (!getCountOfApptTypesForMonthService.isRunning()) {
            getCountOfApptTypesForMonthService.reset();
            getCountOfApptTypesForMonthService.setMonth(monthInt);
            getCountOfApptTypesForMonthService.start();
        }
    }
    
}
