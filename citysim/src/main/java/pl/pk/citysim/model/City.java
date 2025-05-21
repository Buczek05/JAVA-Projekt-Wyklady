package pl.pk.citysim.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * Represents the city in the simulation.
 */
public class City {
    private int day;
    private int families;
    private int budget;
    private int satisfaction;
    private double taxRate;
    private double vatRate;
    private final List<Building> buildings;
    private final Map<BuildingType, Integer> buildingCounts;
    private final List<String> eventLog;

    /**
     * Creates a new city with initial values.
     *
     * @param initialFamilies Initial number of families
     * @param initialBudget Initial budget
     */
    public City(int initialFamilies, int initialBudget) {
        this.day = 1;
        this.families = initialFamilies;
        this.budget = initialBudget;
        this.satisfaction = 50; // Start with neutral satisfaction
        this.taxRate = 0.10; // 10% default income tax rate
        this.vatRate = 0.05; // 5% default VAT rate
        this.buildings = new ArrayList<>();
        this.buildingCounts = new HashMap<>();
        this.eventLog = new ArrayList<>();

        // Initialize building counts
        for (BuildingType type : BuildingType.values()) {
            buildingCounts.put(type, 0);
        }

        // Add initial log entry
        eventLog.add("Day 1: City founded with " + initialFamilies + " families and $" + initialBudget + " budget.");
    }

    /**
     * Advances the city simulation by one day.
     */
    public void nextDay() {
        day++;
        calculateDailyIncome();
        calculateDailyExpenses();
        checkRandomEvents();
        updateSatisfaction();
        updatePopulation();
    }

    /**
     * Checks for random events that might occur.
     */
    private void checkRandomEvents() {
        // Random events occur with a certain probability
        Random random = new Random();

        // Base chance for any event to occur (5%)
        double eventChance = 0.05;

        // Scale event chance with city size (larger cities have more events)
        if (families > 50) {
            eventChance = 0.07; // 7% for medium cities
        }
        if (families > 100) {
            eventChance = 0.10; // 10% for large cities
        }

        // Scale event chance with service quality (poor services = more negative events)
        int educationCapacity = 0;
        int healthcareCapacity = 0;
        int waterCapacity = 0;
        int powerCapacity = 0;

        for (Building building : buildings) {
            if (building.getType() == BuildingType.SCHOOL) {
                educationCapacity += building.getType().getEducationCapacity();
            } else if (building.getType() == BuildingType.HOSPITAL) {
                healthcareCapacity += building.getType().getHealthcareCapacity();
            } else if (building.getType() == BuildingType.WATER_PLANT) {
                waterCapacity += building.getType().getUtilityCapacity();
            } else if (building.getType() == BuildingType.POWER_PLANT) {
                powerCapacity += building.getType().getUtilityCapacity();
            }
        }

        // Calculate service ratios
        double educationRatio = families > 0 ? Math.min(1.0, (double) educationCapacity / families) : 1.0;
        double healthcareRatio = families > 0 ? Math.min(1.0, (double) healthcareCapacity / families) : 1.0;
        double waterRatio = families > 0 ? Math.min(1.0, (double) waterCapacity / families) : 1.0;
        double powerRatio = families > 0 ? Math.min(1.0, (double) powerCapacity / families) : 1.0;

        // Overall service quality (0.0 to 1.0)
        double serviceRatio = (educationRatio + healthcareRatio + waterRatio + powerRatio) / 4.0;

        // Poor services increase negative event chance
        if (serviceRatio < 0.7) {
            eventChance += (0.7 - serviceRatio) * 0.1; // Up to +7% for very poor services
        }

        // Check if an event occurs
        if (random.nextDouble() < eventChance) {
            // Choose a random event type
            Event[] events = Event.values();

            // Adjust event probabilities based on city conditions
            double negativeEventChance = 0.75; // Default 75% chance of negative event

            // Poor services increase negative event chance
            if (serviceRatio < 0.7) {
                negativeEventChance += (0.7 - serviceRatio) * 0.3; // Up to +21% for very poor services
            }

            // Cap at 95% chance for negative events
            negativeEventChance = Math.min(0.95, negativeEventChance);

            // Determine if it's a positive or negative event
            Event event;
            if (random.nextDouble() < negativeEventChance) {
                // Choose a negative event (FIRE, EPIDEMIC, ECONOMIC_CRISIS)
                int eventIndex = random.nextInt(3); // 0, 1, or 2
                event = events[eventIndex];
            } else {
                // Positive event (GRANT)
                event = Event.GRANT;
            }

            // Handle the event
            switch (event) {
                case FIRE:
                    handleFireEvent(random);
                    break;
                case EPIDEMIC:
                    handleEpidemicEvent(random);
                    break;
                case ECONOMIC_CRISIS:
                    handleEconomicCrisisEvent(random);
                    break;
                case GRANT:
                    handleGrantEvent(random);
                    break;
            }
        }
    }

