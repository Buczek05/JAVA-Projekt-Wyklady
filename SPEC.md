# City Simulation Game Specification

## Game Loop
- Each game loop iteration represents 1 day in the simulation
- Each loop iteration occurs every 1 second in real-time (configurable via tickIntervalMs)
- The real-time display refresh rate is synchronized with the game loop tick interval
- The game loop and display run in separate threads to ensure responsive UI

## Buildings
- List of available buildings:
  - Residential: Houses families, increases population
  - Commercial: Provides jobs and generates income
  - Industrial: Generates higher income but reduces satisfaction
  - Park: Increases satisfaction but generates no income
  - School: Improves education and satisfaction
  - Hospital: Improves health and satisfaction

## Building Effects
- Each building type has:
  - Capacity: How many families/jobs it can support
  - Upkeep cost: Daily maintenance cost
  - Satisfaction impact: How it affects citizen happiness
- Buildings provide services and housing for families
- Commercial and industrial buildings provide jobs
- Schools and hospitals improve service quality and satisfaction
- Parks increase satisfaction
- Water plants and power plants provide essential utilities

## Building Capacities
- Residential: Houses 25 families per building
- Commercial: Provides 15 jobs per building
- Industrial: Provides 10 jobs per building
- School: Serves up to 50 families per building
- Hospital: Serves up to 60 families per building
- Water Plant: Serves up to 75 families per building
- Power Plant: Serves up to 100 families per building

## Capacity Penalties
- Families experience penalties when service capacity is exceeded:
  - Education shortage: Up to -25 satisfaction penalty
  - Healthcare shortage: Up to -30 satisfaction penalty
  - Utility shortage (water/power): Up to -40 satisfaction penalty
- Service shortages also affect income:
  - Education quality affects family income (up to 25% bonus)
  - Utility shortages reduce productivity (up to 30% income penalty)
  - Job shortages reduce income (up to 40% penalty)
- Housing constraints:
  - Population growth is capped by available housing
  - Overcrowding (>90% housing capacity) reduces new family arrivals
  - Overcrowding increases family departures
  - Families cannot exceed housing capacity

## Economics & Taxes
- Two types of taxes are collected daily:
  - Income tax: Applied to family income (adjustable rate, 0-40%)
  - VAT: Applied to family spending (adjustable rate, 0-25%)
- Daily expenses include:
  - Building upkeep costs (scales with city size and usage)
  - City services costs (scales with population)
  - Utility operation costs (scales with usage)
- Higher taxes increase income but decrease satisfaction
- Tax rates can be adjusted to balance budget and citizen happiness
- Tax rates are configurable via config.yml and persist throughout the game
- Changes to tax rates immediately affect daily calculations
- Building maintenance costs scale with:
  - City size (larger cities have higher maintenance costs)
  - Service usage (higher usage means higher operational costs)
- Family income scales with:
  - Job quality (commercial vs. industrial ratio)
  - Education quality
  - City size

## Family Dynamics
- Families arrive based on several factors:
  - City satisfaction level
  - Available housing
  - Available jobs
  - Service quality (schools, hospitals)
  - Tax rates
- Families may leave if satisfaction is too low
- Population is limited by available housing capacity

## Random Events
- Events can occur randomly with a 5% chance each day
- Negative events:
  - Fire: Damages buildings and costs money to repair
  - Epidemic: Affects families and costs healthcare expenses
  - Economic Crisis: Reduces city budget
- Positive events:
  - Grant: Provides a financial boost to the city budget
- Events impact budget and satisfaction
- Some buildings (like hospitals) can mitigate negative event effects
- All events are logged for reference

## Pause & Resume Functionality
- The game automatically pauses when any command is entered
- The game can also be paused explicitly using the 'pause' command
- When paused:
  - City data does not update
  - Random events do not trigger
  - Time does not progress
  - The UI displays "*** GAME PAUSED ***" to indicate the paused state
- All commands remain available during pause, allowing players to:
  - Check statistics and logs
  - Save the game
  - Plan their next moves
- The game can be resumed using the 'continue' or 'resume' command
- The game automatically pauses briefly during loading to ensure data consistency
- Pause state is clearly indicated in the UI to avoid confusion
- Commands that don't make sense during pause still work but have no effect until resumed

## Save & Load Functionality
- The game state can be saved to a file and loaded later
- Saved games are stored in JSON format in a 'saves' directory
- The entire city state is preserved, including:
  - Day count
  - Population (families)
  - Budget
  - Satisfaction level
  - Tax rates
  - Buildings
  - Event log
- When loading a game, the simulation pauses briefly during the loading process
- After loading, the game continues from the exact state it was saved in

## Commands
The game supports the following commands:

### Game Management Commands
1. `build <building_type>` - Constructs a new building of the specified type
   - Available building types: RESIDENTIAL, COMMERCIAL, INDUSTRIAL, PARK, SCHOOL, HOSPITAL, WATER_PLANT, POWER_PLANT
   - Shows detailed information about the building cost and upkeep
2. `tax set <income|vat> <rate>` - Sets tax rates (percentage)
   - `tax set income <rate>` - Sets income tax rate (0-40%)
   - `tax set vat <rate>` - Sets VAT rate (0-25%)
   - Provides feedback about the change and potential effects
