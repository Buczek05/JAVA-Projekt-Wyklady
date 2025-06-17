package pl.pk.citysim.model;

/**
 * Factory class for creating Building objects.
 */
public class BuildingFactory {
    
    /**
     * Creates a building of the specified type.
     *
     * @param id The unique identifier for the building
     * @param type The type of building to create
     * @return The newly created building
     */
    public static Building createBuilding(int id, BuildingType type) {
        switch (type) {
            case RESIDENTIAL:
                return new ResidentialBuilding(id);
            case COMMERCIAL:
                return new CommercialBuilding(id);
            case INDUSTRIAL:
                return new IndustrialBuilding(id);
            case PARK:
                return new ParkBuilding(id);
            case SCHOOL:
                return new SchoolBuilding(id);
            case HOSPITAL:
                return new HospitalBuilding(id);
            case WATER_PLANT:
                return new WaterPlantBuilding(id);
            case POWER_PLANT:
                return new PowerPlantBuilding(id);
            default:
                throw new IllegalArgumentException("Unknown building type: " + type);
        }
    }
}