package pl.pk.citysim.model;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Configuration for the city simulation game.
 * Loads settings from config.yml or uses defaults if the file is not found.
 */
public class GameConfig {
    private static final Logger logger = LoggerFactory.getLogger(GameConfig.class);

    /**
     * Enum representing different difficulty levels for the game.
     */
    public enum Difficulty {
        EASY(1.2, 0.8),
        NORMAL(1.0, 1.0),
        HARD(0.8, 1.2);

        private final double incomeMultiplier;
        private final double expenseMultiplier;

        Difficulty(double incomeMultiplier, double expenseMultiplier) {
            this.incomeMultiplier = incomeMultiplier;
            this.expenseMultiplier = expenseMultiplier;
        }

        public double getIncomeMultiplier() {
            return incomeMultiplier;
        }

        public double getExpenseMultiplier() {
            return expenseMultiplier;
        }
    }

    // Default values
    private static final int DEFAULT_INITIAL_FAMILIES = 10;
    private static final int DEFAULT_INITIAL_BUDGET = 1000;
    private static final double DEFAULT_INCOME_TAX_RATE = 0.10;
    private static final double DEFAULT_VAT_RATE = 0.05;
    private static final long DEFAULT_TICK_INTERVAL_MS = 1000; // 1 second
    private static final String DEFAULT_DIFFICULTY = "NORMAL";
    private static final boolean DEFAULT_SANDBOX_MODE = false;
    private static final int SANDBOX_INITIAL_FAMILIES = 20;
    private static final int SANDBOX_INITIAL_BUDGET = 10000;

    // Configuration properties
    private final int initialFamilies;
    private final int initialBudget;
    private final double initialTaxRate;
    private final double initialVatRate;
    private final long tickIntervalMs;
    private final Difficulty difficulty;
    private final boolean sandboxMode;

    /**
     * Creates a new GameConfig by loading from config.yml or using defaults.
     */
    public GameConfig() {
        Properties props = new Properties();

        // Try to load from config.yml
        File configFile = new File("config.yml");
        if (configFile.exists()) {
            try (InputStream input = new FileInputStream(configFile)) {
                props.load(input);
                logger.info("Loaded configuration from config.yml");
            } catch (IOException e) {
                logger.warn("Failed to load config.yml, using defaults", e);
            }
        } else {
            logger.info("config.yml not found, using default configuration");
        }

        // Load properties with defaults
        this.initialFamilies = Integer.parseInt(
                props.getProperty("initialFamilies", String.valueOf(DEFAULT_INITIAL_FAMILIES)));
        this.initialBudget = Integer.parseInt(
                props.getProperty("initialBudget", String.valueOf(DEFAULT_INITIAL_BUDGET)));
        this.initialTaxRate = Double.parseDouble(
                props.getProperty("initialTaxRate", String.valueOf(DEFAULT_INCOME_TAX_RATE)));
        this.initialVatRate = Double.parseDouble(
                props.getProperty("initialVatRate", String.valueOf(DEFAULT_VAT_RATE)));
        this.tickIntervalMs = Long.parseLong(
                props.getProperty("tickIntervalMs", String.valueOf(DEFAULT_TICK_INTERVAL_MS)));

        // Load difficulty setting
        String difficultyStr = props.getProperty("difficulty", DEFAULT_DIFFICULTY);
        try {
            this.difficulty = Difficulty.valueOf(difficultyStr.toUpperCase());
            logger.info("Game difficulty set to: " + this.difficulty);
        } catch (IllegalArgumentException e) {
            logger.warn("Invalid difficulty setting: " + difficultyStr + ", using NORMAL", e);
            this.difficulty = Difficulty.NORMAL;
        }

        // Load sandbox mode setting
        this.sandboxMode = Boolean.parseBoolean(
                props.getProperty("sandboxMode", String.valueOf(DEFAULT_SANDBOX_MODE)));
    }

    /**
     * Gets the initial number of families in the city.
     *
     * @return The initial number of families
     */
    public int getInitialFamilies() {
        return initialFamilies;
    }

    /**
     * Gets the initial budget for the city.
     *
     * @return The initial budget
     */
    public int getInitialBudget() {
        return initialBudget;
    }

    /**
     * Gets the initial tax rate for the city.
     *
     * @return The initial tax rate
     */
    public double getInitialTaxRate() {
        return initialTaxRate;
    }

    /**
     * Gets the initial VAT rate for the city.
     *
     * @return The initial VAT rate
     */
    public double getInitialVatRate() {
        return initialVatRate;
    }

    /**
     * Gets the interval between game ticks in milliseconds.
     *
     * @return The tick interval in milliseconds
     */
    public long getTickIntervalMs() {
        return tickIntervalMs;
    }

    /**
     * Checks if the game is in sandbox mode.
     *
     * @return true if the game is in sandbox mode, false otherwise
     */
    public boolean isSandboxMode() {
        return sandboxMode;
    }

    /**
     * Gets the initial number of families for the current game mode.
     * In sandbox mode, this returns a higher value.
     *
     * @return The initial number of families
     */
    public int getEffectiveInitialFamilies() {
        return sandboxMode ? SANDBOX_INITIAL_FAMILIES : initialFamilies;
    }

    /**
     * Gets the initial budget for the current game mode.
     * In sandbox mode, this returns a higher value.
     *
     * @return The initial budget
     */
    public int getEffectiveInitialBudget() {
        return sandboxMode ? SANDBOX_INITIAL_BUDGET : initialBudget;
    }
}
