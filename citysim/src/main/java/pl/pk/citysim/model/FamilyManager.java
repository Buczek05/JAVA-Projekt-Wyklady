/**
 * Manages the families in the city simulation.
 * This class is responsible for creating, updating, and managing Family objects.
 */
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

        // Create initial families
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

    /**
     * Updates the income of all families based on city conditions.
     *
     * @param jobQualityRatio The ratio of commercial to total jobs (higher is better)
     * @param jobRatio The ratio of available jobs to families (1.0 means enough jobs)
     * @param educationRatio The ratio of education capacity to families (1.0 means enough education)
     * @param difficultyScaling The difficulty scaling factor
     * @return The total income of all families
     */
    public int updateFamilyIncomes(double jobQualityRatio, double jobRatio, double educationRatio, double difficultyScaling) {
        int totalIncome = 0;
        for (Family family : families) {
            totalIncome += family.updateIncome(jobQualityRatio, jobRatio, educationRatio, difficultyScaling);
        }
        return totalIncome;
    }

    /**
     * Gets the total income of all families.
     *
     * @return The total income
     */
    public int getTotalIncome() {
        int totalIncome = 0;
        for (Family family : families) {
            totalIncome += family.getIncome();
        }
        return totalIncome;
    }

    /**
     * Gets the average income per family.
     *
     * @return The average income, or 0 if there are no families
     */
    public int getAverageIncome() {
        if (families.isEmpty()) {
            return 0;
        }
        return getTotalIncome() / families.size();
    }
}
