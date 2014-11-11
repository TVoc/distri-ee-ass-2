package session;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import javax.ejb.Stateful;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TemporalType;
import javax.persistence.TypedQuery;
import rental.CarRentalCompany;
import rental.CarType;
import rental.CompanyNotFoundException;
import rental.Quote;
import rental.RentalStore;
import rental.Reservation;
import rental.ReservationConstraints;
import rental.ReservationException;

@TransactionAttribute(TransactionAttributeType.REQUIRED)
@Stateful
public class CarRentalSession implements CarRentalSessionRemote {

    @PersistenceContext(unitName="CarRental-ejbPU")
    private EntityManager em;
    
    private String renter;
    private List<Quote> quotes = new LinkedList<Quote>();

    @Override
    public Set<String> getAllRentalCompanies() {
        String queryString = "SELECT c.name FROM CarRentalCompany c";
        TypedQuery<String> query = em.createQuery(queryString, String.class);
        return new HashSet<String>(query.getResultList());
    }
    
    @Override
    public List<CarType> getAvailableCarTypes(Date start, Date end) {
        String companyQString = "SELECT c FROM CarRentalCompany c";
        TypedQuery<CarRentalCompany> cQuery = em.createQuery(companyQString, CarRentalCompany.class);
        List<CarRentalCompany> companies = cQuery.getResultList();
        
        List<CarType> toReturn = new ArrayList<CarType>();
        
        for (CarRentalCompany company : companies) {
            String queryString = "SELECT t FROM CarType t WHERE EXISTS (SELECT comp FROM CarRentalCompany comp WHERE comp = :company AND t MEMBER OF comp.carTypes AND EXISTS"
                    + " (SELECT c FROM Car c WHERE c MEMBER OF comp.cars AND c.type = t AND NOT EXISTS (SELECT r FROM Reservation r WHERE r.carId = c.id AND (:start < r.endDate OR :end > r.startDate))))";
            TypedQuery<CarType> query = em.createQuery(queryString, CarType.class);
            query.setParameter("start", start, TemporalType.DATE);
            query.setParameter("end", end, TemporalType.DATE);
            query.setParameter("company", company);
            toReturn.addAll(query.getResultList());
        }
        
        return toReturn;
        /*String queryString = "SELECT t FROM CarType t WHERE NOT EXISTS"
                + " (SELECT r FROM Reservation r WHERE r.carType = t.name AND NOT"
                + " (:end < r.startDate OR :start > r.endDate))";
        TypedQuery<CarType> query = em.createQuery(queryString, CarType.class);
        query.setParameter("start", start, TemporalType.DATE);
        query.setParameter("end", end, TemporalType.DATE);
        return query.getResultList();*/
    }
    
    @Override
    public String getCheapestCarType(Date start, Date end) {
        List<CarType> available = this.getAvailableCarTypes(start, end);
        
        if (available.isEmpty()) {
            return "";
        }
        
        for(CarType type : available) {
            System.out.println("Candidate for cheapest: " + type);
        }
        
        CarType cheapest = null;
        
        for (CarType type : available) {
            if (cheapest == null || (type.getRentalPricePerDay() < cheapest.getRentalPricePerDay())) {
                cheapest = type;
            }
        }
        
        return cheapest.getName();
    }

    @Override
    public Quote createQuote(String company, ReservationConstraints constraints) throws ReservationException {
        try {
            CarRentalCompany companyEn = em.find(CarRentalCompany.class, company);
            if (company == null) {
                throw new CompanyNotFoundException("Company with name " + company + " not found");
            }
            Quote out = companyEn.createQuote(constraints, renter);
            quotes.add(out);
            return out;
        } catch(Exception e) {
            throw new ReservationException(e);
        }
    }

    @Override
    public List<Quote> getCurrentQuotes() {
        return quotes;
    }

    @Override
    public List<Reservation> confirmQuotes() throws ReservationException {
        List<Reservation> done = new ArrayList<Reservation>();
        for (Quote quote : quotes) {
            CarRentalCompany company = em.find(CarRentalCompany.class, quote.getRentalCompany());
            Reservation res = company.confirmQuote(quote);
            done.add(res);
            em.persist(res);
        }
        return done;
        /*
        List<Reservation> done = new LinkedList<Reservation>();
        try {
            for (Quote quote : quotes) {
                done.add(RentalStore.getRental(quote.getRentalCompany()).confirmQuote(quote));
            }
        } catch (Exception e) {
            for(Reservation r:done)
                RentalStore.getRental(r.getRentalCompany()).cancelReservation(r);
            throw new ReservationException(e);
        }
        return done;
                */
    }

    @Override
    public void setRenterName(String name) {
        if (renter != null) {
            throw new IllegalStateException("name already set");
        }
        renter = name;
    }
}