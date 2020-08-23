package ai.hual.labrador.kg.pojo;

public class BnAndDps {
    private String bnIri;
    private String bnLabel;
    private String dpIri;
    private String dpLabel;
    private String value;

    public BnAndDps(String bnIri, String bnLabel, String dpIri, String dpLabel, String value) {
        this.bnIri = bnIri;
        this.bnLabel = bnLabel;
        this.dpIri = dpIri;
        this.dpLabel = dpLabel;
        this.value = value;

    }

    public String getBnIri() {
        return bnIri;
    }

    public void setBnIri(String bnIri) {
        this.bnIri = bnIri;
    }

    public String getBnLabel() {
        return bnLabel;
    }

    public void setBnLabel(String bnLabel) {
        this.bnLabel = bnLabel;
    }

    public String getDpIri() {
        return dpIri;
    }

    public void setDpIri(String dpIri) {
        this.dpIri = dpIri;
    }

    public String getDpLabel() {
        return dpLabel;
    }

    public void setDpLabel(String dpLabel) {
        this.dpLabel = dpLabel;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

}
