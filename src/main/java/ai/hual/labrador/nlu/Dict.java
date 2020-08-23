package ai.hual.labrador.nlu;

import java.io.Serializable;
import java.util.Objects;

/**
 * A word of dict
 * Created by Dai Wentao on 2017/6/28.
 */
public class Dict implements Serializable {

    /**
     * The delimiter between individual words in aliases
     */
    public static final String ALIAS_DELIMITER = ",";

    /**
     * The regex that defines valid aliases.
     */
    public static final String ALIASES_VALIDATION_REGEX = "^(\\S|\\S.*\\S)(,(\\S|\\S.*\\S))*$";

    /**
     * The label of this word
     */
    private String label;

    /**
     * The word of this word
     */
    private String word;

    /**
     * The aliases of the word. Delimited by ALIAS_DELIMITER.
     * null if no aliases is defined.
     */
    private String aliases;

    public Dict() {
    }

    public Dict(Dict dict) {
        this.label = dict.label;
        this.word = dict.word;
        this.aliases = dict.aliases;
    }

    /**
     * Constructor for creating NLU resource, with no alias.
     *
     * @param label The label of the word
     * @param word  The word of the word
     */
    public Dict(String label, String word) {
        this(label, word, null);
    }

    /**
     * Constructor for creating NLU resource.
     *
     * @param label   The label of the word
     * @param word    The word of the word
     * @param aliases The aliases of the word
     */
    public Dict(String label, String word, String aliases) {
        this.label = label;
        this.word = word;
        this.aliases = aliases;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getWord() {
        return word;
    }

    public void setWord(String word) {
        this.word = word;
    }

    public String getAliases() {
        return aliases;
    }

    public void setAliases(String aliases) {
        this.aliases = aliases;
    }

    @Override
    public boolean equals(Object o) {

        if (o == this) return true;
        if (!(o instanceof Dict)) return false;
        Dict other = (Dict) o;
        return Objects.equals(this.label, other.label) && Objects.equals(this.word, other.word) &&
                Objects.equals(this.aliases, other.aliases);
    }

    @Override
    public int hashCode() {
        return this.toString().hashCode();
    }

    public String toString() {
        return "label: " + this.label + ", word: " + this.word + ", aliases: " + this.aliases;
    }

    public String[] getAliasesArray() {
        return aliases == null ? new String[0] : aliases.split(ALIAS_DELIMITER);
    }
}
