package ai.hual.labrador.dialog;

import ai.hual.labrador.kg.Binding;
import ai.hual.labrador.kg.KnowledgeAccessor;
import ai.hual.labrador.kg.SelectResult;
import ai.hual.labrador.nlu.Annotator;
import ai.hual.labrador.nlu.Dict;
import ai.hual.labrador.nlu.DictModel;
import ai.hual.labrador.nlu.Grammar;
import ai.hual.labrador.nlu.GrammarModel;
import ai.hual.labrador.nlu.GrammarType;
import ai.hual.labrador.nlu.constants.SystemIntents;
import ai.hual.labrador.nlu.utils.IntentLabelUtils;
import com.google.common.collect.HashBasedTable;
import com.google.common.collect.ImmutableListMultimap;
import com.google.common.collect.ListMultimap;
import com.google.common.collect.Table;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Properties;
import java.util.regex.Pattern;

/**
 * A handler that generate dict and grammar model by kg.
 * Created by Dai Wentao on 2017/6/28.
 */
public class KnowledgeHandler {

    private static final Logger logger = LoggerFactory.getLogger(KnowledgeHandler.class);

    // TODO add score to dict, to make rank of entity as sub class entity higher than as super class entity
//    private static final String ENTITY_QUERY = "SELECT DISTINCT ?entity_label ?class_label WHERE {\n" +
//            "?entity rdfs:label ?entity_label; a/rdfs:subClassOf* ?class.\n" +
//            "?class rdfs:label ?class_label. }";
    private static final String ENTITY_QUERY = "SELECT DISTINCT ?entity_label ?class_label WHERE {\n" +
            "?entity rdfs:label ?entity_label; a ?class.\n" +
            "?class rdfs:label ?class_label. }";

    private static final String BN_LABEL_IRI = "http://hual.ai/special#bnlabel";

    private static final String BN_QUERY = "SELECT DISTINCT ?bn_label ?class_label WHERE {\n" +
            "?bn <" + BN_LABEL_IRI + "> ?bn_label; a ?class. ?class rdfs:label ?class_label. }";

    private static final String CLASS_QUERY = "SELECT DISTINCT ?class_label WHERE {\n" +
            "?class a owl:Class; rdfs:label ?class_label . }";

    private static final String DATATYPE_QUERY = "SELECT DISTINCT ?datatype_label WHERE {\n" +
            "{ ?p rdf:type owl:DatatypeProperty } UNION { ?p rdf:type/rdfs:subClassOf* owl:DatatypeProperty . }\n" +
            "?p rdfs:label ?datatype_label . }";
    private static final String ENTITY_DATATYPE_QUERY = "SELECT DISTINCT ?domain_subclass_label ?property_label WHERE {\n" +
            "?p rdfs:domain ?domain_class ; rdfs:label ?property_label .\n" +
            "?domain_subclass rdfs:subClassOf* ?domain_class ; rdfs:label ?domain_subclass_label .\n" +
            "{ ?p rdf:type owl:DatatypeProperty } UNION { ?p rdf:type/rdfs:subClassOf* owl:DatatypeProperty . }\n" +
            "}";

    private static final String YSHAPE_QUERY = "SELECT DISTINCT ?label ?p WHERE {\n" +
            "{ ?p rdfs:subPropertyOf <http://hual.ai/standard#YshapeProperty> } UNION { ?p rdfs:subPropertyOf+ <http://hual.ai/standard#YshapeProperty> . }\n" +
            "?p rdfs:label ?label . }";
    private static final String ENTITY_YSHAPE_QUERY = "SELECT DISTINCT ?domain_subclass_label ?p ?property_label WHERE {\n" +
            "?p rdfs:subPropertyOf* <http://hual.ai/standard#YshapeProperty> .\n" +
            "?p rdfs:domain ?domain_class ; rdfs:label ?property_label .\n" +
            "?domain_subclass rdfs:subClassOf* ?domain_class ; rdfs:label ?domain_subclass_label .\n" +
            "}";

