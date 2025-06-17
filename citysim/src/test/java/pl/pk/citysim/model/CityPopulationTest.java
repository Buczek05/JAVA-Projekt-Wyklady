package pl.pk.citysim.model;

import org.junit.jupiter.api.Test;
import java.lang.reflect.Field;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for the population update mechanism in the City class.
 * Specifically tests that high satisfaction levels lead to more families arriving.
 */
public class CityPopulationTest {

    @Test
    void testHighSatisfactionIncreasesPopulationGrowth() throws Exception {
        // Create a city with initial families and budget
        City city = new City(50, 10000);
        
        // Add lots of residential buildings to ensure housing isn't a constraint
        for (int i = 0; i < 10; i++) {
            city.addBuilding(BuildingType.RESIDENTIAL);
        }
        
        // Set satisfaction to a high value using reflection
        setPrivateField(city, "satisfaction", 95); // Very high satisfaction (>90%)
        
        // Get initial number of families
        int initialFamilies = city.getFamilies();
        System.out.println("[DEBUG_LOG] Initial families: " + initialFamilies);
        
        // Run one day to trigger population update
        city.nextDay();
        
        // Get number of families after update
        int familiesAfterUpdate = city.getFamilies();
        System.out.println("[DEBUG_LOG] Families after update: " + familiesAfterUpdate);
        
        // Calculate the number of new families
        int newFamilies = familiesAfterUpdate - initialFamilies;
        System.out.println("[DEBUG_LOG] New families: " + newFamilies);
        
        // With very high satisfaction (>90%), we should see more than 5 new families
        // (the original limit) if there's enough housing
        assertTrue(newFamilies > 5, "With high satisfaction (>90%), more than 5 new families should arrive");
    }
    
    @Test
    void testMediumHighSatisfactionIncreasesPopulationGrowth() throws Exception {
        // Create a city with initial families and budget
        City city = new City(50, 10000);
        
        // Add lots of residential buildings to ensure housing isn't a constraint
        for (int i = 0; i < 10; i++) {
            city.addBuilding(BuildingType.RESIDENTIAL);
        }
        
        // Set satisfaction to a medium-high value using reflection
        setPrivateField(city, "satisfaction", 87); // Medium-high satisfaction (>85% but <90%)
        
        // Get initial number of families
        int initialFamilies = city.getFamilies();
        System.out.println("[DEBUG_LOG] Initial families (medium-high test): " + initialFamilies);
        
        // Run one day to trigger population update
        city.nextDay();
        
        // Get number of families after update
        int familiesAfterUpdate = city.getFamilies();
        System.out.println("[DEBUG_LOG] Families after update (medium-high test): " + familiesAfterUpdate);
        
        // Calculate the number of new families
        int newFamilies = familiesAfterUpdate - initialFamilies;
        System.out.println("[DEBUG_LOG] New families (medium-high test): " + newFamilies);
        
        // With medium-high satisfaction (>85%), we should see more than 5 new families
        // (the original limit) if there's enough housing
        assertTrue(newFamilies > 5, "With medium-high satisfaction (>85%), more than 5 new families should arrive");
    }
    
    /**
     * Helper method to set a private field using reflection.
     */
    private void setPrivateField(Object object, String fieldName, Object value) throws Exception {
        Field field = object.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(object, value);
    }
}