    /**
     * Handles a fire event in the city.
     * 
     * @param random Random number generator
     */
    private void handleFireEvent(Random random) {
        // Only possible if there are buildings
        if (buildings.isEmpty()) {
            return;
        }

        // Select a random building
        int buildingIndex = random.nextInt(buildings.size());
        Building affectedBuilding = buildings.get(buildingIndex);

        // Calculate damage (25-75% of building value)
        int buildingValue = affectedBuilding.getUpkeep() * 10;
        int damage = buildingValue * (25 + random.nextInt(51)) / 100;

        // Scale damage with city size (larger cities have more severe fires)
        if (families > 50) {
            damage = (int)(damage * 1.2); // 20% more damage for medium cities
        }
        if (families > 100) {
            damage = (int)(damage * 1.5); // 50% more damage for large cities
        }

        // Check if there are water plants to mitigate the damage
        int waterPlantCount = buildingCounts.getOrDefault(BuildingType.WATER_PLANT, 0);
        if (waterPlantCount > 0) {
            // Calculate water capacity
            int waterCapacity = 0;
            for (Building building : buildings) {
                if (building.getType() == BuildingType.WATER_PLANT) {
                    waterCapacity += building.getType().getUtilityCapacity();
                }
            }

            // Calculate water ratio
            double waterRatio = families > 0 ? Math.min(1.0, (double) waterCapacity / families) : 1.0;

            // Reduce damage based on water capacity (up to 50% reduction)
            int damageReduction = (int)(damage * waterRatio * 0.5);
            damage -= damageReduction;

            eventLog.add(String.format("Day %d: FIRE! A %s caught fire. Water system helped reduce damage by $%d.", 
                    day, affectedBuilding.getType().getName(), damageReduction));
        }

        // Apply effects
        budget -= damage; // Repair costs

        // Satisfaction impact scales with city size and damage relative to budget
        int satisfactionImpact = 5; // Base impact
        double damageToBudgetRatio = (double) damage / budget;

        if (damageToBudgetRatio > 0.1) {
            satisfactionImpact += 3; // +3 for significant damage
        }
        if (damageToBudgetRatio > 0.2) {
            satisfactionImpact += 5; // +5 more for severe damage
        }

        satisfaction -= satisfactionImpact;

        // Log the event
        eventLog.add(String.format("Day %d: FIRE! A %s caught fire, causing $%d in damages.", 
                day, affectedBuilding.getType().getName(), damage));
    }

    /**
     * Handles an epidemic event in the city.
     * 
     * @param random Random number generator
     */
    private void handleEpidemicEvent(Random random) {
        // Only possible if there are families
        if (families <= 0) {
            return;
        }

        // Calculate affected families (10-30% of population)
        int baseAffectedPercentage = 10 + random.nextInt(21);

        // Scale affected percentage with city size (larger cities have more severe epidemics)
        if (families > 50) {
            baseAffectedPercentage += 5; // +5% for medium cities
        }
        if (families > 100) {
            baseAffectedPercentage += 10; // +10% more for large cities
        }

        // Cap at 50% maximum affected
        baseAffectedPercentage = Math.min(50, baseAffectedPercentage);

        int affectedFamilies = families * baseAffectedPercentage / 100;

        // Calculate healthcare costs (scales with city size)
        int baseCostPerFamily = 20;
        if (families > 50) {
            baseCostPerFamily = 25; // Higher costs for medium cities
        }
        if (families > 100) {
            baseCostPerFamily = 30; // Even higher costs for large cities
        }

        int healthcareCosts = affectedFamilies * baseCostPerFamily;

        // Calculate hospital capacity and effectiveness
        int hospitalCapacity = 0;
        for (Building building : buildings) {
            if (building.getType() == BuildingType.HOSPITAL) {
                hospitalCapacity += building.getType().getHealthcareCapacity();
            }
        }

        // Calculate healthcare ratio
        double healthcareRatio = families > 0 ? Math.min(1.0, (double) hospitalCapacity / families) : 1.0;

        // Apply effects
        int costReduction = 0;
        int satisfactionImpact = 10; // Base impact

        // Hospitals reduce costs and satisfaction impact
        if (hospitalCapacity > 0) {
            // Cost reduction based on hospital capacity (up to 60% reduction)
            costReduction = (int)(healthcareCosts * healthcareRatio * 0.6);
            healthcareCosts -= costReduction;

            // Satisfaction impact reduction (up to 70% reduction)
            satisfactionImpact = (int)(satisfactionImpact * (1 - healthcareRatio * 0.7));
        }

        // Scale satisfaction impact with city size and affected percentage
        double severityFactor = (double) affectedFamilies / families;
        if (severityFactor > 0.3) {
            satisfactionImpact += 5; // +5 for severe epidemics
        }
        if (severityFactor > 0.4) {
            satisfactionImpact += 5; // +5 more for very severe epidemics
        }

        // Apply budget and satisfaction effects
        budget -= healthcareCosts;
        satisfaction -= satisfactionImpact;

        // Log the event with appropriate message
        if (hospitalCapacity > 0) {
            eventLog.add(String.format("Day %d: EPIDEMIC! %d families affected. Hospitals reduced costs by $%d. Total cost: $%d.", 
                    day, affectedFamilies, costReduction, healthcareCosts));
        } else {
            eventLog.add(String.format("Day %d: EPIDEMIC! %d families affected, costing $%d. No hospitals to help!", 
                    day, affectedFamilies, healthcareCosts));
        }
    }

