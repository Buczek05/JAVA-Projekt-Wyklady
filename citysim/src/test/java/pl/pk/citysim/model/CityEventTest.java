package pl.pk.citysim.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.util.List;
import java.util.Random;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import pl.pk.citysim.model.ResidentialBuilding;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for the event handling in the City class.
 */
public class CityEventTest {

    private City city;

    @BeforeEach
    void setUp() {
        // Initialize city with some families and budget
        city = new City(10, 1000);
    }

    @Test
    void testFireEvent() throws Exception {
        // Get the private method using reflection
        Method fireMethod = City.class.getDeclaredMethod("handleFireEvent", Random.class);
        fireMethod.setAccessible(true);

        // Add a building to the city
        city.addBuilding(ResidentialBuilding.class);

        // Get initial budget and satisfaction
        int initialBudget = city.getBudget();
        int initialSatisfaction = city.getSatisfaction();

        // Invoke the fire event
        fireMethod.invoke(city, new Random());

        // Check effects
        assertTrue(city.getBudget() < initialBudget, "Budget should decrease after fire event");
        assertTrue(city.getSatisfaction() <= initialSatisfaction, "Satisfaction should not increase after fire event");

        // Check event log
        List<String> events = city.getRecentEvents();
        assertFalse(events.isEmpty(), "Event log should not be empty");
        // Check the most recent event (last in the list)
        assertTrue(events.get(events.size() - 1).contains("FIRE"), "Event log should contain FIRE event");
    }

    @Test
    void testEpidemicEvent() throws Exception {
        // Get the private method using reflection
        Method epidemicMethod = City.class.getDeclaredMethod("handleEpidemicEvent", Random.class);
        epidemicMethod.setAccessible(true);

        // Get initial budget and satisfaction
        int initialBudget = city.getBudget();
        int initialSatisfaction = city.getSatisfaction();

        // Invoke the epidemic event
        epidemicMethod.invoke(city, new Random());

        // Check effects
        assertTrue(city.getBudget() < initialBudget, "Budget should decrease after epidemic event");
        assertTrue(city.getSatisfaction() <= initialSatisfaction, "Satisfaction should not increase after epidemic event without hospitals");

        // Check event log
        List<String> events = city.getRecentEvents();
        assertFalse(events.isEmpty(), "Event log should not be empty");
        // Check the most recent event (last in the list)
        assertTrue(events.get(events.size() - 1).contains("EPIDEMIC"), "Event log should contain EPIDEMIC event");
    }

    @Test
    void testEconomicCrisisEvent() throws Exception {
        // Get the private method using reflection
        Method crisisMethod = City.class.getDeclaredMethod("handleEconomicCrisisEvent", Random.class);
        crisisMethod.setAccessible(true);

        // Get initial budget and satisfaction
        int initialBudget = city.getBudget();
        int initialSatisfaction = city.getSatisfaction();

        // Invoke the economic crisis event
        crisisMethod.invoke(city, new Random());

        // Check effects
        assertTrue(city.getBudget() < initialBudget, "Budget should decrease after economic crisis event");
        assertTrue(city.getSatisfaction() < initialSatisfaction, "Satisfaction should decrease after economic crisis event");

        // Check event log
        List<String> events = city.getRecentEvents();
        assertFalse(events.isEmpty(), "Event log should not be empty");
        // Check the most recent event (last in the list)
        assertTrue(events.get(events.size() - 1).contains("ECONOMIC CRISIS"), "Event log should contain ECONOMIC CRISIS event");
    }

    @Test
    void testGrantEvent() throws Exception {
        // Get the private method using reflection
        Method grantMethod = City.class.getDeclaredMethod("handleGrantEvent", Random.class);
        grantMethod.setAccessible(true);

        // Get initial budget and satisfaction
        int initialBudget = city.getBudget();
        int initialSatisfaction = city.getSatisfaction();

        // Invoke the grant event
        grantMethod.invoke(city, new Random());

        // Check effects
        assertTrue(city.getBudget() > initialBudget, "Budget should increase after grant event");
        assertTrue(city.getSatisfaction() > initialSatisfaction, "Satisfaction should increase after grant event");

        // Check event log
        List<String> events = city.getRecentEvents();
        assertFalse(events.isEmpty(), "Event log should not be empty");
        // Check the most recent event (last in the list)
        assertTrue(events.get(events.size() - 1).contains("GRANT"), "Event log should contain GRANT event");
    }

    @Test
    void testFireEventWithNoBuildings() throws Exception {
        // Create a special city for this test with no buildings
        // We'll use reflection to clear the buildings list after creation
        City specialCity = new City(10, 1000);

        // Clear the buildings list using reflection
        java.lang.reflect.Field buildingsField = City.class.getDeclaredField("buildings");
        buildingsField.setAccessible(true);
        List<Building> buildings = new ArrayList<>();
        buildingsField.set(specialCity, buildings);

        // Also clear the building counts
        java.lang.reflect.Field buildingCountsField = City.class.getDeclaredField("buildingCounts");
        buildingCountsField.setAccessible(true);
        Map<String, Integer> buildingCounts = new HashMap<>();
        buildingCountsField.set(specialCity, buildingCounts);

        // Get the private method using reflection
        Method fireMethod = City.class.getDeclaredMethod("handleFireEvent", Random.class);
        fireMethod.setAccessible(true);

        // Get initial budget and satisfaction
        int initialBudget = specialCity.getBudget();
        int initialSatisfaction = specialCity.getSatisfaction();

        // Invoke the fire event (should do nothing since there are no buildings)
        fireMethod.invoke(specialCity, new Random());

        // Check effects (should be unchanged)
        assertEquals(initialBudget, specialCity.getBudget(), "Budget should not change if there are no buildings");
        assertEquals(initialSatisfaction, specialCity.getSatisfaction(), "Satisfaction should not change if there are no buildings");

        // Add an event to the log so we can check it
        specialCity.getEventLog().add("Test event");

        // Check event log (should not contain FIRE)
        List<String> events = specialCity.getRecentEvents();
        assertFalse(events.isEmpty(), "Event log should not be empty");
        // Check the most recent event (last in the list)
        assertFalse(events.get(events.size() - 1).contains("FIRE"), "Event log should not contain FIRE event");
    }

    @Test
    void testEpidemicEventWithNoFamilies() throws Exception {
        // Create a city with no families
        City emptyCity = new City(0, 1000);

        // Get the private method using reflection
        Method epidemicMethod = City.class.getDeclaredMethod("handleEpidemicEvent", Random.class);
        epidemicMethod.setAccessible(true);

        // Get initial budget and satisfaction
        int initialBudget = emptyCity.getBudget();
        int initialSatisfaction = emptyCity.getSatisfaction();

        // Invoke the epidemic event (should do nothing since there are no families)
        epidemicMethod.invoke(emptyCity, new Random());

        // Check effects (should be unchanged)
        assertEquals(initialBudget, emptyCity.getBudget(), "Budget should not change if there are no families");
        assertEquals(initialSatisfaction, emptyCity.getSatisfaction(), "Satisfaction should not change if there are no families");
    }
}
