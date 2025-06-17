package pl.pk.citysim.model;

/**
 * Represents a power plant in the city simulation.
 * Provides electricity to families.
 */
public class PowerPlantBuilding extends Building {

    /**
     * Creates a new power plant.
     *
     * @param id Unique identifier for the building
     */
    public PowerPlantBuilding(int id) {
        super(id, "Power Plant", "Provides electricity to families", 0, 40, 2, 0, 0, 100);
    }
}
