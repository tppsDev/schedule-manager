/*
 * Project written by: Tim Smith
 * 
 */
package schedulemanager.view_controller;

import java.time.LocalDateTime;
import javafx.collections.ObservableList;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import schedulemanager.DAO.AppointmentDaoImpl;
import schedulemanager.DAO.SysOpException;
import schedulemanager.model.Appointment;
import schedulemanager.model.User;

/**
 *
 * @author Tim Smith
 */
public class MainScreenService extends Service<ObservableList<Appointment>> {
    
    private User user = Session.getSession().getSessionUser();
    private CalendarView.Type viewType;
    private LocalDateTime startDate;
    private LocalDateTime endDate;

    // Getter methods
    public LocalDateTime getStartDate() {
        return startDate;
    }

    public LocalDateTime getEndDate() {
        return endDate;
    }

    public CalendarView.Type getViewType() {
        return viewType;
    }
    
    // Setter methods
    public void setStartDate(LocalDateTime startDate) {
        this.startDate = startDate;
    }

    public void setEndDate(LocalDateTime endDate) {
        this.endDate = endDate;
    }

    public void setViewType(CalendarView.Type viewType) {
        this.viewType = viewType;
    }

    public void setUser(User currentUser) {
        this.user = currentUser;
    }
    
    
    
    @Override
    protected Task<ObservableList<Appointment>> createTask() {
        return new Task<ObservableList<Appointment>>() {
            @Override
            protected ObservableList<Appointment> call() throws SysOpException, Exception {
                AppointmentDaoImpl appointmentDaoImpl = AppointmentDaoImpl.getAppointmentDaoImpl();
                ObservableList<Appointment> appointments;

                if (viewType.equals(CalendarView.Type.DAY)) {
                    appointments = appointmentDaoImpl.getUserAppointmentsForDate(user, startDate);
                } else {
                    appointments = appointmentDaoImpl.getUserAppointmentsByDateRange(user, startDate, endDate);
                }
                
                return appointments;
            }
        };
    }
    
}
