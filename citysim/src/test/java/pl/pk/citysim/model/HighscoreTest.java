package pl.pk.citysim.model;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for the Highscore class.
 */
public class HighscoreTest {
    
    private static final String TEST_CITY_NAME = "TestCity";
    private static final String HIGHSCORES_DIR = "saves";
    private static final String HIGHSCORES_FILE = "highscores.txt";
    private Path highscoresPath;
    private File originalFile;
    private boolean originalFileExists;
    
    @BeforeEach
    void setUp() throws Exception {
        // Save the original highscores file if it exists
        Path highscoresDir = Paths.get(HIGHSCORES_DIR);
        highscoresPath = highscoresDir.resolve(HIGHSCORES_FILE);
        originalFile = highscoresPath.toFile();
        originalFileExists = originalFile.exists();
        
        if (originalFileExists) {
            // Backup the original file
            Files.copy(highscoresPath, highscoresDir.resolve(HIGHSCORES_FILE + ".bak"));
        }
        
        // Delete the highscores file if it exists to start with a clean state
        Files.deleteIfExists(highscoresPath);
    }
    
    @AfterEach
    void tearDown() throws Exception {
        // Restore the original highscores file if it existed
        if (originalFileExists) {
            Files.deleteIfExists(highscoresPath);
            Files.move(Paths.get(HIGHSCORES_DIR).resolve(HIGHSCORES_FILE + ".bak"), highscoresPath);
        } else {
            Files.deleteIfExists(highscoresPath);
        }
    }
    
    @Test
    void testSaveHighscoreRemovesOldCityEntries() {
        // Create first highscore for TestCity
        Highscore firstHighscore = new Highscore(TEST_CITY_NAME, 1000, 10, 5000, 80, 30);
        
        // Save the first highscore
        boolean saveResult1 = Highscore.saveHighscore(firstHighscore);
        assertTrue(saveResult1, "First highscore should be saved successfully");
        
        // Load highscores and verify TestCity exists
        List<Highscore> highscoresAfterFirst = Highscore.loadHighscores();
        boolean foundFirstCity = false;
        for (Highscore h : highscoresAfterFirst) {
            if (h.getCityName().equals(TEST_CITY_NAME)) {
                foundFirstCity = true;
                assertEquals(1000, h.getScore(), "Score should match first highscore");
                break;
            }
        }
        assertTrue(foundFirstCity, "TestCity should exist in highscores after first save");
        
        // Create second highscore for the same city with different score
        Highscore secondHighscore = new Highscore(TEST_CITY_NAME, 2000, 20, 10000, 90, 60);
        
        // Save the second highscore
        boolean saveResult2 = Highscore.saveHighscore(secondHighscore);
        assertTrue(saveResult2, "Second highscore should be saved successfully");
        
        // Load highscores again
        List<Highscore> highscoresAfterSecond = Highscore.loadHighscores();
        
        // Count how many entries exist for TestCity (should be only 1)
        int testCityCount = 0;
        int lastScore = 0;
        for (Highscore h : highscoresAfterSecond) {
            if (h.getCityName().equals(TEST_CITY_NAME)) {
                testCityCount++;
                lastScore = h.getScore();
            }
        }
        
        // Verify there's only one entry for TestCity
        assertEquals(1, testCityCount, "There should be only one entry for TestCity");
        
        // Verify it's the second highscore (with score 2000)
        assertEquals(2000, lastScore, "The score should be from the second highscore");
        
        System.out.println("[DEBUG_LOG] Test completed successfully. Only one entry for TestCity exists after saving two highscores.");
    }
}