package ai.hual.labrador.kg.pojo;

public class RangeInstance {
    private String entityuri;
    private String entityLabel;
    private String bnLabel;


    public RangeInstance(String entityuri, String entityLabel, String bnLabel) {
        this.entityuri = entityuri;
        this.entityLabel = entityLabel;
        this.bnLabel = bnLabel;

    }

    public String getEntityuri() {
        return entityuri;
    }

    public void setEntityuri(String entityuri) {
        this.entityuri = entityuri;
    }

    public String getEntityLabel() {
        return entityLabel;
    }

    public void setEntityLabel(String entityLabel) {
        this.entityLabel = entityLabel;
    }


    public String getBnLabel() {
        return bnLabel;
    }

    public void setBnLabel(String bnLabel) {
        this.bnLabel = bnLabel;
    }
}