    /**
     * Handles an economic crisis event in the city.
     * 
     * @param random Random number generator
     */
    private void handleEconomicCrisisEvent(Random random) {
        // Base economic impact (5-15% of budget)
        int baseImpactPercentage = 5 + random.nextInt(11);

        // Scale impact with city size (larger cities have more severe economic crises)
        if (families > 50) {
            baseImpactPercentage += 3; // +3% for medium cities
        }
        if (families > 100) {
            baseImpactPercentage += 5; // +5% more for large cities
        }

        // Scale impact with commercial/industrial ratio (more commercial = less severe)
        int commercialCount = buildingCounts.getOrDefault(BuildingType.COMMERCIAL, 0);
        int industrialCount = buildingCounts.getOrDefault(BuildingType.INDUSTRIAL, 0);

        // Calculate job diversity ratio
        double commercialRatio = 0.5; // Default balanced ratio
        int totalJobBuildings = commercialCount + industrialCount;
        if (totalJobBuildings > 0) {
            commercialRatio = (double) commercialCount / totalJobBuildings;
        }

        // Adjust impact based on commercial ratio (balanced economy is more resilient)
        if (Math.abs(commercialRatio - 0.5) < 0.2) {
            // Well-balanced economy (30-70% commercial) reduces impact
            baseImpactPercentage -= 2;
        } else if (commercialRatio < 0.3 || commercialRatio > 0.7) {
            // Poorly balanced economy increases impact
            baseImpactPercentage += 3;
        }

        // Ensure impact is at least 3% and at most 25%
        baseImpactPercentage = Math.max(3, Math.min(25, baseImpactPercentage));

        // Calculate final economic impact
        int economicImpact = budget * baseImpactPercentage / 100;

        // Calculate satisfaction impact (scales with impact severity)
        int satisfactionImpact = 8; // Base impact

        // Scale satisfaction impact with severity
        double impactRatio = (double) economicImpact / budget;
        if (impactRatio > 0.15) {
            satisfactionImpact += 4; // +4 for severe economic impact
        }
        if (impactRatio > 0.2) {
            satisfactionImpact += 5; // +5 more for very severe economic impact
        }

        // Apply effects
        budget -= economicImpact; // Economic loss
        satisfaction -= satisfactionImpact; // Reduced satisfaction

        // Log the event with severity indicator
        String severityIndicator = "";
        if (impactRatio > 0.2) {
            severityIndicator = "SEVERE ";
        } else if (impactRatio > 0.15) {
            severityIndicator = "MAJOR ";
        }

        eventLog.add(String.format("Day %d: %sECONOMIC CRISIS! The city lost $%d (%.1f%% of budget) due to market instability.", 
                day, severityIndicator, economicImpact, impactRatio * 100));
    }

    /**
     * Handles a grant event in the city.
     * 
     * @param random Random number generator
     */
    private void handleGrantEvent(Random random) {
        // Base grant amount (10-20% of budget, minimum $100)
        int baseGrantPercentage = 10 + random.nextInt(11);

        // Scale grant with city size (larger cities get relatively smaller grants)
        if (families > 50) {
            baseGrantPercentage -= 2; // -2% for medium cities
        }
        if (families > 100) {
            baseGrantPercentage -= 3; // -3% more for large cities
        }

        // Scale grant with satisfaction (cities with lower satisfaction get more aid)
        if (satisfaction < 40) {
            baseGrantPercentage += 5; // +5% for unhappy cities
        }

        // Ensure grant percentage is at least 5% and at most 25%
        baseGrantPercentage = Math.max(5, Math.min(25, baseGrantPercentage));

        // Calculate final grant amount with minimum value
        int grantAmount = Math.max(100, budget * baseGrantPercentage / 100);

        // Calculate satisfaction impact (scales with grant size)
        int satisfactionImpact = 5; // Base impact

        // Scale satisfaction impact with grant size relative to budget
        double grantRatio = (double) grantAmount / budget;
        if (grantRatio > 0.15) {
            satisfactionImpact += 2; // +2 for significant grants
        }
        if (grantRatio > 0.2) {
            satisfactionImpact += 3; // +3 more for very large grants
        }

        // Apply effects
        budget += grantAmount; // Financial gain
        satisfaction += satisfactionImpact; // Increased satisfaction

        // Log the event with size indicator
        String sizeIndicator = "";
        if (grantRatio > 0.2) {
            sizeIndicator = "LARGE ";
        } else if (grantRatio > 0.15) {
            sizeIndicator = "SIGNIFICANT ";
        }

        eventLog.add(String.format("Day %d: %sGRANT! The city received a $%d grant (%.1f%% of budget) from the government.", 
                day, sizeIndicator, grantAmount, grantRatio * 100));
    }

    /**
     * Adds a new building to the city.
     *
     * @param type The type of building to add
     * @return The newly created building
     */
    public Building addBuilding(BuildingType type) {
        int id = buildings.size() + 1;
        Building building = new Building(id, type);
        buildings.add(building);

        // Update building count
        int count = buildingCounts.getOrDefault(type, 0);
        buildingCounts.put(type, count + 1);

        // Deduct building cost from budget
        budget -= type.getUpkeep() * 10; // Initial cost is 10x the upkeep

        return building;
    }

