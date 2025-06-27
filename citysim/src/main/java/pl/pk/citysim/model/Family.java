package pl.pk.citysim.model;

import java.util.Random;

public class Family {
    private int income;
    private boolean employed;

    public Family(Random random) {
        this.income = 60 + random.nextInt(61);
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
    
    public int calculateIncome(double jobQualityRatio, double jobRatio, double educationRatio, double difficultyScaling) {
        int calculatedIncome = income;
        calculatedIncome += (int) (calculatedIncome * jobQualityRatio * 0.3);
        if (jobRatio < 1.0) {
            int jobShortagePenalty = (int) (calculatedIncome * (1 - jobRatio) * 0.4); // Up to 40% penalty
            calculatedIncome -= jobShortagePenalty;
            if (!employed) {
                calculatedIncome = (int)(calculatedIncome * 0.6); // Unemployed families earn less
            } else if (Math.random() > jobRatio) {
                employed = false;
                calculatedIncome = (int)(calculatedIncome * 0.6); // Newly unemployed families earn less
            }
        } else if (!employed && Math.random() < 0.2) {
            employed = true;
            calculatedIncome = (int)(calculatedIncome * 1.5); // Newly employed families earn more
        }
        int educationBonus = (int) (calculatedIncome * educationRatio * 0.25); // Up to 25% bonus
        calculatedIncome += educationBonus;
        calculatedIncome = (int)(calculatedIncome * difficultyScaling);
        return calculatedIncome;
    }
}