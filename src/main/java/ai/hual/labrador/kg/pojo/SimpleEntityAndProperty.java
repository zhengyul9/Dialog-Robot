package ai.hual.labrador.kg.pojo;

public class SimpleEntityAndProperty {
    private String sIri;
    private String sLabel;
    private String propertyiri;
    private String pLabel;

    public SimpleEntityAndProperty(String sIri, String sLabel, String propertyiri, String pLabel) {
        this.sIri = sIri;
        this.sLabel = sLabel;
        this.propertyiri = propertyiri;
        this.pLabel = pLabel;


    }

    public String getsIri() {
        return sIri;
    }

    public void setsIri(String sIri) {
        this.sIri = sIri;
    }

    public String getsLabel() {
        return sLabel;
    }

    public void setsLabel(String sLabel) {
        this.sLabel = sLabel;
    }

    public String getPropertyiri() {
        return propertyiri;
    }

    public void setPropertyiri(String propertyiri) {
        this.propertyiri = propertyiri;
    }

    public String getpLabel() {
        return pLabel;
    }

    public void setpLabel(String pLabel) {
        this.pLabel = pLabel;
    }


}
