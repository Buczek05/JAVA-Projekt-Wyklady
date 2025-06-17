package pl.pk.citysim.ui;

import java.util.logging.Logger;
import java.util.logging.Level;
import pl.pk.citysim.engine.GameLoop;
import pl.pk.citysim.model.GameConfig;
import pl.pk.citysim.model.Highscore;
import pl.pk.citysim.model.GameState;
import pl.pk.citysim.service.CityService;
import pl.pk.citysim.model.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.HashMap;
import java.util.Map;

/**
 * Console user interface for the city simulation game.
 * Linear implementation without threads.
 */
public class ConsoleUi {
    private static final Logger logger = Logger.getLogger(ConsoleUi.class.getName());
    private static final int MAX_HIGHSCORES = 10; // Same value as in Highscore class
    private static final Map<String, Class<? extends Building>> BUILDING_MAP = new HashMap<>();

    static {
        BUILDING_MAP.put("RESIDENTIAL", ResidentialBuilding.class);
        BUILDING_MAP.put("COMMERCIAL", CommercialBuilding.class);
        BUILDING_MAP.put("INDUSTRIAL", IndustrialBuilding.class);
        BUILDING_MAP.put("PARK", ParkBuilding.class);
        BUILDING_MAP.put("SCHOOL", SchoolBuilding.class);
        BUILDING_MAP.put("HOSPITAL", HospitalBuilding.class);
        BUILDING_MAP.put("WATER_PLANT", WaterPlantBuilding.class);
        BUILDING_MAP.put("POWER_PLANT", PowerPlantBuilding.class);
    }

    private final CityService cityService;
    private final GameLoop gameLoop;
    private final Scanner scanner;
    private boolean running;
    private boolean waitingForPlayerName;
    private boolean displayPaused;

    /**
     * Creates a new console UI.
     *
     * @param cityService The city service to use for game actions
     * @param gameLoop The game loop to use for pausing/resuming the game
     */
    public ConsoleUi(CityService cityService, GameLoop gameLoop) {
        this.cityService = cityService;
        this.gameLoop = gameLoop;
        this.scanner = new Scanner(System.in);
        this.running = false;
        this.waitingForPlayerName = false;
    }

    /**
     * Starts the console UI.
     */
    public void start() {
        if (!running) {
            running = true;
            System.out.println("Welcome to CitySim!");

            // Display game objective
            if (!cityService.isSandboxMode()) {
                System.out.println(ConsoleFormatter.highlightSuccess(
                    "OBJECTIVE: Achieve the highest population possible within " + 
                    GameConfig.MAX_DAYS + " days!"));
                System.out.println("The game will end after " + GameConfig.MAX_DAYS + 
                    " days, or if your city goes bankrupt or is abandoned.");
                System.out.println();
            }

            System.out.println("Available commands:");
            // Show sandbox mode indicator if applicable
            if (cityService.isSandboxMode()) {
                System.out.println(ConsoleFormatter.highlightInfo("SANDBOX MODE ACTIVE - No game over conditions, no highscores"));
                System.out.println();
            }

            System.out.println("  build <building_type>       - Build a new building");
            System.out.println("  tax set <income|vat> <rate> - Set tax rates (percentage)");
            System.out.println("    - income: 0-40% allowed range");
            System.out.println("    - vat: 0-25% allowed range");
            System.out.println("  stats                      - Display city statistics");
            System.out.println("  log                        - Display recent events");
            System.out.println("  highscore                  - Display the highscore table");
            System.out.println("  save <filename>            - Save the game to a file");
            System.out.println("  load <filename>            - Load the game from a file");
            System.out.println("  exit                       - Exit the game");
            System.out.println();
            System.out.println(ConsoleFormatter.highlightInfo("Type 'continue' to advance to the next day."));
            System.out.println();

            // Display initial city stats
            System.out.println(cityService.getCityStats());

            // Main input loop
            while (running) {
                System.out.print("> ");
                String input = scanner.nextLine().trim();

                if (input.equalsIgnoreCase("continue") || input.equalsIgnoreCase("c") || 
                    input.equalsIgnoreCase("run") || input.equalsIgnoreCase("r") || 
                    input.equalsIgnoreCase("resume")) {
                    // Advance the game by one day
                    boolean continueGame = gameLoop.tick();
                    if (!continueGame) {
                        // Game over condition met
                        break;
                    }
                } else if (!input.isEmpty()) {
                    try {
                        processCommand(input);
                    } catch (Exception e) {
                        System.out.println("Error processing command: " + e.getMessage());
                        logger.log(Level.SEVERE, "Error processing command: " + input, e);
                    }
                }
            }
        }
    }

    /**
     * This method has been removed as it's not needed in the linear implementation.
     * The game now pauses after each day automatically and waits for user input.
     */

    /**
     * Stops the console UI.
     */
    public void stop() {
        running = false;
        System.out.println("Console UI stopped.");
    }

    /**
     * Pauses the real-time display.
     * 
     * @return true if the display was paused, false if it was already paused
     */
    private boolean pauseDisplay() {
        if (!displayPaused) {
            displayPaused = true;
            logger.info("Real-time display paused");
            System.out.println(ConsoleFormatter.highlightInfo("Real-time display paused. Type 'display resume' to resume."));
            return true;
        }
        return false;
    }