    private static final String DIFFUSION_QUERY = "SELECT DISTINCT ?label ?p WHERE {\n" +
            "{ ?p rdfs:subPropertyOf <http://hual.ai/standard#DiffusionProperty> } UNION { ?p rdfs:subPropertyOf+ <http://hual.ai/standard#DiffusionProperty> . }\n" +
            "?p rdfs:label ?label . }";
    private static final String ENTITY_DIFFUSION_QUERY = "SELECT DISTINCT ?domain_subclass_label ?p ?property_label WHERE {\n" +
            "?p rdfs:subPropertyOf* <http://hual.ai/standard#DiffusionProperty> .\n" +
            "?p rdfs:domain ?domain_class ; rdfs:label ?property_label .\n" +
            "?domain_subclass rdfs:subClassOf* ?domain_class ; rdfs:label ?domain_subclass_label .\n" +
            "}";

    private static final String CONDITION_QUERY = "SELECT DISTINCT ?label ?p WHERE {\n" +
            "{ ?p rdfs:subPropertyOf <http://hual.ai/standard#ConditionProperty> } UNION { ?p rdfs:subPropertyOf+ <http://hual.ai/standard#ConditionProperty> . }\n" +
            "?p rdfs:label ?label . }";
    private static final String ENTITY_CONDITION_QUERY = "SELECT DISTINCT ?domain_subclass_label ?p ?property_label WHERE {\n" +
            "?p rdfs:subPropertyOf* <http://hual.ai/standard#ConditionProperty> .\n" +
            "?p rdfs:domain ?domain_class ; rdfs:label ?property_label .\n" +
            "?domain_subclass rdfs:subClassOf* ?domain_class ; rdfs:label ?domain_subclass_label .\n" +
            "}";

    private static final String HUAL_OBJECT_QUERY = "SELECT DISTINCT ?label ?p WHERE {\n" +
            "{ ?p rdfs:subPropertyOf <http://hual.ai/standard#HualObjectProperty> } UNION { ?p rdfs:subPropertyOf+ <http://hual.ai/standard#HualObjectProperty> . }\n" +
            "?p rdfs:label ?label . }";
    private static final String ENTITY_HUAL_OBJECT_QUERY = "SELECT DISTINCT ?domain_subclass_label ?p ?property_label WHERE {\n" +
            "?p rdfs:subPropertyOf* <http://hual.ai/standard#HualObjectProperty> .\n" +
            "?p rdfs:domain ?domain_class ; rdfs:label ?property_label .\n" +
            "?domain_subclass rdfs:subClassOf* ?domain_class ; rdfs:label ?domain_subclass_label .\n" +
            "}";
    private static final String PROPERTY_QUERY = "SELECT DISTINCT ?pLabel ?TypeLabel WHERE{\n" +
            "VALUES (?pType ?TypeLabel) {(<http://hual.ai/new_standard#BooleanProperty> \"BooleanProperty\") (<http://hual.ai/new_standard#ComplexProperty> \"ComplexProperty\") (<http://hual.ai/new_standard#EnumProperty> \"EnumProperty\") (<http://hual.ai/new_standard#NumericalProperty> \"NumericalProperty\") (<http://hual.ai/new_standard#ObjectProperty> \"ObjectProperty\") (<http://hual.ai/new_standard#TextProperty> \"TextProperty\") (<http://hual.ai/new_standard#DateProperty> \"DateProperty\")} \n" +
            "?p a ?pType.\n" +
            "?p rdfs:label ?pLabel.\n" +
            "}";
    private static final String ENUM_VALUE_QUERY = "SELECT DISTINCT ?pLabel ?values WHERE{\n" +
            "?p a <http://hual.ai/new_standard#EnumProperty>.\n" +
            "?p rdfs:label ?pLabel.\n" +
            "?p <http://hual.ai/new_standard#has_value> ?values.\n" +
            "}";

    private static final String ENUM_QUERY = "SELECT DISTINCT ?EnumLabel WHERE{\n" +
            "?Enum a <http://hual.ai/new_standard#EnumProperty>.\n" +
            "?Enum rdfs:label ?EnumLabel.\n" +
            "}";

    public static final double QUERY_DATATYPE_SCORE = Double.MIN_VALUE * 200;
    /**
     * use a minimum score to make sure such an act's score smaller than minimum FAQ score
     *
     * @see ai.hual.labrador.utils.ScoreUtils test
     */
    private static final double QUERY_PROPERTY_SCORE = Double.MIN_VALUE * 2;
    private static final double QUERY_INSTANCES_SCORE = Double.MIN_VALUE * 3;
    private static final double QUERY_ENUM_SCORE = Double.MIN_VALUE * 30;

