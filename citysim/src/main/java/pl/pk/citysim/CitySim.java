package pl.pk.citysim;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pl.pk.citysim.engine.GameLoop;
import pl.pk.citysim.service.CityService;
import pl.pk.citysim.ui.ConsoleUi;

public class CitySim {
    private static final Logger logger = LoggerFactory.getLogger(CitySim.class);

    public static void main(String[] args) {
        logger.info("Starting CitySim application");
        CityService cityService = new CityService();
        GameLoop gameLoop = new GameLoop(cityService, null); // Temporary null for consoleUi
        ConsoleUi consoleUi = new ConsoleUi(cityService, gameLoop);
        try {
            java.lang.reflect.Field consoleUiField = GameLoop.class.getDeclaredField("consoleUi");
            consoleUiField.setAccessible(true);
            consoleUiField.set(gameLoop, consoleUi);
        } catch (Exception e) {
            logger.error("Failed to set consoleUi in gameLoop", e);
        }
        gameLoop.start();

        logger.info("CitySim application started");
    }
}
