package pl.pk.citysim.model;

/**
 * Represents a hospital in the city simulation.
 * Improves health and satisfaction.
 */
public class HospitalBuilding extends Building {
    
    /**
     * Creates a new hospital.
     *
     * @param id Unique identifier for the building
     */
    public HospitalBuilding(int id) {
        super(id);
    }
    
    @Override
    public BuildingType getType() {
        return BuildingType.HOSPITAL;
    }
}