    private static final double QUERY_DATATYPE_STRICT_SCORE = 1d;
    private static final double QUERY_PROPERTY_STRICT_SCORE = 1d;
    private static final double QUERY_ENUM_STRICT_SCORE = 1d;

    public static final String KEY_GENERATE_CLASS_DICT = "knowledge.dict.class";
    public static final String DEFAULT_GENERATE_CLASS_DICT = String.valueOf(true);

    public static final String KEY_GENERATE_BN_DICT = "knowledge.dict.bn";
    public static final String DEFAULT_GENERATE_BN_DICT = String.valueOf(true);

    private KnowledgeAccessor kg;
    private Properties properties;

    public KnowledgeHandler(KnowledgeAccessor kg) {
        this(kg, new Properties());
    }

    public KnowledgeHandler(KnowledgeAccessor kg, Properties properties) {
        this.kg = kg;
        this.properties = properties;
    }

    /**
     * Generate model based on a given {@link DictModel}, modifying that base model and return it.
     * Get all labels and aliases from kg as dict.
     *
     * @param base The model that kg handler generation bases on
     * @return The modified base model
     */
    public DictModel handleDict(DictModel base) {
        // get all entity names (label or alias) and their classes' labels
        logger.debug("Generating dict for entity");
        Table<String, String, Dict> dictTable = HashBasedTable.create();
        base.getDict().forEach(d -> dictTable.put(d.getLabel(), d.getWord(), d));
        kg.select(ENTITY_QUERY).getBindings().forEach(x -> {
            Dict manuallyAddedCounterpart = dictTable.get(x.value("class_label"), x.value("entity_label"));
            if (manuallyAddedCounterpart == null)   // add a dict only when no manually added counterpart
                base.getDict().add(new Dict(x.value("class_label"), x.value("entity_label")));
        });

        // get all class labels
        if (Boolean.parseBoolean(properties.getProperty(KEY_GENERATE_CLASS_DICT, DEFAULT_GENERATE_CLASS_DICT))) {
            logger.debug("Generating dict for class");
            kg.selectOneAsList(CLASS_QUERY, "class_label").forEach(x -> base.getDict().add(
                    new Dict(SystemIntents.KNOWLEDGE_QUERY_SLOT_CLASS, x)));
        }

        // get all bn labels and their classes' labels
        logger.debug("Generating dict for blank node");
        if (Boolean.parseBoolean(properties.getProperty(KEY_GENERATE_BN_DICT, DEFAULT_GENERATE_BN_DICT))) {
            kg.select(BN_QUERY).getBindings().forEach(x -> base.getDict().add(
                    new Dict(x.value("class_label"), x.value("bn_label"))));
        }

        // get all PropertyLabel and their Types
        logger.debug("Generating dict for properties");
        kg.select(PROPERTY_QUERY).getBindings().forEach(x -> base.getDict().add(
                new Dict(x.value("TypeLabel"), x.value("pLabel"))));

        //get all EnumProperty_values
        logger.debug("Generating dict for enumProperty_values");
        kg.select(ENUM_VALUE_QUERY).getBindings().forEach(x -> base.getDict().add(
                new Dict(x.value("pLabel"), x.value("values"))));
        return base;

    }

