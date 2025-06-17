/**
 * Represents a family in the city simulation.
 * Each family has its own income and other attributes.
 */
package pl.pk.citysim.model;

import java.util.Random;

public class Family {
    private int income;
    private boolean employed;
    
    /**
     * Default constructor for Jackson deserialization.
     */
    public Family() {
        this.income = 0;
        this.employed = false;
    }
    
    /**
     * Creates a new family with randomly generated attributes.
     */
    public Family(Random random) {
        // Base family income (between 40 and 60)
        this.income = 40 + random.nextInt(21);
        
        // 80% chance of being employed initially
        this.employed = random.nextDouble() < 0.8;
    }
    
    /**
     * Gets the family's income.
     * 
     * @return The family's income
     */
    public int getIncome() {
        return income;
    }
    
    /**
     * Sets the family's income.
     * 
     * @param income The new income
     */
    public void setIncome(int income) {
        this.income = income;
    }
    
    /**
     * Checks if the family is employed.
     * 
     * @return true if the family is employed, false otherwise
     */
    public boolean isEmployed() {
        return employed;
    }
    
    /**
     * Sets the family's employment status.
     * 
     * @param employed The new employment status
     */
    public void setEmployed(boolean employed) {
        this.employed = employed;
    }
    
    /**
     * Updates the family's attributes based on city conditions.
     * 
     * @param jobQualityRatio The ratio of commercial to total jobs (higher is better)
     * @param jobRatio The ratio of available jobs to families (1.0 means enough jobs)
     * @param educationRatio The ratio of education capacity to families (1.0 means enough education)
     * @param difficultyScaling The difficulty scaling factor
     * @return The updated income
     */
    public int updateIncome(double jobQualityRatio, double jobRatio, double educationRatio, double difficultyScaling) {
        // Start with base income
        int updatedIncome = income;
        
        // Apply job quality bonus (up to 30% more income for high commercial ratio)
        updatedIncome += (int) (updatedIncome * jobQualityRatio * 0.3);
        
        // Job shortage penalty (not enough jobs reduces income)
        if (jobRatio < 1.0) {
            int jobShortagePenalty = (int) (updatedIncome * (1 - jobRatio) * 0.4); // Up to 40% penalty
            updatedIncome -= jobShortagePenalty;
            
            // Update employment status based on job ratio
            // If there aren't enough jobs, some families become unemployed
            if (!employed) {
                // Unemployed families stay unemployed
                updatedIncome = (int)(updatedIncome * 0.6); // Unemployed families earn less
            } else if (Math.random() > jobRatio) {
                // Some employed families become unemployed
                employed = false;
                updatedIncome = (int)(updatedIncome * 0.6); // Newly unemployed families earn less
            }
        } else if (!employed && Math.random() < 0.2) {
            // Some unemployed families find jobs when there are enough jobs
            employed = true;
            updatedIncome = (int)(updatedIncome * 1.5); // Newly employed families earn more
        }
        
        // Education bonus (better education = higher income)
        int educationBonus = (int) (updatedIncome * educationRatio * 0.25); // Up to 25% bonus
        updatedIncome += educationBonus;
        
        // Apply difficulty scaling
        updatedIncome = (int)(updatedIncome * difficultyScaling);
        
        // Update the income
        income = updatedIncome;
        
        return income;
    }
}