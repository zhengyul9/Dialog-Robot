package ai.hual.labrador.nlu;

import java.io.Serializable;

/**
 * A piece of grammar used in NLU.
 * Created by Dai Wentao on 2017/7/3.
 */
public class Grammar implements Serializable {

    /**
     * The type of the grammar
     */
    private GrammarType type;

    /**
     * The label of the grammar. For intent regex, this is the intent type. Others like the slot name, eg: {{数字}}
     */
    private String label;

    /**
     * The content of the grammar. For regex pattern, this is the regex.
     */
    private String content;

    /**
     * The score of this grammar.
     */
    private double score = 1d;


    public Grammar() {
    }

    /**
     * Constructor for creating NLU resource.
     *
     * @param type    The type of the grammar
     * @param label   The label of the grammar
     * @param content The content of the grammar
     */
    public Grammar(GrammarType type, String label, String content) {
        this(type, label, content, 1f);
    }

    /**
     * Constructor for creating NLU resource.
     *
     * @param type    The type of the grammar
     * @param label   The label of the grammar
     * @param content The content of the grammar
     * @param score   The score of the grammar
     */
    public Grammar(GrammarType type, String label, String content, double score) {
        this.type = type;
        this.label = label;
        this.content = content;
        this.score = score;
    }

    public GrammarType getType() {
        return type;
    }

    public void setType(GrammarType type) {
        this.type = type;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public double getScore() {
        return score;
    }

    public void setScore(double score) {
        this.score = score;
    }

    public String toString() {
        String str = "Type " + this.type + " | Label " + this.label + " | Content " +
                this.content + " | Score " + this.score;
        return str;
    }
}
