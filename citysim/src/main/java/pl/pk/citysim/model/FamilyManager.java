package pl.pk.citysim.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class FamilyManager {
    private List<Family> families;
    private Random random;

    public FamilyManager() {
        this.families = new ArrayList<>();
        this.random = new Random();
    }

    public FamilyManager(int initialFamilies) {
        this.families = new ArrayList<>();
        this.random = new Random();
        for (int i = 0; i < initialFamilies; i++) {
            addFamily();
        }
    }

    public int getFamiliesCount() {
        return families.size();
    }

    public List<Family> getFamilies() {
        return new ArrayList<>(families);
    }

    public Family addFamily() {
        Family family = new Family(random);
        families.add(family);
        return family;
    }

    public int removeFamilies(int count) {
        int actualCount = Math.min(count, families.size());
        for (int i = 0; i < actualCount; i++) {
            families.remove(families.size() - 1);
        }
        return actualCount;
    }

    public int updateFamilyIncomes(double jobQualityRatio, double jobRatio, double educationRatio, double difficultyScaling) {
        int totalIncome = 0;
        for (Family family : families) {
            totalIncome += family.updateIncome(jobQualityRatio, jobRatio, educationRatio, difficultyScaling);
        }
        return totalIncome;
    }

    public int getTotalIncome() {
        int totalIncome = 0;
        for (Family family : families) {
            totalIncome += family.getIncome();
        }
        return totalIncome;
    }

    public int getAverageIncome() {
        if (families.isEmpty()) {
            return 0;
        }
        return getTotalIncome() / families.size();
    }
}