    /**
     * Resumes the real-time display.
     * 
     * @return true if the display was resumed, false if it was not paused
     */
    private boolean resumeDisplay() {
        if (displayPaused) {
            displayPaused = false;
            logger.info("Real-time display resumed");
            System.out.println(ConsoleFormatter.highlightInfo("Real-time display resumed. Type 'display pause' to pause."));
            return true;
        }
        return false;
    }

    /**
     * Clears the console screen.
     * Uses ANSI escape codes if supported, otherwise falls back to printing newlines.
     */
    private void clearConsole() {
        try {
            // Check if ANSI is supported (most modern terminals)
            String os = System.getProperty("os.name").toLowerCase();

            if (ConsoleFormatter.areColorsEnabled()) {
                // If colors are enabled, assume ANSI is supported
                // ANSI escape code to clear screen and move cursor to top-left
                System.out.print("\033[H\033[2J");
                System.out.flush();
            } else if (os.contains("win")) {
                // On Windows without ANSI support, use cls command
                new ProcessBuilder("cmd", "/c", "cls").inheritIO().start().waitFor();
            } else {
                // Fallback to printing newlines
                System.out.print("\n".repeat(100));
            }
        } catch (Exception e) {
            // Fallback to printing newlines if any error occurs
            System.out.print("\n".repeat(100));
        }
    }

    /**
     * Refreshes the display with current game state.
     * In the linear implementation, this is called directly when needed.
     */
    private void refreshDisplay() {
        try {
            if (!displayPaused) {
                // Clear the console
                clearConsole();

                // Display city stats
                String cityStats = cityService.getCityStats();

                System.out.println(cityStats);

                // Show input prompt
                System.out.print("> ");
                System.out.flush(); // Ensure prompt is displayed immediately
            }
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error refreshing display", e);
        }
    }

    // Track the current page of the event log
    private int currentLogPage = 0;
    private static final int LOG_PAGE_SIZE = 10;

