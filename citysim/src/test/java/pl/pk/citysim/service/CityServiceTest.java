package pl.pk.citysim.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import pl.pk.citysim.model.Building;
import pl.pk.citysim.model.City;
import pl.pk.citysim.model.ResidentialBuilding;
import pl.pk.citysim.model.CommercialBuilding;
import pl.pk.citysim.model.IndustrialBuilding;
import pl.pk.citysim.model.SchoolBuilding;
import pl.pk.citysim.model.HospitalBuilding;
import pl.pk.citysim.model.ParkBuilding;
import pl.pk.citysim.model.WaterPlantBuilding;
import pl.pk.citysim.model.PowerPlantBuilding;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for the CityService class.
 */
public class CityServiceTest {

    private CityService cityService;

    @BeforeEach
    void setUp() {
        cityService = new CityService();
    }

    @Test
    void testSetTaxRate_NormalValues() {
        // Test setting tax rate to 15%
        cityService.setTaxRate(0.15);
        assertEquals(0.15, cityService.getCity().getTaxRate(), 0.001);

        // Test setting tax rate to 25%
        cityService.setTaxRate(0.25);
        assertEquals(0.25, cityService.getCity().getTaxRate(), 0.001);
    }

    @Test
    void testSetTaxRate_EdgeCases() {
        // Test setting tax rate to 0%
        cityService.setTaxRate(0.0);
        assertEquals(0.0, cityService.getCity().getTaxRate(), 0.001);

        // Test setting tax rate to maximum allowed (40%)
        cityService.setTaxRate(0.4);
        assertEquals(0.4, cityService.getCity().getTaxRate(), 0.001);

        // Test setting tax rate to negative value (should be clamped to 0%)
        cityService.setTaxRate(-0.1);
        assertEquals(0.0, cityService.getCity().getTaxRate(), 0.001);

        // Test setting tax rate to value > 40% (should be clamped to 40%)
        cityService.setTaxRate(0.5);
        assertEquals(0.4, cityService.getCity().getTaxRate(), 0.001);
    }

    @Test
    void testCityTick_TaxIncome() {
        // Get initial budget
        City city = cityService.getCity();
        int initialBudget = city.getBudget();
        int families = city.getFamilies();

        // Add income-generating buildings to ensure income is generated
        cityService.buildBuilding(CommercialBuilding.class);
        cityService.buildBuilding(IndustrialBuilding.class);

        // Simulate one day
        cityService.cityTick();

        // Check that income is being generated
        assertTrue(city.getDailyIncome() > 0,
                "Daily income should be positive after adding commercial and industrial buildings");

        // The budget might not increase due to expenses, but we should verify that income is being generated
        System.out.println("[DEBUG_LOG] Initial budget: " + initialBudget);
        System.out.println("[DEBUG_LOG] Final budget: " + city.getBudget());
        System.out.println("[DEBUG_LOG] Daily income: " + city.getDailyIncome());
        System.out.println("[DEBUG_LOG] Daily expenses: " + city.getDailyExpenses());

        // Reset city to test with different tax rate
        cityService = new CityService();
        city = cityService.getCity();
        initialBudget = city.getBudget();

        // Set both tax rates to 0% and verify no income
        cityService.setTaxRate(0.0);
        cityService.setVatRate(0.0);
        cityService.cityTick();

        // Budget should decrease due to city services expenses
        assertTrue(city.getBudget() < initialBudget,
                "With 0% tax rates, budget should decrease due to expenses");
    }

    @Test
    void testSetVatRate() {
        // Test setting VAT rate to normal values
        cityService.setVatRate(0.05);
        assertEquals(0.05, cityService.getCity().getVatRate(), 0.001);

        cityService.setVatRate(0.2);
        assertEquals(0.2, cityService.getCity().getVatRate(), 0.001);

        // Test edge cases
        cityService.setVatRate(0.0);
        assertEquals(0.0, cityService.getCity().getVatRate(), 0.001);

        cityService.setVatRate(0.25);
        assertEquals(0.25, cityService.getCity().getVatRate(), 0.001);

        // Test invalid values (should be clamped)
        cityService.setVatRate(-0.1);
        assertEquals(0.0, cityService.getCity().getVatRate(), 0.001);

        cityService.setVatRate(0.3);
        assertEquals(0.25, cityService.getCity().getVatRate(), 0.001);
    }

