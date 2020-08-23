package ai.hual.labrador.kg.pojo;

public class YshapeAnotherAndBn {
    private String iri;
    private String label;
    private String bnIri;

    public YshapeAnotherAndBn(String uri, String label, String bnIri) {
        this.iri = uri;
        this.label = label;
        this.bnIri = bnIri;
    }

    public String getUri() {
        return iri;
    }

    public void setUri(String iri) {
        this.iri = iri;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getBnIri() {
        return bnIri;
    }

    public void setBnIri(String bnIri) {
        this.bnIri = bnIri;
    }
}
