package pl.pk.citysim.engine;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pl.pk.citysim.model.GameConfig;
import pl.pk.citysim.service.CityService;
import pl.pk.citysim.ui.ConsoleUi;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Game loop that advances the city simulation at regular intervals.
 */
public class GameLoop {
    private static final Logger logger = LoggerFactory.getLogger(GameLoop.class);

    private final CityService cityService;
    private final ConsoleUi consoleUi;
    private final GameConfig config;
    private final ScheduledExecutorService scheduler;
    private final AtomicBoolean running;
    private final AtomicBoolean paused;

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
        this.scheduler = Executors.newScheduledThreadPool(1);
        this.running = new AtomicBoolean(false);
        this.paused = new AtomicBoolean(false);
    }

    /**
     * Starts the game loop.
     */
    public void start() {
        if (running.compareAndSet(false, true)) {
            logger.info("Starting game loop with tick interval of {} ms", config.getTickIntervalMs());

            scheduler.scheduleAtFixedRate(
                    this::tick,
                    0,
                    config.getTickIntervalMs(),
                    TimeUnit.MILLISECONDS
            );

            // Start the UI in a separate thread
            new Thread(consoleUi::start).start();
        }
    }

    /**
     * Stops the game loop.
     */
    public void stop() {
        if (running.compareAndSet(true, false)) {
            logger.info("Stopping game loop");
            scheduler.shutdown();
            try {
                if (!scheduler.awaitTermination(5, TimeUnit.SECONDS)) {
                    scheduler.shutdownNow();
                }
            } catch (InterruptedException e) {
                scheduler.shutdownNow();
                Thread.currentThread().interrupt();
            }
        }
    }

    /**
     * Performs a single tick of the game loop.
     */
    private void tick() {
        try {
            if (running.get() && !paused.get()) {
                boolean continueGame = cityService.cityTick();

                // Check if game over conditions were met
                if (!continueGame) {
                    // Pause the game
                    pause();

                    // Notify the UI that the game is over
                    consoleUi.handleGameOver();
                }
            }
        } catch (Exception e) {
            logger.error("Error during game tick", e);
        }
    }

    /**
     * Pauses the game loop.
     * 
     * @return true if the game was paused, false if it was already paused
     */
    public boolean pause() {
        if (paused.compareAndSet(false, true)) {
            logger.info("Game loop paused");
            return true;
        }
        return false;
    }

    /**
     * Resumes the game loop.
     * 
     * @return true if the game was resumed, false if it was not paused
     */
    public boolean resume() {
        if (paused.compareAndSet(true, false)) {
            logger.info("Game loop resumed");
            return true;
        }
        return false;
    }

    /**
     * Checks if the game loop is currently paused.
     * 
     * @return true if the game loop is paused, false otherwise
     */
    public boolean isPaused() {
        return paused.get();
    }

    /**
     * Checks if the game loop is currently running.
     *
     * @return true if the game loop is running, false otherwise
     */
    public boolean isRunning() {
        return running.get();
    }
}
