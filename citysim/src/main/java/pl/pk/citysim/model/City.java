package pl.pk.citysim.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import pl.pk.citysim.model.*;

public class City {
    private String name;
    private int day;
    private int families; // Kept for backward compatibility
    private FamilyManager familyManager;
    private int budget;
    private int satisfaction;
    private double taxRate;
    private double vatRate;
    private final List<Building> buildings;
    private final Map<String, Integer> buildingCounts;
    private final List<String> eventLog;
    private int dailyIncome;
    private int dailyExpenses;
    private int dailySatisfactionIncrease;
    private int dailySatisfactionDecrease;
    private final Random random = new Random();

    public City() {
        this.name = "Unnamed City";
        this.day = 1;
        this.families = 0;
        this.familyManager = new FamilyManager();
        this.budget = 0;
        this.satisfaction = 50;
        this.taxRate = 0.10;
        this.vatRate = 0.05;
        this.buildings = new ArrayList<>();
        this.buildingCounts = new HashMap<>();
        this.eventLog = new ArrayList<>();
        this.dailySatisfactionIncrease = 0;
        this.dailySatisfactionDecrease = 0;
        buildingCounts.put("Residential", 0);
        buildingCounts.put("Commercial", 0);
        buildingCounts.put("Industrial", 0);
        buildingCounts.put("Park", 0);
        buildingCounts.put("School", 0);
        buildingCounts.put("Hospital", 0);
        buildingCounts.put("Water Plant", 0);
        buildingCounts.put("Power Plant", 0);
    }

    public City(int initialFamilies, int initialBudget) {
        this("Unnamed City", initialFamilies, initialBudget);
    }

    public City(String name, int initialFamilies, int initialBudget) {
        this.name = name;
        this.day = 1;
        this.families = initialFamilies; // Kept for backward compatibility
        this.familyManager = new FamilyManager(initialFamilies);
        this.budget = initialBudget;
        this.satisfaction = 50; // Start with neutral satisfaction
        this.taxRate = 0.10; // 10% default income tax rate
        this.vatRate = 0.05; // 5% default VAT rate
        this.buildings = new ArrayList<>();
        this.buildingCounts = new HashMap<>();
        this.eventLog = new ArrayList<>();
        this.dailySatisfactionIncrease = 0;
        this.dailySatisfactionDecrease = 0;
        buildingCounts.put("Residential", 0);
        buildingCounts.put("Commercial", 0);
        buildingCounts.put("Industrial", 0);
        buildingCounts.put("Park", 0);
        buildingCounts.put("School", 0);
        buildingCounts.put("Hospital", 0);
        buildingCounts.put("Water Plant", 0);
        buildingCounts.put("Power Plant", 0);
        addInitialBuilding(ResidentialBuilding.class); // Housing for families
        addInitialBuilding(SchoolBuilding.class);      // Education
        addInitialBuilding(HospitalBuilding.class);    // Healthcare
        addInitialBuilding(WaterPlantBuilding.class); // Water supply
        addInitialBuilding(PowerPlantBuilding.class); // Power supply
        eventLog.add("Day 1: City founded with " + initialFamilies + " families and $" + initialBudget + " budget.");
        eventLog.add("Day 1: Initial infrastructure established (housing, school, hospital, water plant, power plant).");
    }

    public void nextDay() {
        day++;
        eventLog.clear();
        dailySatisfactionIncrease = 0;
        dailySatisfactionDecrease = 0;
        eventLog.add("Day " + day + ": === NEW DAY ===");

        calculateDailyIncome();
        calculateDailyExpenses();
        checkRandomEvents();
        updateSatisfaction();
        updatePopulation();
    }

