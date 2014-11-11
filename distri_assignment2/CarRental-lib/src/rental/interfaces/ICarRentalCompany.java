/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package rental.interfaces;

import java.util.Date;
import java.util.List;
import java.util.Set;
import javax.persistence.Id;
import javax.persistence.MappedSuperclass;
import rental.CarType;
import rental.Quote;
import rental.Reservation;
import rental.ReservationConstraints;
import rental.ReservationException;

/**
 *
 * @author Thomas
 */
public interface ICarRentalCompany {
    
    public String getName();
    
    public void setName(String name);
    
    public Set<CarType> getAllTypes();
    
    public void setAllTypes(Set<CarType> types);
    
    public CarType getType(String carTypeName);
    
    public boolean isAvailable(String carTypeName, Date start, Date end);
    
    public Set<CarType> getAvailableCarTypes(Date start, Date end);
    
    public ICar getCar(int uid);
    
    public Set<? extends ICar> getCars(CarType type);
    
    public Set<? extends ICar> getCars(String type);
    
    public List<? extends ICar> getCars();
    
    public Quote createQuote(ReservationConstraints constraints, String guest)
            throws ReservationException;
    
    public Reservation confirmQuote(Quote quote) throws ReservationException;
    
    public void cancelReservation(Reservation res);
    
    public Set<Reservation> getReservationsBy(String renter);
    
    public int getNumReservationsForType(String type);
}