    /**
     * Calculates daily income from taxes.
     */
    private void calculateDailyIncome() {
        // Base family income scales with city development
        int baseFamilyIncome = 50;

        // Income bonus from commercial buildings
        int commercialCount = buildingCounts.getOrDefault(BuildingType.COMMERCIAL, 0);
        int industrialCount = buildingCounts.getOrDefault(BuildingType.INDUSTRIAL, 0);

        // Calculate job quality ratio (commercial jobs are better quality than industrial)
        double jobQualityRatio = 0.0;
        int totalJobBuildings = commercialCount + industrialCount;
        if (totalJobBuildings > 0) {
            jobQualityRatio = (double) commercialCount / totalJobBuildings;
        }

        // Apply job quality bonus (up to 30% more income for high commercial ratio)
        baseFamilyIncome += (int) (baseFamilyIncome * jobQualityRatio * 0.3);

        // Calculate available jobs
        int commercialJobs = 0;
        int industrialJobs = 0;
        for (Building building : buildings) {
            if (building.getType() == BuildingType.COMMERCIAL) {
                commercialJobs += building.getCapacity();
            } else if (building.getType() == BuildingType.INDUSTRIAL) {
                industrialJobs += building.getCapacity();
            }
        }
        int totalJobs = commercialJobs + industrialJobs;

        // Job shortage penalty (not enough jobs reduces income)
        double jobRatio = families > 0 ? Math.min(1.0, (double) totalJobs / families) : 1.0;
        if (jobRatio < 1.0) {
            int jobShortagePenalty = (int) (baseFamilyIncome * (1 - jobRatio) * 0.4); // Up to 40% penalty
            baseFamilyIncome -= jobShortagePenalty;
            eventLog.add(String.format("Day %d: Job shortage (%.1f%% coverage) reducing family income by $%d", 
                    day, jobRatio * 100, jobShortagePenalty));
        }

        // Scale income with city size (larger cities have higher incomes but also higher costs)
        // This creates a difficulty curve - early game is easier, late game is harder
        if (families > 50) {
            baseFamilyIncome += 15; // +15 for medium cities (was +10)
        }
        if (families > 100) {
            baseFamilyIncome += 20; // +20 more for large cities (was +15)
        }
        if (families > 200) {
            baseFamilyIncome += 25; // +25 more for very large cities (new tier)
        }

        // Progressive difficulty scaling - income grows slower as city gets larger
        double difficultyScaling = 1.0;
        if (families > 50) {
            difficultyScaling = 0.95; // 5% income reduction for medium cities
        }
        if (families > 100) {
            difficultyScaling = 0.9; // 10% income reduction for large cities
        }
        if (families > 200) {
            difficultyScaling = 0.85; // 15% income reduction for very large cities
        }

        // Apply difficulty scaling
        baseFamilyIncome = (int)(baseFamilyIncome * difficultyScaling);

        // Log difficulty scaling if it's applied
        if (difficultyScaling < 1.0) {
            eventLog.add(String.format("Day %d: City size difficulty scaling applied (%.0f%% income efficiency)", 
                    day, difficultyScaling * 100));
        }

        // Calculate service quality impact on income
        int educationCapacity = 0;
        for (Building building : buildings) {
            if (building.getType() == BuildingType.SCHOOL) {
                educationCapacity += building.getType().getEducationCapacity();
            }
        }

        // Education bonus (better education = higher income)
        double educationRatio = families > 0 ? Math.min(1.0, (double) educationCapacity / families) : 1.0;
        int educationBonus = (int) (baseFamilyIncome * educationRatio * 0.25); // Up to 25% bonus (was 20%)
        baseFamilyIncome += educationBonus;

        // Utility quality impact on income (poor utilities reduce productivity)
        int waterCapacity = 0;
        int powerCapacity = 0;
        for (Building building : buildings) {
            if (building.getType() == BuildingType.WATER_PLANT) {
                waterCapacity += building.getType().getUtilityCapacity();
            } else if (building.getType() == BuildingType.POWER_PLANT) {
                powerCapacity += building.getType().getUtilityCapacity();
            }
        }

        double waterRatio = families > 0 ? Math.min(1.0, (double) waterCapacity / families) : 1.0;
        double powerRatio = families > 0 ? Math.min(1.0, (double) powerCapacity / families) : 1.0;

        // Utility shortage penalty
        if (waterRatio < 0.8 || powerRatio < 0.8) {
            double worstUtilityRatio = Math.min(waterRatio, powerRatio);
            int utilityPenalty = (int) (baseFamilyIncome * (1 - worstUtilityRatio) * 0.3); // Up to 30% penalty
            baseFamilyIncome -= utilityPenalty;
            eventLog.add(String.format("Day %d: Utility shortage reducing family income by $%d", 
                    day, utilityPenalty));
        }

        // Income tax calculation (based on family income)
        int incomeTaxRevenue = (int) (families * baseFamilyIncome * taxRate);

        // Daily spending per family (scales with income)
        int dailySpendingPerFamily = (int) (baseFamilyIncome * 0.25); // Increased from 0.2 to 0.25

        // Spending increases with satisfaction (happier citizens spend more)
        double satisfactionMultiplier = 0.7 + (satisfaction * 0.006); // 0.7 to 1.3 based on satisfaction (wider range)
        dailySpendingPerFamily = (int) (dailySpendingPerFamily * satisfactionMultiplier);

        // VAT calculation (based on family spending)
        int vatRevenue = (int) (families * dailySpendingPerFamily * vatRate);

        // Total tax revenue
        int totalTaxRevenue = incomeTaxRevenue + vatRevenue;

        // Add to budget
        budget += totalTaxRevenue;

        // Log tax collection with more details
        eventLog.add(String.format("Day %d: Average family income: $%d, spending: $%d", 
                day, baseFamilyIncome, dailySpendingPerFamily));
        eventLog.add(String.format("Day %d: Collected $%d in income tax and $%d in VAT.", 
                day, incomeTaxRevenue, vatRevenue));
    }

