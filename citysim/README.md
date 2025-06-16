# CitySim

A simple city simulation game where you can build and manage a virtual city.

## Features

- Build different types of buildings (Residential, Commercial, Industrial, etc.)
- Set tax rates to balance income and citizen satisfaction
- Watch your city grow day by day
- Manage your budget and keep your citizens happy
- Experience random events that can help or harm your city
- Save your progress and load it later to continue your city's development
- Balance service capacities to avoid penalties
- Manage city growth with limited resources
- Face increasing challenges as your city grows
- Compete for high scores based on city performance
- Play in sandbox mode with abundant resources and no game-over conditions

## Game Mechanics

### Building Capacities
Each building type has specific capacity limits:
- Residential: Houses 25 families per building
- Commercial: Provides 15 jobs per building
- Industrial: Provides 10 jobs per building
- School: Serves up to 50 families per building
- Hospital: Serves up to 60 families per building
- Water Plant: Serves up to 75 families per building
- Power Plant: Serves up to 100 families per building

### Service Penalties
Exceeding service capacities results in penalties:
- Education shortage: Reduces satisfaction and family income
- Healthcare shortage: Reduces satisfaction and increases epidemic risk
- Utility shortage: Reduces satisfaction, income, and increases fire risk
- Housing constraints: Limits population growth and can cause overcrowding

### Difficulty Scaling
The game becomes progressively harder as your city grows:
- Medium cities (50+ families): Reduced income, increased expenses
- Large cities (100+ families): Further reduced income, higher expenses
- Very large cities (200+ families): Maximum difficulty with significant economic challenges

For detailed game mechanics, see the full specification in SPEC.md.

## Requirements

- Java 21 or higher

## Building the Project

To build the project, run:

```bash
mvn clean package
```

This will create a fat JAR file named `citysim-fat.jar` in the `target` directory.

## Running the Game

You can run the game using the provided shell script:

```bash
./run_citysim.sh
```

This script will check if Java is installed, build the project if needed, and run the game.

Alternatively, you can run the game directly with:

```bash
java -jar target/citysim-fat.jar
```

## Game Commands

The game supports the following commands:

### Game Management
- `build <building_type>` - Constructs a new building of the specified type
  - Available building types: RESIDENTIAL, COMMERCIAL, INDUSTRIAL, PARK, SCHOOL, HOSPITAL, WATER_PLANT, POWER_PLANT
- `tax set <income|vat> <rate>` - Sets tax rates (percentage)
  - Example: `tax set income 15` sets the income tax rate to 15% (allowed range: 0-40%)
  - Example: `tax set vat 10` sets the VAT rate to 10% (allowed range: 0-25%)
- `stats` - Displays detailed city statistics with formatted tables

### Event Log
- `log` - Displays the last 5 events that occurred in the city
- `log all` - Displays the complete event log
- `log page <number>` - Displays a specific page of the event log (10 events per page)
- `log next` - Displays the next page of events
- `log prev` - Displays the previous page of events

### Highscores
- `highscore` - Displays the highscore table showing top players
  - Scores are based on population, budget, satisfaction, and days survived
  - Highscores are not recorded in sandbox mode

### Save & Load
- `save <filename>` - Saves the current game state to a file
  - Example: `save mygame` saves the game to `saves/mygame.json`
- `load <filename>` - Loads a game state from a file
  - Example: `load mygame` loads the game from `saves/mygame.json`

### Real-Time Display
The game features a real-time display that automatically refreshes the screen with current city statistics:
- City stats are continuously updated (every 1 second by default)
- Critical status changes are highlighted in real-time
- The input prompt remains responsive during updates
- The display can be paused when you need to read detailed information

### Interface
- `display <pause|resume>` - Pauses or resumes the real-time display
- `pause` - Pauses the game simulation (city data does not update)
- `resume` - Resumes the game simulation after it has been paused
- `continue` - Resumes the game simulation (alias for 'resume')
- `help` - Displays general help information
- `help <command>` - Displays detailed help for a specific command
- `colors <on|off>` - Enables or disables colored output
- `exit` - Exits the game

Type `help` in-game for more detailed information about each command.

## Configuration

The game can be configured by creating a `config.yml` file in the same directory as the JAR file. If the file doesn't exist, default values will be used.

Example configuration:

```
initialFamilies=20
initialBudget=2000
initialTaxRate=0.12
initialVatRate=0.05
tickIntervalMs=1000     # Controls both game speed and display refresh rate
difficulty=NORMAL
sandboxMode=false
```

### Game Modes

The game supports two modes:

1. **Normal Mode** (default): 
   - City can go bankrupt or be abandoned
   - Scores are recorded in the highscore table
   - Resources are limited

2. **Sandbox Mode** (set `sandboxMode=true` in config.yml):
   - No game-over conditions
   - Starts with abundant resources
   - Scores are not recorded
   - Perfect for experimentation and learning

### Difficulty Levels

You can set the game difficulty in the config file:

- `EASY`: More income, fewer expenses
- `NORMAL`: Balanced gameplay
- `HARD`: Less income, more expenses

## Pause & Resume Functionality

The game allows you to pause and resume the simulation at any time:

- The game automatically pauses when you enter any command
- When paused, the city data does not update and random events do not trigger
- The UI displays "*** GAME PAUSED ***" when the game is paused
- All commands remain available during pause, allowing you to check statistics or save the game
- The game automatically pauses briefly during loading to ensure data consistency
- You can resume the game by typing 'continue' or 'resume'

Example usage:
```
> build RESIDENTIAL
Game paused. Enter commands. Type 'continue' to resume.
SUCCESS: Built a new Residential Building
Initial cost: $100
Daily upkeep: $10

> stats
*** GAME PAUSED ***
[City statistics will be displayed...]

> continue
Game resumed.
```

## Save & Load Functionality

The game allows you to save your progress and load it later:

- Saved games are stored in JSON format in a `saves` directory (created automatically)
- The entire city state is preserved, including day count, population, budget, buildings, etc.
- When loading a game, the simulation pauses briefly during the loading process
- After loading, the game continues from the exact state it was saved in

Example usage:
```
> save mygame
Saving game to mygame...
Game saved successfully to mygame.json in the 'saves' directory.

> load mygame
Loading game from mygame...
Game loaded successfully from mygame.json
```

## Development

### Project Structure

```
src/main/java/pl/pk/citysim/
  ├─ CitySim.java        // entry point
  ├─ engine/             // game loop and mechanics
  ├─ model/              // domain objects
  ├─ service/            // business logic
  └─ ui/                 // user interface
```

### Running Tests

To run the tests:

```bash
mvn test
```
