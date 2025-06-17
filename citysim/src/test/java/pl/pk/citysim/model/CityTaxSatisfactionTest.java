package pl.pk.citysim.model;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for the relationship between tax changes, satisfaction, and population movement.
 */
public class CityTaxSatisfactionTest {

    @Test
    void testTaxRateAffectsSatisfaction() {
        // Create a city with initial values
        City city = new City(50, 10000);

        // Get initial satisfaction
        int initialSatisfaction = city.getSatisfaction();

        // Increase tax rate and check that satisfaction decreases
        double initialTaxRate = city.getTaxRate();
        city.setTaxRate(initialTaxRate + 0.1); // Increase by 10%
        int afterIncreaseSatisfaction = city.getSatisfaction();
        assertTrue(afterIncreaseSatisfaction < initialSatisfaction,
                "Satisfaction should decrease after tax increase");

        // Decrease tax rate and check that satisfaction increases
        city.setTaxRate(initialTaxRate); // Back to initial rate
        int afterDecreaseSatisfaction = city.getSatisfaction();
        assertTrue(afterDecreaseSatisfaction > afterIncreaseSatisfaction,
                "Satisfaction should increase after tax decrease");
    }

    @Test
    void testVatRateAffectsSatisfaction() {
        // Create a city with initial values
        City city = new City(50, 10000);

        // Get initial satisfaction
        int initialSatisfaction = city.getSatisfaction();

        // Increase VAT rate and check that satisfaction decreases
        double initialVatRate = city.getVatRate();
        city.setVatRate(initialVatRate + 0.05); // Increase by 5%
        int afterIncreaseSatisfaction = city.getSatisfaction();
        assertTrue(afterIncreaseSatisfaction < initialSatisfaction,
                "Satisfaction should decrease after VAT increase");

        // Decrease VAT rate and check that satisfaction increases
        city.setVatRate(initialVatRate); // Back to initial rate
        int afterDecreaseSatisfaction = city.getSatisfaction();
        assertTrue(afterDecreaseSatisfaction > afterIncreaseSatisfaction,
                "Satisfaction should increase after VAT decrease");
    }
}