    /**
     * Calculates daily expenses for building upkeep and city services.
     */
    private void calculateDailyExpenses() {
        // Building upkeep expenses (scales with building age and city size)
        int buildingUpkeep = 0;
        Map<BuildingType, Integer> upkeepByType = new HashMap<>();

        // Initialize upkeep by type
        for (BuildingType type : BuildingType.values()) {
            upkeepByType.put(type, 0);
        }

        // Scale factor based on city size (larger cities have higher maintenance costs)
        double scaleFactor = 1.0;
        if (families > 50) {
            scaleFactor = 1.15; // 15% increase for medium cities (was 10%)
        }
        if (families > 100) {
            scaleFactor = 1.3; // 30% increase for large cities (was 20%)
        }
        if (families > 200) {
            scaleFactor = 1.5; // 50% increase for very large cities (new tier)
        }

        // Progressive difficulty scaling - expenses grow faster as city gets larger
        double difficultyScaling = 1.0;
        if (families > 50) {
            difficultyScaling = 1.05; // 5% expense increase for medium cities
        }
        if (families > 100) {
            difficultyScaling = 1.1; // 10% expense increase for large cities
        }
        if (families > 200) {
            difficultyScaling = 1.15; // 15% expense increase for very large cities
        }

        // Apply difficulty scaling to scale factor
        scaleFactor *= difficultyScaling;

        // Log difficulty scaling if it's applied
        if (difficultyScaling > 1.0) {
            eventLog.add(String.format("Day %d: City size difficulty scaling applied (%.0f%% expense increase)", 
                    day, (difficultyScaling - 1.0) * 100));
        }

        // Calculate upkeep with scaling and usage-based costs
        for (Building building : buildings) {
            BuildingType type = building.getType();
            int baseUpkeep = building.getUpkeep();

            // Apply scale factor
            int scaledUpkeep = (int) (baseUpkeep * scaleFactor);

            // Apply usage-based scaling for service buildings
            if (type == BuildingType.SCHOOL || type == BuildingType.HOSPITAL || 
                type == BuildingType.WATER_PLANT || type == BuildingType.POWER_PLANT) {

                // Calculate usage ratio
                double usageRatio = 0.0;
                if (type == BuildingType.SCHOOL && families > 0) {
                    usageRatio = Math.min(1.0, (double) families / type.getEducationCapacity());
                } else if (type == BuildingType.HOSPITAL && families > 0) {
                    usageRatio = Math.min(1.0, (double) families / type.getHealthcareCapacity());
                } else if ((type == BuildingType.WATER_PLANT || type == BuildingType.POWER_PLANT) && families > 0) {
                    usageRatio = Math.min(1.0, (double) families / type.getUtilityCapacity());
                }

                // Higher usage means higher operational costs (50% base cost + up to 100% more at full capacity)
                double usageMultiplier = 0.5 + (usageRatio * 1.0);
                scaledUpkeep = (int) (scaledUpkeep * usageMultiplier);
            }

            // Add to total upkeep
            buildingUpkeep += scaledUpkeep;

            // Track upkeep by building type
            upkeepByType.put(type, upkeepByType.get(type) + scaledUpkeep);
        }

        // City services expenses (base cost + per family cost)
        int baseCityServicesCost = 15; // Base cost per day (increased from 10)

        // Per family cost scales with city size (larger cities have higher per-capita costs)
        int perFamilyCost = 3; // Base cost per family (increased from 2)
        if (families > 50) {
            perFamilyCost = 4; // Higher cost for medium cities (increased from 3)
        }
        if (families > 100) {
            perFamilyCost = 6; // Even higher cost for large cities (increased from 4)
        }
        if (families > 200) {
            perFamilyCost = 8; // Highest cost for very large cities (new tier)
        }

        // Calculate city services cost
        int cityServicesCost = baseCityServicesCost + (families * perFamilyCost);

        // Additional costs for utility services (water and power) based on usage
        int waterCapacity = 0;
        int powerCapacity = 0;
        for (Building building : buildings) {
            if (building.getType() == BuildingType.WATER_PLANT) {
                waterCapacity += building.getType().getUtilityCapacity();
            } else if (building.getType() == BuildingType.POWER_PLANT) {
                powerCapacity += building.getType().getUtilityCapacity();
            }
        }

        // Calculate usage ratios
        double waterUsageRatio = families > 0 && waterCapacity > 0 ? 
                Math.min(1.0, (double) families / waterCapacity) : 0.0;
        double powerUsageRatio = families > 0 && powerCapacity > 0 ? 
                Math.min(1.0, (double) families / powerCapacity) : 0.0;

        // Utility operation costs (scales with family count and usage)
        int utilityOperationCost = 0;
        int waterPlantCount = buildingCounts.getOrDefault(BuildingType.WATER_PLANT, 0);
        int powerPlantCount = buildingCounts.getOrDefault(BuildingType.POWER_PLANT, 0);

        if (waterPlantCount > 0) {
            // Base cost + usage-based cost
            int waterCost = 8 + (int)(families * waterUsageRatio * 0.3);
            utilityOperationCost += waterCost;
        }

        if (powerPlantCount > 0) {
            // Base cost + usage-based cost
            int powerCost = 12 + (int)(families * powerUsageRatio * 0.4);
            utilityOperationCost += powerCost;
        }

        // Total expenses
        int totalExpenses = buildingUpkeep + cityServicesCost + utilityOperationCost;

        // Deduct from budget
        budget -= totalExpenses;

        // Log expenses with more detail
        StringBuilder expenseLog = new StringBuilder();
        expenseLog.append(String.format("Day %d: Expenses breakdown:\n", day));
        expenseLog.append(String.format("- Building upkeep: $%d\n", buildingUpkeep));

        // Log upkeep by building type if there are buildings
        if (!buildings.isEmpty()) {
            for (BuildingType type : BuildingType.values()) {
                int typeUpkeep = upkeepByType.get(type);
                if (typeUpkeep > 0) {
                    expenseLog.append(String.format("  - %s: $%d\n", type.getName(), typeUpkeep));
                }
            }
        }

        expenseLog.append(String.format("- City services: $%d\n", cityServicesCost));

        if (utilityOperationCost > 0) {
            expenseLog.append(String.format("- Utility operations: $%d\n", utilityOperationCost));
        }

        expenseLog.append(String.format("Total daily expenses: $%d", totalExpenses));

        // Add to event log
        eventLog.add(expenseLog.toString());
    }

