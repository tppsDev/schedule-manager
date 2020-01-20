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
 * City class is the data model for the city table in the U05EX0 database
 */
public class City {
    private int cityId;
    private String city;
    private Country country;
    private LocalDateTime createDate;
    private String createdBy;
    private LocalDateTime lastUpdate;
    private String lastUpdatedBy;
    
//  Constructor

    public City(int cityId, String city, Country country, LocalDateTime createDate, String createdBy, LocalDateTime lastUpdate, String lastUpdatedBy) {
        this.cityId = cityId;
        this.city = city;
        this.country = country;
        this.createDate = createDate;
        this.createdBy = createdBy;
        this.lastUpdate = lastUpdate;
        this.lastUpdatedBy = lastUpdatedBy;
    }
    
    public City(String city, Country country) {
        this.city = city;
        this.country = country;
    }
    
//  Setter Methods
    public int getCityId() {
        return cityId;
    }

    public String getCity() {
        return city;
    }

    public Country getCountry() {
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
    public void setCityId(int cityId) {
        this.cityId = cityId;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public void setCountry(Country country) {
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
    
    public City copy() {
        return new City(cityId, city, country, createDate, createdBy, lastUpdate, lastUpdatedBy);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(city).append(", ").append(country.getCountry());
        return sb.toString();
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 97 * hash + this.cityId;
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
        final City other = (City) obj;
        if (this.cityId != other.cityId) {
            return false;
        }
        return true;
    }
    
}
