package pl.pk.citysim.engine;

import java.util.logging.Logger;
import java.util.logging.Level;
import pl.pk.citysim.model.GameConfig;
import pl.pk.citysim.service.CityService;
import pl.pk.citysim.ui.ConsoleUi;

/**
 * Game loop that advances the city simulation in a linear fashion.
 */
public class GameLoop {
    private static final Logger logger = Logger.getLogger(GameLoop.class.getName());

    private final CityService cityService;
    private final ConsoleUi consoleUi;
    private final GameConfig config;
    private boolean running;

    /**
     * Creates a new game loop.
     *
     * @param cityService The city service to use for simulation updates
     * @param consoleUi The console UI for user interaction
     */
    public GameLoop(CityService cityService, ConsoleUi consoleUi) {
        this.cityService = cityService;
        this.consoleUi = consoleUi;
        this.config = new GameConfig();
        this.running = false;
    }

    /**
     * Starts the game loop.
     */
    public void start() {
        if (!running) {
            running = true;
            logger.log(Level.INFO, "Starting linear game loop");

            // Start the UI directly (no separate thread)
            consoleUi.start();
        }
    }

    /**
     * Stops the game loop.
     */
    public void stop() {
        if (running) {
            running = false;
            logger.log(Level.INFO, "Stopping game loop");
        }
    }

    /**
     * Performs a single tick of the game loop.
     * 
     * @return true if the game should continue, false if game over
     */
    public boolean tick() {
        try {
            if (running) {
                boolean continueGame = cityService.cityTick();
                if (!continueGame) {
                    consoleUi.handleGameOver();
                    return false;
                } else {
                    consoleUi.waitForSpaceToContinue();
                    return true;
                }
            }
            return running;
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error during game tick", e);
            return false;
        }
    }


    /**
     * Checks if the game loop is currently running.
     *
     * @return true if the game loop is running, false otherwise
     */
    public boolean isRunning() {
        return running;
    }
}
