package ai.hual.labrador.nlg;

import java.io.Serializable;

/**
 * A template to render a natural language sentence.
 * Created by Dai Wentao on 2017/7/5.
 */
public class Template implements Serializable {

    /**
     * The label of the template
     */
    private String label;

    /**
     * The content of the template
     */
    private String content;

    public Template(String label, String content) {
        this.label = label;
        this.content = content;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
}
