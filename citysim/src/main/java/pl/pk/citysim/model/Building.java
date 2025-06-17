package pl.pk.citysim.model;

/**
 * Represents a building in the city simulation.
 */
public abstract class Building {
    private final int id;
    private int occupancy;
    protected final String name;
    protected final String description;
    protected final int capacity;
    protected final int upkeep;
    protected final int satisfactionImpact;
    protected final int educationCapacity;
    protected final int healthcareCapacity;
    protected final int utilityCapacity;

    /**
     * Creates a new building.
     *
     * @param id Unique identifier for the building
     * @param name Name of the building
     * @param description Description of the building
     * @param capacity Housing or job capacity
     * @param upkeep Upkeep cost
     * @param satisfactionImpact Impact on satisfaction
     * @param educationCapacity Education capacity
     * @param healthcareCapacity Healthcare capacity
     * @param utilityCapacity Utility capacity
     */
    protected Building(int id, String name, String description, int capacity, int upkeep, 
                      int satisfactionImpact, int educationCapacity, int healthcareCapacity, 
                      int utilityCapacity) {
        this.id = id;
        this.occupancy = 0;
        this.name = name;
        this.description = description;
        this.capacity = capacity;
        this.upkeep = upkeep;
        this.satisfactionImpact = satisfactionImpact;
        this.educationCapacity = educationCapacity;
        this.healthcareCapacity = healthcareCapacity;
        this.utilityCapacity = utilityCapacity;
    }

    public int getId() {
        return id;
    }

    public int getOccupancy() {
        return occupancy;
    }

    /**
     * Sets the occupancy of the building.
     *
     * @param occupancy The new occupancy value
     */
    public void setOccupancy(int occupancy) {
        if (occupancy < 0) {
            this.occupancy = 0;
        } else if (occupancy > getCapacity()) {
            this.occupancy = getCapacity();
        } else {
            this.occupancy = occupancy;
        }
    }

    /**
     * Gets the capacity of the building.
     *
     * @return The building capacity
     */
    public int getCapacity() {
        return capacity;
    }

    /**
     * Gets the upkeep cost of the building.
     *
     * @return The building upkeep cost
     */
    public int getUpkeep() {
        return upkeep;
    }

    /**
     * Gets the satisfaction impact of the building.
     *
     * @return The building satisfaction impact
     */
    public int getSatisfactionImpact() {
        return satisfactionImpact;
    }

    /**
     * Gets the education capacity of the building.
     *
     * @return The building education capacity
     */
    public int getEducationCapacity() {
        return educationCapacity;
    }

    /**
     * Gets the healthcare capacity of the building.
     *
     * @return The building healthcare capacity
     */
    public int getHealthcareCapacity() {
        return healthcareCapacity;
    }

    /**
     * Gets the utility capacity of the building.
     *
     * @return The building utility capacity
     */
    public int getUtilityCapacity() {
        return utilityCapacity;
    }

    /**
     * Gets the name of the building type.
     *
     * @return The building type name
     */
    public String getTypeName() {
        return name;
    }

    /**
     * Gets the description of the building.
     *
     * @return The building description
     */
    public String getDescription() {
        return description;
    }

    @Override
    public String toString() {
        return "Building{" +
                "id=" + id +
                ", type=" + getTypeName() +
                ", occupancy=" + occupancy + "/" + getCapacity() +
                '}';
    }
}