3. `stats` - Displays detailed city statistics with formatted tables
   - Shows city info, taxes, buildings, capacities, and recent events
   - Uses color-coding (if enabled) to highlight important information


### Highscore Commands
9. `highscore` - Displays the highscore table
   - Shows the top 10 scores with player names and city statistics
   - Indicates the current game's potential rank
   - Shows sandbox mode indicator when applicable

### Save & Load Commands
10. `save <filename>` - Saves the current game state to a file
    - Example: `save mygame` saves the game to `saves/mygame.json`
    - Provides clear feedback about the save operation
11. `load <filename>` - Loads a game state from a file
    - Example: `load mygame` loads the game from `saves/mygame.json`
    - Shows city statistics after loading

### Interface Commands
12. `pause` - Pauses the game simulation
    - Stops city data updates and random events
    - Shows "*** GAME PAUSED ***" in the UI
    - All commands remain available during pause
13. `resume` - Resumes the game simulation after it has been paused
    - Continues city data updates and random events
14. `help` - Displays general help information about all commands
15. `help <command>` - Displays detailed help for a specific command
    - Includes usage, description, examples, and parameters
16. `colors <on|off>` - Enables or disables colored output
    - Makes the interface more readable with color-coded information
17. `exit` - Exits the game

## Stats Display
The stats command shows:
- Basic city info (day, population, budget, satisfaction)
- Tax rates (income tax and VAT)
- Building counts by type
- Current capacities (housing, jobs)
- Recent events log

## Game Summary Display
When the game ends, a summary is displayed showing:
- Final statistics (days survived, population, budget, satisfaction)
- Final score and highscore rank
- Buildings constructed (counts by type)
- Notable events that occurred during the game
- Prompt to enter name for highscore table (if score qualifies)

## Difficulty Scaling
- The game becomes progressively harder as the city grows:
  - Medium cities (50+ families): 
    - 5% income reduction
    - 5% expense increase
    - Higher event probabilities
  - Large cities (100+ families): 
    - 10% income reduction
    - 10% expense increase
    - Even higher event probabilities
  - Very large cities (200+ families): 
    - 15% income reduction
    - 15% expense increase
    - Maximum event probabilities
- Scaling factors are applied automatically based on city size
- Players receive notifications about difficulty scaling

## User Interface Features
- Real-time display system:
  - Automatically refreshes the screen with current city stats (every 1 second by default)
  - Critical status changes are highlighted in real-time
  - Input prompt remains responsive during updates
  - Game pause/resume functionality to stop simulation updates while maintaining UI responsiveness
  - Clear "*** GAME PAUSED ***" indicator when the game is paused
  - Cross-platform compatibility with fallback options for terminals without ANSI support
- Console output is formatted for clarity and readability:
  - Tables with borders for structured data presentation
  - Section headers with clear visual separation
  - Consistent formatting throughout the interface
  - Optional ANSI color support for enhanced readability
- Color-coded information (when enabled):
  - Success messages in green
  - Warnings in yellow
  - Errors in red
  - Important information in blue
- Comprehensive help system:
  - General help overview of all commands
  - Detailed help for each specific command
  - Examples and usage information
  - Parameter descriptions
- Improved error handling:
  - Consistent error message format
  - Clear indication of the problem
  - Suggestions for correct usage
  - Visual distinction of error messages

## Game Progression
- The city starts with a small population and budget
- Player must balance building construction, taxes, and citizen satisfaction
- Random events add challenge and unpredictability
- Game continues until player exits or city goes bankrupt/abandoned
- As the city grows, managing services and balancing the budget becomes more challenging
- Players must plan carefully to ensure sufficient capacity for all services
- Game modes affect progression:
  - Normal mode: Standard progression with game-over conditions
  - Sandbox mode: Unlimited play with no game-over conditions

## End Game Conditions
- The game ends when one of the following conditions is met:
  - City goes bankrupt (budget < 0)
  - City is abandoned (families = 0)
  - Player manually exits the game
- When the game ends, a summary is displayed showing:
  - Final statistics (days survived, population, budget, satisfaction)
  - Final score
  - Buildings constructed
  - Notable events that occurred
- In sandbox mode, these conditions are monitored but do not end the game

## Ranking/Highscore System
- Each game receives a score based on the formula:
  - Score = (families × 10) + (budget ÷ 10) + (satisfaction × 5) + (days × 2)
- When a game ends, the score is compared with previous highscores
- If the score ranks in the top 10, the player can enter their name for the highscore table
- Highscores are stored in a text file and persist between game sessions
- The highscore table shows:
  - Player name
  - Score
  - City statistics (population, budget, satisfaction, days)
  - Date and time achieved
- Highscores are not recorded in sandbox mode

## Sandbox/Free Play Mode
- Sandbox mode can be enabled in the config.yml file
- In sandbox mode:
  - The city starts with more families (20) and a larger budget ($10,000)
  - Game-over conditions (bankruptcy, abandonment) are monitored but not enforced
  - Scores are calculated but not recorded in the highscore table
  - UI clearly indicates when sandbox mode is active
- Sandbox mode is ideal for:
  - Learning the game mechanics
  - Experimenting with different strategies
  - Testing extreme scenarios
  - Casual play without pressure
