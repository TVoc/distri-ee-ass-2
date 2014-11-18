package rental;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.NamedQuery;
import javax.persistence.NamedQueries;

@Entity
@NamedQueries({
    @NamedQuery(name  = "Reservation.bestClients",
                query = "SELECT DISTINCT r.carRenter FROM Reservation r"),
    @NamedQuery(name  = "Reservation.reservationsByClient",
                query = "SELECT COUNT(r) FROM Reservation r WHERE"
                      + " r.carRenter = :renter")
})
public class Reservation extends Quote {
    private int carId;
    
    /***************
     * CONSTRUCTOR *
     ***************/
    public Reservation() {
        super();
    }
    
    public Reservation(Quote quote, int carId) {
    	super(quote.getCarRenter(), quote.getStartDate(), quote.getEndDate(), 
    		quote.getRentalCompany(), quote.getCarType(), quote.getRentalPrice());
        this.carId = carId;
    }
    
    /******
     * ID *
     ******/
    
    public int getCarId() {
    	return carId;
    }
    
    public void setCarId(int id) {
        this.carId = id;
    }
    
    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    public long getId() {
        return id;
    }
    
    public void setId(long id) {
        this.id = id;
    }
    
    private long id;
    
    /*************
     * TO STRING *
     *************/
    
    @Override
    public String toString() {
        return String.format("Reservation for %s from %s to %s at %s\nCar type: %s\tCar: %s\nTotal price: %.2f", 
                getCarRenter(), getStartDate(), getEndDate(), getRentalCompany(), getCarType(), getCarId(), getRentalPrice());
    }	
}