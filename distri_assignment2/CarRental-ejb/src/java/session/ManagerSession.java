package session;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
import rental.Car;
import rental.CarRentalCompany;
import rental.CarType;
import rental.CompanyNotFoundException;
import rental.RentalStore;
import rental.Reservation;
import rental.interfaces.ICar;

@Stateless
public class ManagerSession implements ManagerSessionRemote {
    
    @PersistenceContext(unitName="CarRental-ejbPU")
    private EntityManager em;
    
    @Override
    public Set<CarType> getCarTypes(String company) {
        try {
            return new HashSet<CarType>(RentalStore.getRental(company).getAllTypes());
        } catch (IllegalArgumentException ex) {
            Logger.getLogger(ManagerSession.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }
    }

    @Override
    public Set<Integer> getCarIds(String company, String type) {
        Set<Integer> out = new HashSet<Integer>();
        try {
            for(Car c: RentalStore.getRental(company).getCars(type)){
                out.add(c.getId());
            }
        } catch (IllegalArgumentException ex) {
            Logger.getLogger(ManagerSession.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }
        return out;
    }

    @Override
    public int getNumberOfReservations(String company, String type, int id) {
        try {
            return RentalStore.getRental(company).getCar(id).getReservations().size();
        } catch (IllegalArgumentException ex) {
            Logger.getLogger(ManagerSession.class.getName()).log(Level.SEVERE, null, ex);
            return 0;
        }
    }

    @Override
    public int getNumberOfReservations(String company, String type) {
        Set<Reservation> out = new HashSet<Reservation>();
        try {
            for(Car c: RentalStore.getRental(company).getCars(type)){
                out.addAll(c.getReservations());
            }
        } catch (IllegalArgumentException ex) {
            Logger.getLogger(ManagerSession.class.getName()).log(Level.SEVERE, null, ex);
            return 0;
        }
        return out.size();
    }

    @Override
    public int getNumberOfReservationsBy(String renter) {
        Set<Reservation> out = new HashSet<Reservation>();
        for(CarRentalCompany crc : RentalStore.getRentals().values()) {
            out.addAll(crc.getReservationsBy(renter));
        }
        return out.size();
    }
    
    @Override
    public List<CarRentalCompany> getAllCompanies() {
        TypedQuery<CarRentalCompany> query = em.createQuery("SELECT c FROM CarRentalCompany c", CarRentalCompany.class);
        return query.getResultList();
    }
    
    @Override
    public List<CarType> getTypesFromCompany(String name) {
        CarRentalCompany company = em.find(CarRentalCompany.class, name);
        if (company == null) {
            this.throwCompanyNotFoundException(name);
        }
        return new ArrayList<CarType>(company.getAllTypes());
    }
    
    @Override
    public void addCarRentalCompany(String name, List<? extends ICar> cars) {
        CarRentalCompany company = new CarRentalCompany(name, (List<Car>) cars);
        em.persist(company);
    }
    
    @Override
    public int getNumReservationsForType(String company, String type) {
        String queryString = "SELECT COUNT(r) FROM Reservation r WHERE r.rentalCompany = :company AND r.carType = :type";
        TypedQuery<Integer> query = em.createQuery(queryString, Integer.class);
        query.setParameter("company", company);
        query.setParameter("type", type);
        return query.getFirstResult();
    }
    
    @Override
    public int getNumReservationsForClient(String clientName) {
        String queryString = "SELECT COUNT(r) FROM Reservation r WHERE r.carRenter = :clientName";
        TypedQuery<Integer> query = em.createQuery(queryString, Integer.class);
        query.setParameter("clientName", clientName);
        return query.getFirstResult();
    }
    
    @Override
    public Set<String> getBestClients() {
        String clientRetrievalString = "SELECT DISTINCT r.carRenter FROM Reservation r";
        TypedQuery<String> query = em.createQuery(clientRetrievalString, String.class);
        List<String> clients = query.getResultList();
        
        Set<String> bestClients = new HashSet<String>();
        int most = 0;
        
        for (String client : clients) {
            int reservations = this.getNumReservationsForClient(client);
            if (reservations > most) {
                bestClients.clear();
                bestClients.add(client);
                most = reservations;
            } else if (reservations == most) {
                bestClients.add(client);
            }
        }
        
        return bestClients;
    }
    
    @Override
    public CarType getMostPopularCarTypeIn(String company) {
        List<CarType> types = this.getTypesFromCompany(company);
        
        CarType toReturn = null;
        int most = 0;
        
        for (CarType type : types) {
            int numReservations = this.getNumReservationsForType(company, type.getName());
            if (numReservations > most) {
                toReturn = type;
                most = numReservations;
            }
        }
        
        return toReturn;
    }
    
    private void throwCompanyNotFoundException(String name) {
        throw new CompanyNotFoundException("Could not find company with name " + name);
    }
}