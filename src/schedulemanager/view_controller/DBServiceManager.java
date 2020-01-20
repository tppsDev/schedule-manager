/*
 * Project written by: Tim Smith
 * 
 */
package schedulemanager.view_controller;

import java.time.LocalDateTime;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import javafx.collections.ObservableList;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import schedulemanager.DAO.AddressDaoImpl;
import schedulemanager.DAO.AppointmentDaoImpl;
import schedulemanager.DAO.CityDaoImpl;
import schedulemanager.DAO.CountryDaoImpl;
import schedulemanager.DAO.CustomerDaoImpl;
import schedulemanager.DAO.SysOp.queryType;
import schedulemanager.DAO.SysOpException;
import schedulemanager.DAO.UserDaoImpl;
import schedulemanager.model.Address;
import schedulemanager.model.Appointment;
import schedulemanager.model.ApptTypeByMonthRow;
import schedulemanager.model.AvgDailyApptsByUserRow;
import schedulemanager.model.City;
import schedulemanager.model.Country;
import schedulemanager.model.Customer;
import schedulemanager.model.User;

/**
 *
 * @author Tim Smith
 */
public class DBServiceManager {
    private AppointmentDaoImpl apptDaoImpl = AppointmentDaoImpl.getAppointmentDaoImpl();
    private CustomerDaoImpl customerDaoImpl = CustomerDaoImpl.getCustomerDaoImpl();
    private UserDaoImpl userDaoImpl = UserDaoImpl.getUserDaoImpl();
    private AddressDaoImpl addressDaoImpl = AddressDaoImpl.getAddressDaoImpl();
    private CityDaoImpl cityDaoImpl = CityDaoImpl.getCityDaoImpl();
    private CountryDaoImpl countryDaoImpl = CountryDaoImpl.getCountryDaoImpl();
    private ExecutorService executor = Executors.newFixedThreadPool(1);

// Create Services    
    class InsertAddressService extends Service<Void> {
        private Address addr;
        
        public void setAddress(Address addr) {
            this.addr = addr;
        }
        
        public InsertAddressService() {
            super();
            this.setExecutor(executor);
        }
        
        @Override
        protected Task<Void> createTask() {
            return new Task<Void>() {
                @Override
                protected Void call() throws Exception {
                    addressDaoImpl.insert(addr);
                    return null;
                }
            };
        }
        
    }
    
    class InsertAppointmentService extends Service<Void> {
        private Appointment appt;

        public void setAppt(Appointment appt) {
            this.appt = appt;
        }
        
        public InsertAppointmentService() {
            super();
            this.setExecutor(executor);
        }
        
        @Override
        protected Task<Void> createTask() {
            return new Task<Void>() {
                @Override
                protected Void call() throws Exception {
                    try {
                        apptDaoImpl.insert(appt);
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                    return null;
                }
            };
        }
        
    }
    
    class InsertCityService extends Service<Void> {
        private City city;
        
        public void setCity(City city) {
            this.city = city;
        }
        
        public InsertCityService() {
            super();
            this.setExecutor(executor);
        }
        
        @Override
        protected Task<Void> createTask() {
            return new Task<Void>() {
                @Override
                protected Void call() throws Exception {
                    cityDaoImpl.insert(city);
                    return null;
                }
            };
        }
        
    }
    
    class InsertCountryService extends Service<Void> {
        private Country country;
        
        public void setCountry(Country country) {
            this.country = country;
        }
        
        public InsertCountryService() {
            super();
            this.setExecutor(executor);
        }
        
        @Override
        protected Task<Void> createTask() {
            return new Task<Void>() {
                @Override
                protected Void call() throws Exception {
                    countryDaoImpl.insert(country);
                    return null;
                }
            };
        }
        
    }
    
    class InsertCustomerService extends Service<Void> {
        private Customer customer;
        
        public InsertCustomerService() {
            super();
            this.setExecutor(executor);
        }
        
        public void setCustomer(Customer customer) {
            this.customer = customer;
        }
        
        @Override
        protected Task<Void> createTask() {
            return new Task<Void>() {
                @Override
                protected Void call() throws Exception {
                    customerDaoImpl.insert(customer);
                    return null;
                }
            };
        }
        
    }
    
    class InsertUserService extends Service<Void> {
        private User user;
        
        public void setUser(User user) {
            this.user = user;
        }
        
        public InsertUserService() {
            super();
            this.setExecutor(executor);
        }
        
        @Override
        protected Task<Void> createTask() {
            return new Task<Void>() {
                @Override
                protected Void call() throws Exception {
                    userDaoImpl.insert(user);
                    return null;
                }
            };
        }
        
    }
    
// Read Services
    class GetAllAddressesService extends Service<ObservableList<Address>> {
        public GetAllAddressesService() {
            super();
            this.setExecutor(executor);
        }
        
