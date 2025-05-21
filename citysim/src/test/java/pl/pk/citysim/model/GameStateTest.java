package pl.pk.citysim.model;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for the GameState class.
 */
public class GameStateTest {

    @TempDir
    Path tempDir;

    @Test
    void testGameStateSerialization() throws IOException {
        // Create a test city
        City city = new City(10, 1000);
        city.setTaxRate(0.15);
        city.setVatRate(0.10);
        city.addBuilding(BuildingType.RESIDENTIAL);
        city.addBuilding(BuildingType.COMMERCIAL);
        
        // Create a game state
        GameState gameState = new GameState(city);
        
        // Create a temporary saves directory
        Path savesDir = tempDir.resolve("saves");
        Files.createDirectory(savesDir);
        
        // Save the game state to a file
        File saveFile = savesDir.resolve("test.json").toFile();
        
        // Use reflection to set the saves directory to our temp directory
        try {
            java.lang.reflect.Field savesDirField = GameState.class.getDeclaredField("SAVES_DIR");
            savesDirField.setAccessible(true);
            savesDirField.set(null, savesDir.toString());
        } catch (Exception e) {
            // If the field doesn't exist, we'll use a different approach
            System.out.println("[DEBUG_LOG] SAVES_DIR field not found, using direct file writing");
            
            // Use ObjectMapper directly
            com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
            mapper.enable(com.fasterxml.jackson.databind.SerializationFeature.INDENT_OUTPUT);
            mapper.writeValue(saveFile, gameState);
            
            // Load the game state from the file
            GameState loadedState = mapper.readValue(saveFile, GameState.class);
            
            // Verify the loaded state
            assertNotNull(loadedState);
            assertNotNull(loadedState.getCity());
            assertEquals(city.getDay(), loadedState.getCity().getDay());
            assertEquals(city.getFamilies(), loadedState.getCity().getFamilies());
            assertEquals(city.getBudget(), loadedState.getCity().getBudget());
            assertEquals(city.getTaxRate(), loadedState.getCity().getTaxRate());
            assertEquals(city.getVatRate(), loadedState.getCity().getVatRate());
            assertEquals(city.getBuildings().size(), loadedState.getCity().getBuildings().size());
            
            return;
        }
        
        // Save the game state
        gameState.saveToFile("test");
        
        // Verify the file exists
        assertTrue(Files.exists(savesDir.resolve("test.json")));
        
        // Load the game state
        GameState loadedState = GameState.loadFromFile("test");
        
        // Verify the loaded state
        assertNotNull(loadedState);
        assertNotNull(loadedState.getCity());
        assertEquals(city.getDay(), loadedState.getCity().getDay());
        assertEquals(city.getFamilies(), loadedState.getCity().getFamilies());
        assertEquals(city.getBudget(), loadedState.getCity().getBudget());
        assertEquals(city.getTaxRate(), loadedState.getCity().getTaxRate());
        assertEquals(city.getVatRate(), loadedState.getCity().getVatRate());
        assertEquals(city.getBuildings().size(), loadedState.getCity().getBuildings().size());
    }
    
    @Test
    void testGameStateConstructor() {
        // Create a test city
        City city = new City(10, 1000);
        
        // Create a game state with current time
        GameState gameState1 = new GameState(city);
        assertNotNull(gameState1.getSavedAt());
        
        // Create a game state with specific time
        LocalDateTime savedAt = LocalDateTime.now().minusDays(1);
        GameState gameState2 = new GameState(city, savedAt);
        assertEquals(savedAt, gameState2.getSavedAt());
    }
    
    @Test
    void testFormattedSavedTime() {
        // Create a test city
        City city = new City(10, 1000);
        
        // Create a game state with specific time
        LocalDateTime savedAt = LocalDateTime.of(2023, 1, 15, 14, 30, 0);
        GameState gameState = new GameState(city, savedAt);
        
        // Check formatted time
        String formattedTime = gameState.getFormattedSavedTime();
        assertTrue(formattedTime.contains("2023-01-15"));
        assertTrue(formattedTime.contains("14:30:00"));
    }
}