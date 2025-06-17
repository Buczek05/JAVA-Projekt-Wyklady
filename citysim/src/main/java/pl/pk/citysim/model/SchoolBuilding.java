package pl.pk.citysim.model;

/**
 * Represents a school in the city simulation.
 * Improves education and satisfaction.
 */
public class SchoolBuilding extends Building {
    
    /**
     * Creates a new school.
     *
     * @param id Unique identifier for the building
     */
    public SchoolBuilding(int id) {
        super(id);
    }
    
    @Override
    public BuildingType getType() {
        return BuildingType.SCHOOL;
    }
}