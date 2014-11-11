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
            CarRentalCompany companyEn = em.find(CarRentalCompany.class, company);
            if (companyEn == null) {
                this.throwCompanyNotFoundException(company);
            }
            return companyEn.getAllTypes();
        } catch (IllegalArgumentException ex) {
            Logger.getLogger(ManagerSession.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }
    }

    @Override
    public Set<Integer> getCarIds(String company, String type) {
        Set<Integer> out = new HashSet<Integer>();
        try {
            CarRentalCompany companyEn = em.find(CarRentalCompany.class, company);
            if (companyEn == null) {
                this.throwCompanyNotFoundException(company);
            }
            for (Car c : companyEn.getCars()) {
                out.add(c.getId());
            }
            /*for(Car c: RentalStore.getRental(company).getCars(type)){
                out.add(c.getId());
            }*/
        } catch (IllegalArgumentException ex) {
            Logger.getLogger(ManagerSession.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }
        return out;
    }

    @Override
    public int getNumberOfReservations(String company, String type, int id) {
        try {
            String queryString = "SELECT COUNT(r) FROM Reservation r WHERE"
                    + " r.rentalCompany = :company AND r.carType = :type AND"
                    + " r.carId = :id";
            TypedQuery<Long> query = em.createQuery(queryString, Long.class);
            query.setParameter("company", company);
            query.setParameter("type", type);
            query.setParameter("id", id);
            return query.getSingleResult().intValue();
            //return RentalStore.getRental(company).getCar(id).getReservations().size();
        } catch (IllegalArgumentException ex) {
            Logger.getLogger(ManagerSession.class.getName()).log(Level.SEVERE, null, ex);
            return 0;
        }
    }

    @Override
    public int getNumberOfReservations(String company, String type) {
        try {
            String queryString = "SELECT COUNT(r) FROM Reservation r WHERE"
                    + " r.carType = :type AND r.rentalCompany = :company";
            TypedQuery<Long> query = em.createQuery(queryString, Long.class);
            query.setParameter("type", type);
            query.setParameter("company", company);
            return query.getSingleResult().intValue();
            /*for(Car c: RentalStore.getRental(company).getCars(type)){
                out.addAll(c.getReservations());
            }*/
        } catch (IllegalArgumentException ex) {
            Logger.getLogger(ManagerSession.class.getName()).log(Level.SEVERE, null, ex);
            return 0;
        }
    }

    @Override
    public int getNumberOfReservationsBy(String renter) {
        String queryString = "SELECT COUNT(r) FROM Reservation r WHERE"
                + " r.carRenter = :renter";
        TypedQuery<Long> query = em.createQuery(queryString, Long.class);
        query.setParameter("renter", renter);
        return query.getSingleResult().intValue();
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
        CarRentalCompany company = new CarRentalCompany(name, (List<Car>) cars, em);
        em.persist(company);
    }
    
    @Override
    public int getNumReservationsForType(String company, String type) {
        String queryString = "SELECT COUNT(r) FROM Reservation r WHERE r.rentalCompany = :company AND r.carType = :type";
        TypedQuery<Long> query = em.createQuery(queryString, Long.class);
        query.setParameter("company", company);
        query.setParameter("type", type);
        return query.getSingleResult().intValue();
    }
    
    @Override
    public int getNumReservationsForClient(String clientName) {
        String queryString = "SELECT COUNT(r) FROM Reservation r WHERE r.carRenter = :clientName";
        TypedQuery<Long> query = em.createQuery(queryString, Long.class);
        query.setParameter("clientName", clientName);
        return query.getSingleResult().intValue();
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