//package pl.pk.citysim.service;
//
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//import pl.pk.citysim.model.Building;
//import pl.pk.citysim.model.BuildingType;
//import pl.pk.citysim.model.City;
//
//import java.util.List;
//
//import static org.junit.jupiter.api.Assertions.*;
//
///**
// * Tests for the CityService class.
// */
//public class CityServiceTest {
//
//    private CityService cityService;
//
//    @BeforeEach
//    void setUp() {
//        cityService = new CityService();
//    }
//
//    @Test
//    void testSetTaxRate_NormalValues() {
//        // Test setting tax rate to 15%
//        cityService.setTaxRate(0.15);
//        assertEquals(0.15, cityService.getCity().getTaxRate(), 0.001);
//
//        // Test setting tax rate to 25%
//        cityService.setTaxRate(0.25);
//        assertEquals(0.25, cityService.getCity().getTaxRate(), 0.001);
//    }
//
//    @Test
//    void testSetTaxRate_EdgeCases() {
//        // Test setting tax rate to 0%
//        cityService.setTaxRate(0.0);
//        assertEquals(0.0, cityService.getCity().getTaxRate(), 0.001);
//
//        // Test setting tax rate to maximum allowed (40%)
//        cityService.setTaxRate(0.4);
//        assertEquals(0.4, cityService.getCity().getTaxRate(), 0.001);
//
//        // Test setting tax rate to negative value (should be clamped to 0%)
//        cityService.setTaxRate(-0.1);
//        assertEquals(0.0, cityService.getCity().getTaxRate(), 0.001);
//
//        // Test setting tax rate to value > 40% (should be clamped to 40%)
//        cityService.setTaxRate(0.5);
//        assertEquals(0.4, cityService.getCity().getTaxRate(), 0.001);
//    }
//
//    @Test
//    void testCityTick_TaxIncome() {
//        // Get initial budget
//        City city = cityService.getCity();
//        int initialBudget = city.getBudget();
//        int families = city.getFamilies();
//
//        // Simulate one day
//        cityService.cityTick();
//
//        // Check that budget increased due to tax income
//        assertTrue(city.getBudget() > initialBudget,
//                "Budget should increase after a day due to tax income");
//
//        // Reset city to test with different tax rate
//        cityService = new CityService();
//        city = cityService.getCity();
//        initialBudget = city.getBudget();
//
//        // Set both tax rates to 0% and verify no income
//        cityService.setTaxRate(0.0);
//        cityService.setVatRate(0.0);
//        cityService.cityTick();
//
//        // Budget should decrease due to city services expenses
//        assertTrue(city.getBudget() < initialBudget,
//                "With 0% tax rates, budget should decrease due to expenses");
//    }
//
//    @Test
//    void testSetVatRate() {
//        // Test setting VAT rate to normal values
//        cityService.setVatRate(0.05);
//        assertEquals(0.05, cityService.getCity().getVatRate(), 0.001);
//
//        cityService.setVatRate(0.2);
//        assertEquals(0.2, cityService.getCity().getVatRate(), 0.001);
//
//        // Test edge cases
//        cityService.setVatRate(0.0);
//        assertEquals(0.0, cityService.getCity().getVatRate(), 0.001);
//
//        cityService.setVatRate(0.25);
//        assertEquals(0.25, cityService.getCity().getVatRate(), 0.001);
//
//        // Test invalid values (should be clamped)
//        cityService.setVatRate(-0.1);
//        assertEquals(0.0, cityService.getCity().getVatRate(), 0.001);
//
//        cityService.setVatRate(0.3);
//        assertEquals(0.25, cityService.getCity().getVatRate(), 0.001);
//    }
//
//    @Test
//    void testBuildBuilding() {
//        City city = cityService.getCity();
//        int initialBudget = city.getBudget();
//
//        // Test building a residential building
//        boolean success = cityService.buildBuilding(BuildingType.RESIDENTIAL);
//        assertTrue(success, "Should be able to build with sufficient budget");
//
//        // Verify building was added
//        List<Building> buildings = city.getBuildings();
//        assertEquals(1, buildings.size(), "Should have one building");
//        assertEquals(BuildingType.RESIDENTIAL, buildings.get(0).getType(), "Should be a residential building");
//
//        // Verify budget was reduced
//        assertTrue(city.getBudget() < initialBudget, "Budget should decrease after building");
//
//        // Test building with insufficient funds
//        // First, set budget to a very low value
//        // Set a limit to avoid infinite loop
//        int maxAttempts = 10;
//        int attempts = 0;
//
//        // Manually set budget to a very low value
//        while (city.getBudget() > 50 && attempts < maxAttempts) {
//            // Build expensive buildings until budget is nearly depleted
//            cityService.buildBuilding(BuildingType.HOSPITAL);
//            attempts++;
//        }
//
//        // Force budget to be low
//        if (city.getBudget() > 50) {
//            // This is a hack for testing - we're directly accessing a private field
//            // In a real application, we would add a method to set the budget for testing
//            try {
//                java.lang.reflect.Field budgetField = City.class.getDeclaredField("budget");
//                budgetField.setAccessible(true);
//                budgetField.set(city, 10); // Set budget to 10
//            } catch (Exception e) {
//                // If reflection fails, just log it and continue
//                System.out.println("Failed to set budget: " + e.getMessage());
//            }
//        }
//
//        // Try to build another building
//        success = cityService.buildBuilding(BuildingType.RESIDENTIAL);
//        assertFalse(success, "Should not be able to build with insufficient budget");
//    }
//
//    @Test
//    void testEventLog() {
//        City city = cityService.getCity();
//
//        // Initially should have at least one log entry (city founded)
//        List<String> eventLog = city.getEventLog();
//        assertFalse(eventLog.isEmpty(), "Event log should not be empty");
//
//        // Add a building and check for log entry
//        cityService.buildBuilding(BuildingType.RESIDENTIAL);
//
//        // Simulate a day to generate more log entries
//        cityService.cityTick();
//
//        // Get recent events
//        List<String> recentEvents = city.getRecentEvents(3);
//        assertTrue(recentEvents.size() > 0, "Should have recent events");
//        assertTrue(recentEvents.size() <= 3, "Should have at most 3 recent events");
//    }
//
//    @Test
//    void testGetCityStats() {
//        // Build some buildings
//        cityService.buildBuilding(BuildingType.RESIDENTIAL);
//        cityService.buildBuilding(BuildingType.COMMERCIAL);
//
//        // Get stats
//        String stats = cityService.getCityStats();
//
//        // Verify stats contains key information
//        assertTrue(stats.contains("CITY STATS"), "Stats should include city stats header");
//        assertTrue(stats.contains("Population:"), "Stats should include population");
//        assertTrue(stats.contains("Budget:"), "Stats should include budget");
//        assertTrue(stats.contains("TAXES"), "Stats should include taxes section");
//        assertTrue(stats.contains("BUILDINGS"), "Stats should include buildings section");
//        assertTrue(stats.contains("CAPACITIES"), "Stats should include capacities section");
//        assertTrue(stats.contains("RECENT EVENTS"), "Stats should include events section");
//    }
//}
