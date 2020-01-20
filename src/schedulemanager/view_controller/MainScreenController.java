/*
 * Project written by: Tim Smith
 * 
 */
package schedulemanager.view_controller;

import java.io.IOException;
import java.net.URL;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import static java.time.temporal.TemporalAdjusters.*;
import java.time.temporal.WeekFields;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Service;
import javafx.concurrent.WorkerStateEvent;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceDialog;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import schedulemanager.DAO.SysOpException;
import schedulemanager.model.Appointment;
import schedulemanager.model.User;
import schedulemanager.view_controller.CalendarView.Type;

/**
 * FXML Controller class
 *
 * @author Tim Smith
 */
public class MainScreenController implements Initializable {
    // Buttons
    @FXML private Button prevDateRangeButton;
    @FXML private Button nextDateRangeButton;
    @FXML private Button todayButton;
    @FXML private Button manageCustomersButton;
    @FXML private Button changePasswordButton;
    @FXML private Button manageUsersButton;
    @FXML private Button manageCitiesCountriesButton;
    @FXML private Button addAppointmentMenuButton;
    @FXML private Button viewMyCalendarButton;
    @FXML private Button viewOtherCalendarButton;
    @FXML private Button apptTypeByMonthButton;
    @FXML private Button consultantScheduleButton;
    @FXML private Button avgDailyApptsButton;
    
    // Labels
    @FXML private Label calendarTitle;
    @FXML private Label currentDateLabel;
    @FXML private Label currentUserLabel;
    
    // ComboBoxes
    @FXML private ComboBox<Type> calendarViewComboBox;
    
    // StackPanes
    @FXML private StackPane dayHeaderStackPane;
    
    // GridPane
    @FXML private GridPane monthGridPane;
    @FXML private GridPane weekGridPane;
    @FXML private GridPane dayGridPane;
    @FXML private GridPane dayOfWeekUSLabelGridPane;
    @FXML private GridPane dayOfWeekISOenLabelGridPane;
    @FXML private GridPane dayOfWeekISOfrLabelGridPane;
    
    // Progress Indicator
    @FXML private ProgressIndicator viewProgressIndicator;
    
    private Session currentSession = Session.getSession();
    private LocalDateTime currentViewStartDate;
    private CalendarView.Type currentView;
    private User currentUser;
    private User viewedUser;
    private LocalDateTime today = LocalDateTime.now().withHour(0).withMinute(0);
    private ObservableList<User> consultantChoices = FXCollections.observableArrayList();
    private ChoiceDialog<User> choiceDialog = new ChoiceDialog<>();
    private ExecutorService dbExecutor= Executors.newFixedThreadPool(1, new DBTask.DBThreadFactory());
    private DBTask dbTask = new DBTask();
    private DBTask.GetActiveUsersTask getActiveConsultantsChoiceBoxTask;
    MainScreenService retrieveService;
    DateTimeFormatter dateFormat = DateTimeFormatter.ofPattern("MMMM-dd-yyyy");
    DateTimeFormatter timeFormat = DateTimeFormatter.ofPattern("h:mma");
    
    private boolean firstTime = true;
    private boolean apptWithinFifteenMinutes = false;
    