        @Override
        protected Task<ObservableList<Address>> createTask() {
            return new Task<ObservableList<Address>>() {
                @Override
                protected ObservableList<Address> call() throws Exception {
                    return addressDaoImpl.getAll();
                }
            };
        }
    }
    
    class GetAllCustomersService extends Service<ObservableList<Customer>> {
        
        public GetAllCustomersService() {
            super();
            this.setExecutor(executor);
        }
        
        @Override
        protected Task<ObservableList<Customer>> createTask() {
            return new Task<ObservableList<Customer>>() {
                @Override
                protected ObservableList<Customer> call() throws Exception {
                    return customerDaoImpl.getAllOrderByCustName();
                }
            };
        }
        
    }
    
    class GetActiveCustomersService extends Service<ObservableList<Customer>> {
        
        public GetActiveCustomersService() {
            super();
            this.setExecutor(executor);
        }
        
        @Override
        protected Task<ObservableList<Customer>> createTask() {
            return new Task<ObservableList<Customer>>() {
                @Override
                protected ObservableList<Customer> call() throws Exception {
                    return customerDaoImpl.getAllActiveOrderByCustName();
                }
            };
        }
        
    }
    
    class GetAllUsersService extends Service<ObservableList<User>> {
        
        public GetAllUsersService() {
            super();
            this.setExecutor(executor);
        }
        
        @Override
        protected Task<ObservableList<User>> createTask() {
            return new Task<ObservableList<User>>() {
                @Override
                protected ObservableList<User> call() throws Exception {
                    return userDaoImpl.getAllOrderByUserName();
                }
            };
        }
    }
    
    class GetActiveUsersService extends Service<ObservableList<User>> {
        
        public GetActiveUsersService() {
            super();
            this.setExecutor(executor);
        }
        
        @Override
        protected Task<ObservableList<User>> createTask() {
            return new Task<ObservableList<User>>() {
                @Override
                protected ObservableList<User> call() throws Exception {
                    return userDaoImpl.getAllActiveOrderByUserName();
                }
            };
        }
    }
    
    class GetAllCitiesService extends Service<ObservableList<City>> {

        public GetAllCitiesService() {
            super();
            this.setExecutor(executor);
        }
        
        @Override
        protected Task<ObservableList<City>> createTask() {
            return new Task<ObservableList<City>>() {
                @Override
                protected ObservableList<City> call() throws Exception {
                    return cityDaoImpl.getAllOrderByCityCountry();
                }
            };
        }
        
    }
    
    class GetAllCountriesService extends Service<ObservableList<Country>> {
        
        public GetAllCountriesService() {
            super();
            this.setExecutor(executor);
        }
        
        @Override
        protected Task<ObservableList<Country>> createTask() {
            return new Task<ObservableList<Country>>() {
                @Override
                protected ObservableList<Country> call() throws Exception {
                    return countryDaoImpl.getAll();
                }
            };
        }
        
    }
    
    class GetIfUserNamerExistsService extends Service<Boolean> {
        private String userName;
        private queryType requestType;
        
        public GetIfUserNamerExistsService() {
            super();
            this.setExecutor(executor);
        }
        
        public void setUser(String userName) {
            this.userName = userName;
        }

        public queryType getRequestType() {
            return requestType;
        }

        public void setRequestType(queryType requestType) {
            this.requestType = requestType;
        }
        
        @Override
        protected Task<Boolean> createTask() {
            return new Task<Boolean>() {
                @Override
                protected Boolean call() throws SysOpException, Exception {
                    return userDaoImpl.getIfUserNameExists(userName);
                }
            };
        }
        
    }
    
    class GetUserOverlappingAppointmentsService extends Service<Boolean> {
        private User user;
        private LocalDateTime fromDate;
        private LocalDateTime toDate;
        private Appointment appt;
        private boolean newAppt = false;
        private ObservableList<Appointment> overlappingAppts;

        public GetUserOverlappingAppointmentsService() {
            super();
            this.setExecutor(executor);
        }
        
        public void setUser(User user) {
            this.user = user;
        }

        public void setFromDate(LocalDateTime fromDate) {
            this.fromDate = fromDate;
        }

        public void setToDate(LocalDateTime toDate) {
            this.toDate = toDate;
        }
        
        public void setNewAppt(Boolean newAppt) {
            this.newAppt = newAppt;
        }

        public void setAppt(Appointment appt) {
            this.appt = appt;
        }
        
        public ObservableList<Appointment> getOverlappingAppts() {
            return overlappingAppts;
        }
        