    /**
     * Processes a command from user input.
     *
     * @param input The user input to process
     */
    private void processCommand(String input) {
        // If waiting for player name for highscore, handle that specially
        if (waitingForPlayerName) {
            waitingForPlayerName = false;
            final String playerName = input.trim().isEmpty() ? "Anonymous" : input.trim();

            boolean success = cityService.saveHighscore(playerName);
            if (success) {
                System.out.println(ConsoleFormatter.highlightSuccess(
                    "SUCCESS: Highscore saved for player '" + playerName + "'"));
                displayHighscores();
            } else {
                System.out.println(ConsoleFormatter.highlightError(
                    "ERROR: Failed to save highscore or sandbox mode is active"));
            }
            return;
        }

        String[] parts = input.split("\\s+", 2);
        String command = parts[0].toLowerCase();

        switch (command) {
            case "build":
                if (parts.length < 2) {
                    System.out.println(ConsoleFormatter.highlightError("ERROR: Missing building type"));
                    System.out.println(ConsoleFormatter.createDivider());
                    System.out.println("Usage: build <building_type> [count]");
                    System.out.println("Available building types: " + 
                            String.join(", ", getBuildingTypeNames()));
                } else {
                    String[] buildParts = parts[1].split("\\s+", 2);
                    String buildingTypeName = buildParts[0].toUpperCase();
                    int count = 1; // Default to building one

                    // Check if count is specified
                    if (buildParts.length > 1) {
                        try {
                            count = Integer.parseInt(buildParts[1]);
                            if (count <= 0) {
                                System.out.println(ConsoleFormatter.highlightError(
                                    "ERROR: Building count must be positive"));
                                break;
                            }
                        } catch (NumberFormatException e) {
                            System.out.println(ConsoleFormatter.highlightError(
                                "ERROR: Invalid building count: " + buildParts[1]));
                            break;
                        }
                    }

                    Class<? extends Building> buildingClass = BUILDING_MAP.get(buildingTypeName);
                    if (buildingClass == null) {
                        System.out.println(ConsoleFormatter.highlightError("ERROR: Unknown building type: " + buildingTypeName));
                        System.out.println(ConsoleFormatter.createDivider());
                        System.out.println("Available building types: " + String.join(", ", getBuildingTypeNames()));
                        break;
                    }
                    String typeName = BUILDING_MAP.entrySet().stream()
                            .filter(e -> e.getValue() == buildingClass)
                            .map(Map.Entry::getKey)
                            .findFirst().orElse(buildingClass.getSimpleName());
                    boolean success = cityService.buildBuildings(buildingClass, count);
                    try {
                        CityService.BuildingCost buildingCost = cityService.calculateBuildingCost(buildingClass);
                        if (success) {
                            // Get the building cost information
                            int baseCost = buildingCost.getBaseCost();
                            int actualCost = buildingCost.getActualCost();
                            double multiplier = buildingCost.getMultiplier();


                            if (count == 1) {
                                System.out.println(ConsoleFormatter.highlightSuccess(
                                    "SUCCESS: Built a new " + typeName));
                            } else {
                                System.out.println(ConsoleFormatter.highlightSuccess(
                                    "SUCCESS: Built " + count + " new " + typeName + " buildings"));
                            }

                            // Show base cost and actual cost with multiplier
                            System.out.println("Base cost per building: $" + baseCost);
                            if (multiplier > 1.0) {
                                System.out.println("Actual cost per building: $" + actualCost + 
                                    " (x" + String.format("%.1f", multiplier) + " due to city size)");
                            } else {
                                System.out.println("Actual cost per building: $" + actualCost);
                            }
                            System.out.println("Daily upkeep per building: $" + newInstance(buildingClass).getUpkeep());

                            if (count > 1) {
                                System.out.println("Total initial cost: $" + (actualCost * count));
                                System.out.println("Total daily upkeep: $" + (newInstance(buildingClass).getUpkeep() * count));
                            }
                        } else {
                            // Get the building cost information
                            CityService.BuildingCost buildingCost = cityService.calculateBuildingCost(buildingClass);
                            int actualCost = buildingCost.getActualCost();
                            double multiplier = buildingCost.getMultiplier();

                            if (count == 1) {
                                System.out.println(ConsoleFormatter.highlightError(
                                    "ERROR: Failed to build " + typeName + " - not enough budget"));
                                System.out.println("Required budget: $" + actualCost);
                                if (multiplier > 1.0) {
                                    System.out.println("Note: Cost includes x" + String.format("%.1f", multiplier) + 
                                        " multiplier due to city size");
                                }
                            } else {
                                System.out.println(ConsoleFormatter.highlightError(
                                    "ERROR: Failed to build " + count + " " + typeName + " buildings - not enough budget"));
                                System.out.println("Required budget: $" + (actualCost * count));
                                if (multiplier > 1.0) {
                                    System.out.println("Note: Cost includes x" + String.format("%.1f", multiplier) + 
                                        " multiplier due to city size");
                                }
                            }
                            System.out.println("Current budget: $" + cityService.getCity().getBudget());
                        }
                    } catch (Exception e) {
                        System.out.println(ConsoleFormatter.highlightError("ERROR: " + e.getMessage()));
                    }
                }
                break;

            case "tax":
                if (parts.length < 2) {
                    System.out.println(ConsoleFormatter.highlightError("ERROR: Invalid tax command"));
                    System.out.println(ConsoleFormatter.createDivider());
                    System.out.println("Usage: tax set <income|vat> <value>");
                    System.out.println("Examples:");
                    System.out.println("  tax set income 15 (sets income tax rate to 15%, allowed range: 0-40%)");
                    System.out.println("  tax set vat 10 (sets VAT rate to 10%, allowed range: 0-25%)");
                } else {
                    String[] taxParts = parts[1].split("\\s+", 3);
                    if (taxParts.length < 3 || !taxParts[0].equalsIgnoreCase("set")) {
                        System.out.println(ConsoleFormatter.highlightError("ERROR: Invalid tax command format"));
                        System.out.println(ConsoleFormatter.createDivider());
                        System.out.println("Usage: tax set <income|vat> <value>");
                        System.out.println("Examples:");
                        System.out.println("  tax set income 15 (sets income tax rate to 15%, allowed range: 0-40%)");
                        System.out.println("  tax set vat 10 (sets VAT rate to 10%, allowed range: 0-25%)");
                    } else {
                        String taxType = taxParts[1].toLowerCase();
                        try {
                            double taxValue = Double.parseDouble(taxParts[2]) / 100.0;

                            if (taxType.equals("income")) {
                                if (taxValue < 0.0 || taxValue > 0.4) {
                                    System.out.println(ConsoleFormatter.highlightError(
                                        "ERROR: Income tax rate must be between 0% and 40%"));
                                } else {
                                    double oldRate = cityService.getCity().getTaxRate();
                                    cityService.setTaxRate(taxValue);
                                    System.out.println(ConsoleFormatter.highlightSuccess(
                                        "SUCCESS: Income tax changed from " + 
                                        String.format("%.1f%%", oldRate * 100) + " to " + 
                                        String.format("%.1f%%", taxValue * 100)));
                                    System.out.println("Note: Family arrival chance and satisfaction may change.");
                                }
                            } else if (taxType.equals("vat")) {
                                if (taxValue < 0.0 || taxValue > 0.25) {
                                    System.out.println(ConsoleFormatter.highlightError(
                                        "ERROR: VAT rate must be between 0% and 25%"));
                                } else {
                                    double oldRate = cityService.getCity().getVatRate();
                                    cityService.setVatRate(taxValue);
                                    System.out.println(ConsoleFormatter.highlightSuccess(
                                        "SUCCESS: VAT changed from " + 
                                        String.format("%.1f%%", oldRate * 100) + " to " + 
                                        String.format("%.1f%%", taxValue * 100)));
                                    System.out.println("Note: Family arrival chance and satisfaction may change.");
                                }
                            } else {
                                System.out.println(ConsoleFormatter.highlightError(
                                    "ERROR: Unknown tax type: " + taxType));
                                System.out.println("Available tax types: income, vat");
                            }
                        } catch (NumberFormatException e) {
                            System.out.println(ConsoleFormatter.highlightError(
                                "ERROR: Invalid tax value: " + taxParts[2]));
                            System.out.println("Please enter a valid number (e.g., 15 for 15%)");
                        }
                    }
                }
                break;

            case "stats":
                System.out.println(cityService.getCityStats());
                break;

            case "log":
                if (parts.length < 2) {
                    // Default behavior - show recent events
                    displayRecentEvents();
                } else {
                    String logCommand = parts[1].toLowerCase();

                    if (logCommand.equals("all")) {
                        // Show all events
                        displayAllEvents();
                    } else if (logCommand.startsWith("page")) {
                        // Handle paging
                        String[] logParts = logCommand.split("\\s+", 2);
                        if (logParts.length > 1) {
                            try {
                                int page = Integer.parseInt(logParts[1]);
                                displayEventPage(page);
                            } catch (NumberFormatException e) {
                                System.out.println(ConsoleFormatter.highlightError(
                                    "ERROR: Invalid page number: " + logParts[1]));
                            }
                        } else {
                            displayEventPage(currentLogPage);
                        }
                    } else if (logCommand.equals("next")) {
                        // Show next page
                        displayEventPage(currentLogPage + 1);
                    } else if (logCommand.equals("prev") || logCommand.equals("previous")) {
                        // Show previous page
                        displayEventPage(Math.max(0, currentLogPage - 1));
                    } else {
                        System.out.println(ConsoleFormatter.highlightError(
                            "ERROR: Unknown log command: " + logCommand));
                        System.out.println("Available log commands: all, page <number>, next, prev");
                    }
                }
                break;

            case "save":
                if (parts.length < 2) {
                    System.out.println(ConsoleFormatter.highlightError("ERROR: Missing filename"));
                    System.out.println(ConsoleFormatter.createDivider());
                    System.out.println("Usage: save <filename>");
                    System.out.println("Example: save mygame");
                } else {
                    String filename = parts[1];
                    System.out.println("Saving game to " + filename + "...");
                    boolean success = cityService.saveGame(filename);
                    if (success) {
                        System.out.println(ConsoleFormatter.highlightSuccess(
                            "SUCCESS: Game saved to " + filename + ".json in the 'saves' directory"));
                    } else {
                        System.out.println(ConsoleFormatter.highlightError(
                            "ERROR: Failed to save game. Check logs for details."));
                    }
                }
                break;

            case "load":
                if (parts.length < 2) {
                    System.out.println(ConsoleFormatter.highlightError("ERROR: Missing filename"));
                    System.out.println(ConsoleFormatter.createDivider());
                    System.out.println("Usage: load <filename>");
                    System.out.println("Example: load mygame");
                } else {
                    String filename = parts[1];
                    System.out.println("Loading game from " + filename + "...");

                    boolean success = cityService.loadGame(filename);

                    if (success) {
                        System.out.println(ConsoleFormatter.highlightSuccess(
                            "SUCCESS: Game loaded from " + filename + ".json"));
                        System.out.println(cityService.getCityStats());
                    } else {
                        System.out.println(ConsoleFormatter.highlightError(
                            "ERROR: Failed to load game. Check logs for details."));
                    }
                }
                break;

            case "help":
                displayHelp(parts.length > 1 ? parts[1].toLowerCase() : null);
                break;

            case "highscore":
                displayHighscores();
                break;

            case "display":
                // In linear mode, we don't need real-time display
                System.out.println(ConsoleFormatter.highlightInfo("Real-time display is not available in linear mode."));
                break;

            case "colors":
                if (parts.length > 1 && (parts[1].equalsIgnoreCase("on") || parts[1].equalsIgnoreCase("off"))) {
                    boolean enableColors = parts[1].equalsIgnoreCase("on");
                    ConsoleFormatter.setColorsEnabled(enableColors);
                    System.out.println("ANSI colors " + (enableColors ? "enabled" : "disabled"));
                } else {
                    System.out.println("Usage: colors <on|off>");
                }
                break;

            case "exit":
                System.out.println("Exiting CitySim. Goodbye!");
                stop();
                System.exit(0);
                break;

            case "pause":
                System.out.println(ConsoleFormatter.highlightInfo("Pause system has been removed. Use 'continue', 'c', 'run', 'r', or 'resume' to advance to the next day."));
                break;

            case "resume":
            case "continue":
                // These commands are now handled in the main input loop
                System.out.println(ConsoleFormatter.highlightInfo("Type 'continue', 'c', 'run', 'r', or 'resume' to advance to the next day."));
                break;

            default:
                System.out.println(ConsoleFormatter.highlightError("ERROR: Unknown command: " + command));
                System.out.println(ConsoleFormatter.createDivider());
                System.out.println("Available commands: build, tax, stats, log, highscore, save, load, help, colors, continue, c, run, r, resume, exit");
                System.out.println("Type 'help' for more information about commands.");
                break;
        }
    }

