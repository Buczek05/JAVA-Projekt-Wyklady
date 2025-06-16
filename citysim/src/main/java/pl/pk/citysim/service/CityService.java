package pl.pk.citysim.service;

import java.util.logging.Logger;
import java.util.logging.Level;
import pl.pk.citysim.model.Building;
import pl.pk.citysim.model.Highscore;
import pl.pk.citysim.model.BuildingType;
import pl.pk.citysim.model.City;
import pl.pk.citysim.model.GameConfig;
import pl.pk.citysim.model.GameState;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Service for managing the city simulation.
 */
public class CityService {
    private static final Logger logger = Logger.getLogger(CityService.class.getName());

    private final City city;
    private final GameConfig config;

    /**
     * Creates a new city service with default configuration.
     */
    public CityService() {
        this.config = new GameConfig();
        this.city = new City(config.getEffectiveInitialFamilies(), config.getEffectiveInitialBudget());
        city.setTaxRate(config.getInitialTaxRate());
        city.setVatRate(config.getInitialVatRate());

        String modeInfo = config.isSandboxMode() ? " (SANDBOX MODE)" : "";
        logger.log(Level.INFO, String.format(
                "City initialized with %d families, $%d budget, %.1f%% income tax, and %.1f%% VAT%s", 
                config.getEffectiveInitialFamilies(), 
                config.getEffectiveInitialBudget(),
                config.getInitialTaxRate() * 100,
                config.getInitialVatRate() * 100,
                modeInfo));
    }

    /**
     * Advances the city simulation by one day.
     * 
     * @return true if the game should continue, false if game over conditions are met
     */
    public boolean cityTick() {
        city.nextDay();
        logger.log(Level.FINE, String.format(
                "Day %d: %d families, %d budget, %d satisfaction", 
                city.getDay(), city.getFamilies(), city.getBudget(), city.getSatisfaction()));

        // Check for game over conditions (only if not in sandbox mode)
        if (!config.isSandboxMode()) {
            // Bankruptcy check
            if (city.getBudget() < 0) {
                logger.log(Level.INFO, String.format("Game over: City went bankrupt with $%d debt", -city.getBudget()));
                return false;
            }

            // Population check - city is abandoned
            if (city.getFamilies() <= 0) {
                logger.info("Game over: City has been abandoned (0 families)");
                return false;
            }
        }

        return true;
    }

    /**
     * Checks if the game is in sandbox mode.
     * 
     * @return true if the game is in sandbox mode, false otherwise
     */
    public boolean isSandboxMode() {
        return config.isSandboxMode();
    }

    /**
     * Builds a new building of the specified type.
     *
     * @param buildingType The type of building to build
     * @return true if the building was built successfully, false otherwise
     */
    public boolean buildBuilding(BuildingType buildingType) {
        if (buildingType == null) {
            logger.log(Level.WARNING, "Cannot build null building type");
            return false;
        }

        int cost = buildingType.getUpkeep() * 10; // Initial cost is 10x the upkeep
        if (city.getBudget() < cost) {
            logger.log(Level.INFO, String.format(
                    "Not enough budget to build %s: need %d, have %d", 
                    buildingType, cost, city.getBudget()));
            return false;
        }

        Building building = city.addBuilding(buildingType);
        logger.log(Level.INFO, String.format("Built new %s (ID: %d)", buildingType, building.getId()));
        return true;
    }

    /**
     * Sets the income tax rate for the city.
     *
     * @param taxRate The new income tax rate (0.0 to 1.0)
     */
    public void setTaxRate(double taxRate) {
        double oldRate = city.getTaxRate();
        city.setTaxRate(taxRate);
        logger.log(Level.INFO, String.format(
                "Income tax rate changed from %.2f%% to %.2f%%", 
                oldRate * 100, 
                city.getTaxRate() * 100));
    }

    /**
     * Sets the VAT rate for the city.
     *
     * @param vatRate The new VAT rate (0.0 to 1.0)
     */
    public void setVatRate(double vatRate) {
        double oldRate = city.getVatRate();
        city.setVatRate(vatRate);
        logger.log(Level.INFO, String.format(
                "VAT rate changed from %.2f%% to %.2f%%", 
                oldRate * 100, 
                city.getVatRate() * 100));
    }

    /**
     * Gets the current city state.
     *
     * @return The current city
     */
    public City getCity() {
        return city;
    }

    /**
     * Gets the game configuration.
     *
     * @return The game configuration
     */
    public GameConfig getConfig() {
        return config;
    }

