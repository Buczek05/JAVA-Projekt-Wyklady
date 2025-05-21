package pl.pk.citysim.ui;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pl.pk.citysim.engine.GameLoop;
import pl.pk.citysim.model.BuildingType;
import pl.pk.citysim.service.CityService;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Console user interface for the city simulation game.
 */
public class ConsoleUi {
    private static final Logger logger = LoggerFactory.getLogger(ConsoleUi.class);
    private static final int DISPLAY_REFRESH_INTERVAL_MS = 1000; // 1 second refresh interval

    private final CityService cityService;
    private final GameLoop gameLoop;
    private final Scanner scanner;
    private final BlockingQueue<Runnable> commandQueue;
    private final AtomicBoolean running;

    // Real-time display related fields
    private final ScheduledExecutorService displayScheduler;
    private final AtomicBoolean displayRunning;
    private final AtomicBoolean displayPaused;

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
        this.commandQueue = new LinkedBlockingQueue<>();
        this.running = new AtomicBoolean(false);

        // Initialize real-time display components
        this.displayScheduler = Executors.newScheduledThreadPool(1);
        this.displayRunning = new AtomicBoolean(false);
        this.displayPaused = new AtomicBoolean(false);
    }

    /**
     * Starts the console UI.
     */
    public void start() {
        if (running.compareAndSet(false, true)) {
            System.out.println("Welcome to CitySim!");
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
            System.out.println("  display <pause|resume>     - Pause/resume real-time display");
            System.out.println("  exit                       - Exit the game");
            System.out.println();

            // Start command processor thread
            Thread processorThread = new Thread(this::processCommands);
            processorThread.setDaemon(true);
            processorThread.start();

            // Start real-time display
            startDisplay();

            // Main input loop
            while (running.get()) {
                System.out.print("> ");
                String input = scanner.nextLine().trim();

                if (!input.isEmpty()) {
                    try {
                        parseCommand(input);
                    } catch (Exception e) {
                        System.out.println("Error processing command: " + e.getMessage());
                        logger.error("Error processing command: {}", input, e);
                    }
                }
            }
        }
    }

    /**
     * Stops the console UI.
     */
    public void stop() {
        running.set(false);
        stopDisplay();
    }

    /**
     * Starts the real-time display.
     */
    private void startDisplay() {
        if (displayRunning.compareAndSet(false, true)) {
            logger.info("Starting real-time display with refresh interval of {} ms", DISPLAY_REFRESH_INTERVAL_MS);

            // Schedule periodic display updates
            displayScheduler.scheduleAtFixedRate(
                    this::refreshDisplay,
                    0,
                    DISPLAY_REFRESH_INTERVAL_MS,
                    TimeUnit.MILLISECONDS
            );

            System.out.println(ConsoleFormatter.highlightInfo(
                "Real-time display started. Type 'display pause' to pause or 'display resume' to resume."));
        }
    }

    /**
     * Stops the real-time display.
     */
    private void stopDisplay() {
        if (displayRunning.compareAndSet(true, false)) {
            logger.info("Stopping real-time display");
            displayScheduler.shutdown();
            try {
                if (!displayScheduler.awaitTermination(5, TimeUnit.SECONDS)) {
                    displayScheduler.shutdownNow();
                }
            } catch (InterruptedException e) {
                displayScheduler.shutdownNow();
                Thread.currentThread().interrupt();
            }
        }
    }

    /**
     * Pauses the real-time display.
     * 
     * @return true if the display was paused, false if it was already paused
     */
    private boolean pauseDisplay() {
        if (displayPaused.compareAndSet(false, true)) {
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
        if (displayPaused.compareAndSet(true, false)) {
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
     */
    private void refreshDisplay() {
        try {
            if (displayRunning.get() && !displayPaused.get()) {
                // Clear the console
                clearConsole();

                // Display city stats
                String cityStats = cityService.getCityStats();
                System.out.println(cityStats);

                // Show input prompt
                System.out.print("> ");
            }
        } catch (Exception e) {
            logger.error("Error refreshing display", e);
        }
    }

    // Track the current page of the event log
    private int currentLogPage = 0;
    private static final int LOG_PAGE_SIZE = 10;

    // Track if we're waiting for a player name for highscore
    private boolean waitingForPlayerName = false;

    /**
     * Parses a command from user input.
     *
     * @param input The user input to parse
     */
    private void parseCommand(String input) {
        // If waiting for player name for highscore, handle that specially
        if (waitingForPlayerName) {
            waitingForPlayerName = false;
            String playerName = input.trim();
            if (playerName.isEmpty()) {
                playerName = "Anonymous";
            }

            commandQueue.add(() -> {
                boolean success = cityService.saveHighscore(playerName);
                if (success) {
                    System.out.println(ConsoleFormatter.highlightSuccess(
                        "SUCCESS: Highscore saved for player '" + playerName + "'"));
                    displayHighscores();
                } else {
                    System.out.println(ConsoleFormatter.highlightError(
                        "ERROR: Failed to save highscore or sandbox mode is active"));
                }
            });
            return;
        }

        String[] parts = input.split("\\s+", 2);
        String command = parts[0].toLowerCase();

        switch (command) {
            case "build":
                if (parts.length < 2) {
                    System.out.println(ConsoleFormatter.highlightError("ERROR: Missing building type"));
                    System.out.println(ConsoleFormatter.createDivider());
                    System.out.println("Usage: build <building_type>");
                    System.out.println("Available building types: " + 
                            String.join(", ", getBuildingTypeNames()));
                } else {
                    String buildingTypeName = parts[1].toUpperCase();
                    try {
                        BuildingType buildingType = BuildingType.valueOf(buildingTypeName);
                        commandQueue.add(() -> {
                            boolean success = cityService.buildBuilding(buildingType);
                            if (success) {
                                System.out.println(ConsoleFormatter.highlightSuccess(
                                    "SUCCESS: Built a new " + buildingType.getName()));
                                System.out.println("Initial cost: $" + (buildingType.getUpkeep() * 10));
                                System.out.println("Daily upkeep: $" + buildingType.getUpkeep());
                            } else {
                                System.out.println(ConsoleFormatter.highlightError(
                                    "ERROR: Failed to build " + buildingType.getName() + " - not enough budget"));
                                System.out.println("Required budget: $" + (buildingType.getUpkeep() * 10));
                                System.out.println("Current budget: $" + cityService.getCity().getBudget());
                            }
                        });
                    } catch (IllegalArgumentException e) {
                        System.out.println(ConsoleFormatter.highlightError("ERROR: Unknown building type: " + parts[1]));
                        System.out.println(ConsoleFormatter.createDivider());
                        System.out.println("Available building types: " + 
                                String.join(", ", getBuildingTypeNames()));
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
                                    commandQueue.add(() -> {
                                        double oldRate = cityService.getCity().getTaxRate();
                                        cityService.setTaxRate(taxValue);
                                        System.out.println(ConsoleFormatter.highlightSuccess(
                                            "SUCCESS: Income tax changed from " + 
                                            String.format("%.1f%%", oldRate * 100) + " to " + 
                                            String.format("%.1f%%", taxValue * 100)));
                                        System.out.println("Note: Family arrival chance and satisfaction may change.");
                                    });
                                }
                            } else if (taxType.equals("vat")) {
                                if (taxValue < 0.0 || taxValue > 0.25) {
                                    System.out.println(ConsoleFormatter.highlightError(
                                        "ERROR: VAT rate must be between 0% and 25%"));
                                } else {
                                    commandQueue.add(() -> {
                                        double oldRate = cityService.getCity().getVatRate();
                                        cityService.setVatRate(taxValue);
                                        System.out.println(ConsoleFormatter.highlightSuccess(
                                            "SUCCESS: VAT changed from " + 
                                            String.format("%.1f%%", oldRate * 100) + " to " + 
                                            String.format("%.1f%%", taxValue * 100)));
                                        System.out.println("Note: Family arrival chance and satisfaction may change.");
                                    });
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
                commandQueue.add(() -> {
                    System.out.println(cityService.getCityStats());
                });
                break;

            case "log":
                if (parts.length < 2) {
                    // Default behavior - show recent events
                    commandQueue.add(() -> {
                        displayRecentEvents();
                    });
                } else {
                    String logCommand = parts[1].toLowerCase();

                    if (logCommand.equals("all")) {
                        // Show all events
                        commandQueue.add(() -> {
                            displayAllEvents();
                        });
                    } else if (logCommand.startsWith("page")) {
                        // Handle paging
                        String[] logParts = logCommand.split("\\s+", 2);
                        if (logParts.length > 1) {
                            try {
                                int page = Integer.parseInt(logParts[1]);
                                commandQueue.add(() -> {
                                    displayEventPage(page);
                                });
                            } catch (NumberFormatException e) {
                                System.out.println(ConsoleFormatter.highlightError(
                                    "ERROR: Invalid page number: " + logParts[1]));
                            }
                        } else {
                            commandQueue.add(() -> {
                                displayEventPage(currentLogPage);
                            });
                        }
                    } else if (logCommand.equals("next")) {
                        // Show next page
                        commandQueue.add(() -> {
                            displayEventPage(currentLogPage + 1);
                        });
                    } else if (logCommand.equals("prev") || logCommand.equals("previous")) {
                        // Show previous page
                        commandQueue.add(() -> {
                            displayEventPage(Math.max(0, currentLogPage - 1));
                        });
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
                    commandQueue.add(() -> {
                        System.out.println("Saving game to " + filename + "...");
                        boolean success = cityService.saveGame(filename);
                        if (success) {
                            System.out.println(ConsoleFormatter.highlightSuccess(
                                "SUCCESS: Game saved to " + filename + ".json in the 'saves' directory"));
                        } else {
                            System.out.println(ConsoleFormatter.highlightError(
                                "ERROR: Failed to save game. Check logs for details."));
                        }
                    });
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
                    commandQueue.add(() -> {
                        System.out.println("Loading game from " + filename + "...");

                        // Pause the game loop while loading
                        boolean wasPaused = gameLoop.isPaused();
                        if (!wasPaused) {
                            gameLoop.pause();
                        }

                        boolean success = cityService.loadGame(filename);

                        if (success) {
                            System.out.println(ConsoleFormatter.highlightSuccess(
                                "SUCCESS: Game loaded from " + filename + ".json"));
                            System.out.println(cityService.getCityStats());
                        } else {
                            System.out.println(ConsoleFormatter.highlightError(
                                "ERROR: Failed to load game. Check logs for details."));
                        }

                        // Resume the game loop if it wasn't paused before
                        if (!wasPaused) {
                            gameLoop.resume();
                        }
                    });
                }
                break;

            case "help":
                commandQueue.add(() -> {
                    displayHelp(parts.length > 1 ? parts[1].toLowerCase() : null);
                });
                break;

            case "highscore":
                commandQueue.add(() -> {
                    displayHighscores();
                });
                break;

            case "display":
                if (parts.length < 2) {
                    System.out.println(ConsoleFormatter.highlightError("ERROR: Missing display command"));
                    System.out.println(ConsoleFormatter.createDivider());
                    System.out.println("Usage: display <pause|resume>");
                    System.out.println("Examples:");
                    System.out.println("  display pause  - Pauses the real-time display");
                    System.out.println("  display resume - Resumes the real-time display");
                } else {
                    String displayCommand = parts[1].toLowerCase();
                    if (displayCommand.equals("pause")) {
                        commandQueue.add(() -> {
                            if (pauseDisplay()) {
                                // Already displays a message in pauseDisplay()
                            } else {
                                System.out.println(ConsoleFormatter.highlightInfo("Display is already paused."));
                            }
                        });
                    } else if (displayCommand.equals("resume")) {
                        commandQueue.add(() -> {
                            if (resumeDisplay()) {
                                // Already displays a message in resumeDisplay()
                            } else {
                                System.out.println(ConsoleFormatter.highlightInfo("Display is not paused."));
                            }
                        });
                    } else {
                        System.out.println(ConsoleFormatter.highlightError("ERROR: Unknown display command: " + displayCommand));
                        System.out.println("Available display commands: pause, resume");
                    }
                }
                break;

            case "colors":
                if (parts.length > 1 && (parts[1].equalsIgnoreCase("on") || parts[1].equalsIgnoreCase("off"))) {
                    boolean enableColors = parts[1].equalsIgnoreCase("on");
                    commandQueue.add(() -> {
                        ConsoleFormatter.setColorsEnabled(enableColors);
                        System.out.println("ANSI colors " + (enableColors ? "enabled" : "disabled"));
                    });
                } else {
                    System.out.println("Usage: colors <on|off>");
                }
                break;

            case "exit":
                System.out.println("Exiting CitySim. Goodbye!");
                commandQueue.add(() -> {
                    stop();
                    System.exit(0);
                });
                break;

            default:
                System.out.println(ConsoleFormatter.highlightError("ERROR: Unknown command: " + command));
                System.out.println(ConsoleFormatter.createDivider());
                System.out.println("Available commands: build, tax, stats, log, highscore, save, load, help, colors, exit");
                System.out.println("Type 'help' for more information about commands.");
                break;
        }
    }

    /**
     * Displays recent events (last 5).
     */
    private void displayRecentEvents() {
        System.out.println(ConsoleFormatter.createHeader("RECENT EVENTS"));
        List<String> recentEvents = cityService.getCity().getRecentEvents(5);
        if (recentEvents.isEmpty()) {
            System.out.println("No recent events.");
        } else {
            for (String event : recentEvents) {
                System.out.println(ConsoleFormatter.formatLogEntry(event));
            }
        }
        System.out.println(ConsoleFormatter.createDivider());
        System.out.println("Type 'log all' to view the full event log or 'log page <number>' to view a specific page.");
    }

    /**
     * Displays all events in the log.
     */
    private void displayAllEvents() {
        List<String> allEvents = cityService.getCity().getEventLog();
        System.out.println(ConsoleFormatter.createHeader("FULL EVENT LOG"));

        if (allEvents.isEmpty()) {
            System.out.println("No events in the log.");
        } else {
            for (String event : allEvents) {
                System.out.println(ConsoleFormatter.formatLogEntry(event));
            }
        }

        System.out.println(ConsoleFormatter.createDivider());
        System.out.println("Total events: " + allEvents.size());
    }

    /**
     * Displays a specific page of events.
     *
     * @param page The page number (0-based)
     */
    private void displayEventPage(int page) {
        List<String> allEvents = cityService.getCity().getEventLog();
        int totalEvents = allEvents.size();
        int totalPages = (totalEvents + LOG_PAGE_SIZE - 1) / LOG_PAGE_SIZE;

        // Ensure page is within valid range
        page = Math.max(0, Math.min(page, totalPages - 1));
        currentLogPage = page;

        int startIndex = page * LOG_PAGE_SIZE;
        int endIndex = Math.min(startIndex + LOG_PAGE_SIZE, totalEvents);

        System.out.println(ConsoleFormatter.createHeader("EVENT LOG (PAGE " + (page + 1) + " OF " + totalPages + ")"));

        if (totalEvents == 0) {
            System.out.println("No events in the log.");
        } else if (startIndex >= totalEvents) {
            System.out.println("Page " + (page + 1) + " is empty. Try a lower page number.");
        } else {
            for (int i = startIndex; i < endIndex; i++) {
                System.out.println(ConsoleFormatter.formatLogEntry(allEvents.get(i)));
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
            System.out.println("  build <building_type>       - Build a new building");
            System.out.println("  tax set <income|vat> <rate> - Set tax rates (percentage)");
            System.out.println("  stats                       - Display city statistics");
            System.out.println();

            System.out.println(ConsoleFormatter.highlightInfo("EVENT LOG COMMANDS:"));
            System.out.println("  log                         - Display recent events (last 5)");
            System.out.println("  log all                     - Display all events");
            System.out.println("  log page <number>           - Display specific page of events");
            System.out.println("  log next                    - Display next page of events");
            System.out.println("  log prev                    - Display previous page of events");
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
                    System.out.println("Each building has an initial cost (10x daily upkeep) and provides different benefits.");
                    System.out.println();
                    System.out.println("Available building types:");
                    for (BuildingType type : BuildingType.values()) {
                        System.out.println("  " + type.name() + " - " + type.getDescription());
                        if (type == BuildingType.RESIDENTIAL || type == BuildingType.COMMERCIAL || type == BuildingType.INDUSTRIAL) {
                            System.out.println("    Capacity: " + type.getCapacity() + (type == BuildingType.RESIDENTIAL ? " families" : " jobs"));
                        } else if (type == BuildingType.SCHOOL) {
                            System.out.println("    Serves up to " + type.getEducationCapacity() + " families");
                        } else if (type == BuildingType.HOSPITAL) {
                            System.out.println("    Serves up to " + type.getHealthcareCapacity() + " families");
                        } else if (type == BuildingType.WATER_PLANT || type == BuildingType.POWER_PLANT) {
                            System.out.println("    Serves up to " + type.getUtilityCapacity() + " families");
                        }
                        System.out.println("    Initial cost: $" + (type.getUpkeep() * 10) + ", Daily upkeep: $" + type.getUpkeep());
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
     * Processes commands from the command queue.
     */
    private void processCommands() {
        while (running.get()) {
            try {
                Runnable command = commandQueue.take();
                command.run();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            } catch (Exception e) {
                logger.error("Error executing command", e);
            }
        }
    }

    /**
     * Gets an array of building type names.
     *
     * @return Array of building type names
     */
    private String[] getBuildingTypeNames() {
        BuildingType[] types = BuildingType.values();
        String[] names = new String[types.length];
        for (int i = 0; i < types.length; i++) {
            names[i] = types[i].name();
        }
        return names;
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

            if (rank > 0 && rank <= Highscore.MAX_HIGHSCORES) {
                System.out.println("Current rank: #" + rank + " (would make the highscore table)");
            } else {
                System.out.println("Current rank: Not in top " + Highscore.MAX_HIGHSCORES);
            }
        } else {
            System.out.println(ConsoleFormatter.createDivider());
            System.out.println(ConsoleFormatter.highlightInfo("SANDBOX MODE: Scores are not recorded in sandbox mode"));
        }
    }

    /**
     * Handles game over conditions.
     * Displays a game summary and prompts for a player name for the highscore table.
     */
    public void handleGameOver() {
        commandQueue.add(() -> {
            // Display game summary
            System.out.println(ConsoleFormatter.highlightError("GAME OVER!"));
            System.out.println(cityService.getGameSummary());

            // Only prompt for name if not in sandbox mode
            if (!cityService.isSandboxMode()) {
                int score = cityService.calculateScore();
                int rank = Highscore.getRank(score);

                if (rank > 0 && rank <= Highscore.MAX_HIGHSCORES) {
                    System.out.println(ConsoleFormatter.highlightSuccess(
                        "Congratulations! Your score of " + score + " ranks #" + rank + " on the highscore table!"));
                    System.out.println("Enter your name for the highscore table:");
                    waitingForPlayerName = true;
                } else {
                    System.out.println("Your score of " + score + " did not make the top " + 
                            Highscore.MAX_HIGHSCORES + " highscore table.");
                    System.out.println("Type 'exit' to quit or start a new game.");
                }
            } else {
                System.out.println(ConsoleFormatter.highlightInfo(
                    "SANDBOX MODE: Game over conditions were met, but sandbox mode prevents actual game over."));
                System.out.println("You can continue playing or type 'exit' to quit.");
            }
        });
    }
}