    /**
     * Displays all events from the current day and the 5 most recent events.
     */
    private void displayRecentEvents() {
        System.out.println(ConsoleFormatter.createHeader("CURRENT DAY EVENTS"));

        // Get all events from the current day
        List<String> currentDayEvents = cityService.getCity().getCurrentDayEvents();
        if (currentDayEvents.isEmpty()) {
            System.out.println("No events for the current day.");
        } else {
            for (String event : currentDayEvents) {
                System.out.println(ConsoleFormatter.formatLogEntry(event));
            }
        }

        // Also show the 5 most recent events if they're not all from the current day
        List<String> recentEvents = cityService.getCity().getRecentEvents();
        boolean allCurrentDay = true;

        // Check if all recent events are from the current day
        int currentDay = cityService.getCity().getDay();
        String currentDayPrefix = "Day " + currentDay + ":";
        for (String event : recentEvents) {
            if (!event.startsWith(currentDayPrefix)) {
                allCurrentDay = false;
                break;
            }
        }

        // If not all recent events are from the current day, show them separately
        if (!allCurrentDay) {
            System.out.println(ConsoleFormatter.createHeader("RECENT EVENTS (LAST 5)"));
            for (String event : recentEvents) {
                System.out.println(ConsoleFormatter.formatLogEntry(event));
            }
        }

        System.out.println(ConsoleFormatter.createDivider());
        System.out.println("Type 'log all' to view all events for the current day or 'log page <number>' to view a specific page.");
    }

