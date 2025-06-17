//package pl.pk.citysim.model;
//
//import org.junit.jupiter.api.Test;
//import static org.junit.jupiter.api.Assertions.*;
//
//import java.io.IOException;
//
///**
// * Test to verify that the fix for loading save files with "familiesCount" field works.
// */
//public class LoadSaveFileTest {
//
//    @Test
//    void testLoadSaveFile() {
//        try {
//            // Load the save file that was causing the issue
//            GameState gameState = GameState.loadFromFile("autosave_2025-06-17_22-19-59");
//
//            // If we get here without an exception, the fix worked
//            assertNotNull(gameState);
//            assertNotNull(gameState.getCity());
//
//            // Verify some properties of the loaded game state
//            assertEquals(2, gameState.getCity().getDay());
//            assertEquals(13, gameState.getCity().getFamilies());
//            assertEquals(841, gameState.getCity().getBudget());
//            assertEquals(0.1, gameState.getCity().getTaxRate());
//            assertEquals(0.05, gameState.getCity().getVatRate());
//
//            System.out.println("Successfully loaded save file with familiesCount field!");
//        } catch (IOException e) {
//            fail("Failed to load save file: " + e.getMessage());
//        }
//    }
//}