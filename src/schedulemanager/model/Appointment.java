/*
 * Project written by: Tim Smith
 * 
 */
package schedulemanager.model;

import java.time.LocalDateTime;
import static schedulemanager.utility.DateTimeHandler.*;

/**
 *
 * @author Tim Smith
 * Appointment class is the data model for the appointment table in the U05EX0 database
 */
public class Appointment {
    private int appointmentId;
    private Customer customer;
    private User user;
    private String title;
    private String description;
    private String location;
    private String contact;
    private String type;
    private String url;
    private LocalDateTime start;
    private LocalDateTime end;
    private LocalDateTime createDate;
    private String createdBy;
    private LocalDateTime lastUpdate;
    private String lastUpdatedBy;

    
//  Constructor    
    public Appointment(int appointmentId, Customer customer, User user, String title, String description, String location, 
            String contact, String type, String url, LocalDateTime start, LocalDateTime end, LocalDateTime createDate, String createdBy, 
            LocalDateTime lastUpdate, String lastUpdatedBy) {
        this.appointmentId = appointmentId;
        this.customer = customer;
        this.user = user;
        this.title = title;
        this.description = description;
        this.location = location;
        this.contact = contact;
        this.type = type;
        this.url = url;
        this.start = start;
        this.end = end;
        this.createDate = createDate;
        this.createdBy = createdBy;
        this.lastUpdate = lastUpdate;
        this.lastUpdatedBy = lastUpdatedBy;
    }
    
    public Appointment() {
        
    }

//  Getter Methods    
    public int getAppointmentId() {
        return appointmentId;
    }

    public Customer getCustomer() {
        return customer;
    }

    public User getUser() {
        return user;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public String getLocation() {
        return location;
    }

    public String getContact() {
        return contact;
    }

    public String getType() {
        return type;
    }

    public String getUrl() {
        return url;
    }

    public LocalDateTime getStart() {
        return start;
    }

    public LocalDateTime getEnd() {
        return end;
    }

    public LocalDateTime getCreateDate() {
        return createDate;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public LocalDateTime getLastUpdate() {
        return lastUpdate;
    }

    public String getLastUpdatedBy() {
        return lastUpdatedBy;
    }
    
// Setter Methods    
    public void setAppointmentId(int appointmentId) {
        this.appointmentId = appointmentId;
    }

    public void setCustomer(Customer customer) {
        this.customer = customer;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public void setContact(String contact) {
        this.contact = contact;
    }

    public void setType(String type) {
        this.type = type;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public void setStart(LocalDateTime start) {
        this.start = start;
    }

    public void setEnd(LocalDateTime end) {
        this.end = end;
    }

    public void setCreateDate(LocalDateTime createDate) {
        this.createDate = createDate;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public void setLastUpdate(LocalDateTime lastUpdate) {
        this.lastUpdate = lastUpdate;
    }

    public void setLastUpdatedBy(String lastUpdatedBy) {
        this.lastUpdatedBy = lastUpdatedBy;
    }
    
    public Appointment copy() {
        return new Appointment(appointmentId, customer, user, title, description, location, 
                               contact, type, url, start, end, createDate, createdBy, lastUpdate, lastUpdatedBy);
    }
    
    public String toReportString() {
        StringBuilder sb = new StringBuilder();
        sb.append(title).append("\n\t");
        sb.append("From: ").append(start.format(FULL_DATE_TIME)).append("\n\t");
        sb.append("To: ").append(end.format(FULL_DATE_TIME)).append("\n\t");
        sb.append("Customer: ").append(customer.getCustomerName()).append("\n\t");
        sb.append("Appointment Type: ").append(type).append("\n\t");
        sb.append("Location: ").append(location).append("\n\t");
        return sb.toString();
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(title).append("\n");
        sb.append("From: ").append(start.format(FULL_DATE_TIME)).append("\n");
        sb.append("To: ").append(end.format(FULL_DATE_TIME)).append("\n");
        sb.append("Customer: ").append(customer.getCustomerName()).append("\n");
        sb.append("Consultant: ").append(user.getUserName()).append("\n");
        return sb.toString();
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 31 * hash + this.appointmentId;
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Appointment other = (Appointment) obj;
        return this.appointmentId == other.appointmentId;
    }

}
