package pl.pk.citysim.model;
public class IndustrialBuilding extends Building {

    public IndustrialBuilding(int id) {
        super(id, "Industrial", "Generates higher income but reduces satisfaction", 10, 20, -3, 0, 0, 0);
    }
}
