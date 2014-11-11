package rental;

import java.io.Serializable;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import rental.interfaces.ICar;

@Entity
public class Car implements ICar, Serializable {
    private int id;
    
    private CarType type;
    private Set<Reservation> reservations;

    /***************
     * CONSTRUCTOR *
     ***************/
    
    public Car() {
    }
    
    public Car(int uid, CarType type) {
    	this.id = uid;
        this.type = type;
        this.reservations = new HashSet<Reservation>();
    }

    /******
     * ID *
     * @return 
     ******/
    
    @Override
    public int getId() {
    	return id;
    }
    
    @Override
    public void setId(int id) {
        this.id = id;
    }
    
    private long dbId;
    
    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    public long getDbId() {
        return this.dbId;
    }
    
    public void setDbId(long dbId) {
        this.dbId = dbId;
    }
    
    /************
     * CAR TYPE *
     * @return 
     ************/
    
    @Override
    public CarType getType() {
        return type;
    }

    /****************
     * RESERVATIONS *
     ****************/

    @Override
    public boolean isAvailable(Date start, Date end) {
        if(!start.before(end))
            throw new IllegalArgumentException("Illegal given period");

        for(Reservation reservation : reservations) {
            if(reservation.getEndDate().before(start) || reservation.getStartDate().after(end))
                continue;
            return false;
        }
        return true;
    }
    
    @Override
    public void addReservation(Reservation res) {
        reservations.add(res);
    }
    
    @Override
    public void removeReservation(Reservation reservation) {
        // equals-method for Reservation is required!
        reservations.remove(reservation);
    }

    @OneToMany
    public Set<Reservation> getReservations() {
        return reservations;
    }
    
    @Override
    public void setReservations(Set<Reservation> reservations) {
        this.reservations = reservations;
    }
    
    @Override
    public int getNumReservations() {
        return this.getReservations().size();
    }
}