    /**
     * Updates the city's satisfaction level based on buildings, tax rates, and services.
     */
    private void updateSatisfaction() {
        // Base satisfaction change
        int satisfactionChange = 0;

        // Building effects
        for (Building building : buildings) {
            satisfactionChange += building.getSatisfactionImpact();
        }

        // Tax effects (higher taxes reduce satisfaction)
        // Income tax has more impact on satisfaction than VAT
        satisfactionChange -= (int) (taxRate * 120); // Income tax impact
        satisfactionChange -= (int) (vatRate * 80);  // VAT impact

        // Calculate service capacities and penalties
        int educationCapacity = 0;
        int healthcareCapacity = 0;
        int waterCapacity = 0;
        int powerCapacity = 0;

        for (Building building : buildings) {
            if (building.getType() == BuildingType.SCHOOL) {
                educationCapacity += building.getType().getEducationCapacity();
            } else if (building.getType() == BuildingType.HOSPITAL) {
                healthcareCapacity += building.getType().getHealthcareCapacity();
            } else if (building.getType() == BuildingType.WATER_PLANT) {
                waterCapacity += building.getType().getUtilityCapacity();
            } else if (building.getType() == BuildingType.POWER_PLANT) {
                powerCapacity += building.getType().getUtilityCapacity();
            }
        }

        // Apply penalties for insufficient services
        int educationPenalty = 0;
        int healthcarePenalty = 0;
        int utilityPenalty = 0;

        // Education penalty
        if (educationCapacity < families) {
            double educationRatio = (double) educationCapacity / families;
            // Stronger penalty: up to -25 (was -15)
            educationPenalty = (int) ((1 - educationRatio) * 25);

            // Add severity level to warning
            String severityLevel = educationRatio < 0.5 ? "CRITICAL" : "WARNING";
            eventLog.add(String.format("Day %d: %s - Not enough schools! Education capacity: %d/%d families (%.1f%%). Satisfaction penalty: -%d", 
                    day, severityLevel, educationCapacity, families, educationRatio * 100, educationPenalty));
        }

        // Healthcare penalty
        if (healthcareCapacity < families) {
            double healthcareRatio = (double) healthcareCapacity / families;
            // Stronger penalty: up to -30 (was -20)
            healthcarePenalty = (int) ((1 - healthcareRatio) * 30);

            // Add severity level to warning
            String severityLevel = healthcareRatio < 0.5 ? "CRITICAL" : "WARNING";
            eventLog.add(String.format("Day %d: %s - Not enough hospitals! Healthcare capacity: %d/%d families (%.1f%%). Satisfaction penalty: -%d", 
                    day, severityLevel, healthcareCapacity, families, healthcareRatio * 100, healthcarePenalty));
        }

        // Utility penalty (water and power)
        if (waterCapacity < families || powerCapacity < families) {
            double waterRatio = (double) waterCapacity / families;
            double powerRatio = (double) powerCapacity / families;
            double worstUtilityRatio = Math.min(waterRatio, powerRatio);
            // Stronger penalty: up to -40 (was -25)
            utilityPenalty = (int) ((1 - worstUtilityRatio) * 40);

            if (waterCapacity < families) {
                String severityLevel = waterRatio < 0.6 ? "CRITICAL" : "WARNING";
                eventLog.add(String.format("Day %d: %s - Not enough water supply! Water capacity: %d/%d families (%.1f%%)", 
                        day, severityLevel, waterCapacity, families, waterRatio * 100));
            }

            if (powerCapacity < families) {
                String severityLevel = powerRatio < 0.6 ? "CRITICAL" : "WARNING";
                eventLog.add(String.format("Day %d: %s - Not enough power supply! Power capacity: %d/%d families (%.1f%%)", 
                        day, severityLevel, powerCapacity, families, powerRatio * 100));
            }

            eventLog.add(String.format("Day %d: Utility shortage causing a satisfaction penalty of -%d", 
                    day, utilityPenalty));
        }

        // Apply service penalties
        satisfactionChange -= (educationPenalty + healthcarePenalty + utilityPenalty);

        // Service quality effect (more buildings = better services, but only if capacity is sufficient)
        int serviceQuality = 0;
        if (educationCapacity >= families && healthcareCapacity >= families && 
            waterCapacity >= families && powerCapacity >= families) {
            serviceQuality = Math.min(buildings.size() * 2, 20); // Cap at +20
        } else {
            serviceQuality = Math.min(buildings.size(), 10); // Cap at +10 if services are insufficient
        }
        satisfactionChange += serviceQuality;

        // Apply change with limits
        satisfaction += satisfactionChange / 10;
        if (satisfaction > 100) {
            satisfaction = 100;
        } else if (satisfaction < 0) {
            satisfaction = 0;
        }

        // Log satisfaction change
        eventLog.add(String.format("Day %d: Satisfaction level is now %d%%.", day, satisfaction));
    }