    /**
     * Initializes the controller class.
     * @param url
     * @param rb
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // TODO
        currentView = Type.MONTH;
        retrieveService = new MainScreenService();
        
        LocalDateTime fromDate = today.with(firstDayOfMonth());
        LocalDateTime toDate = today.with(lastDayOfMonth());
        
        currentViewStartDate = fromDate;
        
        currentUser = currentSession.getSessionUser();
        viewedUser = currentUser;
        
        currentDateLabel.setText(today.format(dateFormat));
        currentUserLabel.setText("Signed in as: " + currentUser.getUserName().toUpperCase());
                       
        calendarViewComboBox.getItems().addAll(Type.values());
        calendarViewComboBox.getSelectionModel().select(Type.MONTH);
                
        // TODO consider switch and using full locale
        if (currentSession.getSessionLocale().getCountry().equals("US")) {
            dayOfWeekUSLabelGridPane.toFront();
        } else if (currentSession.getSessionLocale().getLanguage().equals("en")) {
            dayOfWeekISOenLabelGridPane.toFront();
        } else {
            dayOfWeekISOfrLabelGridPane.toFront();
        }
        
        
        // Set bindings to retrieveService task runningProporty
        viewProgressIndicator.visibleProperty().bind(retrieveService.runningProperty());
        prevDateRangeButton.disableProperty().bind(retrieveService.runningProperty());
        nextDateRangeButton.disableProperty().bind(retrieveService.runningProperty());
        todayButton.disableProperty().bind(retrieveService.runningProperty());
        calendarViewComboBox.disableProperty().bind(retrieveService.runningProperty());
        
        // Use Lambda to call loadCalendarView with appointments result from successful task 
        retrieveService.setOnSucceeded((WorkerStateEvent workerStateEvent) -> {
            clearGridpane();
            if (currentView.equals(Type.DAY)) {
                dayHeaderStackPane.setVisible(false);
            } else {
                dayHeaderStackPane.setVisible(true);
            }
            if (firstTime) {
                firstTime = false;
                Appointment upcomingAppt;
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Upcoming Appointment Alert");
                alert.setHeaderText("");
                alert.initModality(Modality.APPLICATION_MODAL);
                
                // Using lamba to set complex predicate simpler code and eliminates iterating though whole list
                apptWithinFifteenMinutes = retrieveService.getValue().stream().anyMatch((appt) -> {
                    LocalDateTime now = LocalDateTime.now();
                    if (now.isAfter(appt.getStart())) {
                        return false;
                    }

                    Duration duration = Duration.between(now, appt.getStart());
                    long minuteTill = duration.toMinutes();

                    if (minuteTill < 15) alert.setContentText(appt.toReportString());

                    return minuteTill < 15;
                });
                
                if (apptWithinFifteenMinutes) {
                    alert.show();
                }
                
            }
            loadCalendarView(retrieveService.getStartDate(), retrieveService.getEndDate(), retrieveService.getValue());
        });
        
        // Use Lambda to handle failed task
        retrieveService.setOnFailed((WorkerStateEvent workerStateEvent) -> {
            clearGridpane();
            if (currentView.equals(Type.DAY)) {
                dayHeaderStackPane.setVisible(false);
            } else {
                dayHeaderStackPane.setVisible(true);
            }
            String msg = retrieveService.getException().getMessage();
            // Check for SysOpException which is thrown for no records, if so reset service and load blank CalendarView
            if (retrieveService.getException() instanceof SysOpException) {
                retrieveService.reset();
                loadCalendarView(retrieveService.getStartDate(), retrieveService.getEndDate());
            } else {
                // Otherwise show alert with error
                Alert alert = new Alert(Alert.AlertType.WARNING);
                alert.setTitle("Warning");
                alert.setHeaderText(null);
                alert.setContentText(msg);
                alert.showAndWait();
            }
            retrieveService.reset();
        });
        
        createView(fromDate, toDate);

    }
    
   private void initializeGetActiveUsersChoiceBoxTask() {
        getActiveConsultantsChoiceBoxTask = dbTask.new GetActiveUsersTask();
        getActiveConsultantsChoiceBoxTask.setOnSucceeded((event) -> {
            consultantChoices.setAll(getActiveConsultantsChoiceBoxTask.getValue());
            choiceDialog.getItems().addAll(consultantChoices);
            choiceDialog.setSelectedItem(currentUser);
            choiceDialog.setTitle("Consultant Choice");
            choiceDialog.setHeaderText("");
            choiceDialog.setContentText("Select a consultant:");
            Optional<User> result = choiceDialog.showAndWait();
            if (result.isPresent()) {
                viewedUser = result.get();
                LocalDateTime fromDate;
                LocalDateTime toDate;
                currentViewStartDate = LocalDateTime.now().withHour(0).withMinute(0);
                switch (currentView) {
                    case DAY:
                        fromDate = currentViewStartDate;
                        toDate = currentViewStartDate.plusDays(1);
                        break;
                    case WEEK:

                        fromDate = currentViewStartDate.with(WeekFields.of(currentSession.getSessionLocale()).dayOfWeek(), 1);
                        toDate = fromDate.plusDays(6);
                        break;
                    case MONTH:
                    default:
                        fromDate = currentViewStartDate.with(firstDayOfMonth());
                        toDate = currentViewStartDate.with(lastDayOfMonth());
                        break;
                }

                createView(fromDate, toDate);
            }
        });
        getActiveConsultantsChoiceBoxTask.setOnFailed((event) -> {
            System.out.println(getActiveConsultantsChoiceBoxTask.getException().getMessage());
        });
    }

    private void createView(LocalDateTime fromDate, LocalDateTime toDate) {

        if (retrieveService.getState() == Service.State.SUCCEEDED || retrieveService.getState() == Service.State.READY) {
            if (retrieveService.getState() == Service.State.SUCCEEDED) {
                retrieveService.reset();
            }
            retrieveService.setViewType(currentView);
            retrieveService.setUser(viewedUser);
            retrieveService.setStartDate(fromDate);
            retrieveService.setEndDate(toDate);
        
            retrieveService.start();
        }
    }
    
    private CalendarView loadCalendarView(LocalDateTime fromDate, LocalDateTime toDate,
                                 ObservableList<Appointment> appointments) {
        CalendarView calendarView;

        switch (currentView) {
            case DAY:
                calendarView = loadDayView(fromDate, appointments);
                break;
            case WEEK:
                calendarView = loadWeekView(fromDate, toDate, appointments);
                break;
            case MONTH:
            default:
                calendarView = loadMonthView(fromDate, toDate, appointments);
                break;
        }
        calendarTitle.setText(calendarView.getTitle());
        
        return calendarView;
    }
    
    private CalendarView loadCalendarView(LocalDateTime fromDate, LocalDateTime toDate) {
        CalendarView calendarView;

        switch (currentView) {
            case DAY:
                calendarView = loadDayView(fromDate);
                break;
            case WEEK:
                calendarView = loadWeekView(fromDate, toDate);
                break;
            case MONTH:
            default:
                calendarView = loadMonthView(fromDate, toDate);
                break;
        }
        calendarTitle.setText(calendarView.getTitle());
        
        return calendarView;
    }
// TODO Consider moving load****View methods to each class constructor or other method in class    
    private CalendarView loadMonthView(LocalDateTime fromDate, LocalDateTime toDate,
                                 ObservableList<Appointment> appointments) {
        CalendarView calendarView = new MonthView(fromDate, toDate, appointments);
        int firstColumn = calendarView.getFirstColumn();
        int day = 1;
        int daysInMonth = calendarView.getStartDate().getMonth().length(true);
        // This will get moved to view controller
        monthGridPane.setAlignment(Pos.TOP_LEFT);
        for (int row = 0; row < calendarView.getTotalRows(); row++ ) {
            for (int col = firstColumn; col < 7; col++) {
                int stopAt;
                DayPlan dayPlan = calendarView.getDayPlanByDay(day);
                VBox vbox = new VBox();
                Label firstLabel = new Label(String.valueOf(day));
                firstLabel.prefWidthProperty().bind(vbox.widthProperty());
                firstLabel.setAlignment(Pos.CENTER);
                monthGridPane.add(vbox, col, row);
                vbox.getChildren().add(firstLabel);

                if (dayPlan != null) {
                    stopAt = (calendarView.maxAppointmentsPerDay() < dayPlan.getAppointments().size()) 
                        ? calendarView.maxAppointmentsPerDay() : dayPlan.getAppointments().size();

                    for (int i = 0; i < stopAt; i++) {
                        Appointment appt = dayPlan.getAppointments().get(i);
                        String apptTime = dayPlan.getAppointments().get(i).getStart().format(timeFormat);
                        String apptTitle = dayPlan.getAppointments().get(i).getTitle();
                        Label label = new Label(apptTime + " " + apptTitle);
                        label.prefWidthProperty().bind(vbox.widthProperty());
                        label.getStyleClass().add("calendar-appt-label");
                        label.setOnMouseClicked(event -> {
                            FXMLLoader qvLoader = new FXMLLoader(getClass()
                                    .getResource("QuickViewAppointment.fxml"));
                            Scene qvScene;
                            try {    
                                qvScene = new Scene(qvLoader.load());
                            } catch (IOException ex) {
                                // TODO handle this error
                                return;
                            }

                            Stage qvStage = new Stage();
                            qvStage.initOwner(monthGridPane.getScene().getWindow());
                            qvStage.initModality(Modality.APPLICATION_MODAL);
                            qvStage.setTitle("Appointment Quick View");
                            qvStage.setScene(qvScene);
                            QuickViewAppointmentController qvController = qvLoader.getController();
                            qvController.loadAppointment(appt);
                            qvStage.showAndWait();
                            if (qvController.isApptChanged()) {
                                todayButton.fire();
                            }
                            
                        });
                        vbox.getChildren().add(label);
                    }
                }

                day++;
                if (day > daysInMonth) break;
            }
            firstColumn = 0;
            if (day > daysInMonth) break;
        }
        return calendarView;
    }
    
    private CalendarView loadMonthView(LocalDateTime fromDate, LocalDateTime toDate) {
        CalendarView calendarView = new MonthView(fromDate, toDate);
        int firstColumn = calendarView.getFirstColumn();
        int day = 1;
        int daysInMonth = calendarView.getStartDate().getMonth().length(true);
        // This will get moved to view controller
        monthGridPane.setAlignment(Pos.TOP_LEFT);
        for (int row = 0; row < calendarView.getTotalRows(); row++ ) {
            for (int col = firstColumn; col < 7; col++) {
                VBox vbox = new VBox();
                Label firstLabel = new Label(String.valueOf(day));
                firstLabel.prefWidthProperty().bind(vbox.widthProperty());
                firstLabel.setAlignment(Pos.CENTER);
                monthGridPane.add(vbox, col, row);
                vbox.getChildren().add(firstLabel);

                day++;
                if (day > daysInMonth) break;
            }
            firstColumn = 0;
            if (day > daysInMonth) break;
        }
        return calendarView;
    }
    
    private CalendarView loadWeekView(LocalDateTime fromDate, LocalDateTime toDate,
                                     ObservableList<Appointment> appointments) {
        CalendarView calendarView = new WeekView(fromDate, toDate, appointments);
        DateTimeFormatter newMonth = DateTimeFormatter.ofPattern("MMM-d");
        int firstColumn = calendarView.getFirstColumn();
        int day = fromDate.getDayOfMonth();
        int daysInMonth = calendarView.getStartDate().getMonth().length(true);
        boolean printMonth = false;
        // This will get moved to view controller
        weekGridPane.setAlignment(Pos.TOP_LEFT);

            for (int col = firstColumn; col < 7; col++) {
                DayPlan dayPlan = calendarView.getDayPlanByDay(day);
                VBox vbox = new VBox();
                String labelText = (printMonth) ? fromDate.plusDays(col).format(newMonth) : String.valueOf(day);
                Label firstLabel = new Label(labelText);
                firstLabel.prefWidthProperty().bind(vbox.widthProperty());
                firstLabel.setAlignment(Pos.CENTER);
                weekGridPane.add(vbox, col, 0);
                vbox.getChildren().add(firstLabel);

                if (dayPlan != null) {
                    int stopAt = (calendarView.maxAppointmentsPerDay() < dayPlan.getAppointments().size()) 
                        ? calendarView.maxAppointmentsPerDay() : dayPlan.getAppointments().size(); 
                    
                    for (int i = 0; i < stopAt; i++) {
                        Appointment appt = dayPlan.getAppointments().get(i);
                        String apptTime = dayPlan.getAppointments().get(i).getStart().format(timeFormat);
                        String apptTitle = dayPlan.getAppointments().get(i).getTitle();
                        Label label = new Label(apptTime + " " + apptTitle);
                        label.getStyleClass().add("calendar-appt-label");
                        label.prefWidthProperty().bind(vbox.widthProperty());
                        label.setOnMouseClicked(event -> {
                            FXMLLoader qvLoader = new FXMLLoader(getClass()
                                    .getResource("QuickViewAppointment.fxml"));
                            Scene qvScene;
                            try {    
                                qvScene = new Scene(qvLoader.load());
                            } catch (IOException ex) {
                                // TODO handle this error
                                return;
                            }

                            Stage qvStage = new Stage();
                            qvStage.initOwner(monthGridPane.getScene().getWindow());
                            qvStage.initModality(Modality.APPLICATION_MODAL);
                            qvStage.setTitle("Appointment Quick View");
                            qvStage.setScene(qvScene);
                            QuickViewAppointmentController qvController = qvLoader.getController();
                            qvController.loadAppointment(appt);
                            qvStage.showAndWait();
                            if (qvController.isApptChanged()) {
                                todayButton.fire();
                            }
                        });
                        vbox.getChildren().add(label);
                    }
                }
                
                day++;
                if (day > daysInMonth) {
                    day = 1;
                    printMonth = true;
                }
            }

        return calendarView;
    }
    
    private CalendarView loadWeekView(LocalDateTime fromDate, LocalDateTime toDate) {
        CalendarView calendarView = new WeekView(fromDate, toDate);
        DateTimeFormatter newMonth = DateTimeFormatter.ofPattern("MMM-d");
        int firstColumn = calendarView.getFirstColumn();
        int day = fromDate.getDayOfMonth();
        int daysInMonth = calendarView.getStartDate().getMonth().length(true);
        boolean printMonth = false;
        // This will get moved to view controller
        weekGridPane.setAlignment(Pos.TOP_LEFT);

            for (int col = firstColumn; col < 7; col++) {
                VBox vbox = new VBox();
                String labelText = (printMonth) ? fromDate.plusDays(col).format(newMonth) : String.valueOf(day);
                Label firstLabel = new Label(labelText);
                firstLabel.prefWidthProperty().bind(vbox.widthProperty());
                firstLabel.setAlignment(Pos.CENTER);
                weekGridPane.add(vbox, col, 0);
                vbox.getChildren().add(firstLabel);

                day++;
                if (day > daysInMonth) {
                    day = 1;
                    printMonth = true;
                }
            }

        return calendarView;
    }
    
    private CalendarView loadDayView(LocalDateTime fromDate, ObservableList<Appointment> appointments) {
        CalendarView calendarView = new DayView(fromDate, appointments);
        DayPlan dayPlan = calendarView.getDayPlans().get(0);
        DateTimeFormatter dayLabel = DateTimeFormatter.ofPattern("EEEE MMMM d");
        int day = fromDate.getDayOfMonth();
        // This will get moved to view controller
        dayGridPane.setAlignment(Pos.TOP_LEFT);

        VBox vbox = new VBox();
        String labelText = fromDate.format(dayLabel);
        Label firstLabel = new Label(labelText);
        firstLabel.setStyle("-fx-font-size: 18px");
        firstLabel.prefWidthProperty().bind(vbox.widthProperty());
        firstLabel.setAlignment(Pos.CENTER);
        dayGridPane.add(vbox, 0, 0);
        vbox.getChildren().add(firstLabel);
        
        if (dayPlan != null) {
            int stopAt = (calendarView.maxAppointmentsPerDay() < dayPlan.getAppointments().size()) 
                        ? calendarView.maxAppointmentsPerDay() : dayPlan.getAppointments().size(); 
                    
            for (int i = 0; i < stopAt; i++) {
                Appointment appt = dayPlan.getAppointments().get(i);
                String apptTime = dayPlan.getAppointments().get(i).getStart().format(timeFormat);
                String apptTitle = dayPlan.getAppointments().get(i).getTitle();
                Label label = new Label(apptTime + " " + apptTitle);
                label.getStyleClass().add("calendar-appt-label");
                label.prefWidthProperty().bind(vbox.widthProperty());
                // Using lambda here allows for simply handling of adding listener to control added on the fly
                label.setOnMouseClicked(event -> {
                    FXMLLoader qvLoader = new FXMLLoader(getClass()
                            .getResource("QuickViewAppointment.fxml"));
                    Scene qvScene;
                    try {
                        qvScene = new Scene(qvLoader.load());
                    } catch (IOException ex) {
                        // TODO handle this error
                        return;
                    }

                    Stage qvStage = new Stage();
                    qvStage.initOwner(monthGridPane.getScene().getWindow());
                    qvStage.initModality(Modality.APPLICATION_MODAL);
                    qvStage.setTitle("Appointment Quick View");
                    qvStage.setScene(qvScene);
                    QuickViewAppointmentController qvController = qvLoader.getController();
                    qvController.loadAppointment(appt);
                    qvStage.showAndWait();
                    if (qvController.isApptChanged()) {
                        todayButton.fire();
                    }
                });
                vbox.getChildren().add(label);
            }
        }
        
        return calendarView;
    }
    
    private CalendarView loadDayView(LocalDateTime fromDate) {
        CalendarView calendarView = new DayView(fromDate);
        DateTimeFormatter dayLabel = DateTimeFormatter.ofPattern("EEEE MMMM d");
        int day = fromDate.getDayOfMonth();
        // This will get moved to view controller
        dayGridPane.setAlignment(Pos.TOP_LEFT);

        VBox vbox = new VBox();
        String labelText = fromDate.format(dayLabel);
        Label firstLabel = new Label(labelText);
        firstLabel.setStyle("-fx-font-size: 18px");
        firstLabel.prefWidthProperty().bind(vbox.widthProperty());
        firstLabel.setAlignment(Pos.CENTER);
        dayGridPane.add(vbox, 0, 0);
        vbox.getChildren().add(firstLabel);

        return calendarView;
    }
    
    @FXML
    private void  handleNextButtonClick(ActionEvent event) {
        LocalDateTime fromDate;
        LocalDateTime toDate;

        switch (currentView) {
            case DAY:
                currentViewStartDate = currentViewStartDate.plusDays(1);
                fromDate = currentViewStartDate;
                toDate = currentViewStartDate.plusDays(1);
                
                break;
            case WEEK:
                currentViewStartDate = currentViewStartDate.plusWeeks(1);
                fromDate = currentViewStartDate;
                toDate = currentViewStartDate.plusDays(6);
                break;
            case MONTH:
            default:
                currentViewStartDate = currentViewStartDate.plusMonths(1);
                fromDate = currentViewStartDate.with(firstDayOfMonth());
                toDate = currentViewStartDate.with(lastDayOfMonth());
                break;
        }
        
        
        createView(fromDate, toDate);
    }
    
    @FXML
    private void  handlePrevButtonClick(ActionEvent event) {
        LocalDateTime fromDate;
        LocalDateTime toDate;
        switch (currentView) {
            case DAY:
                currentViewStartDate = currentViewStartDate.minusDays(1);
                fromDate = currentViewStartDate;
                toDate = currentViewStartDate.plusDays(1);
                break;
            case WEEK:
                currentViewStartDate = currentViewStartDate.minusWeeks(1);
                fromDate = currentViewStartDate;
                toDate = currentViewStartDate.plusDays(6);
                break;
            case MONTH:
            default:
                currentViewStartDate = currentViewStartDate.minusMonths(1);
                fromDate = currentViewStartDate.with(firstDayOfMonth());
                toDate = currentViewStartDate.with(lastDayOfMonth());
                break;
        }
       
        createView(fromDate, toDate);
    }
    
    @FXML
    private void  handleTodayButtonClick(ActionEvent event) {
        LocalDateTime fromDate;
        LocalDateTime toDate;
        currentViewStartDate = LocalDateTime.now().withHour(0).withMinute(0);
        switch (currentView) {
            case DAY:
                fromDate = currentViewStartDate;
                toDate = currentViewStartDate.plusDays(1);
                break;
            case WEEK:

                fromDate = currentViewStartDate.with(WeekFields.of(currentSession.getSessionLocale()).dayOfWeek(), 1);
                toDate = fromDate.plusDays(6);
                break;
            case MONTH:
            default:
                fromDate = currentViewStartDate.with(firstDayOfMonth());
                toDate = currentViewStartDate.with(lastDayOfMonth());
                break;
        }
       
        createView(fromDate, toDate);
    }
    
    @FXML
    private void  handleViewMyCalendarButtonClick(ActionEvent event) {
        LocalDateTime fromDate;
        LocalDateTime toDate;
        viewedUser = currentUser;
        currentViewStartDate = LocalDateTime.now().withHour(0).withMinute(0);
        switch (currentView) {
            case DAY:
                fromDate = currentViewStartDate;
                toDate = currentViewStartDate.plusDays(1);
                break;
            case WEEK:

                fromDate = currentViewStartDate.with(WeekFields.of(currentSession.getSessionLocale()).dayOfWeek(), 1);
                toDate = fromDate.plusDays(6);
                break;
            case MONTH:
            default:
                fromDate = currentViewStartDate.with(firstDayOfMonth());
                toDate = currentViewStartDate.with(lastDayOfMonth());
                break;
        }
       
        createView(fromDate, toDate);
    }
    
    @FXML void handleAddAppointmentButton() {
        FXMLLoader addApptLoader = new FXMLLoader(getClass()
                            .getResource("AddAppointment.fxml"));
        Scene addApptScene;
        try {
            addApptScene = new Scene(addApptLoader.load());
        } catch (IOException ex) {
            ex.printStackTrace();
            return;
        }
        Stage addApptStage = new Stage();
        addApptStage.initOwner(monthGridPane.getScene().getWindow());
        addApptStage.initModality(Modality.APPLICATION_MODAL);
        addApptStage.setTitle("Schedule Manager 1.0 - Manage Users");
        AddAppointmentController addApptController = addApptLoader.getController();
        addApptStage.setOnCloseRequest(new EventHandler<WindowEvent>() {
            @Override
            public void handle(WindowEvent event) {
                if (!addApptController.canExit()) {
                    event.consume();
                    addApptController.cancelButton.fire();
                }
            }
        });
        addApptStage.setScene(addApptScene);
        addApptStage.showAndWait();
        if (addApptController.wasApptAdded()) {
            LocalDateTime fromDate;
            LocalDateTime toDate;
            switch (currentView) {
                    case DAY:
                        fromDate = currentViewStartDate;
                        toDate = currentViewStartDate.plusDays(1);
                        break;
                    case WEEK:

                        fromDate = currentViewStartDate.with(WeekFields.of(currentSession.getSessionLocale()).dayOfWeek(), 1);
                        toDate = fromDate.plusDays(6);
                        break;
                    case MONTH:
                    default:
                        fromDate = currentViewStartDate.with(firstDayOfMonth());
                        toDate = currentViewStartDate.with(lastDayOfMonth());
                        break;
                }

                createView(fromDate, toDate);
        }
    }
    
    @FXML
    private void handleViewOtherCalendarButton() {
        initializeGetActiveUsersChoiceBoxTask();
        dbExecutor.submit(getActiveConsultantsChoiceBoxTask);
    }
    
    @FXML  // TODO remove this
    private void doStuff() {
        FXMLLoader  tlLoader = new FXMLLoader(getClass().getResource("TempLoad.fxml"));
        Scene  tlScene;
        try {
             tlScene = new Scene( tlLoader.load());
        } catch (IOException ex) {

            return;
        }
        Stage  tlStage = new Stage();
         tlStage.initOwner(currentUserLabel.getScene().getWindow());
         tlStage.initModality(Modality.APPLICATION_MODAL);
         tlStage.setTitle("Schedule Manager 1.0 - Temp Load");
         tlStage.setScene( tlScene);
         tlStage.showAndWait();
    }
    
    @FXML
    private void handleApptTypeByMonthButton() {
        FXMLLoader reportLoader = new FXMLLoader(getClass().getResource("AppointmentTypesByMonthReport.fxml"));
        Scene reportScene;
        try {
            reportScene = new Scene(reportLoader.load());
        } catch (IOException ex) {

            return;
        }
        Stage reportStage = new Stage();
        reportStage.initOwner(currentUserLabel.getScene().getWindow());
        reportStage.initModality(Modality.APPLICATION_MODAL);
        reportStage.setTitle("Schedule Manager 1.0 - Appointment Types By Month Report");
        reportStage.setScene(reportScene);
        reportStage.showAndWait();
    }
    
    @FXML
    private void handleAvgDailyApptsButton() {
        FXMLLoader reportLoader = new FXMLLoader(getClass().getResource("AvgDailyApptsByUserReport.fxml"));
        Scene reportScene;
        try {
            reportScene = new Scene(reportLoader.load());
        } catch (IOException ex) {

            return;
        }
        Stage reportStage = new Stage();
        reportStage.initOwner(currentUserLabel.getScene().getWindow());
        reportStage.initModality(Modality.APPLICATION_MODAL);
        reportStage.setTitle("Schedule Manager 1.0 - Avg Daily Appts Report");
        reportStage.setScene(reportScene);
        reportStage.showAndWait();
    }
    
    @FXML
    private void handleConsultantScheduleButton() {
        getActiveConsultantsChoiceBoxTask = dbTask.new GetActiveUsersTask();
        getActiveConsultantsChoiceBoxTask.setOnSucceeded((event) -> {
            consultantChoices.setAll(getActiveConsultantsChoiceBoxTask.getValue());
            choiceDialog.getItems().addAll(consultantChoices);
            choiceDialog.setSelectedItem(currentUser);
            choiceDialog.setTitle("Consultant Choice");
            choiceDialog.setHeaderText("");
            choiceDialog.setContentText("Select a consultant:");
            Optional<User> result = choiceDialog.showAndWait();
            if (result.isPresent()) {
                FXMLLoader reportLoader = new FXMLLoader(getClass().getResource("ConsultantSchedule.fxml"));
                Scene reportScene;
                try {
                    reportScene = new Scene(reportLoader.load());
                } catch (IOException ex) {

                    return;
                }
                Stage reportStage = new Stage();
                reportStage.initOwner(currentUserLabel.getScene().getWindow());
                reportStage.initModality(Modality.APPLICATION_MODAL);
                reportStage.setTitle("Schedule Manager 1.0 - Appointment Types By Month Report");
                ConsultantScheduleController reportController = reportLoader.getController();
                reportController.setUser(result.get());
                reportStage.setScene(reportScene);
                reportStage.showAndWait();
            }
        });
        
        getActiveConsultantsChoiceBoxTask.setOnFailed((event) -> {
            event.getSource().getException().printStackTrace();
        });
        dbExecutor.submit(getActiveConsultantsChoiceBoxTask);
    }
        
    @FXML
    private void handleCalendarViewComboBox(ActionEvent event) {
        LocalDateTime fromDate;
        LocalDateTime toDate;

        switch (calendarViewComboBox.getValue()) {
            case DAY:
                currentView = Type.DAY;
                dayGridPane.toFront();
                fromDate = currentViewStartDate;
                toDate = currentViewStartDate.plusDays(1);
                break;
            case WEEK:
                currentView = Type.WEEK;
                weekGridPane.toFront();
                fromDate = currentViewStartDate.with(WeekFields.of(currentSession.getSessionLocale()).dayOfWeek(), 1);
                toDate = fromDate.plusDays(6);
                break;
            case MONTH:
                currentView = Type.MONTH;
                monthGridPane.toFront();
                fromDate = currentViewStartDate.with(firstDayOfMonth());
                toDate = currentViewStartDate.with(lastDayOfMonth());
                break;
            default:
                // All enum values have case, so this should never be reached, but in the event of system issue handle it
                Alert alert = new Alert(Alert.AlertType.WARNING);
                alert.setTitle("Warning");
                alert.setHeaderText(null);
                alert.setContentText("System error, please try again.");
                alert.showAndWait();
                calendarViewComboBox.getSelectionModel().select(currentView);
                throw new RuntimeException("System error, please try again");
        }

        createView(fromDate, toDate);
    }
    
    @FXML
    private void handleManageCustomersButton() {
        FXMLLoader mcLoader = new FXMLLoader(getClass().getResource("ManageCustomers.fxml"));
        Scene mcScene;
        try {
            mcScene = new Scene(mcLoader.load());
        } catch (IOException ex) {

            return;
        }
        Stage mcStage = new Stage();
        mcStage.initOwner(currentUserLabel.getScene().getWindow());
        mcStage.initModality(Modality.APPLICATION_MODAL);
        mcStage.setTitle("Schedule Manager 1.0 - Manage Customers");
        mcStage.setScene(mcScene);
        ManageCustomersController mcController = mcLoader.getController();
        mcStage.showAndWait();
    }
    
    @FXML
    private void handleChangePasswordButton() {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("ChangePassword.fxml"));
        ChangePasswordController cpController = new ChangePasswordController();
        loader.setController(cpController);
        Scene cpScene;
        try {
            cpScene = new Scene(loader.load());
        } catch (IOException ex) {
            return;
        }
        Stage cpStage = new Stage();
        cpStage.initOwner(currentUserLabel.getScene().getWindow());
        cpStage.initModality(Modality.APPLICATION_MODAL);
        cpStage.setTitle("Schedule Manager 1.0 - Change Password");
        cpStage.setScene(cpScene);
        cpStage.showAndWait();
    }
    
    @FXML
    private void handleManagerUsersButton() {

        FXMLLoader manageUserLoader = new FXMLLoader(getClass()
                            .getResource("ManageUsers.fxml"));
        Scene manageUserScene;
        try {
            manageUserScene = new Scene(manageUserLoader.load());
        } catch (IOException ex) {

            return;
        }
        Stage manageUserStage = new Stage();
        manageUserStage.initOwner(monthGridPane.getScene().getWindow());
        manageUserStage.initModality(Modality.APPLICATION_MODAL);
        manageUserStage.setTitle("Schedule Manager 1.0 - Manage Users");
        ManageUsersController muController = manageUserLoader.getController();
        manageUserStage.setOnCloseRequest(new EventHandler<WindowEvent>() {
            @Override
            public void handle(WindowEvent event) {
                if (!muController.canExit()) {
                    event.consume();
                }
            }
        });
        manageUserStage.setScene(manageUserScene);
        manageUserStage.showAndWait();
    }
    
    @FXML
    private void handleManagerCitiesCountriesButton() {

        FXMLLoader manageCityCountryLoader = new FXMLLoader(getClass()
                            .getResource("ManageCitiesCountries.fxml"));
        Scene manageCityCountryScene;
        try {
            manageCityCountryScene = new Scene(manageCityCountryLoader.load());
        } catch (IOException ex) {

            return;
        }
        Stage manageCityCountryStage = new Stage();
        manageCityCountryStage.initOwner(monthGridPane.getScene().getWindow());
        manageCityCountryStage.initModality(Modality.APPLICATION_MODAL);
        manageCityCountryStage.setTitle("Schedule Manager 1.0 - Manage Cities & Countries");
        ManageCitiesCountriesController mccController = manageCityCountryLoader.getController();
        manageCityCountryStage.setOnCloseRequest(new EventHandler<WindowEvent>() {
            @Override
            public void handle(WindowEvent event) {
                if (!mccController.canExit()) {
                    event.consume();
                }
            }
        });
        manageCityCountryStage.setScene(manageCityCountryScene);
        manageCityCountryStage.showAndWait();
    }
    
    private void clearGridpane() {
        Node node;
        switch (currentView) {
            case DAY:
                node = dayGridPane.getChildren().get(0);
                dayGridPane.getChildren().clear();
                dayGridPane.getChildren().add(0, node);
            case WEEK:
                node = weekGridPane.getChildren().get(0);
                weekGridPane.getChildren().clear();
                weekGridPane.getChildren().add(0, node);
            case MONTH:
                node = monthGridPane.getChildren().get(0);
                monthGridPane.getChildren().clear();
                monthGridPane.getChildren().add(0, node);
        }
    }
}