    /**
     * Generate model based on a given {@link GrammarModel}, modifying that base model and return it.
     *
     * @param base The model that kg handler generation bases on
     * @return The modified base model
     */
    public GrammarModel handleGrammar(GrammarModel base) {
        // sys.knowledge_query?datatype=xxx
        logger.debug("Generating grammar for datatype properties");
        addDatatypePropertyGrammar(kg.selectOneAsList(DATATYPE_QUERY, "datatype_label"), base);
        addEntityDatatypePropertyGrammar(kg.select(ENTITY_DATATYPE_QUERY), base);

        // sys.knowledge_query?YshapeProperty=xxx
        logger.debug("Generating grammar for yshape properties");
        addObjectPropertyGrammar(kg.select(YSHAPE_QUERY), SystemIntents.KNOWLEDGE_QUERY_SLOT_YSHAPE, base);
        addEntityObjectPropertyGrammar(kg.select(ENTITY_YSHAPE_QUERY), SystemIntents.KNOWLEDGE_QUERY_SLOT_YSHAPE, base);

        // sys.knowledge_query?DiffusionProperty=xxx
        logger.debug("Generating grammar for diffusion properties");
        addObjectPropertyGrammar(kg.select(DIFFUSION_QUERY), SystemIntents.KNOWLEDGE_QUERY_SLOT_DIFFUSION, base);
        addEntityObjectPropertyGrammar(kg.select(ENTITY_DIFFUSION_QUERY), SystemIntents.KNOWLEDGE_QUERY_SLOT_DIFFUSION, base);

        // sys.knowledge_query?ConditionProperty=xxx
        logger.debug("Generating grammar for condition properties");
        addObjectPropertyGrammar(kg.select(CONDITION_QUERY), SystemIntents.KNOWLEDGE_QUERY_SLOT_CONDITION, base);
        addEntityObjectPropertyGrammar(kg.select(ENTITY_CONDITION_QUERY), SystemIntents.KNOWLEDGE_QUERY_SLOT_CONDITION, base);

        // sys.knowledge_query?HualObjectProperty=xxx
        logger.debug("Generating grammar for hual object properties");
        addObjectPropertyGrammar(kg.select(HUAL_OBJECT_QUERY), SystemIntents.KNOWLEDGE_QUERY_SLOT_HUAL_OBJECT, base);
        addEntityObjectPropertyGrammar(kg.select(ENTITY_HUAL_OBJECT_QUERY), SystemIntents.KNOWLEDGE_QUERY_SLOT_HUAL_OBJECT, base);

        // sys.knowledge_query?target=instances
        //new_standard still use
        logger.debug("Generating grammar for instance query");
        ListMultimap<String, String> queryInstancesSlots = ImmutableListMultimap.of(
                SystemIntents.KNOWLEDGE_QUERY_SLOT_TARGET, SystemIntents.KNOWLEDGE_QUERY_TARGET_VALUE_INSTANCES);
        String queryInstancesIntent = IntentLabelUtils.buildParamLabel(
                SystemIntents.KNOWLEDGE_QUERY, queryInstancesSlots);
        String queryInstancesContent = slot(quote(SystemIntents.KNOWLEDGE_QUERY_SLOT_CLASS));
        base.getGrammars().add(new Grammar(GrammarType.INTENT_REGEX, queryInstancesIntent, queryInstancesContent, QUERY_INSTANCES_SCORE));

        // sys.knowledge_query?target=property
        //new_standard still use
        logger.debug("Generating grammar for entity");
        ListMultimap<String, String> queryPropertySlots = ImmutableListMultimap.of(
                SystemIntents.KNOWLEDGE_QUERY_SLOT_TARGET, SystemIntents.KNOWLEDGE_QUERY_TARGET_VALUE_PROPERTY);
        String queryPropertyIntent = IntentLabelUtils.buildParamLabel(
                SystemIntents.KNOWLEDGE_QUERY, queryPropertySlots);
        for (String classLabel : kg.selectOneAsList(CLASS_QUERY, "class_label")) {
            String queryEnumContent = slot(quote(classLabel));
            base.getGrammars().add(new Grammar(GrammarType.INTENT_REGEX, queryPropertyIntent, queryEnumContent, QUERY_ENUM_SCORE));
            base.getGrammars().add(new Grammar(GrammarType.INTENT_REGEX, queryPropertyIntent, strict(queryEnumContent), QUERY_PROPERTY_STRICT_SCORE));
        }
        //sys.knowledge_query?target = EnumProperty
        //new_standard newly
        logger.debug("Generating grammar for EnumValues");
        ListMultimap<String, String> queryEnumSlots = ImmutableListMultimap.of(
                SystemIntents.KNOWLEDGE_QUERY_SLOT_TARGET, SystemIntents.KNOWLEDGE_QUERY_TARGET_VALUE_ENUM);
        String queryEnumIntent = IntentLabelUtils.buildParamLabel(SystemIntents.KNOWLEDGE_QUERY, queryEnumSlots);
        for (String EnumLabel : kg.selectOneAsList(ENUM_QUERY, "EnumLabel")) {
            String queryEnumContent = slot(quote(EnumLabel));
            base.getGrammars().add(new Grammar(GrammarType.INTENT_REGEX, queryEnumIntent, queryEnumContent, QUERY_ENUM_SCORE));
            base.getGrammars().add(new Grammar(GrammarType.INTENT_REGEX, queryEnumIntent, strict(queryEnumContent), QUERY_ENUM_STRICT_SCORE));
        }
        return base;
    }


