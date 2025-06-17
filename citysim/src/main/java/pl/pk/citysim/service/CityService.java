package pl.pk.citysim.service;

import java.util.logging.Logger;
import java.util.logging.Level;
import pl.pk.citysim.model.Building;
import pl.pk.citysim.model.Highscore;
import pl.pk.citysim.model.City;
import pl.pk.citysim.model.GameConfig;
import pl.pk.citysim.model.GameState;
import pl.pk.citysim.model.ResidentialBuilding;
import pl.pk.citysim.model.CommercialBuilding;
import pl.pk.citysim.model.IndustrialBuilding;
import pl.pk.citysim.model.SchoolBuilding;
import pl.pk.citysim.model.HospitalBuilding;
import pl.pk.citysim.model.ParkBuilding;
import pl.pk.citysim.model.WaterPlantBuilding;
import pl.pk.citysim.model.PowerPlantBuilding;

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
    private static final Class<? extends Building>[] BUILDING_CLASSES = new Class[]{
            ResidentialBuilding.class,
            CommercialBuilding.class,
            IndustrialBuilding.class,
            ParkBuilding.class,
            SchoolBuilding.class,
            HospitalBuilding.class,
            WaterPlantBuilding.class,
            PowerPlantBuilding.class
    };

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
            // Day limit check - game ends after MAX_DAYS
            if (city.getDay() >= GameConfig.MAX_DAYS) {
                logger.log(Level.INFO, String.format("Game over: Reached day limit (%d days)", GameConfig.MAX_DAYS));
                return false;
            }

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
     * @param buildingClass The type of building to build
     * @return true if the building was built successfully, false otherwise
     */
    public boolean buildBuilding(Class<? extends Building> buildingClass) {
        return buildBuildings(buildingClass, 1);
    }

    /**
     * Calculates the cost of a building with the population-based multiplier.
     *
     * @param buildingClass The type of building
     * @return A BuildingCost object containing the cost and multiplier information
     */
    public BuildingCost calculateBuildingCost(Class<? extends Building> buildingClass) {
        int baseCost;
        try {
            Building temp = buildingClass.getConstructor(int.class).newInstance(0);
            baseCost = temp.getUpkeep() * 10;
        } catch (Exception e) {
            throw new RuntimeException("Failed to instantiate building", e);
        }

        // Scale cost with population size
        int families = city.getFamilies();
        double costMultiplier = 1.0;

        // Apply progressive cost scaling based on population tiers
        if (families > 200) {
            costMultiplier = 3.0; // 200% increase for very large cities
        } else if (families > 100) {
            costMultiplier = 2.0; // 100% increase for large cities
        } else if (families > 50) {
            costMultiplier = 1.5; // 50% increase for medium cities
        } else if (families > 20) {
            costMultiplier = 1.2; // 20% increase for small cities
        }

        int actualCost = (int)(baseCost * costMultiplier);
        return new BuildingCost(baseCost, actualCost, costMultiplier);
    }

    /**
     * Class to hold building cost information.
     */
    public static class BuildingCost {
        private final int baseCost;
        private final int actualCost;
        private final double multiplier;

        public BuildingCost(int baseCost, int actualCost, double multiplier) {
            this.baseCost = baseCost;
            this.actualCost = actualCost;
            this.multiplier = multiplier;
        }

        public int getBaseCost() {
            return baseCost;
        }

        public int getActualCost() {
            return actualCost;
        }

        public double getMultiplier() {
            return multiplier;
        }
    }

    /**
     * Builds multiple buildings of the specified type.
     *
     * @param buildingClass The type of building to build
     * @param count The number of buildings to build
     * @return true if all buildings were built successfully, false otherwise
     */
    public boolean buildBuildings(Class<? extends Building> buildingClass, int count) {
        if (buildingClass == null) {
            logger.log(Level.WARNING, "Cannot build null building type");
            return false;
        }

        if (count <= 0) {
            logger.log(Level.WARNING, "Cannot build non-positive number of buildings: " + count);
            return false;
        }

        BuildingCost buildingCost = calculateBuildingCost(buildingClass);
        int costPerBuilding = buildingCost.getActualCost();
        double costMultiplier = buildingCost.getMultiplier();
        int totalCost = costPerBuilding * count;

        if (city.getBudget() < totalCost) {
            logger.log(Level.INFO, String.format(
                    "Not enough budget to build %d %s: need %d, have %d",
                    count, buildingClass.getSimpleName(), totalCost, city.getBudget()));
            return false;
        }

        // Log the cost scaling if applicable
        if (costMultiplier > 1.0) {
            logger.log(Level.INFO, String.format(
                    "Building cost scaled by %.1fx due to city size (%d families)", 
                    costMultiplier, city.getFamilies()));
        }

        // Build all buildings
        for (int i = 0; i < count; i++) {
            Building building = city.addBuilding(buildingClass, costPerBuilding);
            logger.log(Level.INFO, String.format("Built new %s (ID: %d) at cost $%d (%.1fx multiplier)", 
                    buildingClass.getSimpleName(), building.getId(), costPerBuilding, costMultiplier));
        }

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

        // Show different stats based on whether the game ended due to reaching the day limit
        if (city.getDay() >= GameConfig.MAX_DAYS && !config.isSandboxMode()) {
            stats.put("Game Duration", GameConfig.MAX_DAYS + " days (completed)");
            stats.put("Final Population", city.getFamilies() + " families");
            stats.put("Population Objective", "Maximize population in " + GameConfig.MAX_DAYS + " days");
            stats.put("Achievement", "You reached " + city.getFamilies() + " families!");
        } else {
            stats.put("Days Survived", String.valueOf(city.getDay()) + " of " + GameConfig.MAX_DAYS);
            stats.put("Final Population", city.getFamilies() + " families");
            if (!config.isSandboxMode()) {
                stats.put("Population Objective", "Maximize population in " + GameConfig.MAX_DAYS + " days");
                stats.put("Game Ended Early", city.getBudget() < 0 ? "Bankruptcy" : "City Abandoned");
            }
        }

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

        Map<String, Integer> buildingCounts = city.getBuildingCounts();
        List<String[]> buildingRows = new ArrayList<>();

        for (String typeName : buildingCounts.keySet()) {
            int count = buildingCounts.get(typeName);
            if (count > 0) {
                buildingRows.add(new String[]{
                    typeName,
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
     * The primary objective is to maximize population (families) within 100 days.
     * 
     * @return The calculated score
     */
    public int calculateScore() {
        // Score formula: (families * 50) + (budget / 20) + (satisfaction * 2)
        // Population (families) is now the primary success metric with a much higher weight
        int families = city.getFamilies();
        int budget = city.getBudget();
        int satisfaction = city.getSatisfaction();

        return (families * 50) + (budget / 20) + (satisfaction * 2);
    }

    /**
     * Saves the current game score to the highscores file.
     * 
     * @return true if the score was saved successfully, false otherwise
     */
    public boolean saveHighscore() {
        // Don't save highscores in sandbox mode
        if (config.isSandboxMode()) {
            return false;
        }

        Highscore highscore = Highscore.calculateScore(city);
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

        // Income and expenses info
        Map<String, String> financialInfo = new HashMap<>();
        financialInfo.put("Daily Income", "$" + city.getDailyIncome());
        financialInfo.put("Daily Expenses", "$" + city.getDailyExpenses());
        financialInfo.put("Net Daily Profit/Loss", "$" + (city.getDailyIncome() - city.getDailyExpenses()));

        stats.append(pl.pk.citysim.ui.ConsoleFormatter.createKeyValueTable("FINANCIAL INFO", financialInfo));

        // Tax information
        Map<String, String> taxInfo = new HashMap<>();
        taxInfo.put("Income Tax", String.format("%.1f%%", city.getTaxRate() * 100));
        taxInfo.put("VAT", String.format("%.1f%%", city.getVatRate() * 100));

        stats.append(pl.pk.citysim.ui.ConsoleFormatter.createKeyValueTable("TAXES", taxInfo));

        // Building counts
        stats.append(pl.pk.citysim.ui.ConsoleFormatter.createHeader("BUILDINGS"));

        Map<String, Integer> buildingCounts = city.getBuildingCounts();
        List<String[]> buildingRows = new ArrayList<>();

        for (Class<? extends Building> cls : BUILDING_CLASSES) {
            try {
                Building tmp = cls.getConstructor(int.class).newInstance(0);
                String typeName = tmp.getTypeName();
                int count = buildingCounts.getOrDefault(typeName, 0);
                if (count > 0) {
                    buildingRows.add(new String[]{
                        typeName,
                        String.valueOf(count),
                        tmp.getDescription()
                    });
                }
            } catch (Exception e) {
                // ignore
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
            if (building instanceof ResidentialBuilding) {
                residentialCapacity += building.getCapacity();
            } else if (building instanceof CommercialBuilding) {
                commercialCapacity += building.getCapacity();
            } else if (building instanceof IndustrialBuilding) {
                industrialCapacity += building.getCapacity();
            } else if (building instanceof SchoolBuilding) {
                educationCapacity += building.getEducationCapacity();
            } else if (building instanceof HospitalBuilding) {
                healthcareCapacity += building.getHealthcareCapacity();
            } else if (building instanceof WaterPlantBuilding) {
                waterCapacity += building.getUtilityCapacity();
            } else if (building instanceof PowerPlantBuilding) {
                powerCapacity += building.getUtilityCapacity();
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
        List<String> recentEvents = city.getRecentEvents();
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