    @Test
    void testBuildBuilding() {
        City city = cityService.getCity();
        int initialBudget = city.getBudget();

        // Get initial building count
        int initialBuildingCount = city.getBuildings().size();

        // Test building a residential building
        boolean success = cityService.buildBuilding(ResidentialBuilding.class);
        assertTrue(success, "Should be able to build with sufficient budget");

        // Verify building was added
        List<Building> buildings = city.getBuildings();
        assertEquals(initialBuildingCount + 1, buildings.size(), "Should have one more building than before");

        // Find the newly added residential building
        Building newBuilding = null;
        for (Building building : buildings) {
            if (building instanceof ResidentialBuilding && building.getId() > initialBuildingCount) {
                newBuilding = building;
                break;
            }
        }

        assertNotNull(newBuilding, "Should find the newly added residential building");
        assertTrue(newBuilding instanceof ResidentialBuilding, "Should be a residential building");

        // Verify budget was reduced
        assertTrue(city.getBudget() < initialBudget, "Budget should decrease after building");

        // Test building with insufficient funds
        // First, set budget to a very low value
        // Set a limit to avoid infinite loop
        int maxAttempts = 10;
        int attempts = 0;

        // Manually set budget to a very low value
        while (city.getBudget() > 50 && attempts < maxAttempts) {
            // Build expensive buildings until budget is nearly depleted
            cityService.buildBuilding(HospitalBuilding.class);
            attempts++;
        }

        // Force budget to be low
        if (city.getBudget() > 50) {
            // This is a hack for testing - we're directly accessing a private field
            // In a real application, we would add a method to set the budget for testing
            try {
                java.lang.reflect.Field budgetField = City.class.getDeclaredField("budget");
                budgetField.setAccessible(true);
                budgetField.set(city, 10); // Set budget to 10
            } catch (Exception e) {
                // If reflection fails, just log it and continue
                System.out.println("Failed to set budget: " + e.getMessage());
            }
        }

        // Try to build another building
        success = cityService.buildBuilding(ResidentialBuilding.class);
        assertFalse(success, "Should not be able to build with insufficient budget");
    }

    @Test
    void testEventLog() {
        City city = cityService.getCity();

        // Initially should have at least one log entry (city founded)
        List<String> eventLog = city.getEventLog();
        assertFalse(eventLog.isEmpty(), "Event log should not be empty");

        // Add a building and check for log entry
        cityService.buildBuilding(ResidentialBuilding.class);

        // Simulate a day to generate more log entries
        cityService.cityTick();

        // Get recent events
        List<String> recentEvents = city.getRecentEvents();
        assertTrue(recentEvents.size() > 0, "Should have recent events");
    }

    @Test
    void testGetCityStats() {
        // Build some buildings
        cityService.buildBuilding(ResidentialBuilding.class);
        cityService.buildBuilding(CommercialBuilding.class);

        // Get stats
        String stats = cityService.getCityStats();

        // Verify stats contains key information
        assertTrue(stats.contains("CITY STATS"), "Stats should include city stats header");
        assertTrue(stats.contains("Population:"), "Stats should include population");
        assertTrue(stats.contains("Budget:"), "Stats should include budget");
        assertTrue(stats.contains("TAXES"), "Stats should include taxes section");
        assertTrue(stats.contains("BUILDINGS"), "Stats should include buildings section");
        assertTrue(stats.contains("CAPACITIES"), "Stats should include capacities section");
        assertTrue(stats.contains("RECENT EVENTS"), "Stats should include events section");
    }
}
