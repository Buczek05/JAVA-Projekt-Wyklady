package pl.pk.citysim.model;

/**
 * Enum representing different types of events that can occur in the city.
 */
public enum Event {
    FIRE("Fire", "A building caught fire, causing damage and repair costs.", false),
    EPIDEMIC("Epidemic", "A disease outbreak affected families and required healthcare expenses.", false),
    ECONOMIC_CRISIS("Economic Crisis", "Market instability caused financial losses.", false),
    GRANT("Grant", "The city received a financial grant from the government.", true);

    private final String name;
    private final String description;
    private final boolean positive;

    Event(String name, String description, boolean positive) {
        this.name = name;
        this.description = description;
        this.positive = positive;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public boolean isPositive() {
        return positive;
    }

    @Override
    public String toString() {
        return name;
    }
}