    /**
     * Displays all events in the log for the current day.
     */
    private void displayAllEvents() {
        List<String> currentDayEvents = cityService.getCity().getCurrentDayEvents();
        System.out.println(ConsoleFormatter.createHeader("CURRENT DAY EVENT LOG"));

        if (currentDayEvents.isEmpty()) {
            System.out.println("No events for today.");
        } else {
            for (String event : currentDayEvents) {
                System.out.println(ConsoleFormatter.formatLogEntry(event));
            }
        }

        System.out.println(ConsoleFormatter.createDivider());
        System.out.println("Total events today: " + currentDayEvents.size());
    }

    /**
     * Displays a specific page of events for the current day.
     *
     * @param page The page number (0-based)
     */
    private void displayEventPage(int page) {
        List<String> currentDayEvents = cityService.getCity().getCurrentDayEvents();
        int totalEvents = currentDayEvents.size();
        int totalPages = (totalEvents + LOG_PAGE_SIZE - 1) / LOG_PAGE_SIZE;

        // Ensure page is within valid range
        page = Math.max(0, Math.min(page, totalPages - 1));
        currentLogPage = page;

        int startIndex = page * LOG_PAGE_SIZE;
        int endIndex = Math.min(startIndex + LOG_PAGE_SIZE, totalEvents);

        System.out.println(ConsoleFormatter.createHeader("CURRENT DAY EVENT LOG (PAGE " + (page + 1) + " OF " + totalPages + ")"));

        if (totalEvents == 0) {
            System.out.println("No events for today.");
        } else if (startIndex >= totalEvents) {
            System.out.println("Page " + (page + 1) + " is empty. Try a lower page number.");
        } else {
            for (int i = startIndex; i < endIndex; i++) {
                System.out.println(ConsoleFormatter.formatLogEntry(currentDayEvents.get(i)));
            }
        }

        System.out.println(ConsoleFormatter.createDivider());
        System.out.println("Showing events " + (startIndex + 1) + "-" + endIndex + " of " + totalEvents);
        System.out.println("Type 'log next' for the next page or 'log prev' for the previous page.");
    }

