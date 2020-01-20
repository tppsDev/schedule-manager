/*
 * Project written by: Tim Smith
 * 
 */
package schedulemanager.view_controller;

import java.time.LocalDateTime;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import schedulemanager.model.Appointment;

/**
 *
 * @author Tim Smith
 */
public abstract class CalendarView {
    public static enum Type {
        MONTH("Month"),
        WEEK("Week"),
        DAY("Day");
        
        private String label;
        
        Type(String label) {
            this.label = label;
        }
        
        @Override
        public String toString() {
            return label;
        }
    }
    private String title;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private Type viewType;
    private ObservableList<DayPlan> dayPlans;

    public CalendarView(LocalDateTime startDate, LocalDateTime endDate, Type viewType) {
        this.startDate = startDate;
        this.endDate = endDate;
        this.viewType = viewType;
        dayPlans = FXCollections.observableArrayList();
    }

    // Getter methods
    public String getTitle() {
        return title;
    }

    public LocalDateTime getStartDate() {
        return startDate;
    }

    public LocalDateTime getEndDate() {
        return endDate;
    }

    public Type getViewType() {
        return viewType;
    }
    
    public ObservableList<DayPlan> getDayPlans() {
        return dayPlans;
    }

    public DayPlan getDayPlanByDay(int day) {
        DayPlan dayPlan = null;

        for (DayPlan dp : getDayPlans()) {
            if ( dp.getPlanDate().getDayOfMonth() == day) dayPlan = dp;
        }
        return dayPlan;
    }
    
    // Setter methods
    public void setTitle(String title) {
        this.title = title;
    }

    public void setStartDate(LocalDateTime startDate) {
        this.startDate = startDate;
    }

    public void setEndDate(LocalDateTime endDate) {
        this.endDate = endDate;
    }

    public void setViewType(Type viewType) {
        this.viewType = viewType;
    }

    public void setDayPlans(ObservableList<DayPlan> dayPlans) {
        this.dayPlans = dayPlans;
    }
    
   
    public abstract void createDatePlans(ObservableList<Appointment> appointments);
    
    public abstract void addDayPlan(DayPlan dayPlan);
    
    public abstract int maxAppointmentsPerDay();
    
    public abstract int getTotalRows();
    
    public abstract int getFirstColumn();
    
    public abstract void setTotalRows();
    
    public abstract void setFirstColumn();
}
