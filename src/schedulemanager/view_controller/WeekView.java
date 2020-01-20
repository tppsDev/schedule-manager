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
public final class WeekView extends CalendarView {
    // TODO Class only has skelton
    private Session currentSession = Session.getSession();
    private DateTimeFormatter weekViewFormat = DateTimeFormatter.ofPattern("MMM d");
    private int totalRows;
    private int firstColumn;
    
    public WeekView(LocalDateTime startDate, LocalDateTime endDate, ObservableList<Appointment> appointments) {
        super(startDate, endDate, Type.WEEK);
        setTitle(startDate.format(weekViewFormat) + " - " + endDate.format(weekViewFormat));
        setTotalRows();
        setFirstColumn();
        createDatePlans(appointments);
    }
    
    public WeekView(LocalDateTime startDate, LocalDateTime endDate) {
        super(startDate, endDate, Type.WEEK);
        setTitle(startDate.format(weekViewFormat) + " - " + endDate.format(weekViewFormat));
        setTotalRows();
        setFirstColumn();
        setDayPlans(FXCollections.observableArrayList());
    }
    
    // Getter methods
    @Override
    public int getFirstColumn() {
        return firstColumn;
    }

    @Override
    public int getTotalRows() {
        return totalRows;
    }

    // Setter methods
    @Override
    public void setTotalRows() {
        totalRows = 0; 
    }

    @Override
    public void setFirstColumn() {
        firstColumn = 0;
    }
    
    @Override
    public void createDatePlans(ObservableList<Appointment> appointments) {
        FilteredList<Appointment> filteredAppts = new FilteredList<>(appointments, (p) -> true);

        for (int i = 0; i < 7; i++) {
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

    @Override
    public void addDayPlan(DayPlan dayPlan) {
        getDayPlans().add(dayPlan);
    }

    @Override
    public int maxAppointmentsPerDay() {
        return 25;
    }


}
