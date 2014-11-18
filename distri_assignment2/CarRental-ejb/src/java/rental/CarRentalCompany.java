package rental;

import java.io.Serializable;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import static javax.persistence.CascadeType.PERSIST;
import javax.persistence.Entity;
import javax.persistence.EntityManager;
import javax.persistence.Id;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.PersistenceContext;
import rental.interfaces.ICarRentalCompany;


@Entity
@NamedQueries({
    // -------------------------------------------------------------------------
    // General Queries.
    // -------------------------------------------------------------------------
    @NamedQuery(name  = "CarRentalCompany.getAllCompanies",
                query = "SELECT c FROM CarRentalCompany c"),
    @NamedQuery(name  = "CarRentalCompany.getAllcompaniesName",
                query = "Select c.name From CarRentalCompany c"),
    // -------------------------------------------------------------------------
    // Manager Session Queries
    //--------------------------------------------------------------------------
    @NamedQuery(name  = "CarRentalCompany.numberOfReservationsId", 
                query = "SELECT COUNT(r) FROM Reservation r WHERE"
                      + " r.rentalCompany = :company AND r.carType = :type AND"
                      + " r.carId = :id"),
    @NamedQuery(name  = "CarRentalCompany.numberOfReservations",
                query = "SELECT COUNT(r) FROM Reservation r WHERE"
                      + " r.carType = :type AND r.rentalCompany = :company"),
    @NamedQuery(name  = "CarRentalCompany.numberOfReservationsType",
                query = "SELECT COUNT(r) FROM Reservation r WHERE" 
                      + " r.rentalCompany = :company AND r.carType = :type"),
    @NamedQuery(name  = "CarRentalCompany.numberOfReservationsClient",
                query = "SELECT COUNT(r) FROM Reservation r WHERE"
                      + " r.carRenter = :clientName"),
    // -------------------------------------------------------------------------
    // CarRental Session Queries
    // -------------------------------------------------------------------------
    @NamedQuery(name  = "CarRentalCompany.availableCarTypes",
                query = "SELECT c FROM CarRentalCompany c")
})
public class CarRentalCompany implements Serializable, ICarRentalCompany {    
    
    private static Logger logger = Logger.getLogger(CarRentalCompany.class.getName());
    
    @Id
    private String name;
    
    @OneToMany(cascade=PERSIST)
    private List<Car> cars;
    
    @OneToMany(cascade=PERSIST)
    private Set<CarType> carTypes = new HashSet<CarType>();

    /***************
     * CONSTRUCTOR *
     ***************/
    public CarRentalCompany() {
    }
    
    public CarRentalCompany(String name, List<Car> cars, EntityManager em) {
        logger.log(Level.INFO, "<{0}> Car Rental Company {0} starting up...", name);
        setName(name);
        this.cars = cars;
        for (Car car : cars) {
            carTypes.add(car.getType());
            em.merge(car.getType());
            em.persist(car);
        }
    }

    /********
     * NAME *
     ********/
    
    @Override
    public String getName() {
        return name;
    }

    @Override
    public void setName(String name) {
        this.name = name;
    }

    /*************
     * CAR TYPES *
     *************/
    
    @Override
    public Set<CarType> getAllTypes() {
        return carTypes;
    }

    @Override
    public void setAllTypes(Set<CarType> carTypes) {
        this.carTypes = carTypes;
    }
    
    @Override
    public CarType getType(String carTypeName) {
        for(CarType type:carTypes){
            if(type.getName().equals(carTypeName))
                return type;
        }
        throw new IllegalArgumentException("<" + carTypeName + "> No cartype of name " + carTypeName);
    }

    @Override
    public boolean isAvailable(String carTypeName, Date start, Date end) {
        logger.log(Level.INFO, "<{0}> Checking availability for car type {1}", new Object[]{name, carTypeName});
        return getAvailableCarTypes(start, end).contains(getType(carTypeName));
    }

    @Override
    public Set<CarType> getAvailableCarTypes(Date start, Date end) {
        Set<CarType> availableCarTypes = new HashSet<CarType>();
        for (Car car : cars) {
            if (car.isAvailable(start, end)) {
                availableCarTypes.add(car.getType());
            }
        }
        return availableCarTypes;
    }

