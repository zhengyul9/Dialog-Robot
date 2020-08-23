package ai.hual.labrador.train;

/**
 * The field content is a sentence in text format, intent is the classification label of this corpus, and annotation
 * is the entity annotated on this corpus (like date, location, or entity in KG) which is in JSON format of: <br/>
 * [{
 * "from":     integer(The index where this annotation starts), <br/>
 * "to":       integer(The index where this annotation ends), <br/>
 * "category": string(category of the annotated area. dict/kg/sys), <br/>
 * "type":     string(dict name, kg class label or system slot name of the annotated area) <br/>
 * "value":    string(The value of the annotation with given type. only used in attached intent parameter)<br/>
 * }, ... ]
 * Created by Dai Wentao on 2017/7/3.
 */
public class CorpusAnnotation {

    public static final String CATEGORY_DICT = "dict";
    public static final String CATEGORY_KG = "kg";
    public static final String CATEGORY_SYS = "sys";

    /**
     * The index where this annotation starts
     */
    private int from;

    /**
     * The index where this annotation ends
     */
    private int to;

    /**
     * category of the annotated area. dict/kg/sys
     */
    private String category;

    /**
     * dict name, kg class label or system slot name of the annotated area
     */
    private String type;

    /**
     * The value of the annotation with given type. only used in attached intent parameter.
     */
    private String value;

    public CorpusAnnotation() {

    }

    public CorpusAnnotation(int from, int to, String category, String type) {
        this.from = from;
        this.to = to;
        this.category = category;
        this.type = type;
    }

    public CorpusAnnotation(int from, int to, String category, String type, String value) {
        this(from, to, category, type);
        this.value = value;
    }

    public int getFrom() {
        return from;
    }

    public void setFrom(int from) {
        this.from = from;
    }

    public int getTo() {
        return to;
    }

    public void setTo(int to) {
        this.to = to;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}
