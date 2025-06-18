package pl.pk.citysim.model;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

// Represents a highscore entry
public class Highscore implements Comparable<Highscore> {
    // Default highscores directory path
    private static final String HIGHSCORES_FILE = "highscores.txt";
    private static final String HIGHSCORES_DIR = "saves";
    private static final int MAX_HIGHSCORES = 10;
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final String cityName;
    private final int score;
    private final LocalDateTime achievedAt;
    private final int families;
    private final int budget;
    private final int satisfaction;
    private final int days;

    public Highscore(String cityName, int score, int families, int budget, int satisfaction, int days) {
        this.cityName = cityName;
        this.score = score;
        this.families = families;
        this.budget = budget;
        this.satisfaction = satisfaction;
        this.days = days;
        this.achievedAt = LocalDateTime.now();
    }

    public Highscore(
            String cityName,
            int score,
            int families,
            int budget,
            int satisfaction,
            int days,
            LocalDateTime achievedAt) {
        this.cityName = cityName;
        this.score = score;
        this.families = families;
        this.budget = budget;
        this.satisfaction = satisfaction;
        this.days = days;
        this.achievedAt = achievedAt;
    }

    public static Highscore calculateScore(City city) {
        // Score formula: (families * 10) + (budget / 10) + (satisfaction * 5) + (days * 2)
        String cityName = city.getName();
        int families = city.getFamilies();
        int budget = city.getBudget();
        int satisfaction = city.getSatisfaction();
        int days = city.getDay();

        int score = (families * 10) + (budget / 10) + (satisfaction * 5) + (days * 2);

        return new Highscore(cityName, score, families, budget, satisfaction, days);
    }

    public static boolean saveHighscore(Highscore highscore) {
        try {
            // Create the highscores directory if it doesn't exist
            Path highscoresDir = Paths.get(HIGHSCORES_DIR);
            if (!Files.exists(highscoresDir)) {
                Files.createDirectory(highscoresDir);
            }

            // Load existing highscores
            List<Highscore> highscores = loadHighscores();

            // Remove any existing highscore for the same city
            highscores.removeIf(h -> h.getCityName().equals(highscore.getCityName()));

            // Add the new highscore
            highscores.add(highscore);

            // Sort by score (descending)
            Collections.sort(highscores);
            if (highscores.size() > MAX_HIGHSCORES) {
                highscores = highscores.subList(0, MAX_HIGHSCORES);
            }

            // Save the updated highscores
            File file = new File(highscoresDir.toFile(), HIGHSCORES_FILE);
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
                for (Highscore h : highscores) {
                    // Format: cityName,score,families,budget,satisfaction,days,achievedAt
                    writer.write(String.format("%s,%d,%d,%d,%d,%d,%s%n",
                            h.cityName,
                            h.score,
                            h.families,
                            h.budget,
                            h.satisfaction,
                            h.days,
                            h.achievedAt.format(DATE_FORMATTER)));
                }
            }
            System.out.println("Highscore saved to " + file.getAbsolutePath());
            return true;
        } catch (IOException e) {
            System.err.println("Failed to save highscore: " + e.getMessage());
            return false;
        }
    }

    public static List<Highscore> loadHighscores() {
        Path highscoresDir = Paths.get(HIGHSCORES_DIR);
        File file = new File(highscoresDir.toFile(), HIGHSCORES_FILE);

        if (!file.exists()) {
            System.out.println("Highscores file not found, returning empty list");
            return new ArrayList<>();
        }

        List<Highscore> highscores = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                try {
                    String[] parts = line.split(",");
                    if (parts.length >= 8) {
                        String cityName = parts[1];
                        int score = Integer.parseInt(parts[2]);
                        int families = Integer.parseInt(parts[3]);
                        int budget = Integer.parseInt(parts[4]);
                        int satisfaction = Integer.parseInt(parts[5]);
                        int days = Integer.parseInt(parts[6]);
                        LocalDateTime achievedAt = LocalDateTime.parse(parts[7], DATE_FORMATTER);

                        highscores.add(new Highscore(cityName, score, families, budget, satisfaction, days, achievedAt));
                    } else if (parts.length >= 7) {
                        String cityName = parts[0];
                        int score = Integer.parseInt(parts[1]);
                        int families = Integer.parseInt(parts[2]);
                        int budget = Integer.parseInt(parts[3]);
                        int satisfaction = Integer.parseInt(parts[4]);
                        int days = Integer.parseInt(parts[5]);
                        LocalDateTime achievedAt = LocalDateTime.parse(parts[6], DATE_FORMATTER);

                        highscores.add(new Highscore(cityName, score, families, budget, satisfaction, days, achievedAt));
                    }
                } catch (Exception e) {
                    System.err.println("Error parsing highscore line: " + line + " - " + e.getMessage());
                }
            }
            Collections.sort(highscores);
            return highscores;
        } catch (IOException e) {
            System.err.println("Failed to load highscores: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    public static int getRank(int score) {
        List<Highscore> highscores = loadHighscores();

        // If the list is empty, the score is #1
        if (highscores.isEmpty()) {
            return 1;
        }

        // Check each highscore
        for (int i = 0; i < highscores.size(); i++) {
            if (score >= highscores.get(i).getScore()) {
                return i + 1;
            }
        }

        // If the list is not full, the score can still make it
        if (highscores.size() < MAX_HIGHSCORES) {
            return highscores.size() + 1;
        }

        // The score is not in the top MAX_HIGHSCORES
        return -1;
    }


    public String getCityName() {
        return cityName;
    }

    public int getScore() {
        return score;
    }

    public LocalDateTime getAchievedAt() {
        return achievedAt;
    }

    public int getFamilies() {
        return families;
    }

    public int getBudget() {
        return budget;
    }

    public int getSatisfaction() {
        return satisfaction;
    }

    public int getDays() {
        return days;
    }

    public String getFormattedAchievedTime() {
        return achievedAt.format(DATE_FORMATTER);
    }

    @Override
    public int compareTo(Highscore other) {
        // Sort by score (descending)
        return Integer.compare(other.score, this.score);
    }

    @Override
    public String toString() {
        return String.format("%s: %d points (Families: %d, Budget: $%d, Satisfaction: %d%%, Days: %d) - %s",
                cityName, score, families, budget, satisfaction, days, getFormattedAchievedTime());
    }
}