    /**
     * Displays help information about commands.
     *
     * @param command The specific command to show help for, or null for general help
     */
    private void displayHelp(String command) {
        if (command == null) {
            // General help
            System.out.println(ConsoleFormatter.createHeader("CITYSIM HELP"));
            System.out.println("Available commands:");
            System.out.println();

            // Show sandbox mode indicator if applicable
            if (cityService.isSandboxMode()) {
                System.out.println(ConsoleFormatter.highlightInfo("SANDBOX MODE ACTIVE - No game over conditions, no highscores"));
                System.out.println();
            }

            System.out.println(ConsoleFormatter.highlightInfo("GAME COMMANDS:"));
            System.out.println("  build <building_type> [count] - Build one or more buildings");
            System.out.println("  tax set <income|vat> <rate>   - Set tax rates (percentage)");
            System.out.println("  stats                         - Display city statistics");
            System.out.println();

            System.out.println(ConsoleFormatter.highlightInfo("EVENT LOG COMMANDS:"));
            System.out.println("  log                         - Display current day events");
            System.out.println("  log all                     - Display all events for the current day");
            System.out.println("  log page <number>           - Display specific page of current day events");
            System.out.println("  log next                    - Display next page of current day events");
            System.out.println("  log prev                    - Display previous page of current day events");
            System.out.println();

            System.out.println(ConsoleFormatter.highlightInfo("HIGHSCORE COMMANDS:"));
            System.out.println("  highscore                   - Display the highscore table");
            System.out.println();

            System.out.println(ConsoleFormatter.highlightInfo("SAVE/LOAD COMMANDS:"));
            System.out.println("  save <filename>             - Save the game to a file");
            System.out.println("  load <filename>             - Load the game from a file");
            System.out.println();

            System.out.println(ConsoleFormatter.highlightInfo("INTERFACE COMMANDS:"));
            System.out.println("  display <pause|resume>      - Pause/resume real-time display");
            System.out.println("  continue, c, run, r, resume - Advance to the next day");
            System.out.println("  help [command]              - Display help information");
            System.out.println("  colors <on|off>             - Enable/disable colored output");
            System.out.println("  exit                        - Exit the game");
            System.out.println();

            System.out.println("Type 'help <command>' for more information about a specific command.");
        } else {
            // Command-specific help
            switch (command) {
                case "build":
                    System.out.println(ConsoleFormatter.createHeader("BUILD COMMAND HELP"));
                    System.out.println("Usage: build <building_type>");
                    System.out.println();
                    System.out.println("Constructs a new building of the specified type in your city.");
                    System.out.println("Each building has an initial cost (10x daily upkeep, scaled by city size) and provides different benefits.");
                    System.out.println("Note: As your city grows, building costs increase (up to 3x for very large cities).");
                    System.out.println();
                    System.out.println("Available building types:");
                    for (Map.Entry<String, Class<? extends Building>> entry : BUILDING_MAP.entrySet()) {
                        Building tmp = newInstance(entry.getValue());
                        System.out.println("  " + entry.getKey() + " - " + tmp.getDescription());
                        if (tmp instanceof ResidentialBuilding || tmp instanceof CommercialBuilding || tmp instanceof IndustrialBuilding) {
                            System.out.println("    Capacity: " + tmp.getCapacity() + (tmp instanceof ResidentialBuilding ? " families" : " jobs"));
                        } else if (tmp instanceof SchoolBuilding) {
                            System.out.println("    Serves up to " + tmp.getEducationCapacity() + " families");
                        } else if (tmp instanceof HospitalBuilding) {
                            System.out.println("    Serves up to " + tmp.getHealthcareCapacity() + " families");
                        } else if (tmp instanceof WaterPlantBuilding || tmp instanceof PowerPlantBuilding) {
                            System.out.println("    Serves up to " + tmp.getUtilityCapacity() + " families");
                        }
                        CityService.BuildingCost buildingCost = cityService.calculateBuildingCost(entry.getValue());
                        int baseCost = buildingCost.getBaseCost();
                        int actualCost = buildingCost.getActualCost();
                        double multiplier = buildingCost.getMultiplier();

                        if (multiplier > 1.0) {
                            System.out.println("    Base cost: $" + baseCost + ", Actual cost: $" + actualCost +
                                " (x" + String.format("%.1f", multiplier) + " due to city size)");
                        } else {
                            System.out.println("    Initial cost: $" + actualCost);
                        }
                        System.out.println("    Daily upkeep: $" + tmp.getUpkeep());
                    }
                    break;

                case "tax":
                    System.out.println(ConsoleFormatter.createHeader("TAX COMMAND HELP"));
                    System.out.println("Usage: tax set <income|vat> <rate>");
                    System.out.println();
                    System.out.println("Sets tax rates for your city. Higher taxes increase revenue but decrease satisfaction.");
                    System.out.println();
                    System.out.println("Tax types:");
                    System.out.println("  income - Income tax applied to family earnings (0-40%)");
                    System.out.println("  vat    - Value-added tax applied to spending (0-25%)");
                    System.out.println();
                    System.out.println("Examples:");
                    System.out.println("  tax set income 15  - Sets income tax to 15%");
                    System.out.println("  tax set vat 10     - Sets VAT to 10%");
                    break;

                case "stats":
                    System.out.println(ConsoleFormatter.createHeader("STATS COMMAND HELP"));
                    System.out.println("Usage: stats");
                    System.out.println();
                    System.out.println("Displays detailed statistics about your city, including:");
                    System.out.println("  - Basic city information (day, population, budget, satisfaction)");
                    System.out.println("  - Current tax rates");
                    System.out.println("  - Building counts and descriptions");
                    System.out.println("  - Service capacities and status");
                    System.out.println("  - Recent events");
                    break;

                case "log":
                    System.out.println(ConsoleFormatter.createHeader("LOG COMMAND HELP"));
                    System.out.println("Usage: log [option]");
                    System.out.println();
                    System.out.println("Displays the event log for your city.");
                    System.out.println();
                    System.out.println("Options:");
                    System.out.println("  (no option) - Shows the 5 most recent events");
                    System.out.println("  all         - Shows all events in the log");
                    System.out.println("  page <num>  - Shows a specific page of events (10 events per page)");
                    System.out.println("  next        - Shows the next page of events");
                    System.out.println("  prev        - Shows the previous page of events");
                    System.out.println();
                    System.out.println("Examples:");
                    System.out.println("  log          - Shows the 5 most recent events");
                    System.out.println("  log all      - Shows all events");
                    System.out.println("  log page 2   - Shows the second page of events");
                    System.out.println("  log next     - Shows the next page after the current one");
                    break;

                case "save":
                    System.out.println(ConsoleFormatter.createHeader("SAVE COMMAND HELP"));
                    System.out.println("Usage: save <filename>");
                    System.out.println();
                    System.out.println("Saves the current game state to a file in the 'saves' directory.");
                    System.out.println("The .json extension will be added automatically if not provided.");
                    System.out.println();
                    System.out.println("Example:");
                    System.out.println("  save mygame  - Saves the game to 'saves/mygame.json'");
                    break;

                case "load":
                    System.out.println(ConsoleFormatter.createHeader("LOAD COMMAND HELP"));
                    System.out.println("Usage: load <filename>");
                    System.out.println();
                    System.out.println("Loads a game state from a file in the 'saves' directory.");
                    System.out.println("The .json extension will be added automatically if not provided.");
                    System.out.println();
                    System.out.println("Example:");
                    System.out.println("  load mygame  - Loads the game from 'saves/mygame.json'");
                    break;

                case "colors":
                    System.out.println(ConsoleFormatter.createHeader("COLORS COMMAND HELP"));
                    System.out.println("Usage: colors <on|off>");
                    System.out.println();
                    System.out.println("Enables or disables ANSI color codes in the output.");
                    System.out.println("Colors can make the interface more readable but may not work in all terminals.");
                    System.out.println();
                    System.out.println("Examples:");
                    System.out.println("  colors on   - Enables colored output");
                    System.out.println("  colors off  - Disables colored output");
                    break;

                case "exit":
                    System.out.println(ConsoleFormatter.createHeader("EXIT COMMAND HELP"));
                    System.out.println("Usage: exit");
                    System.out.println();
                    System.out.println("Exits the game. Any unsaved progress will be lost.");
                    break;

                case "pause":
                    System.out.println(ConsoleFormatter.createHeader("PAUSE COMMAND HELP"));
                    System.out.println("Usage: pause");
                    System.out.println();
                    System.out.println("The pause system has been removed. The game now waits for a command after each day.");
                    System.out.println("Use 'continue', 'c', 'run', 'r', or 'resume' to advance to the next day.");
                    break;

                case "resume":
                case "continue":
                case "c":
                case "run":
                case "r":
                    System.out.println(ConsoleFormatter.createHeader("CONTINUE COMMAND HELP"));
                    System.out.println("Usage: continue");
                    System.out.println("   or: c");
                    System.out.println("   or: run");
                    System.out.println("   or: r");
                    System.out.println("   or: resume");
                    System.out.println();
                    System.out.println("Advances the game to the next day.");
                    System.out.println("After each day, the game waits for one of these commands to continue.");
                    break;

                case "help":
                    System.out.println(ConsoleFormatter.createHeader("HELP COMMAND HELP"));
                    System.out.println("Usage: help [command]");
                    System.out.println();
                    System.out.println("Displays help information about commands.");
                    System.out.println("If a command is specified, shows detailed help for that command.");
                    System.out.println("Otherwise, shows a list of all available commands.");
                    break;

                case "display":
                    System.out.println(ConsoleFormatter.createHeader("DISPLAY COMMAND HELP"));
                    System.out.println("Usage: display <pause|resume>");
                    System.out.println();
                    System.out.println("Controls the real-time display of city statistics.");
                    System.out.println("The real-time display automatically refreshes the screen with current city stats.");
                    System.out.println();
                    System.out.println("Options:");
                    System.out.println("  pause  - Temporarily stops the automatic screen refresh");
                    System.out.println("  resume - Restarts the automatic screen refresh after pausing");
                    System.out.println();
                    System.out.println("Examples:");
                    System.out.println("  display pause  - Pauses the real-time display");
                    System.out.println("  display resume - Resumes the real-time display");
                    break;

                case "highscore":
                    System.out.println(ConsoleFormatter.createHeader("HIGHSCORE COMMAND HELP"));
                    System.out.println("Usage: highscore");
                    System.out.println();
                    System.out.println("Displays the highscore table showing the top players.");
                    System.out.println("Highscores are based on a combination of:");
                    System.out.println("  - Population (families)");
                    System.out.println("  - Budget");
                    System.out.println("  - Satisfaction level");
                    System.out.println("  - Days survived");
                    System.out.println();
                    System.out.println("Note: Highscores are not recorded in sandbox mode.");
                    break;

                default:
                    System.out.println(ConsoleFormatter.highlightError("ERROR: Unknown command: " + command));
                    System.out.println("Type 'help' for a list of available commands.");
                    break;
            }
        }
    }

