package pl.pk.citysim.model;

/**
 * Represents a building in the city simulation.
 */
public class Building {
    private final BuildingType type;
    private final int id;
    private int occupancy;

    /**
     * Creates a new building of the specified type.
     *
     * @param id   Unique identifier for the building
     * @param type Type of the building
     */
    public Building(int id, BuildingType type) {
        this.id = id;
        this.type = type;
        this.occupancy = 0;
    }

    /**
     * Gets the type of the building.
     *
     * @return The building type
     */
    public BuildingType getType() {
        return type;
    }

    /**
     * Gets the unique identifier of the building.
     *
     * @return The building ID
     */
    public int getId() {
        return id;
    }

    /**
     * Gets the current occupancy of the building.
     *
     * @return The current occupancy
     */
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
        } else if (occupancy > type.getCapacity()) {
            this.occupancy = type.getCapacity();
        } else {
            this.occupancy = occupancy;
        }
    }

    /**
     * Gets the maximum capacity of the building.
     *
     * @return The maximum capacity
     */
    public int getCapacity() {
        return type.getCapacity();
    }

    /**
     * Gets the daily upkeep cost of the building.
     *
     * @return The upkeep cost
     */
    public int getUpkeep() {
        return type.getUpkeep();
    }

    /**
     * Gets the satisfaction impact of the building.
     *
     * @return The satisfaction impact
     */
    public int getSatisfactionImpact() {
        return type.getSatisfactionImpact();
    }

    @Override
    public String toString() {
        return "Building{" +
                "id=" + id +
                ", type=" + type +
                ", occupancy=" + occupancy + "/" + type.getCapacity() +
                '}';
    }
}