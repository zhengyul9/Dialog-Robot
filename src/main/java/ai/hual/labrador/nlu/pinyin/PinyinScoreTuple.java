package ai.hual.labrador.nlu.pinyin;

/**
 * This class constructed a tuple of pinyin and
 * its score, used to represent a pinyin's robust
 * synonym. Score is the likelihood for a pinyin
 * to transform to its synonym.
 * e.g
 * "rou" : PinyinScoreTuple("lou", 0.7)
 */
public class PinyinScoreTuple {
    private String pinyin;
    private double score;

    public PinyinScoreTuple(String pinyin, double score) {
        this.pinyin = pinyin;
        this.score = score;
    }

    public String getPinyin() {
        return this.pinyin;
    }

    public double getScore() {
        return this.score;
    }

    public String toString() {
        return "pinyin: " + pinyin + " | score: " + score;
    }
}
