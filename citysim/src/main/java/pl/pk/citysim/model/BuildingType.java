package pl.pk.citysim.model;

/**
 * Enum representing different types of buildings in the city simulation.
 */
public enum BuildingType {
    RESIDENTIAL("Residential", 25, 5, 5, "Houses families, increases population", 0, 0, 0),
    COMMERCIAL("Commercial", 15, 10, 2, "Provides jobs and generates income", 0, 0, 0),
    INDUSTRIAL("Industrial", 10, 20, -3, "Generates higher income but reduces satisfaction", 0, 0, 0),
    PARK("Park", 0, 2, 8, "Increases satisfaction but generates no income", 0, 0, 0),
    SCHOOL("School", 0, 15, 6, "Improves education and satisfaction", 50, 0, 0),
    HOSPITAL("Hospital", 0, 25, 7, "Improves health and satisfaction", 0, 60, 0),
    WATER_PLANT("Water Plant", 0, 30, 3, "Provides water to families", 0, 0, 75),
    POWER_PLANT("Power Plant", 0, 40, 2, "Provides electricity to families", 0, 0, 100);

    private final String name;
    private final int capacity; // Housing or job capacity
    private final int upkeep;
    private final int satisfactionImpact;
    private final String description;
    private final int educationCapacity; // How many families one school can serve
    private final int healthcareCapacity; // How many families one hospital can serve
    private final int utilityCapacity; // How many families one utility building can serve

    BuildingType(String name, int capacity, int upkeep, int satisfactionImpact, String description,
                int educationCapacity, int healthcareCapacity, int utilityCapacity) {
        this.name = name;
        this.capacity = capacity;
        this.upkeep = upkeep;
        this.satisfactionImpact = satisfactionImpact;
        this.description = description;
        this.educationCapacity = educationCapacity;
        this.healthcareCapacity = healthcareCapacity;
        this.utilityCapacity = utilityCapacity;
    }

    public String getName() {
        return name;
    }

    public int getCapacity() {
        return capacity;
    }

    public int getUpkeep() {
        return upkeep;
    }

    public int getSatisfactionImpact() {
        return satisfactionImpact;
    }

    public String getDescription() {
        return description;
    }

    public int getEducationCapacity() {
        return educationCapacity;
    }

    public int getHealthcareCapacity() {
        return healthcareCapacity;
    }

    public int getUtilityCapacity() {
        return utilityCapacity;
    }

    @Override
    public String toString() {
        return name;
    }
}
