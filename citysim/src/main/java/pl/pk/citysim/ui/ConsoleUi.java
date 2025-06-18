package pl.pk.citysim.ui;

import java.util.logging.Logger;
import java.util.logging.Level;
import pl.pk.citysim.engine.GameLoop;
import pl.pk.citysim.model.GameConfig;
import pl.pk.citysim.model.Highscore;
import pl.pk.citysim.service.CityService;
import pl.pk.citysim.model.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.HashMap;
import java.util.Map;

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
    private boolean waitingForCityName;

    public ConsoleUi(CityService cityService, GameLoop gameLoop) {
        this.cityService = cityService;
        this.gameLoop = gameLoop;
        this.scanner = new Scanner(System.in);
        this.running = false;
        this.waitingForCityName = true; // Start by waiting for city name
    }

    public void start() {
        if (!running) {
            running = true;
            System.out.println("Welcome to CitySim!");
            if (waitingForCityName) {
                System.out.println("Please enter a name for your city:");
                System.out.print("> ");
                String cityName = scanner.nextLine().trim();
                if (cityName.isEmpty()) {
                    cityName = "Unnamed City";
                }
                cityService.getCity().setName(cityName);
                waitingForCityName = false;
                System.out.println("City name set to: " + cityName);
                System.out.println();
            }
            if (!cityService.isSandboxMode()) {
                System.out.println(ConsoleFormatter.highlightSuccess(
                    "OBJECTIVE: Achieve the highest population possible within " + 
                    GameConfig.MAX_DAYS + " days!"));
                System.out.println("The game will end after " + GameConfig.MAX_DAYS + 
                    " days, or if your city goes bankrupt or is abandoned.");
                System.out.println();
            }

            System.out.println("Available commands:");
            if (cityService.isSandboxMode()) {
                System.out.println(ConsoleFormatter.highlightInfo("SANDBOX MODE ACTIVE - No game over conditions, no highscores"));
                System.out.println();
            }

            System.out.println("  build <building_type>       - Build a new building");
            System.out.println("  tax set <income|vat> <rate> - Set tax rates (percentage)");
            System.out.println("    - income: 0-40% allowed range");
            System.out.println("    - vat: 0-25% allowed range");
            System.out.println("  stats                      - Display city statistics");
            System.out.println("  highscore                  - Display the highscore table");
            System.out.println("  exit                       - Exit the game");
            System.out.println();
            System.out.println(ConsoleFormatter.highlightInfo("Type 'continue' to advance to the next day."));
            System.out.println();
            System.out.println(cityService.getCityStats());
            while (running) {
                System.out.print("> ");
                String input = scanner.nextLine().trim();

                if (input.equalsIgnoreCase("continue") || input.equalsIgnoreCase("c") || 
                    input.equalsIgnoreCase("run") || input.equalsIgnoreCase("r") || 
                    input.equalsIgnoreCase("resume")) {
                    boolean continueGame = gameLoop.tick();
                    if (!continueGame) {
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
    public void stop() {
        running = false;
        System.out.println("Console UI stopped.");
    }

    private void processCommand(String input) {

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



            case "help":
                displayHelp(parts.length > 1 ? parts[1].toLowerCase() : null);
                break;

            case "highscore":
                displayHighscores();
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
                System.out.println(ConsoleFormatter.highlightInfo("Type 'continue', 'c', 'run', 'r', or 'resume' to advance to the next day."));
                break;

            default:
                System.out.println(ConsoleFormatter.highlightError("ERROR: Unknown command: " + command));
                System.out.println(ConsoleFormatter.createDivider());
                System.out.println("Available commands: build, tax, stats, highscore, help, colors, continue, c, run, r, resume, exit");
                System.out.println("Type 'help' for more information about commands.");
                break;
        }
    }

    private void displayHelp(String command) {
        if (command == null) {
            System.out.println(ConsoleFormatter.createHeader("CITYSIM HELP"));
            System.out.println("Available commands:");
            System.out.println();
            if (cityService.isSandboxMode()) {
                System.out.println(ConsoleFormatter.highlightInfo("SANDBOX MODE ACTIVE - No game over conditions, no highscores"));
                System.out.println();
            }

            System.out.println(ConsoleFormatter.highlightInfo("GAME COMMANDS:"));
            System.out.println("  build <building_type> [count] - Build one or more buildings");
            System.out.println("  tax set <income|vat> <rate>   - Set tax rates (percentage)");
            System.out.println("  stats                         - Display city statistics");
            System.out.println();


            System.out.println(ConsoleFormatter.highlightInfo("HIGHSCORE COMMANDS:"));
            System.out.println("  highscore                   - Display the highscore table");
            System.out.println();


            System.out.println(ConsoleFormatter.highlightInfo("INTERFACE COMMANDS:"));
            System.out.println("  continue, c, run, r, resume - Advance to the next day");
            System.out.println("  help [command]              - Display help information");
            System.out.println("  colors <on|off>             - Enable/disable colored output");
            System.out.println("  exit                        - Exit the game");
            System.out.println();

            System.out.println("Type 'help <command>' for more information about a specific command.");
        } else {
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
                    h.getCityName(),
                    String.valueOf(h.getScore()),
                    h.getFamilies() + " families",
                    "$" + h.getBudget(),
                    h.getSatisfaction() + "%",
                    String.valueOf(h.getDays()),
                    h.getFormattedAchievedTime()
                });
            }

            System.out.println(ConsoleFormatter.createTable(
                new String[] {"Rank", "City", "Score", "Population", "Budget", "Satisfaction", "Days", "Date"},
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


    public void waitForSignalToContinue() {
        System.out.println(cityService.getCityStats());
        cityService.saveHighscore();
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
    }

    public void handleGameOver() {
        if (cityService.getCity().getDay() >= GameConfig.MAX_DAYS && !cityService.isSandboxMode()) {
            System.out.println(ConsoleFormatter.highlightSuccess("GAME COMPLETED!"));
            System.out.println("You've reached day " + GameConfig.MAX_DAYS + " with a population of " + 
                cityService.getCity().getFamilies() + " families!");
        } else {
            System.out.println(ConsoleFormatter.highlightError("GAME OVER!"));
        }

        System.out.println(cityService.getGameSummary());
        if (!cityService.isSandboxMode()) {
            int score = cityService.calculateScore();
            int rank = Highscore.getRank(score);

            if (rank > 0 && rank <= 10) {
                System.out.println(ConsoleFormatter.highlightSuccess(
                    "Congratulations! Your score of " + score + " ranks #" + rank + " on the highscore table!"));
                boolean success = cityService.saveHighscore();
                if (success) {
                    System.out.println(ConsoleFormatter.highlightSuccess(
                        "SUCCESS: Highscore saved!"));
                    displayHighscores();
                } else {
                    System.out.println(ConsoleFormatter.highlightError(
                        "ERROR: Failed to save highscore or sandbox mode is active"));
                }
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