    private static void addDatatypePropertyGrammar(List<String> datatypeLabels, GrammarModel base) {
        for (String datatypeLabel : datatypeLabels) {
            ListMultimap<String, String> slots = ImmutableListMultimap.<String, String>builder().put(SystemIntents.KNOWLEDGE_QUERY_SLOT_DATATYPE, datatypeLabel).build();
            String intent = IntentLabelUtils.buildParamLabel(SystemIntents.KNOWLEDGE_QUERY, slots);
            base.getGrammars().add(new Grammar(GrammarType.INTENT_REGEX, intent, quote(datatypeLabel), QUERY_DATATYPE_SCORE));
            base.getGrammars().add(new Grammar(GrammarType.INTENT_REGEX, intent, strict(quote(datatypeLabel)), QUERY_DATATYPE_STRICT_SCORE));
        }
    }

    private static void addEntityDatatypePropertyGrammar(SelectResult selectResult, GrammarModel base) {
        for (Binding binding : selectResult.getBindings()) {
            String classLabel = binding.value("domain_subclass_label");
            String propertyLabel = binding.value("property_label");
            ListMultimap<String, String> slots = ImmutableListMultimap.<String, String>builder().put(SystemIntents.KNOWLEDGE_QUERY_SLOT_DATATYPE, propertyLabel).build();
            String intent = IntentLabelUtils.buildParamLabel(SystemIntents.KNOWLEDGE_QUERY, slots);
            base.getGrammars().add(new Grammar(GrammarType.INTENT_REGEX, intent,
                    strict(xOfa(slot(quote(classLabel)), quote(propertyLabel))), QUERY_DATATYPE_STRICT_SCORE));
        }
    }

    private static void addObjectPropertyGrammar(SelectResult selectResult, String typeSlotName, GrammarModel base) {
        for (Binding binding : selectResult.getBindings()) {
            String propertyIRI = binding.value("p");
            String propertyLabel = binding.value("label");
            ListMultimap<String, String> slots = ImmutableListMultimap.<String, String>builder().put(typeSlotName, propertyIRI).build();
            String intent = IntentLabelUtils.buildParamLabel(SystemIntents.KNOWLEDGE_QUERY, slots);
            base.getGrammars().add(new Grammar(GrammarType.INTENT_REGEX, intent, quote(propertyLabel), QUERY_DATATYPE_SCORE));
            base.getGrammars().add(new Grammar(GrammarType.INTENT_REGEX, intent, strict(quote(propertyLabel)), QUERY_DATATYPE_STRICT_SCORE));
        }
    }

    private static void addEntityObjectPropertyGrammar(SelectResult selectResult, String typeSlotName, GrammarModel base) {
        for (Binding binding : selectResult.getBindings()) {
            String classLabel = binding.value("domain_subclass_label");
            String propertyIRI = binding.value("p");
            String propertyLabel = binding.value("property_label");
            ListMultimap<String, String> slots = ImmutableListMultimap.<String, String>builder().put(typeSlotName, propertyIRI).build();
            String intent = IntentLabelUtils.buildParamLabel(SystemIntents.KNOWLEDGE_QUERY, slots);
            base.getGrammars().add(new Grammar(GrammarType.INTENT_REGEX, intent,
                    strict(xOfa(slot(quote(classLabel)), quote(propertyLabel))), QUERY_DATATYPE_STRICT_SCORE));
        }
    }

    // a的x
    private static String xOfa(String a, String x) {
        return a + "的?" + x;
    }

    private static String quote(String str) {
        return Pattern.quote(str);
    }

    private static String slot(String str) {
        return Annotator.SLOT_PREFIX + str + Annotator.SLOT_SUFFIX;
    }

    private static String strict(String str) {
        return "^" + str + "$";
    }

}
