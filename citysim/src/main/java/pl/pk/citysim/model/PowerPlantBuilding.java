package pl.pk.citysim.model;
public class PowerPlantBuilding extends Building {

    public PowerPlantBuilding(int id) {
        super(id, "Power Plant", "Provides electricity to families", 0, 40, 2, 0, 0, 100);
    }
}