    /**
     * Generates a game summary for display at the end of the game.
     * 
     * @return A formatted string with the game summary
     */
    public String getGameSummary() {
        StringBuilder summary = new StringBuilder();

        // Create header
        summary.append(pl.pk.citysim.ui.ConsoleFormatter.createHeader("GAME SUMMARY"));

        // Basic stats
        Map<String, String> stats = new HashMap<>();
        stats.put("Days Survived", String.valueOf(city.getDay()));
        stats.put("Final Population", city.getFamilies() + " families");
        stats.put("Final Budget", "$" + city.getBudget());
        stats.put("Final Satisfaction", city.getSatisfaction() + "%");

        // Calculate score
        int score = calculateScore();
        stats.put("Final Score", String.valueOf(score));

        // Get rank
        int rank = Highscore.getRank(score);
        if (rank > 0 && rank <= 10) {
            stats.put("Highscore Rank", "#" + rank);
        } else {
            stats.put("Highscore Rank", "Not in top 10");
        }

        summary.append(pl.pk.citysim.ui.ConsoleFormatter.createKeyValueTable("FINAL STATISTICS", stats));

        // Building counts
        summary.append(pl.pk.citysim.ui.ConsoleFormatter.createHeader("BUILDINGS CONSTRUCTED"));

        Map<BuildingType, Integer> buildingCounts = city.getBuildingCounts();
        List<String[]> buildingRows = new ArrayList<>();

        for (BuildingType type : BuildingType.values()) {
            int count = buildingCounts.getOrDefault(type, 0);
            if (count > 0) {
                buildingRows.add(new String[]{
                    type.getName(),
                    String.valueOf(count)
                });
            }
        }

        if (!buildingRows.isEmpty()) {
            summary.append(pl.pk.citysim.ui.ConsoleFormatter.createTable(
                new String[]{"Building Type", "Count"}, 
                buildingRows
            ));
        } else {
            summary.append("No buildings constructed.\n");
        }

        // Notable events
        summary.append(pl.pk.citysim.ui.ConsoleFormatter.createHeader("NOTABLE EVENTS"));

        List<String> allEvents = city.getEventLog();
        List<String> notableEvents = new ArrayList<>();

        // Filter for notable events (fires, epidemics, economic crises, grants)
        for (String event : allEvents) {
            if (event.contains("FIRE") || event.contains("EPIDEMIC") || 
                event.contains("ECONOMIC CRISIS") || event.contains("GRANT") ||
                event.contains("CRITICAL")) {
                notableEvents.add(event);
            }
        }

        if (!notableEvents.isEmpty()) {
            // Show up to 10 notable events
            int eventsToShow = Math.min(10, notableEvents.size());
            for (int i = 0; i < eventsToShow; i++) {
                summary.append(pl.pk.citysim.ui.ConsoleFormatter.formatLogEntry(notableEvents.get(i))).append("\n");
            }

            if (notableEvents.size() > eventsToShow) {
                summary.append("... and ").append(notableEvents.size() - eventsToShow).append(" more notable events.\n");
            }
        } else {
            summary.append("No notable events recorded.\n");
        }

        summary.append(pl.pk.citysim.ui.ConsoleFormatter.createDivider());

        return summary.toString();
    }

    /**
     * Calculates the score for the current game.
     * 
     * @return The calculated score
     */
    public int calculateScore() {
        // Score formula: (families * 10) + (budget / 10) + (satisfaction * 5) + (days * 2)
        int families = city.getFamilies();
        int budget = city.getBudget();
        int satisfaction = city.getSatisfaction();
        int days = city.getDay();

        return (families * 10) + (budget / 10) + (satisfaction * 5) + (days * 2);
    }

    /**
     * Saves the current game score to the highscores file.
     * 
     * @param playerName The name of the player
     * @return true if the score was saved successfully, false otherwise
     */
    public boolean saveHighscore(String playerName) {
        // Don't save highscores in sandbox mode
        if (config.isSandboxMode()) {
            return false;
        }

        Highscore highscore = Highscore.calculateScore(city, playerName);
        return Highscore.saveHighscore(highscore);
    }

