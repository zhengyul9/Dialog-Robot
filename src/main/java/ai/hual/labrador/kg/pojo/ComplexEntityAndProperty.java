package ai.hual.labrador.kg.pojo;

public class ComplexEntityAndProperty {
    private String bn;
    private String bnLabel;
    private String dp;
    private String dpLabel;
    private String cLabel;
    private String cClassLabel;

    public ComplexEntityAndProperty(String bn, String bnLabel, String dp, String dpLabel, String cLabel, String cClassLabel) {
        this.bn = bn;
        this.bnLabel = bnLabel;
        this.dp = dp;
        this.dpLabel = dpLabel;
        this.cLabel = cLabel;
        this.cClassLabel = cClassLabel;


    }

    public String getBn() {
        return bn;
    }

    public void setBn(String bn) {
        this.bnLabel = bn;
    }

    public String getBnLabel() {
        return bnLabel;
    }

    public void setBnLabel(String bnLabel) {
        this.bnLabel = bnLabel;
    }

    public String getDp() {
        return dp;
    }

    public void setDp(String dp) {
        this.dp = dp;
    }

    public String getDpLabel() {
        return dpLabel;
    }

    public void setDpLabel(String dpLabel) {
        this.dpLabel = dpLabel;
    }

    public String getCLabel() {
        return cLabel;
    }

    public void setCLabel(String cLabel) {
        this.cLabel = cLabel;
    }

    public String getcClassLabel() {
        return cClassLabel;
    }

    public void setcClassLabel(String cClassLabel) {
        this.cClassLabel = cClassLabel;
    }

}
