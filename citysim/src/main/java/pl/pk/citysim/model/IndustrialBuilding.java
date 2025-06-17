package pl.pk.citysim.model;

/**
 * Represents an industrial building in the city simulation.
 * Generates higher income but reduces satisfaction.
 */
public class IndustrialBuilding extends Building {
    
    /**
     * Creates a new industrial building.
     *
     * @param id Unique identifier for the building
     */
    public IndustrialBuilding(int id) {
        super(id);
    }
    
    @Override
    public BuildingType getType() {
        return BuildingType.INDUSTRIAL;
    }
}