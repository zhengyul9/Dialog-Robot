package ai.hual.labrador.nlu;

import java.io.Serializable;

/**
 * The type of grammar
 * Created by Dai Wentao on 2017/5/6.
 * Updated by Dai Wentao on 2017/7/3. Moved from labrador_database to labrador_nlu.
 */
public enum GrammarType implements Serializable {
    // Regex to represent a intent.
    INTENT_REGEX,

    // Regex to represent a phrase, eg: 不要taste->dislike[taste]
    PHRASE_REGEX,

//新的模板类型
    INTENT_SYNTPL;
}
