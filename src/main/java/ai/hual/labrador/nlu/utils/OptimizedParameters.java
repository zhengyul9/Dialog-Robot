package ai.hual.labrador.nlu.utils;

import java.util.Arrays;

public class OptimizedParameters {
    private double[] alpha;
    private double[] refuseScore;
    private double percentage;


    public double[] getAlpha() {
        return Arrays.copyOf(alpha, alpha.length);
    }

    public void setAlpha(double[] alpha) {
        this.alpha = Arrays.copyOf(alpha, alpha.length);
    }

    public double[] getRefuseScore() {
        return Arrays.copyOf(refuseScore, refuseScore.length);
    }

    public void setRefuseScore(double[] refuseScore) {
        this.refuseScore = Arrays.copyOf(refuseScore, refuseScore.length);
    }

    public double getPercentage() {
        return percentage;
    }

    public void setPercentage(double percentage) {
        this.percentage = percentage;
    }
}
