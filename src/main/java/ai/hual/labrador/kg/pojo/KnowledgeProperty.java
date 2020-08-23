package ai.hual.labrador.kg.pojo;

public class KnowledgeProperty {
    private String uri;
    private String label;
    private String type;


    public KnowledgeProperty(String uri, String label, String type) {
        this.uri = uri;
        this.label = label;
        this.type = type;

    }

    public String getUri() {
        return uri;
    }

    public void setUri(String uri) {
        this.uri = uri;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }


    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}

