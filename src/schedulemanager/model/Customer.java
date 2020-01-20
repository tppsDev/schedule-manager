/*
 * Project written by: Tim Smith
 * 
 */
package schedulemanager.model;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 *
 * @author Tim Smith
 * 
 * Customer class is the data model for the customer table in the U05EX0 database
 */
public class Customer {
    private int customerId;
    private String customerName;
    private Address address;
    private boolean active;
    private LocalDateTime createDate;
    private String createdBy;
    private LocalDateTime lastUpdate;
    private String lastUpdatedBy;

//  Contructor
    public Customer(int customerId, String customerName, Address address, boolean active, LocalDateTime createDate, 
                    String createdBy, LocalDateTime lastUpdate, String lastUpdatedBy) {
        this.customerId = customerId;
        this.customerName = customerName;
        this.address = address;
        this.active = active;
        this.createDate = createDate;
        this.createdBy = createdBy;
        this.lastUpdate = lastUpdate;
        this.lastUpdatedBy = lastUpdatedBy;
    }
    
    public Customer() {
        
    }
    
//  Getter Methods
    public int getCustomerId() {
        return customerId;
    }

    public String getCustomerName() {
        return customerName;
    }

    public Address getAddress() {
        return address;
    }

    public boolean isActive() {
        return active;
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

//  Setter Methods    
    public void setCustomerId(int customerId) {
        this.customerId = customerId;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    public void setAddress(Address address) {
        this.address = address;
    }

    public void setActive(boolean active) {
        this.active = active;
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
    
    public Customer copy() {
        return new Customer(customerId, customerName, address, active, createDate, createdBy, lastUpdate, lastUpdatedBy);
    }

    @Override
    public String toString() {
        return customerName;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 23 * hash + this.customerId;
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
        final Customer other = (Customer) obj;
        return this.customerId == other.customerId;
    }
}