    /**
     * Gets a string representation of the city statistics.
     *
     * @return A string with city statistics
     */
    public String getCityStats() {
        StringBuilder stats = new StringBuilder();

        // Basic city info
        Map<String, String> cityInfo = new HashMap<>();
        cityInfo.put("Day", String.valueOf(city.getDay()));
        cityInfo.put("Population", city.getFamilies() + " families");
        cityInfo.put("Budget", "$" + city.getBudget());
        cityInfo.put("Satisfaction", city.getSatisfaction() + "%");

        stats.append(pl.pk.citysim.ui.ConsoleFormatter.createKeyValueTable("CITY STATS", cityInfo));

        // Tax information
        Map<String, String> taxInfo = new HashMap<>();
        taxInfo.put("Income Tax", String.format("%.1f%%", city.getTaxRate() * 100));
        taxInfo.put("VAT", String.format("%.1f%%", city.getVatRate() * 100));

        stats.append(pl.pk.citysim.ui.ConsoleFormatter.createKeyValueTable("TAXES", taxInfo));

        // Building counts
        stats.append(pl.pk.citysim.ui.ConsoleFormatter.createHeader("BUILDINGS"));

        Map<BuildingType, Integer> buildingCounts = city.getBuildingCounts();
        List<String[]> buildingRows = new ArrayList<>();

        for (BuildingType type : BuildingType.values()) {
            int count = buildingCounts.getOrDefault(type, 0);
            if (count > 0) {
                buildingRows.add(new String[]{
                    type.getName(),
                    String.valueOf(count),
                    type.getDescription()
                });
            }
        }

        if (!buildingRows.isEmpty()) {
            stats.append(pl.pk.citysim.ui.ConsoleFormatter.createTable(
                new String[]{"Building Type", "Count", "Description"}, 
                buildingRows
            ));
        } else {
            stats.append("No buildings constructed yet.\n");
        }

        // Calculate capacities
        int residentialCapacity = 0;
        int commercialCapacity = 0;
        int industrialCapacity = 0;
        int educationCapacity = 0;
        int healthcareCapacity = 0;
        int waterCapacity = 0;
        int powerCapacity = 0;

        for (Building building : city.getBuildings()) {
            BuildingType type = building.getType();
            if (type == BuildingType.RESIDENTIAL) {
                residentialCapacity += building.getCapacity();
            } else if (type == BuildingType.COMMERCIAL) {
                commercialCapacity += building.getCapacity();
            } else if (type == BuildingType.INDUSTRIAL) {
                industrialCapacity += building.getCapacity();
            } else if (type == BuildingType.SCHOOL) {
                educationCapacity += type.getEducationCapacity();
            } else if (type == BuildingType.HOSPITAL) {
                healthcareCapacity += type.getHealthcareCapacity();
            } else if (type == BuildingType.WATER_PLANT) {
                waterCapacity += type.getUtilityCapacity();
            } else if (type == BuildingType.POWER_PLANT) {
                powerCapacity += type.getUtilityCapacity();
            }
        }

        // Capacity information
        stats.append(pl.pk.citysim.ui.ConsoleFormatter.createHeader("CAPACITIES"));
        List<String[]> capacityRows = new ArrayList<>();

        // Housing capacity
        int families = city.getFamilies();
        int housingUsed = Math.min(families, residentialCapacity);
        double housingRatio = residentialCapacity > 0 ? (double) housingUsed / residentialCapacity : 0;
        String housingStatus = getCapacityStatus(housingRatio);

        // Jobs capacity
        int totalJobs = commercialCapacity + industrialCapacity;
        String jobsStatus = totalJobs >= families ? "" : pl.pk.citysim.ui.ConsoleFormatter.highlightWarning("[SHORTAGE]");

        // Education capacity
        double educationRatio = families > 0 ? (double) educationCapacity / families : 1.0;
        String educationStatus = getCapacityStatus(educationRatio);

        // Healthcare capacity
        double healthcareRatio = families > 0 ? (double) healthcareCapacity / families : 1.0;
        String healthcareStatus = getCapacityStatus(healthcareRatio);

        // Water capacity
        double waterRatio = families > 0 ? (double) waterCapacity / families : 1.0;
        String waterStatus = getCapacityStatus(waterRatio);

        // Power capacity
        double powerRatio = families > 0 ? (double) powerCapacity / families : 1.0;
        String powerStatus = getCapacityStatus(powerRatio);

        // Add capacity rows
        capacityRows.add(new String[]{
            "Housing", 
            String.format("%d/%d families", housingUsed, residentialCapacity),
            String.format("%.1f%%", housingRatio * 100),
            formatStatusForDisplay(housingStatus)
        });

        capacityRows.add(new String[]{
            "Jobs", 
            String.format("%d total", totalJobs),
            String.format("%d comm, %d ind", commercialCapacity, industrialCapacity),
            jobsStatus
        });

        capacityRows.add(new String[]{
            "Education", 
            String.format("%d/%d families", Math.min(educationCapacity, families), families),
            String.format("%.1f%%", educationRatio * 100),
            formatStatusForDisplay(educationStatus)
        });

        capacityRows.add(new String[]{
            "Healthcare", 
            String.format("%d/%d families", Math.min(healthcareCapacity, families), families),
            String.format("%.1f%%", healthcareRatio * 100),
            formatStatusForDisplay(healthcareStatus)
        });

        capacityRows.add(new String[]{
            "Water", 
            String.format("%d/%d families", Math.min(waterCapacity, families), families),
            String.format("%.1f%%", waterRatio * 100),
            formatStatusForDisplay(waterStatus)
        });

        capacityRows.add(new String[]{
            "Power", 
            String.format("%d/%d families", Math.min(powerCapacity, families), families),
            String.format("%.1f%%", powerRatio * 100),
            formatStatusForDisplay(powerStatus)
        });

        stats.append(pl.pk.citysim.ui.ConsoleFormatter.createTable(
            new String[]{"Service", "Capacity", "Coverage", "Status"}, 
            capacityRows
        ));

        // Recent events
        stats.append(pl.pk.citysim.ui.ConsoleFormatter.createHeader("RECENT EVENTS"));
        List<String> recentEvents = city.getRecentEvents(5);
        if (recentEvents.isEmpty()) {
            stats.append("No recent events.\n");
        } else {
            for (String event : recentEvents) {
                stats.append(pl.pk.citysim.ui.ConsoleFormatter.formatLogEntry(event)).append("\n");
            }
        }

        stats.append(pl.pk.citysim.ui.ConsoleFormatter.createDivider());
        stats.append("Type 'log all' to view the full event log or 'help' for more commands.\n");

        return stats.toString();
    }

