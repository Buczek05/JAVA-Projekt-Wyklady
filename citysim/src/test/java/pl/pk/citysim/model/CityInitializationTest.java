package pl.pk.citysim.model;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Test to verify that the City initialization prevents immediate family departure.
 */
public class CityInitializationTest {

    @Test
    void testInitialBuildingsPreventFamilyDeparture() {
        // Create a city with initial families and budget
        City city = new City(10, 1000);
        
        // Verify that initial buildings were created
        assertFalse(city.getBuildings().isEmpty(), "City should have initial buildings");
        
        // Check that we have one of each essential building type
        assertEquals(1, countBuildingsByType(city, BuildingType.RESIDENTIAL), "Should have one residential building");
        assertEquals(1, countBuildingsByType(city, BuildingType.SCHOOL), "Should have one school");
        assertEquals(1, countBuildingsByType(city, BuildingType.HOSPITAL), "Should have one hospital");
        assertEquals(1, countBuildingsByType(city, BuildingType.WATER_PLANT), "Should have one water plant");
        assertEquals(1, countBuildingsByType(city, BuildingType.POWER_PLANT), "Should have one power plant");
        
        // Initial number of families
        int initialFamilies = city.getFamilies();
        assertEquals(10, initialFamilies, "Should start with 10 families");
        
        // Simulate a few days to ensure families don't leave immediately
        for (int i = 0; i < 5; i++) {
            city.nextDay();
        }
        
        // Check that we still have families (they didn't all leave)
        int familiesAfterDays = city.getFamilies();
        assertTrue(familiesAfterDays > 0, "Should still have families after several days");
        System.out.println("[DEBUG_LOG] Initial families: " + initialFamilies + ", Families after 5 days: " + familiesAfterDays);
    }
    
    /**
     * Helper method to count buildings of a specific type.
     */
    private int countBuildingsByType(City city, BuildingType type) {
        return (int) city.getBuildings().stream()
                .filter(b -> b.getType() == type)
                .count();
    }
}