package pl.pk.citysim.model;

/**
 * Represents a water plant in the city simulation.
 * Provides water to families.
 */
public class WaterPlantBuilding extends Building {

    /**
     * Creates a new water plant.
     *
     * @param id Unique identifier for the building
     */
    public WaterPlantBuilding(int id) {
        super(id, "Water Plant", "Provides water to families", 0, 30, 3, 0, 0, 75);
    }
}
