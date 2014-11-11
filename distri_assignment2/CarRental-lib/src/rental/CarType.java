package rental;

import java.io.Serializable;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

@Entity
public class CarType implements Serializable {    
    //--------------------------------------------------------------------------
    // Constructor
    //--------------------------------------------------------------------------
    // Java ee entity convention.
    /**
     * Create a new instance of CarType
     */
    public CarType() {
    }
    
    public CarType(String name, 
                   int nbOfSeats, 
                   float trunkSpace, 
                   double rentalPricePerDay, 
                   boolean smokingAllowed) {
        this.name = name;
        this.nbOfSeats = nbOfSeats;
        this.trunkSpace = trunkSpace;
        this.rentalPricePerDay = rentalPricePerDay;
        this.smokingAllowed = smokingAllowed;
    }

    private long id;
    
    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    public long getId() {
        return this.id;
    }
    
    public void setId(long id) {
        this.id = id;
    }
    
    //--------------------------------------------------------------------------
    // Properties
    //--------------------------------------------------------------------------
    public String getName() {
    	return this.name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
   
    private String name;

    //--------------------------------------------------------------------------
    public int getNbOfSeats() {
        return nbOfSeats;
    }
    
    public void setNbOfSeats(int num) {
        this.nbOfSeats = num;
    }
    
    private int nbOfSeats;
    
    //--------------------------------------------------------------------------
    public boolean isSmokingAllowed() {
        return smokingAllowed;
    }
    
    public void setSmokingAllowed(boolean allowed) {
        this.smokingAllowed = allowed;
    }
    
    private boolean smokingAllowed;

    //--------------------------------------------------------------------------
    public double getRentalPricePerDay() {
        return rentalPricePerDay;
    }
    
    public void setRentalPricePerDay(double price) {
        this.rentalPricePerDay = price;
    }
    
    private double rentalPricePerDay;
    
    //--------------------------------------------------------------------------
    public float getTrunkSpace() {
    	return trunkSpace;
    }
    
    public void setTrunkSpace(float space) {
        this.trunkSpace = space;
    }
    
    //trunk space in liters
    private float trunkSpace;
    
    //--------------------------------------------------------------------------
    // Class methods
    //--------------------------------------------------------------------------
    @Override
    public String toString() {
    	return String.format("Car type: %s \t[seats: %d, price: %.2f, smoking: %b, trunk: %.0fl]" , 
                getName(), getNbOfSeats(), getRentalPricePerDay(), isSmokingAllowed(), getTrunkSpace());
    }

    @Override
    public int hashCode() {
        final int prime = 31;
	int result = 1;
	result = prime * result + ((name == null) ? 0 : name.hashCode());
	return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
	if (obj == null)
            return false;
	if (getClass() != obj.getClass())
            return false;
	CarType other = (CarType) obj;
	if (name == null) {
            if (other.name != null)
                return false;
        } else if (!name.equals(other.name))
            return false;
	return true;
    }
}