    /**
     * Updates the city's population based on satisfaction, available housing, and other factors.
     */
    private void updatePopulation() {
        // Calculate available housing capacity
        int housingCapacity = 0;
        for (Building building : buildings) {
            if (building.getType() == BuildingType.RESIDENTIAL) {
                housingCapacity += building.getCapacity();
            }
        }

        // Calculate housing occupancy ratio
        double housingOccupancyRatio = housingCapacity > 0 ? (double) families / housingCapacity : 1.0;

        // Check for overcrowding
        boolean isOvercrowded = housingOccupancyRatio > 0.9;

        // Calculate available jobs (commercial and industrial buildings)
        int availableJobs = 0;
        for (Building building : buildings) {
            if (building.getType() == BuildingType.COMMERCIAL || 
                building.getType() == BuildingType.INDUSTRIAL) {
                availableJobs += building.getCapacity();
            }
        }

        // Calculate service capacities
        int educationCapacity = 0;
        int healthcareCapacity = 0;
        int waterCapacity = 0;
        int powerCapacity = 0;

        for (Building building : buildings) {
            if (building.getType() == BuildingType.SCHOOL) {
                educationCapacity += building.getType().getEducationCapacity();
            } else if (building.getType() == BuildingType.HOSPITAL) {
                healthcareCapacity += building.getType().getHealthcareCapacity();
            } else if (building.getType() == BuildingType.WATER_PLANT) {
                waterCapacity += building.getType().getUtilityCapacity();
            } else if (building.getType() == BuildingType.POWER_PLANT) {
                powerCapacity += building.getType().getUtilityCapacity();
            }
        }

        // Calculate service quality based on capacity ratios
        double educationRatio = families > 0 ? Math.min(1.0, (double) educationCapacity / families) : 1.0;
        double healthcareRatio = families > 0 ? Math.min(1.0, (double) healthcareCapacity / families) : 1.0;
        double waterRatio = families > 0 ? Math.min(1.0, (double) waterCapacity / families) : 1.0;
        double powerRatio = families > 0 ? Math.min(1.0, (double) powerCapacity / families) : 1.0;

        // Overall service quality (0.0 to 1.0)
        double serviceRatio = (educationRatio + healthcareRatio + waterRatio + powerRatio) / 4.0;
        int serviceQuality = (int) (serviceRatio * 30); // Up to +30 for perfect services

        // Calculate family arrival chance
        double arrivalChance = 0.1; // Base 10% chance

        // Bonuses
        if (satisfaction > 50) {
            arrivalChance += (satisfaction - 50) * 0.005; // Up to +25% for 100% satisfaction
        }

        if (availableJobs > families) {
            arrivalChance += 0.1; // +10% if jobs available
        }

        // Service quality bonus
        arrivalChance += serviceRatio * 0.2; // Up to +20% for perfect services

        // Penalties
        if (taxRate > 0.2) {
            arrivalChance -= (taxRate - 0.2) * 0.5; // Up to -40% for high taxes
        }

        if (vatRate > 0.1) {
            arrivalChance -= (vatRate - 0.1) * 0.3; // Up to -30% for high VAT
        }

        // Service penalties
        if (educationRatio < 0.5) {
            arrivalChance -= (0.5 - educationRatio) * 0.3; // Up to -15% for poor education
        }

        if (healthcareRatio < 0.5) {
            arrivalChance -= (0.5 - healthcareRatio) * 0.3; // Up to -15% for poor healthcare
        }

        if (waterRatio < 0.7 || powerRatio < 0.7) {
            arrivalChance -= 0.2; // -20% for critical utility shortage
        }

        // Housing constraints
        int availableHousing = Math.max(0, housingCapacity - families);

        if (availableHousing <= 0) {
            arrivalChance = 0; // No chance if no housing available
            eventLog.add(String.format("Day %d: WARNING - No available housing! New families cannot move in.", day));
        } else if (isOvercrowded) {
            // Reduce arrival chance when housing is nearly full (90%+ occupancy)
            double reductionFactor = (housingOccupancyRatio - 0.9) * 10; // 0 to 1 as occupancy goes from 90% to 100%
            arrivalChance *= (1 - reductionFactor);
            eventLog.add(String.format("Day %d: NOTICE - Housing nearly full (%.1f%% occupied). Fewer families moving in.", 
                    day, housingOccupancyRatio * 100));
        }

        // Cap growth based on available housing
        int maxNewFamilies = availableHousing;

        // Ensure chance is between 0 and 1
        arrivalChance = Math.max(0, Math.min(1, arrivalChance));

        // Random roll for new families
        Random random = new Random();
        int newFamilies = 0;

        // Try up to 5 times for new families, but respect the housing cap
        for (int i = 0; i < 5 && newFamilies < maxNewFamilies; i++) {
            if (random.nextDouble() < arrivalChance) {
                newFamilies++;
            }
        }

        // Handle family departures based on satisfaction and service quality
        int departures = 0;

        // Base departure chance based on satisfaction
        double baseDepartureChance = 0.0;
        if (satisfaction < 30) {
            baseDepartureChance = (30 - satisfaction) * 0.01; // Up to 30% chance for very low satisfaction
        }

        // Additional departure chance based on service shortages
        double serviceDepartureChance = 0.0;

        // Critical utility shortage (water or power)
        if (waterRatio < 0.5 || powerRatio < 0.5) {
            serviceDepartureChance += 0.15; // 15% chance due to critical utility shortage
            eventLog.add(String.format("Day %d: CRITICAL - Severe utility shortage causing families to leave!", day));
        }

        // Severe education shortage
        if (educationRatio < 0.4) {
            serviceDepartureChance += 0.1; // 10% chance due to education shortage
            eventLog.add(String.format("Day %d: CRITICAL - Severe education shortage causing families to leave!", day));
        }

        // Severe healthcare shortage
        if (healthcareRatio < 0.4) {
            serviceDepartureChance += 0.1; // 10% chance due to healthcare shortage
            eventLog.add(String.format("Day %d: CRITICAL - Severe healthcare shortage causing families to leave!", day));
        }

        // Overcrowding penalty - more families leave when housing is overcrowded
        if (isOvercrowded) {
            double overcrowdingFactor = (housingOccupancyRatio - 0.9) * 10; // 0 to 1 as occupancy goes from 90% to 100%
            double overcrowdingChance = 0.1 * overcrowdingFactor; // Up to 10% additional departure chance
            serviceDepartureChance += overcrowdingChance;
            eventLog.add(String.format("Day %d: WARNING - Housing overcrowding (%.1f%% occupied) causing families to leave!", 
                    day, housingOccupancyRatio * 100));
        }

        // Combined departure chance
        double departureChance = Math.min(0.5, baseDepartureChance + serviceDepartureChance); // Cap at 50%

        // Apply departure chance to each family
        for (int i = 0; i < families; i++) {
            if (random.nextDouble() < departureChance) {
                departures++;
            }
        }

        // Apply population changes
        int oldFamilies = families;
        families += newFamilies - departures;

        // Ensure within housing capacity
        if (families > housingCapacity) {
            int excess = families - housingCapacity;
            families = housingCapacity;
            eventLog.add(String.format("Day %d: CRITICAL - %d families couldn't find housing and left the city!", 
                    day, excess));
        }

        // Ensure non-negative
        if (families < 0) {
            families = 0;
        }

        // Log population changes
        if (newFamilies > 0) {
            eventLog.add(String.format("Day %d: %d new families moved to the city.", day, newFamilies));
        }

        if (departures > 0) {
            eventLog.add(String.format("Day %d: %d families left the city.", day, departures));
        }

        if (families > oldFamilies) {
            eventLog.add(String.format("Day %d: Population increased to %d families (%.1f%% housing capacity).", 
                    day, families, families * 100.0 / housingCapacity));
        } else if (families < oldFamilies) {
            eventLog.add(String.format("Day %d: Population decreased to %d families (%.1f%% housing capacity).", 
                    day, families, housingCapacity > 0 ? families * 100.0 / housingCapacity : 0));
        }
    }