    /*********
     * CARS *
     *********/
    
    @Override
    public Car getCar(int uid) {
        for (Car car : cars) {
            if (car.getId() == uid) {
                return car;
            }
        }
        throw new IllegalArgumentException("<" + name + "> No car with uid " + uid);
    }

    @Override
    public Set<Car> getCars(CarType type) {
        Set<Car> out = new HashSet<Car>();
        for (Car car : cars) {
            if (car.getType().equals(type)) {
                out.add(car);
            }
        }
        return out;
    }
    
    @Override
     public Set<Car> getCars(String type) {
        Set<Car> out = new HashSet<Car>();
        for (Car car : cars) {
            if (type.equals(car.getType().getName())) {
                out.add(car);
            }
        }
        return out;
    }

    private List<Car> getAvailableCars(String carType, Date start, Date end) {
        List<Car> availableCars = new LinkedList<Car>();
        for (Car car : cars) {
            if (car.getType().getName().equals(carType) && car.isAvailable(start, end)) {
                availableCars.add(car);
            }
        }
        return availableCars;
    }
    
    @Override
    public List<Car> getCars() {
        return this.cars;
    }
    
    public void setCars(List<Car> cars) {
        this.cars = cars;
    }

    /****************
     * RESERVATIONS *
     ****************/
    
    @Override
    public Quote createQuote(ReservationConstraints constraints, String guest)
            throws ReservationException {
        logger.log(Level.INFO, "<{0}> Creating tentative reservation for {1} with constraints {2}",
                new Object[]{name, guest, constraints.toString()});

        CarType type = getType(constraints.getCarType());

        if (!isAvailable(constraints.getCarType(), constraints.getStartDate(), constraints.getEndDate())) {
            throw new ReservationException("<" + name
                    + "> No cars available to satisfy the given constraints.");
        }

        double price = calculateRentalPrice(type.getRentalPricePerDay(), constraints.getStartDate(), constraints.getEndDate());

        return new Quote(guest, constraints.getStartDate(), constraints.getEndDate(), getName(), constraints.getCarType(), price);
    }

    // Implementation can be subject to different pricing strategies
    private double calculateRentalPrice(double rentalPricePerDay, Date start, Date end) {
        return rentalPricePerDay * Math.ceil((end.getTime() - start.getTime())
                / (1000 * 60 * 60 * 24D));
    }

    @Override
    public Reservation confirmQuote(Quote quote) throws ReservationException {
        logger.log(Level.INFO, "<{0}> Reservation of {1}", new Object[]{name, quote.toString()});
        List<Car> availableCars = getAvailableCars(quote.getCarType(), quote.getStartDate(), quote.getEndDate());
        if (availableCars.isEmpty()) {
            throw new ReservationException("Reservation failed, all cars of type " + quote.getCarType()
                    + " are unavailable from " + quote.getStartDate() + " to " + quote.getEndDate());
        }
        Car car = availableCars.get((int) (Math.random() * availableCars.size()));

        Reservation res = new Reservation(quote, car.getId());
        car.addReservation(res);
        return res;
    }

    @Override
    public void cancelReservation(Reservation res) {
        logger.log(Level.INFO, "<{0}> Cancelling reservation {1}", new Object[]{name, res.toString()});
        getCar(res.getCarId()).removeReservation(res);
    }
    
    @Override
    public Set<Reservation> getReservationsBy(String renter) {
        logger.log(Level.INFO, "<{0}> Retrieving reservations by {1}", new Object[]{name, renter});
        Set<Reservation> out = new HashSet<Reservation>();
        for(Car c : cars) {
            for(Reservation r : c.getReservations()) {
                if(r.getCarRenter().equals(renter))
                    out.add(r);
            }
        }
        return out;
    }
    
    @Override
    public int getNumReservationsForType(String type) {
        int count = 0;
        CarType carType = this.getType(type);
        for (Car car : cars) {
            if (car.getType().equals(carType)) {
                count += car.getNumReservations();
            }
        }
        return count;
    }
}