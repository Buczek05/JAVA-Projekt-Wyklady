//package pl.pk.citysim.model;
//
//import org.junit.jupiter.api.Test;
//import static org.junit.jupiter.api.Assertions.*;
//
///**
// * Tests for the relationship between tax changes, satisfaction, and population movement.
// */
//public class CityTaxSatisfactionTest {
//
//    @Test
//    void testTaxRateAffectsSatisfaction() {
//        // Create a city with initial values
//        City city = new City(50, 10000);
//
//        // Get initial satisfaction
//        int initialSatisfaction = city.getSatisfaction();
//
//        // Increase tax rate and check that satisfaction decreases
//        double initialTaxRate = city.getTaxRate();
//        city.setTaxRate(initialTaxRate + 0.1); // Increase by 10%
//        int afterIncreaseSatisfaction = city.getSatisfaction();
//        assertTrue(afterIncreaseSatisfaction < initialSatisfaction,
//                "Satisfaction should decrease after tax increase");
//
//        // Decrease tax rate and check that satisfaction increases
//        city.setTaxRate(initialTaxRate); // Back to initial rate
//        int afterDecreaseSatisfaction = city.getSatisfaction();
//        assertTrue(afterDecreaseSatisfaction > afterIncreaseSatisfaction,
//                "Satisfaction should increase after tax decrease");
//    }
//
//    @Test
//    void testVatRateAffectsSatisfaction() {
//        // Create a city with initial values
//        City city = new City(50, 10000);
//
//        // Get initial satisfaction
//        int initialSatisfaction = city.getSatisfaction();
//
//        // Increase VAT rate and check that satisfaction decreases
//        double initialVatRate = city.getVatRate();
//        city.setVatRate(initialVatRate + 0.05); // Increase by 5%
//        int afterIncreaseSatisfaction = city.getSatisfaction();
//        assertTrue(afterIncreaseSatisfaction < initialSatisfaction,
//                "Satisfaction should decrease after VAT increase");
//
//        // Decrease VAT rate and check that satisfaction increases
//        city.setVatRate(initialVatRate); // Back to initial rate
//        int afterDecreaseSatisfaction = city.getSatisfaction();
//        assertTrue(afterDecreaseSatisfaction > afterIncreaseSatisfaction,
//                "Satisfaction should increase after VAT decrease");
//    }
//
//    @Test
//    void testSatisfactionAffectsPopulationMovement() {
//        // Create a city with initial values
//        City city = new City(50, 10000);
//
//        // Set initial satisfaction to a high value
//        // We'll use reflection to set the satisfaction directly
//        try {
//            java.lang.reflect.Field satisfactionField = City.class.getDeclaredField("satisfaction");
//            satisfactionField.setAccessible(true);
//            satisfactionField.set(city, 90); // High satisfaction
//        } catch (Exception e) {
//            fail("Failed to set satisfaction field: " + e.getMessage());
//        }
//
//        // Run several days with high satisfaction and count new families
//        int initialFamilies = city.getFamilies();
//        int daysToRun = 30;
//        for (int i = 0; i < daysToRun; i++) {
//            city.nextDay();
//        }
//        int highSatisfactionFamilies = city.getFamilies();
//
//        // Create a new city with low satisfaction
//        City lowSatCity = new City(50, 10000);
//        try {
//            java.lang.reflect.Field satisfactionField = City.class.getDeclaredField("satisfaction");
//            satisfactionField.setAccessible(true);
//            satisfactionField.set(lowSatCity, 20); // Low satisfaction
//        } catch (Exception e) {
//            fail("Failed to set satisfaction field: " + e.getMessage());
//        }
//
//        // Run the same number of days with low satisfaction
//        for (int i = 0; i < daysToRun; i++) {
//            lowSatCity.nextDay();
//        }
//        int lowSatisfactionFamilies = lowSatCity.getFamilies();
//
//        // The high satisfaction city should have more families than the low satisfaction city
//        System.out.println("[DEBUG_LOG] High satisfaction families: " + highSatisfactionFamilies);
//        System.out.println("[DEBUG_LOG] Low satisfaction families: " + lowSatisfactionFamilies);
//        assertTrue(highSatisfactionFamilies > lowSatisfactionFamilies,
//                "City with higher satisfaction should have more families");
//    }
//}