    /**
     * Sets the income tax rate for the city.
     *
     * @param taxRate The new income tax rate (0.0 to 0.4)
     */
    public void setTaxRate(double taxRate) {
        if (taxRate < 0.0) {
            this.taxRate = 0.0;
        } else if (taxRate > 0.4) {
            this.taxRate = 0.4;
        } else {
            this.taxRate = taxRate;
        }

        // Log income tax rate change
        eventLog.add(String.format("Day %d: Income tax rate set to %.1f%%.", day, this.taxRate * 100));
    }

    // Getters

    public int getDay() {
        return day;
    }

    public int getFamilies() {
        return families;
    }

    public int getBudget() {
        return budget;
    }

    public int getSatisfaction() {
        return satisfaction;
    }

    public double getTaxRate() {
        return taxRate;
    }

    /**
     * Gets the current VAT rate for the city.
     *
     * @return The VAT rate (0.0 to 1.0)
     */
    public double getVatRate() {
        return vatRate;
    }

    /**
     * Sets the VAT rate for the city.
     *
     * @param vatRate The new VAT rate (0.0 to 0.25)
     */
    public void setVatRate(double vatRate) {
        if (vatRate < 0.0) {
            this.vatRate = 0.0;
        } else if (vatRate > 0.25) {
            this.vatRate = 0.25;
        } else {
            this.vatRate = vatRate;
        }

        // Log VAT rate change
        eventLog.add(String.format("Day %d: VAT rate set to %.1f%%.", day, this.vatRate * 100));
    }

    public List<Building> getBuildings() {
        return new ArrayList<>(buildings);
    }

    public Map<BuildingType, Integer> getBuildingCounts() {
        return new HashMap<>(buildingCounts);
    }

    /**
     * Gets the event log for the city.
     *
     * @return The event log
     */
    public List<String> getEventLog() {
        return new ArrayList<>(eventLog);
    }

    /**
     * Gets the most recent events (up to the specified limit).
     *
     * @param limit The maximum number of events to return
     * @return The most recent events
     */
    public List<String> getRecentEvents(int limit) {
        int size = eventLog.size();
        int startIndex = Math.max(0, size - limit);
        return new ArrayList<>(eventLog.subList(startIndex, size));
    }
}
