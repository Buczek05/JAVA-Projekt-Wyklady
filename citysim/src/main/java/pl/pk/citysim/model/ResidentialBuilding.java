package pl.pk.citysim.model;

/**
 * Represents a residential building in the city simulation.
 * Houses families and increases population.
 */
public class ResidentialBuilding extends Building {

    /**
     * Creates a new residential building.
     *
     * @param id Unique identifier for the building
     */
    public ResidentialBuilding(int id) {
        super(id, "Residential", "Houses families, increases population", 25, 5, 5, 0, 0, 0);
    }
}
