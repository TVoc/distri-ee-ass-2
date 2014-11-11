package session;

import java.util.List;
import java.util.Set;
import javax.ejb.Remote;
import rental.CarType;
import rental.interfaces.ICar;
import rental.interfaces.ICarRentalCompany;

@Remote
public interface ManagerSessionRemote {
    
    public Set<CarType> getCarTypes(String company);
    
    public Set<Integer> getCarIds(String company,String type);
    
    public int getNumberOfReservations(String company, String type, int carId);
    
    public int getNumberOfReservations(String company, String type);
      
    public int getNumberOfReservationsBy(String renter);
    
    public List<? extends ICarRentalCompany> getAllCompanies();
    
    public void addCarRentalCompany(String name, List<? extends ICar> cars);
    
    public List<CarType> getTypesFromCompany(String name);
    
    public int getNumReservationsForType(String company, String carType);
    
    public int getNumReservationsForClient(String clientName);
    
    public Set<String> getBestClients();
    
    public CarType getMostPopularCarTypeIn(String company);
}