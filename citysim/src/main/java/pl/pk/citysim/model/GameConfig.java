package pl.pk.citysim.model;

import java.util.logging.Logger;
import java.util.logging.Level;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class GameConfig {
    private static final Logger logger = Logger.getLogger(GameConfig.class.getName());

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
    private static final int DEFAULT_INITIAL_FAMILIES = 10;
    private static final int DEFAULT_INITIAL_BUDGET = 1000;
    private static final double DEFAULT_INCOME_TAX_RATE = 0.10;
    private static final double DEFAULT_VAT_RATE = 0.05;
    private static final long DEFAULT_TICK_INTERVAL_MS = 1000; // 1 second
    private static final String DEFAULT_DIFFICULTY = "NORMAL";
    private static final boolean DEFAULT_SANDBOX_MODE = false;
    private static final int SANDBOX_INITIAL_FAMILIES = 20;
    private static final int SANDBOX_INITIAL_BUDGET = 10000;
    public static final int MAX_DAYS = 100;
    private final int initialFamilies;
    private final int initialBudget;
    private final double initialTaxRate;
    private final double initialVatRate;
    private final long tickIntervalMs;
    private final Difficulty difficulty;
    private final boolean sandboxMode;

    public GameConfig() {
        Properties props = new Properties();
        File configFile = new File("config.yml");
        if (configFile.exists()) {
            try (InputStream input = new FileInputStream(configFile)) {
                props.load(input);
                logger.log(Level.INFO, "Loaded configuration from config.yml");
            } catch (IOException e) {
                logger.log(Level.WARNING, "Failed to load config.yml, using defaults", e);
            }
        } else {
            logger.log(Level.INFO, "config.yml not found, using default configuration");
        }
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
        String difficultyStr = props.getProperty("difficulty", DEFAULT_DIFFICULTY);
        this.difficulty = Difficulty.valueOf(difficultyStr.toUpperCase());
        this.sandboxMode = Boolean.parseBoolean(props.getProperty("sandboxMode", String.valueOf(DEFAULT_SANDBOX_MODE)));
    }

    public int getInitialFamilies() {
        return initialFamilies;
    }

    public int getInitialBudget() {
        return initialBudget;
    }

    public double getInitialTaxRate() {
        return initialTaxRate;
    }

    public double getInitialVatRate() {
        return initialVatRate;
    }

    public long getTickIntervalMs() {
        return tickIntervalMs;
    }

    public boolean isSandboxMode() {
        return sandboxMode;
    }

    public int getEffectiveInitialFamilies() {
        return sandboxMode ? SANDBOX_INITIAL_FAMILIES : initialFamilies;
    }

    public int getEffectiveInitialBudget() {
        return sandboxMode ? SANDBOX_INITIAL_BUDGET : initialBudget;
    }
}
