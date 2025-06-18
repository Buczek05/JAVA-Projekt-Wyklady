package pl.pk.citysim.model;

import java.util.Random;

public class Family {
    private int income;
    private boolean employed;
    
    public Family() {
        this.income = 0;
        this.employed = false;
    }
    
    public Family(Random random) {
        this.income = 40 + random.nextInt(21);
        this.employed = random.nextDouble() < 0.8;
    }
    
    public int getIncome() {
        return income;
    }
    
    public void setIncome(int income) {
        this.income = income;
    }
    
    public boolean isEmployed() {
        return employed;
    }
    
    public void setEmployed(boolean employed) {
        this.employed = employed;
    }
    
    public int updateIncome(double jobQualityRatio, double jobRatio, double educationRatio, double difficultyScaling) {
        int updatedIncome = income;
        updatedIncome += (int) (updatedIncome * jobQualityRatio * 0.3);
        if (jobRatio < 1.0) {
            int jobShortagePenalty = (int) (updatedIncome * (1 - jobRatio) * 0.4); // Up to 40% penalty
            updatedIncome -= jobShortagePenalty;
            if (!employed) {
                updatedIncome = (int)(updatedIncome * 0.6); // Unemployed families earn less
            } else if (Math.random() > jobRatio) {
                employed = false;
                updatedIncome = (int)(updatedIncome * 0.6); // Newly unemployed families earn less
            }
        } else if (!employed && Math.random() < 0.2) {
            employed = true;
            updatedIncome = (int)(updatedIncome * 1.5); // Newly employed families earn more
        }
        int educationBonus = (int) (updatedIncome * educationRatio * 0.25); // Up to 25% bonus
        updatedIncome += educationBonus;
        updatedIncome = (int)(updatedIncome * difficultyScaling);
        income = updatedIncome;
        
        return income;
    }
}