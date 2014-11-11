/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package rental.interfaces;

import java.util.Date;
import java.util.Set;
import javax.persistence.Id;
import javax.persistence.MappedSuperclass;
import rental.CarType;
import rental.Reservation;

/**
 *
 * @author Thomas
 */
@MappedSuperclass
public interface ICar {
    
    public int getId();
    
    public void setId(int id);
    
    public CarType getType();
    
    public boolean isAvailable(Date start, Date end);
    
    public void addReservation(Reservation res);
    
    public void removeReservation(Reservation res);
    
    public Set<Reservation> getReservations();
    
    public void setReservations(Set<Reservation> reservations);
    
    public int getNumReservations();
}
