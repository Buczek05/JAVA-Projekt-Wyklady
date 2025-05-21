package pl.pk.citysim.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Represents the complete state of the game that can be saved and loaded.
 */
public class GameState {
    private static final Logger logger = LoggerFactory.getLogger(GameState.class);
    private static final ObjectMapper mapper = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .enable(SerializationFeature.INDENT_OUTPUT);

    // Default saves directory path, can be overridden in tests
    private static String SAVES_DIR = "saves";

    private final City city;
    private final LocalDateTime savedAt;

    /**
     * Creates a new game state with the given city.
     *
     * @param city The city to save
     */
    public GameState(City city) {
        this.city = city;
        this.savedAt = LocalDateTime.now();
    }

    /**
     * Creates a new game state with the given city and saved time.
     * This constructor is used by Jackson for deserialization.
     *
     * @param city The city to save
     * @param savedAt The time when the game was saved
     */
    @JsonCreator
    public GameState(
            @JsonProperty("city") City city,
            @JsonProperty("savedAt") LocalDateTime savedAt) {
        this.city = city;
        this.savedAt = savedAt;
    }

    /**
     * Gets the city from this game state.
     *
     * @return The city
     */
    public City getCity() {
        return city;
    }

    /**
     * Gets the time when this game state was saved.
     *
     * @return The saved time
     */
    public LocalDateTime getSavedAt() {
        return savedAt;
    }

    /**
     * Saves this game state to a file.
     *
     * @param filename The name of the file to save to
     * @throws IOException If an I/O error occurs
     */
    public void saveToFile(String filename) throws IOException {
        // Ensure the filename has a .json extension
        String actualFilename = filename;
        if (!filename.toLowerCase().endsWith(".json")) {
            actualFilename = filename + ".json";
        }

        // Create the saves directory if it doesn't exist
        Path savesDir = Paths.get(SAVES_DIR);
        if (!Files.exists(savesDir)) {
            Files.createDirectory(savesDir);
        }

        // Save the game state to the file
        File file = new File(savesDir.toFile(), actualFilename);
        mapper.writeValue(file, this);
        logger.info("Game saved to {}", file.getAbsolutePath());
    }

    /**
     * Loads a game state from a file.
     *
     * @param filename The name of the file to load from
     * @return The loaded game state
     * @throws IOException If an I/O error occurs
     */
    public static GameState loadFromFile(String filename) throws IOException {
        // Ensure the filename has a .json extension
        String actualFilename = filename;
        if (!filename.toLowerCase().endsWith(".json")) {
            actualFilename = filename + ".json";
        }

        // Check if the saves directory exists
        Path savesDir = Paths.get(SAVES_DIR);
        if (!Files.exists(savesDir)) {
            throw new IOException("Saves directory not found");
        }

        // Load the game state from the file
        File file = new File(savesDir.toFile(), actualFilename);
        if (!file.exists()) {
            throw new IOException("Save file not found: " + file.getAbsolutePath());
        }

        try {
            GameState gameState = mapper.readValue(file, GameState.class);
            logger.info("Game loaded from {}", file.getAbsolutePath());
            return gameState;
        } catch (IOException e) {
            logger.error("Failed to load game from {}", file.getAbsolutePath(), e);
            throw new IOException("Failed to load game: " + e.getMessage(), e);
        }
    }

    /**
     * Gets a formatted string representation of the saved time.
     *
     * @return The formatted saved time
     */
    public String getFormattedSavedTime() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        return savedAt.format(formatter);
    }

    @Override
    public String toString() {
        return "GameState{" +
                "city=" + city +
                ", savedAt=" + getFormattedSavedTime() +
                '}';
    }
}
