package ai.hual.labrador.kg.pojo;

public class ConditionAndUndercondition {
    private String conditionLabel;
    private String underConditionIri;
    private String underConditionLabel;
    private String bnIri;

    public ConditionAndUndercondition(String conditionLabel, String underConditionIri, String underConditionLabel, String bnIri) {
        this.conditionLabel = conditionLabel;
        this.underConditionIri = underConditionIri;
        this.underConditionLabel = underConditionLabel;
        this.bnIri = bnIri;

    }

    public String getConditionLabel() {
        return conditionLabel;
    }

    public void setConditionLabel(String conditionLabel) {
        this.conditionLabel = conditionLabel;
    }

    public String getUnderConditionIri() {
        return underConditionIri;
    }

    public void setUnderConditionIri(String underConditionIri) {
        this.underConditionIri = underConditionIri;
    }

    public String getUnderConditionLabel() {
        return underConditionLabel;
    }

    public void setUnderConditionLabel(String underConditionLabel) {
        this.underConditionLabel = underConditionLabel;
    }

    public String getBnIri() {
        return bnIri;
    }

    public void setBnIri(String bnIri) {
        this.bnIri = bnIri;
    }

}
