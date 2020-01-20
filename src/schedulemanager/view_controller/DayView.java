/*
 * Project written by: Tim Smith
 * 
 */
package schedulemanager.view_controller;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import schedulemanager.DAO.AppointmentDaoImpl;
import schedulemanager.model.Appointment;

/**
 *
 * @author Tim Smith
 */
public final class DayView extends CalendarView {
    // TODO Class only has skelton
    private Session currentSession = Session.getSession();
    private AppointmentDaoImpl appointmentDaoImpl = AppointmentDaoImpl.getAppointmentDaoImpl();
    private DateTimeFormatter monthDay = DateTimeFormatter.ofPattern("MMMM d");
    private int totalRows;
    private int firstColumn;
    
    public DayView(LocalDateTime startDate, ObservableList<Appointment> appointments) {
        super(startDate, startDate, Type.DAY);
        setTitle(startDate.format(monthDay));
        createDatePlans(appointments);
    }
    public DayView(LocalDateTime startDate) {
        super(startDate, startDate, Type.DAY);
        setTitle(startDate.format(monthDay));
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
        DayPlan dayPlan = new DayPlan(getStartDate().toLocalDate());
        addDayPlan(dayPlan);
        
        // Use lambda to simplify function operation iterating through list
        appointments.forEach((appt) -> {
            dayPlan.addAppointment(appt);
        });
    }

    @Override
    public void addDayPlan(DayPlan dayPlan) {
        getDayPlans().removeAll();
        getDayPlans().add(dayPlan);
    }

    @Override
    public int maxAppointmentsPerDay() {
        return 25;
    }


}

