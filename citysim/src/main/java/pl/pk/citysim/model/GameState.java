package pl.pk.citysim.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.module.SimpleModule;
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
    private static final ObjectMapper mapper = createObjectMapper();

    // Default saves directory path, can be overridden in tests
    private static String SAVES_DIR = "saves";

    /**
     * Creates and configures an ObjectMapper for JSON serialization/deserialization.
     * 
     * @return A configured ObjectMapper
     */
    private static ObjectMapper createObjectMapper() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
        objectMapper.configure(com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

        // Register a custom deserializer for the Building class
        SimpleModule module = new SimpleModule();
        module.addDeserializer(Building.class, new BuildingDeserializer());
        objectMapper.registerModule(module);

        return objectMapper;
    }

    /**
     * Custom deserializer for Building class that can determine the concrete subclass
     * based on the building's name.
     */
    private static class BuildingDeserializer extends JsonDeserializer<Building> {
        @Override
        public Building deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
            JsonNode node = p.getCodec().readTree(p);

            // Get the building ID and name
            int id = node.has("id") ? node.get("id").asInt() : 0;
            String name = node.has("name") ? node.get("name").asText() : "";

            // Create the appropriate building type based on the name
            Building building = null;
            if (name.equals("Residential")) {
                building = new ResidentialBuilding(id);
            } else if (name.equals("Commercial")) {
                building = new CommercialBuilding(id);
            } else if (name.equals("Industrial")) {
                building = new IndustrialBuilding(id);
            } else if (name.equals("School")) {
                building = new SchoolBuilding(id);
            } else if (name.equals("Hospital")) {
                building = new HospitalBuilding(id);
            } else if (name.equals("Park")) {
                building = new ParkBuilding(id);
            } else if (name.equals("Water Plant")) {
                building = new WaterPlantBuilding(id);
            } else if (name.equals("Power Plant")) {
                building = new PowerPlantBuilding(id);
            } else {
                throw new IOException("Unknown building type: " + name);
            }

            // Set the occupancy if available
            if (node.has("occupancy")) {
                building.setOccupancy(node.get("occupancy").asInt());
            }

            return building;
        }
    }

    private final City city;
    private final LocalDateTime savedAt;
    private final LocalDateTime gameStartedAt;

    /**
     * Creates a new game state with the given city.
     *
     * @param city The city to save
     */
    public GameState(City city) {
        this.city = city;
        this.savedAt = LocalDateTime.now();
        this.gameStartedAt = LocalDateTime.now();
    }

    /**
     * Creates a new game state with the given city and saved time.
     * This constructor is used by Jackson for deserialization.
     *
     * @param city The city to save
     * @param savedAt The time when the game was saved
     * @param gameStartedAt The time when the game was started
     */
    @JsonCreator
    public GameState(
            @JsonProperty("city") City city,
            @JsonProperty("savedAt") LocalDateTime savedAt,
            @JsonProperty("gameStartedAt") LocalDateTime gameStartedAt) {
        this.city = city;
        this.savedAt = savedAt;
        this.gameStartedAt = gameStartedAt != null ? gameStartedAt : savedAt; // Fallback for backward compatibility
    }

    public City getCity() {
        return city;
    }

    public LocalDateTime getSavedAt() {
        return savedAt;
    }

    /**
     * Gets the time when this game was started.
     *
     * @return The game start time
     */
    public LocalDateTime getGameStartedAt() {
        return gameStartedAt;
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

    /**
     * Gets a formatted string representation of the game start time.
     *
     * @return The formatted game start time
     */
    public String getFormattedGameStartTime() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        return gameStartedAt.format(formatter);
    }

    /**
     * Gets a formatted string representation of the game start time for use in filenames.
     *
     * @return The formatted game start time for filenames
     */
    public String getFormattedGameStartTimeForFilename() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss");
        return gameStartedAt.format(formatter);
    }

    @Override
    public String toString() {
        return "GameState{" +
                "city=" + city +
                ", savedAt=" + getFormattedSavedTime() +
                ", gameStartedAt=" + getFormattedGameStartTime() +
                '}';
    }
}