    /**
     * This method has been removed as it's not needed in the linear implementation.
     * Commands are processed directly in the main input loop.
     */

    /**
     * Gets an array of building type names.
     *
     * @return Array of building type names
     */
    private String[] getBuildingTypeNames() {
        return BUILDING_MAP.keySet().toArray(new String[0]);
    }

    private Building newInstance(Class<? extends Building> clazz) {
        try {
            return clazz.getConstructor(int.class).newInstance(0);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Displays the highscore table.
     */
    private void displayHighscores() {
        System.out.println(ConsoleFormatter.createHeader("HIGHSCORE TABLE"));

        List<Highscore> highscores = Highscore.loadHighscores();

        if (highscores.isEmpty()) {
            System.out.println("No highscores recorded yet. Be the first to make the list!");
        } else {
            List<String[]> rows = new ArrayList<>();

            for (int i = 0; i < highscores.size(); i++) {
                Highscore h = highscores.get(i);
                rows.add(new String[] {
                    String.valueOf(i + 1),
                    h.getPlayerName(),
                    String.valueOf(h.getScore()),
                    h.getFamilies() + " families",
                    "$" + h.getBudget(),
                    h.getSatisfaction() + "%",
                    String.valueOf(h.getDays()),
                    h.getFormattedAchievedTime()
                });
            }

            System.out.println(ConsoleFormatter.createTable(
                new String[] {"Rank", "Player", "Score", "Population", "Budget", "Satisfaction", "Days", "Date"},
                rows
            ));
        }

        // Show current game score if not in sandbox mode
        if (!cityService.isSandboxMode()) {
            int currentScore = cityService.calculateScore();
            int rank = Highscore.getRank(currentScore);

            System.out.println(ConsoleFormatter.createDivider());
            System.out.println("Your current score: " + currentScore);

            if (rank > 0 && rank <= 10) {
                System.out.println("Current rank: #" + rank + " (would make the highscore table)");
            } else {
                System.out.println("Current rank: Not in top 10");
            }
        } else {
            System.out.println(ConsoleFormatter.createDivider());
            System.out.println(ConsoleFormatter.highlightInfo("SANDBOX MODE: Scores are not recorded in sandbox mode"));
        }
    }

    // Flag to track if autosave has been done for this game session
    private boolean autosaveDone = false;

    /**
     * Waits for the user to press the space key to continue the game.
     * Displays the current city stats and a prompt.
     * In the linear implementation, this is called directly from the game loop.
     */
    public void waitForSpaceToContinue() {
        // Display city stats
        System.out.println(cityService.getCityStats());

        // Autosave the game only once per session
        if (!autosaveDone) {
            // Create a timestamp for the filename
            String timestamp = java.time.LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss"));
            String autosaveFilename = "autosave_" + timestamp;
            boolean saveSuccess = cityService.saveGame(autosaveFilename);
            if (saveSuccess) {
                System.out.println(ConsoleFormatter.highlightInfo(
                    "Game automatically saved to " + autosaveFilename + ".json"));
                autosaveDone = true;
            }
        }

        // Show progress towards objective if not in sandbox mode
        if (!cityService.isSandboxMode()) {
            int currentDay = cityService.getCity().getDay();
            int daysRemaining = GameConfig.MAX_DAYS - currentDay;
            int currentPopulation = cityService.getCity().getFamilies();

            System.out.println(ConsoleFormatter.highlightInfo(
                "Day " + currentDay + " completed. " + 
                daysRemaining + " days remaining. Current population: " + currentPopulation + " families."));
            System.out.println(ConsoleFormatter.highlightInfo(
                "Type 'continue', 'c', 'run', 'r', or 'resume' to advance to the next day..."));
        } else {
            System.out.println(ConsoleFormatter.highlightInfo(
                "Day " + cityService.getCity().getDay() + " completed. Type 'continue', 'c', 'run', 'r', or 'resume' to advance to the next day..."));
        }

        // In the linear implementation, we don't need to wait for a key press here
        // The main input loop in the start() method will handle this
    }

    /**
     * Handles game over conditions.
     * Displays a game summary and prompts for a player name for the highscore table.
     * In the linear implementation, this is called directly from the game loop.
     */
    public void handleGameOver() {
        // Check if game ended due to reaching day limit
        if (cityService.getCity().getDay() >= GameConfig.MAX_DAYS && !cityService.isSandboxMode()) {
            System.out.println(ConsoleFormatter.highlightSuccess("GAME COMPLETED!"));
            System.out.println("You've reached day " + GameConfig.MAX_DAYS + " with a population of " + 
                cityService.getCity().getFamilies() + " families!");
        } else {
            // Display standard game over message for other end conditions
            System.out.println(ConsoleFormatter.highlightError("GAME OVER!"));
        }

        System.out.println(cityService.getGameSummary());

        // Only prompt for name if not in sandbox mode
        if (!cityService.isSandboxMode()) {
            int score = cityService.calculateScore();
            int rank = Highscore.getRank(score);

            if (rank > 0 && rank <= 10) {
                System.out.println(ConsoleFormatter.highlightSuccess(
                    "Congratulations! Your score of " + score + " ranks #" + rank + " on the highscore table!"));
                System.out.println("Enter your name for the highscore table:");
                waitingForPlayerName = true;
            } else {
                System.out.println("Your score of " + score + " did not make the top 10 highscore table.");
                System.out.println("Type 'exit' to quit or start a new game.");
            }
        } else {
            System.out.println(ConsoleFormatter.highlightInfo(
                "SANDBOX MODE: Game over conditions were met, but sandbox mode prevents actual game over."));
            System.out.println("You can continue playing or type 'exit' to quit.");
        }
    }
}
