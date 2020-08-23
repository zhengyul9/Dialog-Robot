package ai.hual.labrador.nlu.annotators;

import javax.annotation.Nonnull;
import java.util.regex.Pattern;

/**
 * A compiled regex pattern with label and score.
 * Created by Dai Wentao on 2017/7/12.
 */
public class LabeledRegex<T> implements Comparable<LabeledRegex> {

    private Pattern regex;
    private String key;
    private T label;
    private double score;

    public LabeledRegex(String regex, String key, T label) {
        this(regex, key, label, 1f);
    }

    public LabeledRegex(String regex, String key, T label, double score) {
        this.regex = Pattern.compile(regex);
        this.key = key;
        this.label = label;
        this.score = score;
    }

    public Pattern getRegex() {
        return regex;
    }

    public void setRegex(Pattern regex) {
        this.regex = regex;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public T getLabel() {
        return label;
    }

    public void setLabel(T label) {
        this.label = label;
    }

    public double getScore() {
        return score;
    }

    public void setScore(double score) {
        this.score = score;
    }

    public String toString() {
        return "Regex: " + regex + ", Key: " + key + ", " + "Label: " + label;
    }

    @Override
    public int compareTo(@Nonnull LabeledRegex other) {
        return Double.compare(this.score, other.score);
    }

    @Override
    public boolean equals(Object b) {
        return this == b;
    }
}
