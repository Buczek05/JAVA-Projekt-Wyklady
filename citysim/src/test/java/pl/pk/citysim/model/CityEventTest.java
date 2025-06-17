//package pl.pk.citysim.model;
//
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//import java.util.List;
//import java.util.Random;
//import java.lang.reflect.Method;
//
//import static org.junit.jupiter.api.Assertions.*;
//
///**
// * Tests for the event handling in the City class.
// */
//public class CityEventTest {
//
//    private City city;
//
//    @BeforeEach
//    void setUp() {
//        // Initialize city with some families and budget
//        city = new City(10, 1000);
//    }
//
//    @Test
//    void testFireEvent() throws Exception {
//        // Get the private method using reflection
//        Method fireMethod = City.class.getDeclaredMethod("handleFireEvent", Random.class);
//        fireMethod.setAccessible(true);
//
//        // Add a building to the city
//        city.addBuilding(BuildingType.RESIDENTIAL);
//
//        // Get initial budget and satisfaction
//        int initialBudget = city.getBudget();
//        int initialSatisfaction = city.getSatisfaction();
//
//        // Invoke the fire event
//        fireMethod.invoke(city, new Random());
//
//        // Check effects
//        assertTrue(city.getBudget() < initialBudget, "Budget should decrease after fire event");
//        assertTrue(city.getSatisfaction() <= initialSatisfaction, "Satisfaction should not increase after fire event");
//
//        // Check event log
//        List<String> events = city.getRecentEvents(1);
//        assertFalse(events.isEmpty(), "Event log should not be empty");
//        assertTrue(events.get(0).contains("FIRE"), "Event log should contain FIRE event");
//    }
//
//    @Test
//    void testEpidemicEvent() throws Exception {
//        // Get the private method using reflection
//        Method epidemicMethod = City.class.getDeclaredMethod("handleEpidemicEvent", Random.class);
//        epidemicMethod.setAccessible(true);
//
//        // Get initial budget and satisfaction
//        int initialBudget = city.getBudget();
//        int initialSatisfaction = city.getSatisfaction();
//
//        // Invoke the epidemic event
//        epidemicMethod.invoke(city, new Random());
//
//        // Check effects
//        assertTrue(city.getBudget() < initialBudget, "Budget should decrease after epidemic event");
//        assertTrue(city.getSatisfaction() <= initialSatisfaction, "Satisfaction should not increase after epidemic event without hospitals");
//
//        // Check event log
//        List<String> events = city.getRecentEvents(1);
//        assertFalse(events.isEmpty(), "Event log should not be empty");
//        assertTrue(events.get(0).contains("EPIDEMIC"), "Event log should contain EPIDEMIC event");
//    }
//
//    @Test
//    void testEconomicCrisisEvent() throws Exception {
//        // Get the private method using reflection
//        Method crisisMethod = City.class.getDeclaredMethod("handleEconomicCrisisEvent", Random.class);
//        crisisMethod.setAccessible(true);
//
//        // Get initial budget and satisfaction
//        int initialBudget = city.getBudget();
//        int initialSatisfaction = city.getSatisfaction();
//
//        // Invoke the economic crisis event
//        crisisMethod.invoke(city, new Random());
//
//        // Check effects
//        assertTrue(city.getBudget() < initialBudget, "Budget should decrease after economic crisis event");
//        assertTrue(city.getSatisfaction() < initialSatisfaction, "Satisfaction should decrease after economic crisis event");
//
//        // Check event log
//        List<String> events = city.getRecentEvents(1);
//        assertFalse(events.isEmpty(), "Event log should not be empty");
//        assertTrue(events.get(0).contains("ECONOMIC CRISIS"), "Event log should contain ECONOMIC CRISIS event");
//    }
//
//    @Test
//    void testGrantEvent() throws Exception {
//        // Get the private method using reflection
//        Method grantMethod = City.class.getDeclaredMethod("handleGrantEvent", Random.class);
//        grantMethod.setAccessible(true);
//
//        // Get initial budget and satisfaction
//        int initialBudget = city.getBudget();
//        int initialSatisfaction = city.getSatisfaction();
//
//        // Invoke the grant event
//        grantMethod.invoke(city, new Random());
//
//        // Check effects
//        assertTrue(city.getBudget() > initialBudget, "Budget should increase after grant event");
//        assertTrue(city.getSatisfaction() > initialSatisfaction, "Satisfaction should increase after grant event");
//
//        // Check event log
//        List<String> events = city.getRecentEvents(1);
//        assertFalse(events.isEmpty(), "Event log should not be empty");
//        assertTrue(events.get(0).contains("GRANT"), "Event log should contain GRANT event");
//    }
//
//    @Test
//    void testFireEventWithNoBuildings() throws Exception {
//        // Get the private method using reflection
//        Method fireMethod = City.class.getDeclaredMethod("handleFireEvent", Random.class);
//        fireMethod.setAccessible(true);
//
//        // Get initial budget and satisfaction
//        int initialBudget = city.getBudget();
//        int initialSatisfaction = city.getSatisfaction();
//
//        // Invoke the fire event (should do nothing since there are no buildings)
//        fireMethod.invoke(city, new Random());
//
//        // Check effects (should be unchanged)
//        assertEquals(initialBudget, city.getBudget(), "Budget should not change if there are no buildings");
//        assertEquals(initialSatisfaction, city.getSatisfaction(), "Satisfaction should not change if there are no buildings");
//
//        // Check event log (should not have a new entry)
//        List<String> events = city.getRecentEvents(1);
//        assertFalse(events.isEmpty(), "Event log should not be empty");
//        assertFalse(events.get(0).contains("FIRE"), "Event log should not contain FIRE event");
//    }
//
//    @Test
//    void testEpidemicEventWithNoFamilies() throws Exception {
//        // Create a city with no families
//        City emptyCity = new City(0, 1000);
//
//        // Get the private method using reflection
//        Method epidemicMethod = City.class.getDeclaredMethod("handleEpidemicEvent", Random.class);
//        epidemicMethod.setAccessible(true);
//
//        // Get initial budget and satisfaction
//        int initialBudget = emptyCity.getBudget();
//        int initialSatisfaction = emptyCity.getSatisfaction();
//
//        // Invoke the epidemic event (should do nothing since there are no families)
//        epidemicMethod.invoke(emptyCity, new Random());
//
//        // Check effects (should be unchanged)
//        assertEquals(initialBudget, emptyCity.getBudget(), "Budget should not change if there are no families");
//        assertEquals(initialSatisfaction, emptyCity.getSatisfaction(), "Satisfaction should not change if there are no families");
//    }
//}