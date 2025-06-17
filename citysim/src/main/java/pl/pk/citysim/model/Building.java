package pl.pk.citysim.model;

/**
 * Represents a building in the city simulation.
 */
public abstract class Building {
    private final int id;
    private int occupancy;

    /**
     * Creates a new building.
     *
     * @param id Unique identifier for the building
     */
    protected Building(int id) {
        this.id = id;
        this.occupancy = 0;
    }

    /**
     * Gets the type of the building.
     *
     * @return The building type
     */
    public abstract BuildingType getType();

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

    public int getCapacity() {
        return getType().getCapacity();
    }

    public int getUpkeep() {
        return getType().getUpkeep();
    }

    public int getSatisfactionImpact() {
        return getType().getSatisfactionImpact();
    }

    public int getEducationCapacity() {
        return getType().getEducationCapacity();
    }

    public int getHealthcareCapacity() {
        return getType().getHealthcareCapacity();
    }

    public int getUtilityCapacity() {
        return getType().getUtilityCapacity();
    }

    @Override
    public String toString() {
        return "Building{" +
                "id=" + id +
                ", type=" + getType() +
                ", occupancy=" + occupancy + "/" + getCapacity() +
                '}';
    }
}
