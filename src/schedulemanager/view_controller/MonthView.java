/*
 * Project written by: Tim Smith
 * 
 */
package schedulemanager.view_controller;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import schedulemanager.model.Appointment;

/**
 *
 * @author Tim Smith
 */
public final class MonthView extends CalendarView {
    private Session currentSession = Session.getSession();
    private int firstColumn;
    private int totalRows;
    
    public MonthView(LocalDateTime startDate, LocalDateTime endDate, ObservableList<Appointment> appointments) {
        super(startDate, endDate, Type.MONTH);
        setFirstColumn();
        setTotalRows();
        DateTimeFormatter monthYear = DateTimeFormatter.ofPattern("MMMM yyyy");
        setTitle(getStartDate().format(monthYear));
        createDatePlans(appointments);
    }
    
    public MonthView(LocalDateTime startDate, LocalDateTime endDate) {
        super(startDate, endDate, Type.MONTH);
        setFirstColumn();
        setTotalRows();
        DateTimeFormatter monthYear = DateTimeFormatter.ofPattern("MMMM yyyy");
        setTitle(getStartDate().format(monthYear));
        setDayPlans(FXCollections.observableArrayList());
    }
    
    // Getter methods
    @Override
    public int getTotalRows() {
        // Check for months that start on day 6 or 7 of the week and have enough days to need a 6th row
        if (firstDayOfMonth() > 5
                && firstDayOfMonth() + getStartDate().getMonth().length(true) > 35) {
            return 6;
        }
        // Check for February on a non leap year that starts on day 1 of the week which only needs 4 rows
        if (firstDayOfMonth() == 1 
                && getStartDate().getMonth().length(true) == 28) {
            return 4;
        }
        // All other cases require 5 rows
        return 5;
    }
    
    @Override
    public int getFirstColumn() {
        return firstColumn;
    }
    
    // Setter methods
    @Override
    public void setFirstColumn() {
        firstColumn = firstDayOfMonth();
    }
    
    @Override
    public void setTotalRows() {
        // Default case is 5 rows
        totalRows = 5;
        // Check for months that start on day 6 or 7 of the week and have enough days to need a 6th row
        if (firstDayOfMonth() > 5
                && firstDayOfMonth() + getStartDate().getMonth().length(true) > 35) {
            totalRows = 6;
        }
        // Check for February on a non leap year that starts on day 1 of the week which only needs 4 rows
        if (firstDayOfMonth() == 1 
                && getStartDate().getMonth().length(true) == 28) {
            totalRows = 4;
        }
    }
    
    // getApointmentsPerDay returns the number of appointment slots/day that fit on screen based on number of rows
    @Override
    public int maxAppointmentsPerDay() {
        switch (getTotalRows()) {
            case 4:
                return 6;
            case 5:
                return 5;
            case 6:
                return 4;
            default:
                return 5;
        }
    }
      
    @Override
    public void addDayPlan(DayPlan dayPlan) {
        getDayPlans().add(dayPlan);
    }

    @Override
    public void createDatePlans(ObservableList<Appointment> appointments) {
        FilteredList<Appointment> filteredAppts = new FilteredList<>(appointments, (p) -> true);

        int dayNumber = 1;
        int lastDay = getEndDate().getDayOfMonth();

        for (int i = 0; i < lastDay; i++) {
            int index = i;
            DayPlan dayPlan = new DayPlan(getStartDate().plusDays(index).toLocalDate());
            filteredAppts.setPredicate(p -> p.getStart().toLocalDate().isEqual(getStartDate().plusDays(index).toLocalDate()));
            
            if (filteredAppts.size() < 1) {
                continue;
            }
            filteredAppts.forEach(e -> {
                dayPlan.addAppointment(e);
            });
            getDayPlans().add(dayPlan);
        }
    }
        
    public void importDayPlans(ObservableList<DayPlan> dayPlans) {
        setDayPlans(dayPlans);
    }

    public int firstDayOfMonth() {
        if (currentSession.getSessionLocale().getCountry().equals("US")) {
            return getStartDate().getDayOfWeek().getValue() % 7;
        } else {
            return getStartDate().getDayOfWeek().getValue() - 1;
        }
    }
    
}
