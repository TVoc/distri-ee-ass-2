/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package rental;

/**
 *
 * @author Thomas
 */
public class CompanyNotFoundException extends RuntimeException {
    
    public CompanyNotFoundException(String message) {
        super(message);
    }
    
}
