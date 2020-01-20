/*
 * Project written by: Tim Smith
 * 
 */
package schedulemanager.view_controller;

import java.time.LocalDate;
import java.util.List;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import schedulemanager.model.Appointment;
import schedulemanager.model.User;

/**
 *
 * @author Tim Smith
 */
public class DayPlan {
    private LocalDate planDate;
    private int gridPoint[] = new int[2];
    private ObservableList<Appointment> appointments;

    public DayPlan(LocalDate planDate) {
        this.planDate = planDate;
        appointments = FXCollections.observableArrayList();
    }
    
    public DayPlan(LocalDate planDate, ObservableList<Appointment> appointments) {
        this.planDate = planDate;
        this.appointments = appointments;
    }

    // Getter Methods
    public LocalDate getPlanDate() {
        return planDate;
    }

    public List<Appointment> getAppointments() {
        return appointments;
    }

    public int[] getGridPoint() {
        return gridPoint;
    }

    // Setter methods

    public void setGridPoint(int row, int col) {
        gridPoint[0] = row;
        gridPoint[1] = col;
    }

    public void setAppointments(ObservableList<Appointment> appointments) {
        this.appointments = appointments;
    }
    
    public void addAppointment(Appointment appointment) {
        appointments.add(appointment);
    }
    
    public void updateAppointment(Appointment appointment) {
        
    }
    
    public void deleteAppointment(Appointment appointment) {
        
    }
    
    public void refreshDayPlan(User user) {
        // TODO call appointment dao to run query for this users appts on this day
    }
}
