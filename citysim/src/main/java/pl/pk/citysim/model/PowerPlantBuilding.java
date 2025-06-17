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
        super(id);
    }
    
    @Override
    public BuildingType getType() {
        return BuildingType.POWER_PLANT;
    }
}