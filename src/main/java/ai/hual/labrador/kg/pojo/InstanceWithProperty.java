package ai.hual.labrador.kg.pojo;


public class InstanceWithProperty {
    private String entityUri;
    private String entityLabel;
    private String propertyIri;
    private String value;
    private String objectLabel;
    private String bnLabel;

    public InstanceWithProperty(String entityUri, String entityLabel, String propertyIri, String value, String objectLabel, String bnLabel) {
        this.entityUri = entityUri;
        this.entityLabel = entityLabel;
        this.propertyIri = propertyIri;
        this.value = value;
        this.objectLabel = objectLabel;
        this.bnLabel = bnLabel;
    }

    public String getEntityUri() {
        return entityUri;
    }

    public void setEntityUri(String entityUri) {
        this.entityUri = entityUri;
    }

    public String getEntityLabel() {
        return entityLabel;
    }

    public void setEntityLabel(String entityLabel) {
        this.entityLabel = entityLabel;
    }

    public String getPropertyIri() {
        return propertyIri;
    }

    public void setPropertyIri(String propertyIri) {
        this.propertyIri = propertyIri;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getObjectLabel() {
        return objectLabel;
    }

    public void setObjectLabel(String objectLabel) {
        this.objectLabel = objectLabel;
    }

    public String getBnLabel() {
        return bnLabel;
    }

    public void setBnLabel(String bnLabel) {
        this.bnLabel = bnLabel;
    }

}
