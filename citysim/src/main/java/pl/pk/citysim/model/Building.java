package pl.pk.citysim.model;
public abstract class Building {
    private final int id;
    private int occupancy;
    protected final String name;
    protected final String description;
    protected final int capacity;
    protected final int upkeep;
    protected final int satisfactionImpact;
    protected final int educationCapacity;
    protected final int healthcareCapacity;
    protected final int utilityCapacity;

    protected Building(int id, String name, String description, int capacity, int upkeep,
                      int satisfactionImpact, int educationCapacity, int healthcareCapacity,
                      int utilityCapacity) {
        this.id = id;
        this.occupancy = 0;
        this.name = name;
        this.description = description;
        this.capacity = capacity;
        this.upkeep = upkeep;
        this.satisfactionImpact = satisfactionImpact;
        this.educationCapacity = educationCapacity;
        this.healthcareCapacity = healthcareCapacity;
        this.utilityCapacity = utilityCapacity;
    }

    public int getId() {
        return id;
    }

    public int getOccupancy() {
        return occupancy;
    }

    public void setOccupancy(int occupancy) {
        if (occupancy < 0) {
            this.occupancy = 0;
        } else if (occupancy > getCapacity()) {
            this.occupancy = getCapacity();
        } else {
            this.occupancy = occupancy;
        }
    }

    public int getCapacity() {
        return capacity;
    }

    public int getUpkeep() {
        return upkeep;
    }

    public int getSatisfactionImpact() {
        return satisfactionImpact;
    }

    public int getEducationCapacity() {
        return educationCapacity;
    }

    public int getHealthcareCapacity() {
        return healthcareCapacity;
    }

    public int getUtilityCapacity() {
        return utilityCapacity;
    }

    public String getTypeName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    @Override
    public String toString() {
        return "Building{" +
                "id=" + id +
                ", type=" + getTypeName() +
                ", occupancy=" + occupancy + "/" + getCapacity() +
                '}';
    }
}