    private void checkRandomEvents() {
        Random random = new Random();
        double eventChance = 0.05;
        if (families > 100) {
            eventChance = 0.10; // 10% for large cities
        } else if (families > 50) {
            eventChance = 0.07; // 7% for medium cities
        }
        int educationCapacity = 0;
        int healthcareCapacity = 0;
        int waterCapacity = 0;
        int powerCapacity = 0;

        for (Building building : buildings) {
            if (building instanceof SchoolBuilding) {
                educationCapacity += building.getEducationCapacity();
            } else if (building instanceof HospitalBuilding) {
                healthcareCapacity += building.getHealthcareCapacity();
            } else if (building instanceof WaterPlantBuilding) {
                waterCapacity += building.getUtilityCapacity();
            } else if (building instanceof PowerPlantBuilding) {
                powerCapacity += building.getUtilityCapacity();
            }
        }
        double educationRatio = families > 0 ? Math.min(1.0, (double) educationCapacity / families) : 1.0;
        double healthcareRatio = families > 0 ? Math.min(1.0, (double) healthcareCapacity / families) : 1.0;
        double waterRatio = families > 0 ? Math.min(1.0, (double) waterCapacity / families) : 1.0;
        double powerRatio = families > 0 ? Math.min(1.0, (double) powerCapacity / families) : 1.0;
        double serviceRatio = (educationRatio + healthcareRatio + waterRatio + powerRatio) / 4.0;
        if (serviceRatio < 0.7) {
            eventChance += (0.7 - serviceRatio) * 0.1; // Up to +7% for very poor services
        }
        if (random.nextDouble() < eventChance) {
            Event[] events = Event.values();
            double negativeEventChance = 0.75; // Default 75% chance of negative event
            if (serviceRatio < 0.7) {
                negativeEventChance += (0.7 - serviceRatio) * 0.3; // Up to +21% for very poor services
            }
            negativeEventChance = Math.min(0.95, negativeEventChance);
            Event event;
            if (random.nextDouble() < negativeEventChance) {
                int eventIndex = random.nextInt(3); // 0, 1, or 2
                event = events[eventIndex];
            } else {
                event = Event.GRANT;
            }
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

    private void handleFireEvent(Random random) {
        if (buildings.isEmpty()) {
            return;
        }
        int buildingIndex = random.nextInt(buildings.size());
        Building affectedBuilding = buildings.get(buildingIndex);
        int buildingValue = affectedBuilding.getUpkeep() * 10;
        int damage = buildingValue * (25 + random.nextInt(51)) / 100;
        if (families > 100) {
            damage = (int)(damage * 1.5); // 50% more damage for large cities
        } else if (families > 50) {
            damage = (int)(damage * 1.2); // 20% more damage for medium cities
        }
        int waterPlantCount = buildingCounts.getOrDefault("Water Plant", 0);
        int damageReduction = 0; // Initialize damage reduction to 0

        if (waterPlantCount > 0) {
            int waterCapacity = 0;
            for (Building building : buildings) {
                if (building instanceof WaterPlantBuilding) {
                    waterCapacity += building.getUtilityCapacity();
                }
            }
            double waterRatio = families > 0 ? Math.min(1.0, (double) waterCapacity / families) : 1.0;
            damageReduction = (int)(damage * waterRatio * 0.5);
            damage -= damageReduction;
        }
        budget -= damage; // Repair costs
        int satisfactionImpact = 5; // Base impact
        double damageToBudgetRatio = (double) damage / budget;

        if (damageToBudgetRatio > 0.2) {
            satisfactionImpact += 8; // +8 for severe damage (+3 and +5)
        } else if (damageToBudgetRatio > 0.1) {
            satisfactionImpact += 3; // +3 for significant damage
        }
        int actualChange = updateSatisfactionValue(-satisfactionImpact);

        // Log the event with appropriate message
        if (waterPlantCount > 0) {
            eventLog.add(String.format("Day %d: FIRE! A %s caught fire. Water system helped reduce damage by $%d. Total damage: $%d.", 
                    day, affectedBuilding.getTypeName(), damageReduction, damage));
        } else {
            eventLog.add(String.format("Day %d: FIRE! A %s caught fire, causing $%d in damages.", 
                    day, affectedBuilding.getTypeName(), damage));
        }
    }

    private void handleEpidemicEvent(Random random) {
        if (families <= 0) {
            return;
        }
        int baseAffectedPercentage = 10 + random.nextInt(21);
        if (families > 100) {
            baseAffectedPercentage += 10; // +10% more for large cities
        } else if (families > 50) {
            baseAffectedPercentage += 5; // +5% for medium cities
        }
        baseAffectedPercentage = Math.min(50, baseAffectedPercentage);

        int affectedFamilies = families * baseAffectedPercentage / 100;
        int baseCostPerFamily = 20;
        if (families > 100) {
            baseCostPerFamily = 30; // Even higher costs for large cities
        } else if (families > 50) {
            baseCostPerFamily = 25; // Higher costs for medium cities
        }

        int healthcareCosts = affectedFamilies * baseCostPerFamily;
        int hospitalCapacity = 0;
        for (Building building : buildings) {
            if (building instanceof HospitalBuilding) {
                hospitalCapacity += building.getHealthcareCapacity();
            }
        }
        double healthcareRatio = families > 0 ? Math.min(1.0, (double) hospitalCapacity / families) : 1.0;
        int costReduction = 0;
        int satisfactionImpact = 10; // Base impact
        if (hospitalCapacity > 0) {
            costReduction = (int)(healthcareCosts * healthcareRatio * 0.6);
            healthcareCosts -= costReduction;
            satisfactionImpact = (int)(satisfactionImpact * (1 - healthcareRatio * 0.7));
        }
        double severityFactor = (double) affectedFamilies / families;
        if (severityFactor > 0.4) {
            satisfactionImpact += 10; // +10 for very severe epidemics (+5 and +5)
        } else if (severityFactor > 0.3) {
            satisfactionImpact += 5; // +5 for severe epidemics
        }
        budget -= healthcareCosts;
        int actualChange = updateSatisfactionValue(-satisfactionImpact);

        // Log the event with appropriate message
        if (hospitalCapacity > 0) {
            eventLog.add(String.format("Day %d: EPIDEMIC! %d families affected. Hospitals reduced costs by $%d. Total cost: $%d.", 
                    day, affectedFamilies, costReduction, healthcareCosts));
        } else {
            eventLog.add(String.format("Day %d: EPIDEMIC! %d families affected, costing $%d. No hospitals to help!", 
                    day, affectedFamilies, healthcareCosts));
        }
    }

    private void handleEconomicCrisisEvent(Random random) {
        int baseImpactPercentage = 5 + random.nextInt(11);
        if (families > 100) {
            baseImpactPercentage += 5; // +5% more for large cities
        } else if (families > 50) {
            baseImpactPercentage += 3; // +3% for medium cities
        }
        int commercialCount = buildingCounts.getOrDefault("Commercial", 0);
        int industrialCount = buildingCounts.getOrDefault("Industrial", 0);
        double commercialRatio = 0.5; // Default balanced ratio
        int totalJobBuildings = commercialCount + industrialCount;
        if (totalJobBuildings > 0) {
            commercialRatio = (double) commercialCount / totalJobBuildings;
        }
        if (Math.abs(commercialRatio - 0.5) < 0.2) {
            baseImpactPercentage -= 2;
        } else if (commercialRatio < 0.3 || commercialRatio > 0.7) {
            baseImpactPercentage += 3;
        }
        baseImpactPercentage = Math.max(3, Math.min(25, baseImpactPercentage));
        int economicImpact = budget * baseImpactPercentage / 100;
        int satisfactionImpact = 8; // Base impact
        double impactRatio = (double) economicImpact / budget;
        if (impactRatio > 0.2) {
            satisfactionImpact += 9; // +9 for very severe economic impact (+4 and +5)
        } else if (impactRatio > 0.15) {
            satisfactionImpact += 4; // +4 for severe economic impact
        }
        budget -= economicImpact; // Economic loss
        int actualChange = updateSatisfactionValue(-satisfactionImpact); // Reduced satisfaction

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

    private void handleGrantEvent(Random random) {
        int baseGrantPercentage = 10 + random.nextInt(11);
        if (families > 100) {
            baseGrantPercentage -= 5; // -5% total for large cities (-2% and -3%)
        } else if (families > 50) {
            baseGrantPercentage -= 2; // -2% for medium cities
        }
        if (satisfaction < 40) {
            baseGrantPercentage += 5; // +5% for unhappy cities
        }
        baseGrantPercentage = Math.max(5, Math.min(25, baseGrantPercentage));
        int grantAmount = Math.max(100, budget * baseGrantPercentage / 100);
        int satisfactionImpact = 5; // Base impact
        double grantRatio = (double) grantAmount / budget;
        if (grantRatio > 0.2) {
            satisfactionImpact += 5; // +5 for very large grants (+2 and +3)
        } else if (grantRatio > 0.15) {
            satisfactionImpact += 2; // +2 for significant grants
        }
        budget += grantAmount; // Financial gain
        int actualChange = updateSatisfactionValue(satisfactionImpact); // Increased satisfaction

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

    private Building createBuilding(int id, Class<? extends Building> clazz) {
        try {
            return clazz.getConstructor(int.class).newInstance(id);
        } catch (Exception e) {
            throw new RuntimeException("Unable to create building", e);
        }
    }

    public Building addBuilding(Class<? extends Building> clazz, int cost) {
        int id = buildings.size() + 1;
        Building building = createBuilding(id, clazz);
        buildings.add(building);
        String typeName = building.getTypeName();
        int count = buildingCounts.getOrDefault(typeName, 0);
        buildingCounts.put(typeName, count + 1);
        budget -= cost;

        return building;
    }


    public Building addBuilding(Class<? extends Building> clazz) {
        try {
            Building temp = clazz.getConstructor(int.class).newInstance(0);
            return addBuilding(clazz, temp.getUpkeep() * 10);
        } catch (Exception e) {
            throw new RuntimeException("Failed to create building", e);
        }
    }

    private Building addInitialBuilding(Class<? extends Building> clazz) {
        int id = buildings.size() + 1;
        Building building = createBuilding(id, clazz);
        buildings.add(building);
        String typeName = building.getTypeName();
        int count = buildingCounts.getOrDefault(typeName, 0);
        buildingCounts.put(typeName, count + 1);

        return building;
    }

    private void calculateDailyIncome() {
        int familiesCount = familyManager.getFamiliesCount();
        if (familiesCount == 0) {
            dailyIncome = 0;
            return;
        }
        int commercialCount = buildingCounts.getOrDefault("Commercial", 0);
        int industrialCount = buildingCounts.getOrDefault("Industrial", 0);
        double jobQualityRatio = 0.0;
        int totalJobBuildings = commercialCount + industrialCount;
        if (totalJobBuildings > 0) {
            jobQualityRatio = (double) commercialCount / totalJobBuildings;
        }
        int commercialJobs = 0;
        int industrialJobs = 0;
        for (Building building : buildings) {
            if (building instanceof CommercialBuilding) {
                commercialJobs += building.getCapacity();
            } else if (building instanceof IndustrialBuilding) {
                industrialJobs += building.getCapacity();
            }
        }
        int totalJobs = commercialJobs + industrialJobs;
        double jobRatio = Math.min(1.0, (double) totalJobs / familiesCount);
        if (jobRatio < 1.0) {
            eventLog.add(String.format("Day %d: Job shortage (%.1f%% coverage) reducing family income", 
                    day, jobRatio * 100));
        }
        double difficultyScaling = 1.0;
        if (familiesCount > 50) {
            difficultyScaling = 0.95; // 5% income reduction for medium cities
        }
        if (familiesCount > 100) {
            difficultyScaling = 0.9; // 10% income reduction for large cities
        }
        if (familiesCount > 200) {
            difficultyScaling = 0.85; // 15% income reduction for very large cities
        }

        // Log difficulty scaling if it's applied
        if (difficultyScaling < 1.0) {
            eventLog.add(String.format("Day %d: City size difficulty scaling applied (%.0f%% income efficiency)", 
                    day, difficultyScaling * 100));
        }
        int educationCapacity = 0;
        for (Building building : buildings) {
            if (building instanceof SchoolBuilding) {
                educationCapacity += building.getEducationCapacity();
            }
        }
        double educationRatio = Math.min(1.0, (double) educationCapacity / familiesCount);
        int waterCapacity = 0;
        int powerCapacity = 0;
        for (Building building : buildings) {
            if (building instanceof WaterPlantBuilding) {
                waterCapacity += building.getUtilityCapacity();
            } else if (building instanceof PowerPlantBuilding) {
                powerCapacity += building.getUtilityCapacity();
            }
        }

        double waterRatio = Math.min(1.0, (double) waterCapacity / familiesCount);
        double powerRatio = Math.min(1.0, (double) powerCapacity / familiesCount);
        if (waterRatio < 0.8 || powerRatio < 0.8) {
            double worstUtilityRatio = Math.min(waterRatio, powerRatio);
            eventLog.add(String.format("Day %d: Utility shortage reducing family income", day));
        }
        familyManager.updateFamilyIncomes(jobQualityRatio, jobRatio, educationRatio, difficultyScaling);
        int totalFamilyIncome = familyManager.getTotalIncome();
        int incomeTaxRevenue = (int) (totalFamilyIncome * taxRate);
        int averageFamilyIncome = familyManager.getAverageIncome();
        int dailySpendingPerFamily = (int) (averageFamilyIncome * 0.25); // Increased from 0.2 to 0.25
        double satisfactionMultiplier = 0.7 + (satisfaction * 0.006); // 0.7 to 1.3 based on satisfaction (wider range)
        dailySpendingPerFamily = (int) (dailySpendingPerFamily * satisfactionMultiplier);
        int vatRevenue = (int) (familiesCount * dailySpendingPerFamily * vatRate);
        int totalTaxRevenue = incomeTaxRevenue + vatRevenue;
        dailyIncome = totalTaxRevenue;
        budget += totalTaxRevenue;

        // Log tax collection with more details
        eventLog.add(String.format("Day %d: Collected $%d in income tax and $%d in VAT.", 
                day, incomeTaxRevenue, vatRevenue));
    }

    private void calculateDailyExpenses() {
        int buildingUpkeep = 0;
        Map<String, Integer> upkeepByType = new HashMap<>();
        buildingCounts.keySet().forEach(typeName -> {
            upkeepByType.put(typeName, 0);
        });
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
        scaleFactor *= difficultyScaling;

        // Log difficulty scaling if it's applied
        if (difficultyScaling > 1.0) {
            eventLog.add(String.format("Day %d: City size difficulty scaling applied (%.0f%% expense increase)", 
                    day, (difficultyScaling - 1.0) * 100));
        }
        for (Building building : buildings) {
            String typeName = building.getTypeName();
            int baseUpkeep = building.getUpkeep();
            int scaledUpkeep = (int) (baseUpkeep * scaleFactor);
            if (building instanceof SchoolBuilding || building instanceof HospitalBuilding || 
                building instanceof WaterPlantBuilding || building instanceof PowerPlantBuilding) {
                double usageRatio = 0.0;
                if (building instanceof SchoolBuilding && families > 0) {
                    usageRatio = Math.min(1.0, (double) families / building.getEducationCapacity());
                } else if (building instanceof HospitalBuilding && families > 0) {
                    usageRatio = Math.min(1.0, (double) families / building.getHealthcareCapacity());
                } else if ((building instanceof WaterPlantBuilding || building instanceof PowerPlantBuilding) && families > 0) {
                    usageRatio = Math.min(1.0, (double) families / building.getUtilityCapacity());
                }
                double usageMultiplier = 0.5 + (usageRatio * 1.0);
                scaledUpkeep = (int) (scaledUpkeep * usageMultiplier);
            }
            buildingUpkeep += scaledUpkeep;
            upkeepByType.put(typeName, upkeepByType.get(typeName) + scaledUpkeep);
        }
        int baseCityServicesCost = 15; // Base cost per day (increased from 10)
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
        int cityServicesCost = baseCityServicesCost + (families * perFamilyCost);
        int waterCapacity = 0;
        int powerCapacity = 0;
        for (Building building : buildings) {
            if (building instanceof WaterPlantBuilding) {
                waterCapacity += building.getUtilityCapacity();
            } else if (building instanceof PowerPlantBuilding) {
                powerCapacity += building.getUtilityCapacity();
            }
        }
        double waterUsageRatio = families > 0 && waterCapacity > 0 ? 
                Math.min(1.0, (double) families / waterCapacity) : 0.0;
        double powerUsageRatio = families > 0 && powerCapacity > 0 ? 
                Math.min(1.0, (double) families / powerCapacity) : 0.0;
        int utilityOperationCost = 0;
        int waterPlantCount = buildingCounts.getOrDefault("Water Plant", 0);
        int powerPlantCount = buildingCounts.getOrDefault("Power Plant", 0);

        if (waterPlantCount > 0) {
            int waterCost = 8 + (int)(families * waterUsageRatio * 0.3);
            utilityOperationCost += waterCost;
        }

        if (powerPlantCount > 0) {
            int powerCost = 12 + (int)(families * powerUsageRatio * 0.4);
            utilityOperationCost += powerCost;
        }
        int totalExpenses = buildingUpkeep + cityServicesCost + utilityOperationCost;
        dailyExpenses = totalExpenses;
        budget -= totalExpenses;

        // Log expenses with more detail
        StringBuilder expenseLog = new StringBuilder();
        expenseLog.append(String.format("Day %d: Expenses breakdown:\n", day));
        expenseLog.append(String.format("- Building upkeep: $%d\n", buildingUpkeep));

        // Log upkeep by building type if there are buildings
        if (!buildings.isEmpty()) {
            for (String typeName : buildingCounts.keySet()) {
                int typeUpkeep = upkeepByType.get(typeName);
                if (typeUpkeep > 0) {
                    expenseLog.append(String.format("  - %s: $%d\n", typeName, typeUpkeep));
                }
            }
        }

        expenseLog.append(String.format("- City services: $%d\n", cityServicesCost));

        if (utilityOperationCost > 0) {
            expenseLog.append(String.format("- Utility operations: $%d\n", utilityOperationCost));
        }

        expenseLog.append(String.format("Total daily expenses: $%d", totalExpenses));
        eventLog.add(expenseLog.toString());
    }

    private void updateSatisfaction() {
        int satisfactionChange = 0;
        for (Building building : buildings) {
            satisfactionChange += building.getSatisfactionImpact();
        }
        double defaultIncomeTax = 0.10; // 10% default rate
        double defaultVAT = 0.05; // 5% default rate
        double incomeTaxDeviation = Math.max(0, taxRate - defaultIncomeTax);
        double vatDeviation = Math.max(0, vatRate - defaultVAT);
        int incomeTaxImpact = (int) (taxRate * 120); // Base income tax impact
        int vatImpact = (int) (vatRate * 80);  // Base VAT impact
        incomeTaxImpact += (int) (incomeTaxDeviation * 180); // Stronger penalty for exceeding default
        vatImpact += (int) (vatDeviation * 120); // Stronger penalty for exceeding default

        satisfactionChange -= incomeTaxImpact;
        satisfactionChange -= vatImpact;

        // Log significant tax impacts
        if (incomeTaxDeviation > 0 || vatDeviation > 0) {
            eventLog.add(String.format("Day %d: High tax rates reducing satisfaction (Income Tax: -%d, VAT: -%d)", 
                    day, incomeTaxImpact, vatImpact));
        }
        int educationCapacity = 0;
        int healthcareCapacity = 0;
        int waterCapacity = 0;
        int powerCapacity = 0;

        for (Building building : buildings) {
            if (building instanceof SchoolBuilding) {
                educationCapacity += building.getEducationCapacity();
            } else if (building instanceof HospitalBuilding) {
                healthcareCapacity += building.getHealthcareCapacity();
            } else if (building instanceof WaterPlantBuilding) {
                waterCapacity += building.getUtilityCapacity();
            } else if (building instanceof PowerPlantBuilding) {
                powerCapacity += building.getUtilityCapacity();
            }
        }
        int educationPenalty = 0;
        int healthcarePenalty = 0;
        int utilityPenalty = 0;
        if (educationCapacity < families) {
            double educationRatio = (double) educationCapacity / families;
            educationPenalty = (int) ((1 - educationRatio) * 25);
            String severityLevel = educationRatio < 0.5 ? "CRITICAL" : "WARNING";
            eventLog.add(String.format("Day %d: %s - Not enough schools! Education capacity: %d/%d families (%.1f%%). Satisfaction penalty: -%d", 
                    day, severityLevel, educationCapacity, families, educationRatio * 100, educationPenalty));
        }
        if (healthcareCapacity < families) {
            double healthcareRatio = (double) healthcareCapacity / families;
            healthcarePenalty = (int) ((1 - healthcareRatio) * 30);
            String severityLevel = healthcareRatio < 0.5 ? "CRITICAL" : "WARNING";
            eventLog.add(String.format("Day %d: %s - Not enough hospitals! Healthcare capacity: %d/%d families (%.1f%%). Satisfaction penalty: -%d", 
                    day, severityLevel, healthcareCapacity, families, healthcareRatio * 100, healthcarePenalty));
        }
        if (waterCapacity < families || powerCapacity < families) {
            double waterRatio = (double) waterCapacity / families;
            double powerRatio = (double) powerCapacity / families;
            double worstUtilityRatio = Math.min(waterRatio, powerRatio);
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
        satisfactionChange -= (educationPenalty + healthcarePenalty + utilityPenalty);
        int serviceQuality = 0;
        if (educationCapacity >= families && healthcareCapacity >= families && 
            waterCapacity >= families && powerCapacity >= families) {
            serviceQuality = Math.min(buildings.size() * 2, 25); // Cap at +25 (increased from +20)
        } else {
            serviceQuality = Math.min(buildings.size(), 15); // Cap at +15 if services are insufficient (increased from +10)
        }
        satisfactionChange += serviceQuality;
        int scaledChange = satisfactionChange / 10;
        int oldSatisfaction = satisfaction;
        int actualChange = updateSatisfactionValue(scaledChange);
        if (satisfaction > 80) {
            double excessSatisfaction = satisfaction - 80;
            double diminishedExcess = excessSatisfaction * (1.0 - (excessSatisfaction / 40.0));
            int newSatisfaction = 80 + (int)diminishedExcess;
            satisfaction = newSatisfaction;
        }

        // Log satisfaction change
        eventLog.add(String.format("Day %d: Satisfaction level is now %d%%.", day, satisfaction));
    }

    private void updatePopulation() {
        int housingCapacity = 0;
        for (Building building : buildings) {
            if (building instanceof ResidentialBuilding) {
                housingCapacity += building.getCapacity();
            }
        }
        int familiesCount = familyManager.getFamiliesCount();
        double housingOccupancyRatio = housingCapacity > 0 ? (double) familiesCount / housingCapacity : 1.0;
        boolean isOvercrowded = housingOccupancyRatio > 0.9;
        int availableJobs = 0;
        for (Building building : buildings) {
            if (building instanceof CommercialBuilding || 
                building instanceof IndustrialBuilding) {
                availableJobs += building.getCapacity();
            }
        }
        int educationCapacity = 0;
        int healthcareCapacity = 0;
        int waterCapacity = 0;
        int powerCapacity = 0;

        for (Building building : buildings) {
            if (building instanceof SchoolBuilding) {
                educationCapacity += building.getEducationCapacity();
            } else if (building instanceof HospitalBuilding) {
                healthcareCapacity += building.getHealthcareCapacity();
            } else if (building instanceof WaterPlantBuilding) {
                waterCapacity += building.getUtilityCapacity();
            } else if (building instanceof PowerPlantBuilding) {
                powerCapacity += building.getUtilityCapacity();
            }
        }
        double educationRatio = familiesCount > 0 ? Math.min(1.0, (double) educationCapacity / familiesCount) : 1.0;
        double healthcareRatio = familiesCount > 0 ? Math.min(1.0, (double) healthcareCapacity / familiesCount) : 1.0;
        double waterRatio = familiesCount > 0 ? Math.min(1.0, (double) waterCapacity / familiesCount) : 1.0;
        double powerRatio = familiesCount > 0 ? Math.min(1.0, (double) powerCapacity / familiesCount) : 1.0;
        double serviceRatio = (educationRatio + healthcareRatio + waterRatio + powerRatio) / 4.0;
        int serviceQuality = (int) (serviceRatio * 30); // Up to +30 for perfect services
        double arrivalChance = 0.05; // Lower base chance (5% instead of 10%)
        arrivalChance += satisfaction * 0.0065; // Up to +65% for 100% satisfaction

        if (availableJobs > familiesCount) {
            arrivalChance += 0.1; // +10% if jobs available
        }
        arrivalChance += serviceRatio * 0.2; // Up to +20% for perfect services
        double defaultIncomeTax = 0.10; // 10% default rate
        double defaultVAT = 0.05; // 5% default rate
        double incomeTaxDeviation = Math.max(0, taxRate - defaultIncomeTax);
        double vatDeviation = Math.max(0, vatRate - defaultVAT);
        if (taxRate > 0.2) {
            arrivalChance -= (taxRate - 0.2) * 0.5; // Up to -40% for high taxes
        }

        if (vatRate > 0.1) {
            arrivalChance -= (vatRate - 0.1) * 0.3; // Up to -30% for high VAT
        }
        if (incomeTaxDeviation > 0) {
            arrivalChance -= incomeTaxDeviation * 0.8; // Stronger penalty for exceeding default income tax
            eventLog.add(String.format("Day %d: High income tax (%.1f%% above default) reducing family arrival chance", 
                    day, incomeTaxDeviation * 100));
        }

        if (vatDeviation > 0) {
            arrivalChance -= vatDeviation * 0.6; // Stronger penalty for exceeding default VAT
            eventLog.add(String.format("Day %d: High VAT (%.1f%% above default) reducing family arrival chance", 
                    day, vatDeviation * 100));
        }
        if (educationRatio < 0.5) {
            arrivalChance -= (0.5 - educationRatio) * 0.3; // Up to -15% for poor education
        }

        if (healthcareRatio < 0.5) {
            arrivalChance -= (0.5 - healthcareRatio) * 0.3; // Up to -15% for poor healthcare
        }

        if (waterRatio < 0.7 || powerRatio < 0.7) {
            arrivalChance -= 0.2; // -20% for critical utility shortage
        }
        int availableHousing = Math.max(0, housingCapacity - familiesCount);

        if (availableHousing <= 0) {
            arrivalChance = 0; // No chance if no housing available
            eventLog.add(String.format("Day %d: WARNING - No available housing! New families cannot move in.", day));
        } else if (isOvercrowded) {
            double reductionFactor = (housingOccupancyRatio - 0.9) * 10; // 0 to 1 as occupancy goes from 90% to 100%
            arrivalChance *= (1 - reductionFactor);
            eventLog.add(String.format("Day %d: NOTICE - Housing nearly full (%.1f%% occupied). Fewer families moving in.", 
                    day, housingOccupancyRatio * 100));
        }
        int maxNewFamilies = availableHousing;
        arrivalChance = Math.max(0, Math.min(1, arrivalChance));
        Random random = new Random();
        int newFamilies = 0;
        int maxAttempts = 5; // Default max attempts
        if (satisfaction > 90 && availableHousing >= 100) {
            maxAttempts = 50; // Up to 50 families when satisfaction > 90%
            eventLog.add(String.format("Day %d: EXCELLENT - Very high satisfaction (>90%%) attracting many new families!", day));
        } else if (satisfaction > 85 && availableHousing >= 100) {
            maxAttempts = 30; // Up to 30 families when satisfaction > 85%
            eventLog.add(String.format("Day %d: GREAT - High satisfaction (>85%%) attracting more new families!", day));
        }
        for (int i = 0; i < maxAttempts && newFamilies < maxNewFamilies; i++) {
            if (random.nextDouble() < arrivalChance) {
                newFamilies++;
            }
        }
        int departures = 0;
        double baseDepartureChance = 0.0;
        if (satisfaction < 50) {
            baseDepartureChance = (50 - satisfaction) * 0.01;
        }
        double serviceDepartureChance = 0.0;
        if (waterRatio < 0.5 || powerRatio < 0.5) {
            serviceDepartureChance += 0.15; // 15% chance due to critical utility shortage
            eventLog.add(String.format("Day %d: CRITICAL - Severe utility shortage causing families to leave!", day));
        }
        if (educationRatio < 0.4) {
            serviceDepartureChance += 0.1; // 10% chance due to education shortage
            eventLog.add(String.format("Day %d: CRITICAL - Severe education shortage causing families to leave!", day));
        }
        if (healthcareRatio < 0.4) {
            serviceDepartureChance += 0.1; // 10% chance due to healthcare shortage
            eventLog.add(String.format("Day %d: CRITICAL - Severe healthcare shortage causing families to leave!", day));
        }
        if (isOvercrowded) {
            double overcrowdingFactor = (housingOccupancyRatio - 0.9) * 10; // 0 to 1 as occupancy goes from 90% to 100%
            double overcrowdingChance = 0.1 * overcrowdingFactor; // Up to 10% additional departure chance
            serviceDepartureChance += overcrowdingChance;
            eventLog.add(String.format("Day %d: WARNING - Housing overcrowding (%.1f%% occupied) causing families to leave!", 
                    day, housingOccupancyRatio * 100));
        }
        double departureChance = Math.min(0.5, baseDepartureChance + serviceDepartureChance); // Cap at 50%
        for (int i = 0; i < familiesCount; i++) {
            if (random.nextDouble() < departureChance) {
                departures++;
            }
        }
        int oldFamilies = familiesCount;
        for (int i = 0; i < newFamilies; i++) {
            familyManager.addFamily();
        }
        familyManager.removeFamilies(departures);
        familiesCount = familyManager.getFamiliesCount();
        if (familiesCount > housingCapacity) {
            int excess = familiesCount - housingCapacity;
            familyManager.removeFamilies(excess);
            familiesCount = familyManager.getFamiliesCount();
            eventLog.add(String.format("Day %d: CRITICAL - %d families couldn't find housing and left the city!", 
                    day, excess));
        }
        families = familiesCount;

        // Log population changes
        if (newFamilies > 0) {
            eventLog.add(String.format("Day %d: %d new families moved to the city.", day, newFamilies));
        }

        if (departures > 0) {
            eventLog.add(String.format("Day %d: %d families left the city.", day, departures));
        }

        // Log satisfaction impact on population movement
        if (newFamilies > 0 || departures > 0) {
            String satisfactionImpact;
            if (satisfaction >= 75) {
                satisfactionImpact = "high satisfaction is attracting new residents";
            } else if (satisfaction >= 50) {
                satisfactionImpact = "moderate satisfaction is maintaining stable population";
            } else if (satisfaction >= 25) {
                satisfactionImpact = "low satisfaction is causing some residents to leave";
            } else {
                satisfactionImpact = "very low satisfaction is causing many residents to leave";
            }
            eventLog.add(String.format("Day %d: Current satisfaction level (%d%%) - %s.", day, satisfaction, satisfactionImpact));
        }

        if (families > oldFamilies) {
            eventLog.add(String.format("Day %d: Population increased to %d families (%.1f%% housing capacity).", 
                    day, families, families * 100.0 / housingCapacity));
        } else if (families < oldFamilies) {
            eventLog.add(String.format("Day %d: Population decreased to %d families (%.1f%% housing capacity).", 
                    day, families, housingCapacity > 0 ? families * 100.0 / housingCapacity : 0));
        }
    }

    public void setTaxRate(double taxRate) {
        double oldRate = this.taxRate;

        if (taxRate < 0.0) {
            this.taxRate = 0.0;
        } else if (taxRate > 0.4) {
            this.taxRate = 0.4;
        } else {
            this.taxRate = taxRate;
        }
        if (this.taxRate > oldRate) {
            int satisfactionChange = (int)((this.taxRate - oldRate) * -500);
            int actualChange = updateSatisfactionValue(satisfactionChange);
            eventLog.add(String.format("Day %d: Tax increase reduced satisfaction by %d points.", day, -actualChange));
        } else if (this.taxRate < oldRate) {
            int satisfactionChange = (int)((oldRate - this.taxRate) * 100);
            int actualChange = updateSatisfactionValue(satisfactionChange);
            eventLog.add(String.format("Day %d: Tax decrease improved satisfaction by %d points.", day, actualChange));
        }

        // Log income tax rate change
        eventLog.add(String.format("Day %d: Income tax rate set to %.1f%%.", day, this.taxRate * 100));
    }

    public int getDay() {
        return day;
    }

    public int getFamilies() {
        return familyManager.getFamiliesCount();
    }

    public FamilyManager getFamilyManager() {
        return familyManager;
    }

    public int getBudget() {
        return budget;
    }

    public int getSatisfaction() {
        return satisfaction;
    }

    private int updateSatisfactionValue(int change) {
        int actualChange = 0;

        if (change > 0) {
            int remainingIncrease = 5 - dailySatisfactionIncrease;
            if (remainingIncrease <= 0) {
                return 0;
            }
            actualChange = Math.min(change, remainingIncrease);
            satisfaction += actualChange;
            dailySatisfactionIncrease += actualChange;
        } else if (change < 0) {
            int remainingDecrease = 50 - dailySatisfactionDecrease;
            if (remainingDecrease <= 0) {
                return 0;
            }
            actualChange = Math.max(change, -remainingDecrease);
            satisfaction += actualChange;
            dailySatisfactionDecrease += Math.abs(actualChange);
        }
        if (satisfaction > 100) {
            int overflow = satisfaction - 100;
            satisfaction = 100;
            actualChange -= overflow;
        } else if (satisfaction < 0) {
            int underflow = -satisfaction;
            satisfaction = 0;
            actualChange += underflow;
        }

        return actualChange;
    }

    public double getTaxRate() {
        return taxRate;
    }

    public double getVatRate() {
        return vatRate;
    }

    public void setVatRate(double vatRate) {
        double oldRate = this.vatRate;

        if (vatRate < 0.0) {
            this.vatRate = 0.0;
        } else if (vatRate > 0.25) {
            this.vatRate = 0.25;
        } else {
            this.vatRate = vatRate;
        }
        if (this.vatRate > oldRate) {
            int satisfactionChange = (int)((this.vatRate - oldRate) * -80);
            int actualChange = updateSatisfactionValue(satisfactionChange);
            eventLog.add(String.format("Day %d: VAT increase reduced satisfaction by %d points.", day, -actualChange));
        } else if (this.vatRate < oldRate) {
            int satisfactionChange = (int)((oldRate - this.vatRate) * 40);
            int actualChange = updateSatisfactionValue(satisfactionChange);
            eventLog.add(String.format("Day %d: VAT decrease improved satisfaction by %d points.", day, actualChange));
        }

        // Log VAT rate change
        eventLog.add(String.format("Day %d: VAT rate set to %.1f%%.", day, this.vatRate * 100));
    }

    public List<Building> getBuildings() {
        return new ArrayList<>(buildings);
    }

    public Map<String, Integer> getBuildingCounts() {
        return new HashMap<>(buildingCounts);
    }

    public List<String> getEventLog() {
        return new ArrayList<>(eventLog);
    }

    public List<String> getRecentEvents() {
        return eventLog;
    }

    public List<String> getEventsByDay(int day) {
        List<String> dayEvents = new ArrayList<>();
        String dayPrefix = "Day " + day + ":";

        for (String event : eventLog) {
            if (event.startsWith(dayPrefix)) {
                dayEvents.add(event);
            }
        }

        return dayEvents;
    }

    public List<String> getCurrentDayEvents() {
        return getEventsByDay(day);
    }

    public int getDailyIncome() {
        return dailyIncome;
    }

    public int getDailyExpenses() {
        return dailyExpenses;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
