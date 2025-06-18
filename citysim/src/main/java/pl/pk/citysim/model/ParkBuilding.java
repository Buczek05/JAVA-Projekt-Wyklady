package pl.pk.citysim.model;
public class ParkBuilding extends Building {

    public ParkBuilding(int id) {
        super(id, "Park", "Increases satisfaction but generates no income", 0, 2, 8, 0, 0, 0);
    }
}
