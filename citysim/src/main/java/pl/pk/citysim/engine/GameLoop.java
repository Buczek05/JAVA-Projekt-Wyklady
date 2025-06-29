package pl.pk.citysim.engine;

import java.util.logging.Logger;
import java.util.logging.Level;
import pl.pk.citysim.model.GameConfig;
import pl.pk.citysim.service.CityService;
import pl.pk.citysim.ui.ConsoleUi;
public class GameLoop {
    private static final Logger logger = Logger.getLogger(GameLoop.class.getName());

    private final CityService cityService;
    private final ConsoleUi consoleUi;
    private final GameConfig config;
    private boolean running;

    public GameLoop(CityService cityService, ConsoleUi consoleUi) {
        this.cityService = cityService;
        this.consoleUi = consoleUi;
        this.config = new GameConfig();
        this.running = false;
    }

    public void start() {
        if (!running) {
            running = true;
            logger.log(Level.INFO, "Starting linear game loop");
            consoleUi.start();
        }
    }

    public boolean tick() {
        try {
            if (running) {
                boolean continueGame = cityService.cityTick();
                if (!continueGame) {
                    consoleUi.handleGameOver();
                    return false;
                } else {
                    consoleUi.waitForSignalToContinue();
                    return true;
                }
            }
            return running;
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error during game tick", e);
            return false;
        }
    }
}
