package ai.hual.labrador.dm;

/**
 * This class stores the params in DialogConfig.
 */
public class Param {

    /**
     * Key.
     */
    private String key;

    /**
     * Value.
     */
    private String value;

    public Param() {
    }

    public Param(String key, String value) {
        this.key = key;
        this.value = value;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}
