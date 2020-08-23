package ai.hual.labrador.nlu.constants;

public class SystemIntents {

    public static final String UNKNOWN = "sys.unknown";
    public static final String GREETING = "sys.greeting";
    public static final String THANK = "sys.thank";
    public static final String BYE = "sys.bye";
    public static final String REPEATED = "sys.repeated";

    public static final String KNOWLEDGE_QUERY = "sys.knowledge_query";

    public static final String KNOWLEDGE_QUERY_SLOT_TARGET = "target";
    public static final String KNOWLEDGE_QUERY_TARGET_VALUE_TYPE = "type";
    public static final String KNOWLEDGE_QUERY_TARGET_VALUE_SUBCLASSES = "subclasses";
    public static final String KNOWLEDGE_QUERY_TARGET_VALUE_INSTANCES = "instances";
    public static final String KNOWLEDGE_QUERY_TARGET_VALUE_PROPERTY = "property";
    public static final String KNOWLEDGE_QUERY_TARGET_VALUE_ENUM = "EnumProperty";

    public static final String KNOWLEDGE_QUERY_SLOT_CLASS = "class";
    public static final String KNOWLEDGE_QUERY_SLOT_DATATYPE = "datatype";
    public static final String KNOWLEDGE_QUERY_SLOT_OBJECT = "object";

    public static final String KNOWLEDGE_QUERY_SLOT_YSHAPE = "YshapeProperty";
    public static final String KNOWLEDGE_QUERY_SLOT_DIFFUSION = "DiffusionProperty";
    public static final String KNOWLEDGE_QUERY_SLOT_CONDITION = "ConditionProperty";
    public static final String KNOWLEDGE_QUERY_SLOT_HUAL_OBJECT = "HualObjectProperty";

    public static final String CHAT_INTENT = "sys.chat";
    public static final String FAQ_INTENT = "sys.faq";
    public static final String CLASSIFIER_UNKNOWN_INTENT = "sys.unknown";
    public static final String FAQ_UNKNOWN_INTENT = "sys.unknown";
    public static final String CHAT_UNKNOWN_INTENT = "sys.unknown";

    public static final String FAQ_SLOT_QUESTION = "question";
    public static final String FAQ_SLOT_RESULT = "result";

}
