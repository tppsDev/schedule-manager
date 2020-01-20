/*
 * Project written by: Tim Smith
 * 
 */
package schedulemanager.model;

import java.time.LocalDateTime;

/**
 *
 * @author Tim Smith
 * 
 * Country class is the data model for the country table in the U05EX0 database
 */
public class Country {
    private int countryId;
    private String country;
    private LocalDateTime createDate;
    private String createdBy;
    private LocalDateTime lastUpdate;
    private String lastUpdatedBy;

//  Constructor
    public Country(String country) {
        countryId = -1;
        this.country = country;
    }
    
    public Country(int countryId, String country, LocalDateTime createDate, String createdBy, LocalDateTime lastUpdate, 
            String lastUpdatedBy) {
        this.countryId = countryId;
        this.country = country;
        this.createDate = createDate;
        this.createdBy = createdBy;
        this.lastUpdate = lastUpdate;
        this.lastUpdatedBy = lastUpdatedBy;
    }

//  Getter Methods
    public int getCountryId() {
        return countryId;
    }

    public String getCountry() {
        return country;
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
    public void setCountryId(int countryId) {
        this.countryId = countryId;
    }

    public void setCountry(String country) {
        this.country = country;
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
    
    public Country copy() {
        return new Country(countryId, country, createDate, createdBy, lastUpdate, lastUpdatedBy);
    }

    @Override
    public String toString() {
        return country;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 97 * hash + this.countryId;
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
        final Country other = (Country) obj;
        if (this.countryId != other.countryId) {
            return false;
        }
        return true;
    }
    
}