    /**
     * Formats a status indicator for display, with appropriate highlighting.
     *
     * @param status The status indicator
     * @return The formatted status for display
     */
    private String formatStatusForDisplay(String status) {
        if (status.contains("OPTIMAL") || status.contains("GOOD")) {
            return pl.pk.citysim.ui.ConsoleFormatter.highlightSuccess(status);
        } else if (status.contains("ADEQUATE")) {
            return pl.pk.citysim.ui.ConsoleFormatter.highlightInfo(status);
        } else if (status.contains("LOW")) {
            return pl.pk.citysim.ui.ConsoleFormatter.highlightWarning(status);
        } else if (status.contains("CRITICAL") || status.contains("SEVERE") || status.contains("SHORTAGE")) {
            return pl.pk.citysim.ui.ConsoleFormatter.highlightError(status);
        }
        return status;
    }

    /**
     * Saves the current game state to a file.
     *
     * @param filename The name of the file to save to
     * @return true if the game was saved successfully, false otherwise
     */
    public boolean saveGame(String filename) {
        try {
            GameState gameState = new GameState(city);
            gameState.saveToFile(filename);
            logger.log(Level.INFO, String.format("Game saved to %s", filename));
            return true;
        } catch (IOException e) {
            logger.log(Level.SEVERE, String.format("Failed to save game to %s", filename), e);
            return false;
        }
    }

    /**
     * Loads a game state from a file and replaces the current city.
     *
     * @param filename The name of the file to load from
     * @return true if the game was loaded successfully, false otherwise
     */
    public boolean loadGame(String filename) {
        try {
            GameState gameState = GameState.loadFromFile(filename);
            replaceCity(gameState.getCity());
            logger.log(Level.INFO, String.format("Game loaded from %s", filename));
            return true;
        } catch (IOException e) {
            logger.log(Level.SEVERE, String.format("Failed to load game from %s", filename), e);
            return false;
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Failed to replace city after loading game", e);
            return false;
        }
    }

    /**
     * Gets a status indicator for a capacity ratio.
     *
     * @param ratio The capacity ratio (0.0 to 1.0)
     * @return A status indicator string
     */
    private String getCapacityStatus(double ratio) {
        if (ratio >= 1.0) {
            return "[OPTIMAL]";
        } else if (ratio >= 0.9) {
            return "[GOOD]";
        } else if (ratio >= 0.7) {
            return "[ADEQUATE]";
        } else if (ratio >= 0.5) {
            return "[LOW]";
        } else if (ratio >= 0.3) {
            return "[CRITICAL]";
        } else {
            return "[SEVERE SHORTAGE]";
        }
    }

    /**
     * Replaces the current city with a new one.
     * This is used when loading a game.
     *
     * @param newCity The new city to use
     * @throws Exception If the city could not be replaced
     */
    private void replaceCity(City newCity) throws Exception {
        try {
            // Use reflection to replace the final city field
            Field cityField = CityService.class.getDeclaredField("city");
            cityField.setAccessible(true);

            // Remove the final modifier
            Field modifiersField = Field.class.getDeclaredField("modifiers");
            modifiersField.setAccessible(true);
            modifiersField.setInt(cityField, cityField.getModifiers() & ~java.lang.reflect.Modifier.FINAL);

            // Replace the city
            cityField.set(this, newCity);

            logger.log(Level.INFO, String.format(
                    "City replaced with loaded city (day: %d, families: %d, budget: %d)",
                    newCity.getDay(), newCity.getFamilies(), newCity.getBudget()));
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Failed to replace city", e);
            throw new Exception("Failed to replace city: " + e.getMessage(), e);
        }
    }
}
