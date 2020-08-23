package ai.hual.labrador.kg;

/**
 * The type of RDFTerm.
 * See {@link RDFTerm}
 * Created by Dai Wentao on 2017/5/8.
 */
public enum RDFTermType {
    uri, literal, bnode;

    public static RDFTermType parse(String type) {

        if ("uri".equals(type)) {
            return uri;
        }
        if ("literal".equals(type) || "typed-literal".equals(type)) {
            return literal;
        }
        if ("bnode".equals(type)) {
            return bnode;
        }
        return null;
    }
}