        @Override
        protected Task<Boolean> createTask() {
            return new Task<Boolean>() {
                @Override
                protected Boolean call() throws Exception {
                    overlappingAppts = apptDaoImpl.getUserOverlappingAppointments(user, fromDate, toDate, appt, newAppt);

                    if (!overlappingAppts.isEmpty()) {
                        throw new ApptFormException(ApptFormException.InputField.ALL_TIME_FIELDS, "Schedule conflict");
                    }
                    return true;
                }
            };
        }
        
    }
    
    class  GetJustAddedAddrService extends Service<Address> {
        private Address addr;

        public void setAddr(Address addr) {
            this.addr = addr;
        }
        
        @Override
        protected Task<Address> createTask() {
            return new Task<Address>() {
                @Override
                protected Address call() throws Exception {
                    return addressDaoImpl.getJustAddedAddr(addr);
                }
                
            };
        }
        
    }
    
    class CheckIfCityExistsService extends Service<Boolean> {
        private String cityName;
        private int countryId;
        private queryType requestType;

        public CheckIfCityExistsService() {
            super();
            this.setExecutor(executor);
        }
        
        public void setCityName(String cityName) {
            this.cityName = cityName;
        }

        public void setCountryId(int countryId) {
            this.countryId = countryId;
        }

        public queryType getRequestType() {
            return requestType;
        }

        public void setRequestType(queryType requestType) {
            this.requestType = requestType;
        }
        
        
        @Override
        protected Task<Boolean> createTask() {
            return new Task<Boolean>() {
                @Override
                protected Boolean call() throws Exception {
                    return cityDaoImpl.checkIfExists(cityName, countryId);
                }
            };
        }
        
    }
    
    class CheckIfCountryExistsService extends Service<Boolean> {
        private String countryName;
        private queryType requestType;

        public CheckIfCountryExistsService() {
            super();
            this.setExecutor(executor);
        }
        
        public void setCountryName(String countryName) {
            this.countryName = countryName;
        }
        
        public queryType getRequestType() {
            return requestType;
        }

        public void setRequestType(queryType requestType) {
            this.requestType = requestType;
        }
        
        
        @Override
        protected Task<Boolean> createTask() {
            return new Task<Boolean>() {
                @Override
                protected Boolean call() throws Exception {
                    return countryDaoImpl.checkIfExists(countryName);
                }
            };
        }
        
    }
    
    class GetCountOfApptTypesForMonthService extends Service<ObservableList<ApptTypeByMonthRow>> {
        private int month;

        public void setMonth(int month) {
            this.month = month;
        }

        @Override
        protected Task<ObservableList<ApptTypeByMonthRow>> createTask() {
            return new Task<ObservableList<ApptTypeByMonthRow>>() {
                @Override
                protected ObservableList<ApptTypeByMonthRow> call() throws SysOpException {
                    try {
                        return apptDaoImpl.getCountOfApptTypesForMonth(month);
                    } catch (Exception ex) {
                        // TODO
                        throw new SysOpException("DBServiceManager threw Error");
                    }
                }
            };
        }
    }
    
    class GetAvgDailyAppointmentCountByUserService extends Service<ObservableList<AvgDailyApptsByUserRow>> {

        @Override
        protected Task<ObservableList<AvgDailyApptsByUserRow>> createTask() {
            return new Task<ObservableList<AvgDailyApptsByUserRow>>() {
                @Override
                protected ObservableList<AvgDailyApptsByUserRow> call() throws Exception {
                    return apptDaoImpl.getAvgDailyAppointmentCountByUser();
                }
            };
        }
    }
    
    class GetUserScheduleService extends Service<ObservableList<Appointment>> {
        private User user;
        private LocalDateTime fromDate;

        public void setUser(User user) {
            this.user = user;
        }

        public void setFromDate(LocalDateTime fromDate) {
            this.fromDate = fromDate;
        }

        @Override
        protected Task<ObservableList<Appointment>> createTask() {
            return new Task<ObservableList<Appointment>>() {
                @Override
                protected ObservableList<Appointment> call() throws SysOpException {
                    try {
                        return apptDaoImpl.getUserSchedule(user, fromDate);
                    } catch (Exception ex) {
                        // TODO
                        throw new SysOpException("DBServiceManager threw Error");
                    }
                }
            };
        }
        
    }
    
    
    
// Update Services
    class UpdateAddressService extends Service<Void> {
        private Address address;

        public UpdateAddressService() {
            super();
            this.setExecutor(executor);
        }
        
        public void setAddress(Address address) {
            this.address = address;
        }

        @Override
        protected Task<Void> createTask() {
            return new Task<Void>() {
                @Override
                protected Void call() throws Exception {
                    try {
                        addressDaoImpl.update(address);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    return null;
                }
            };
        }
        
    }
    
    class UpdateAppointmentService extends Service<Void> {
        private Appointment appt;

        public UpdateAppointmentService() {
            super();
            this.setExecutor(executor);
        }
        
        public void setAppt(Appointment appt) {
            this.appt = appt;
        }

