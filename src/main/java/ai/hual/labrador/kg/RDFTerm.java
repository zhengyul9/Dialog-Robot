package ai.hual.labrador.kg;

/**
 * An RDFTerm entity in SPARQL query result binding.
 * See {@link SelectResult} for more information.
 * Created by Dai Wentao on 2017/5/8.
 */
public class RDFTerm {
    private RDFTermType type;
    private String value;
    private String lang;
    private String dataType;

    public RDFTermType getType() {
        return type;
    }

    public void setType(RDFTermType type) {
        this.type = type;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getLang() {
        return lang;
    }

    public void setLang(String lang) {
        this.lang = lang;
    }

    public String getDataType() {
        return dataType;
    }

    public void setDataType(String dataType) {
        this.dataType = dataType;
    }
}
