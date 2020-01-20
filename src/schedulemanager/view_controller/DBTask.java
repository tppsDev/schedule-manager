/*
 * Project written by: Tim Smith
 * 
 */
package schedulemanager.view_controller;

import java.time.LocalDateTime;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import schedulemanager.DAO.AddressDaoImpl;
import schedulemanager.DAO.AppointmentDaoImpl;
import schedulemanager.DAO.CityDaoImpl;
import schedulemanager.DAO.CountryDaoImpl;
import schedulemanager.DAO.CustomerDaoImpl;
import schedulemanager.DAO.UserDaoImpl;
import schedulemanager.model.Appointment;
import schedulemanager.model.City;
import schedulemanager.model.Country;
import schedulemanager.model.Customer;
import schedulemanager.model.User;

/**
 *
 * @author Tim Smith
 */
public class DBTask {
    AppointmentDaoImpl apptDaoImpl = AppointmentDaoImpl.getAppointmentDaoImpl();
    CustomerDaoImpl customerDaoImpl = CustomerDaoImpl.getCustomerDaoImpl();
    UserDaoImpl userDaoImpl = UserDaoImpl.getUserDaoImpl();
    AddressDaoImpl addressDaoImpl = AddressDaoImpl.getAddressDaoImpl();
    CityDaoImpl cityDaoImpl = CityDaoImpl.getCityDaoImpl();
    CountryDaoImpl countryDaoImpl = CountryDaoImpl.getCountryDaoImpl();
    
    // Create/Insert Tasks
    class InsertAppointmentTask extends Task<Void> {
        private Appointment appt;
        
        public void setAppt(Appointment appt) {
            try {
                apptDaoImpl.insert(appt);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
            this.appt = appt;
        }
        
        @Override
        protected Void call() throws Exception {
            return null;
        }
        
    }
    
    class AddCityTask extends Task<Void> {
        private City city;
        
        public void setCity(City city) {
            this.city = city;
        }
        
        @Override
        protected Void call() throws Exception {
            cityDaoImpl.insert(city);
            return null;
        }
        
    }

    // Retrieval Tasks
    class GetAllCustomersTask extends Task<ObservableList<Customer>> {

        @Override
        protected ObservableList<Customer> call() throws Exception {
            return customerDaoImpl.getAllOrderByCustName();
        }
        
    }
    
    class GetActiveCustomersTask extends Task<ObservableList<Customer>> {

        @Override
        protected ObservableList<Customer> call() throws Exception {
            return customerDaoImpl.getAllActiveOrderByCustName();
        }
        
    }
    
    class GetAllUsersTask extends Task<ObservableList<User>> {

        @Override
        protected ObservableList<User> call() throws Exception {
            return userDaoImpl.getAllOrderByUserName();
        }
        
    }
    
    class GetActiveUsersTask extends Task<ObservableList<User>> {

        @Override
        protected ObservableList<User> call() throws Exception {
            return userDaoImpl.getAllActiveOrderByUserName();
       }
        
    }
    
    class GetAllCityAndCountryTask extends Task<ObservableList<City>> {

        @Override
        protected ObservableList<City> call() throws Exception {
            return cityDaoImpl.getAllOrderByCityCountry();
        }
        
    }
    
    class GetAllCountriesTask extends Task<ObservableList<Country>> {

        @Override
        protected ObservableList<Country> call() throws Exception {
            return countryDaoImpl.getAll();
        }
        
    }
    
    class GetUserOverlappingAppointmentsTask extends Task<Boolean> {
        private User user;
        private LocalDateTime fromDate;
        private LocalDateTime toDate;
        private Appointment appt;
        private boolean newAppt = false;
        private ObservableList<Appointment> overlappingAppts;

        public void setUser(User user) {
            this.user = user;
        }

        public void setFromDate(LocalDateTime fromDate) {
            this.fromDate = fromDate;
        }

        public void setToDate(LocalDateTime toDate) {
            this.toDate = toDate;
        }

        public void setAppt(Appointment appt) {
            this.appt = appt;
        }
        
        public void setNewAppt(Boolean newAppt) {
            this.newAppt = newAppt;
        }
        
        public ObservableList<Appointment> getOverlappingAppts() {
            return overlappingAppts;
        }

        @Override
        protected Boolean call() throws Exception, ApptFormException {
            overlappingAppts = apptDaoImpl.getUserOverlappingAppointments(user, fromDate, toDate, appt, newAppt);

            if (!overlappingAppts.isEmpty()) {
                throw new ApptFormException(ApptFormException.InputField.ALL_TIME_FIELDS, "Schedule conflict");
            }
            return true;
        }
}
    
    // Update Tasks
    class UpdateAppointmentTask extends Task<Void> {
        private Appointment appt;

        public void setAppt(Appointment appt) {
            this.appt = appt;
        }

        @Override
        protected Void call() throws Exception {
            try {
                apptDaoImpl.update(appt);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }
        
    }
    
    // Delete Tasks
    class DeleteSelectedAppointmentTask extends Task<Void> {
        private Appointment appt;
        
        public DeleteSelectedAppointmentTask(Appointment appt) {
            this.appt = appt;
        }
        
        @Override
        protected Void call() throws Exception {
            apptDaoImpl.delete(appt);
            return null;
        }
        
    }
    
    class DeleteUserTask extends Task<Void> {
        private User user;

        public void setUser(User user) {
            this.user = user;
        }
        
        @Override
        protected Void call() throws Exception {
            userDaoImpl.delete(user);
            return null;
        }
        
    }
    
    static class DBThreadFactory implements ThreadFactory {
        static final AtomicInteger poolNumber = new AtomicInteger(1);

        @Override public Thread newThread(Runnable runnable) {
          Thread thread = new Thread(runnable, "Database-Query-" + poolNumber.getAndIncrement() + "-thread");
          thread.setDaemon(true);

          return thread;
        }
    }  
}