        @Override
        protected Task<Void> createTask() {
            return new Task<Void>() {
                @Override
                protected Void call() throws Exception {
                    try {
                        apptDaoImpl.update(appt);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    return null;
                }
            };
        }
        
    }
    
    class UpdateCityService extends Service<Void> {
        private City city;
        
        public UpdateCityService() {
            super();
            this.setExecutor(executor);
        }
        
        public void setCity(City city) {
            this.city = city;
        }
        
        @Override
        protected Task<Void> createTask() {
            return new Task<Void>() {
                @Override
                protected Void call() throws Exception {
                    cityDaoImpl.update(city);
                    return null;
                }
            };
        }
        
    }
    
    class UpdateCountryService extends Service<Void> {
        private Country country;
        
        public UpdateCountryService() {
            super();
            this.setExecutor(executor);
        }
        
        public void setCountry(Country country) {
            this.country = country;
        }
        
        @Override
        protected Task<Void> createTask() {
            return new Task<Void>() {
                @Override
                protected Void call() throws Exception {
                    countryDaoImpl.update(country);
                    return null;
                }
            };
        }
        
    }
    
    class UpdateCustomerService extends Service<Void> {
        private Customer customer;
        
        public UpdateCustomerService() {
            super();
            this.setExecutor(executor);
        }
        
        public void setCustomer(Customer customer) {
            this.customer = customer;
        }
        
        @Override
        protected Task<Void> createTask() {
            return new Task<Void>() {
                @Override
                protected Void call() throws Exception {
                    customerDaoImpl.update(customer);
                    return null;
                }
            };
        }
        
    }
    
    class UpdateUserService extends Service<Void> {
        private User user;

        public UpdateUserService() {
            super();
            this.setExecutor(executor);
        }
        
        public void setUser(User user) {
            this.user = user;
        }

        @Override
        protected Task<Void> createTask() {
            return new Task<Void>() {
                @Override
                protected Void call() throws Exception {
                    try {
                        userDaoImpl.update(user);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    return null;
                }
            };
        }
        
    }
    
// Delete Services
    class DeleteAddressService extends Service<Void> {
        private Address addr;

        public DeleteAddressService() {
            super();
            this.setExecutor(executor);
        }
        
        public void setAddress(Address addr) {
            this.addr = addr;
        }

        @Override
        protected Task<Void> createTask() {
            return new Task<Void>() {
                @Override
                protected Void call() throws Exception {
                    addressDaoImpl.delete(addr);
                    return null;
                }
            };
        }
        
    }
    
    class DeleteAppointmentService extends Service<Void> {
        private Appointment appt;

        public DeleteAppointmentService() {
            super();
            this.setExecutor(executor);
        }
        
        public void setAppt(Appointment appt) {
            this.appt = appt;
        }
        
        @Override
        protected Task<Void> createTask() {
            return new Task<Void>() {
                @Override
                protected Void call() throws Exception {
                    apptDaoImpl.delete(appt);
                    return null;                    
                }
            };
        }
    }
    
    class DeleteCityService extends Service<Void> {
        private City city;

        public DeleteCityService() {
            super();
            this.setExecutor(executor);
        }
        
        public void setCity(City city) {
            this.city = city;
        }

        @Override
        protected Task<Void> createTask() {
            return new Task<Void>() {
                @Override
                protected Void call() throws Exception {
                    cityDaoImpl.delete(city);
                    return null;
                }
            };
        }
        
    }
    
    class DeleteCountryService extends Service<Void> {
        private Country country;

        public DeleteCountryService() {
            super();
            this.setExecutor(executor);
        }
        
        public void setCountry(Country country) {
            this.country = country;
        }

        @Override
        protected Task<Void> createTask() {
            return new Task<Void>() {
                @Override
                protected Void call() throws Exception {
                    countryDaoImpl.delete(country);
                    return null;
                }
            };
        }
        
    }
    
    class DeleteCustomerService extends Service<Void> {
        private Customer customer;

        public DeleteCustomerService() {
            super();
            this.setExecutor(executor);
        }
        
        public void setCustomer(Customer customer) {
            this.customer = customer;
        }

        @Override
        protected Task<Void> createTask() {
            return new Task<Void>() {
                @Override
                protected Void call() throws Exception {
                    customerDaoImpl.delete(customer);
                    return null;
                }
            };
        }
        
    }
    
    class DeleteUserService extends Service<Void> {
        private User user;

        public DeleteUserService() {
            super();
            this.setExecutor(executor);
        }
        
        public void setUser(User user) {
            this.user = user;
        }

        @Override
        protected Task<Void> createTask() {
            return new Task<Void>() {
                @Override
                protected Void call() throws Exception {
                    userDaoImpl.delete(user);
                    return null;
                }
            };
        }
        
    }
}
