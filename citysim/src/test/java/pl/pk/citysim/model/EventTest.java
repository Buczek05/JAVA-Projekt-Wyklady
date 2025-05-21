package pl.pk.citysim.model;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for the Event enum.
 */
public class EventTest {

    @Test
    void testEventProperties() {
        // Test FIRE event
        assertEquals("Fire", Event.FIRE.getName());
        assertFalse(Event.FIRE.isPositive());
        
        // Test EPIDEMIC event
        assertEquals("Epidemic", Event.EPIDEMIC.getName());
        assertFalse(Event.EPIDEMIC.isPositive());
        
        // Test ECONOMIC_CRISIS event
        assertEquals("Economic Crisis", Event.ECONOMIC_CRISIS.getName());
        assertFalse(Event.ECONOMIC_CRISIS.isPositive());
        
        // Test GRANT event
        assertEquals("Grant", Event.GRANT.getName());
        assertTrue(Event.GRANT.isPositive());
    }
    
    @Test
    void testEventToString() {
        assertEquals("Fire", Event.FIRE.toString());
        assertEquals("Epidemic", Event.EPIDEMIC.toString());
        assertEquals("Economic Crisis", Event.ECONOMIC_CRISIS.toString());
        assertEquals("Grant", Event.GRANT.toString());
    }
}