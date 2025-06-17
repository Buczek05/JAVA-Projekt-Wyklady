package pl.pk.citysim.model;

import java.io.IOException;

/**
 * Simple test program to verify that the fix for loading save files with "familiesCount" field works.
 */
public class TestLoadSaveFile {
    public static void main(String[] args) {
        try {
            System.out.println("Attempting to load save file...");
            
            // Load the save file that was causing the issue
            GameState gameState = GameState.loadFromFile("autosave_2025-06-17_22-19-59");
            
            // If we get here without an exception, the fix worked
            System.out.println("SUCCESS: Save file loaded successfully!");
            System.out.println("Game state details:");
            System.out.println("- Day: " + gameState.getCity().getDay());
            System.out.println("- Families: " + gameState.getCity().getFamilies());
            System.out.println("- Budget: " + gameState.getCity().getBudget());
            System.out.println("- Tax Rate: " + gameState.getCity().getTaxRate());
            System.out.println("- VAT Rate: " + gameState.getCity().getVatRate());
            
        } catch (IOException e) {
            System.err.println("FAILED: Could not load save file: " + e.getMessage());
            e.printStackTrace();
        }
    }
}