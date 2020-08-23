package ai.hual.labrador.nlu.trie;

/**
 * This class is used to represent an emit instance
 * when using Trie to parsing text.
 *
 * @author Yuqi
 * @see Trie
 * @since 1.8
 */
public class Emit {

    private Object content; // content attached to TrieNode
    private int start;  // start position in text
    private int end;    // end position in text
    private int level;  // level in trie
    private double score;   // final score

    public Emit() {
        this(null, 0, 0, 0, 0.0);
    }

    public Emit(Object content, int start, int end) {
        this(content, start, end, 0, 1f);
    }

    public Emit(Object content, int start, int end, int level) {
        this(content, start, end, level, 1f);
    }

    public Emit(Object content, int start, int end, int level, double score) {
        this.content = content;
        this.start = start;
        this.end = end;
        this.level = level;
        this.score = score;
    }

    public Object getContent() {
        return content;
    }

    public void setContent(Object content) {
        this.content = content;
    }

    public int getStart() {
        return start;
    }

    public void setStart(int start) {
        this.start = start;
    }

    public int getEnd() {
        return end;
    }

    public void setEnd(int end) {
        this.end = end;
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public double getScore() {
        return score;
    }

    public void setScore(double score) {
        this.score = score;
    }

    public String toString() {
        return content.toString() + " | start: " + start + ", end: " + end + ", score: " + score;
    }
}
