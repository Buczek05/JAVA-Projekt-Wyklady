package pl.pk.citysim.model;

/**
 * Represents a park in the city simulation.
 * Increases satisfaction but generates no income.
 */
public class ParkBuilding extends Building {

    /**
     * Creates a new park.
     *
     * @param id Unique identifier for the building
     */
    public ParkBuilding(int id) {
        super(id, "Park", "Increases satisfaction but generates no income", 0, 2, 8, 0, 0, 0);
    }
}
