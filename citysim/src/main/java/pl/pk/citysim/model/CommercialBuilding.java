package pl.pk.citysim.model;

/**
 * Represents a commercial building in the city simulation.
 * Provides jobs and generates income.
 */
public class CommercialBuilding extends Building {

    /**
     * Creates a new commercial building.
     *
     * @param id Unique identifier for the building
     */
    public CommercialBuilding(int id) {
        super(id, "Commercial", "Provides jobs and generates income", 15, 10, 2, 0, 0, 0);
    }
}
