package ai.hual.labrador.kg.utils;

import ai.hual.labrador.kg.KnowledgeAccessor;
import ai.hual.labrador.kg.SelectResult;
import ai.hual.labrador.kg.pojo.BnAndDps;
import ai.hual.labrador.kg.pojo.ComplexEntityAndProperty;
import ai.hual.labrador.kg.pojo.ConditionAndUndercondition;
import ai.hual.labrador.kg.pojo.InstanceWithProperty;
import ai.hual.labrador.kg.pojo.KnowledgeProperty;
import ai.hual.labrador.kg.pojo.ObjectInstance;
import ai.hual.labrador.kg.pojo.RangeInstance;
import ai.hual.labrador.kg.pojo.SimpleEntityAndProperty;
import ai.hual.labrador.kg.pojo.YshapeAnotherAndBn;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import java.util.List;
import java.util.stream.Collectors;

public class KnowledgeQueryUtils {

    private static final Logger logger = LoggerFactory.getLogger(KnowledgeQueryUtils.class);

    /**
     * find value
     * entity -> relationship -> value
     *
     * @param entity
     * @param relationship
     * @param knowledge
     * @return
     */
    public static List<String> query(String entity, String relationship, KnowledgeAccessor knowledge) {
        String queryString = String.join("\n", "SELECT DISTINCT ?o WHERE {",
                String.format("?s rdfs:label '%s' .", entity),
                String.format("?p rdfs:subPropertyOf*/rdfs:label '%s' .", relationship),
                "?s ?p ?o .",
                "} LIMIT 5");
        logger.debug("SPARQL {}", queryString);
        List<String> result = knowledge.selectOneAsList(queryString, "o");
        logger.debug("SPARQL size: {}, query: {}", result.size(), queryString);
        return result;
    }

    /**
     * find value
     * entity -> object -> datatype -> value
     *
     * @param entity
     * @param object
     * @param property
     * @param knowledge
     * @return
     */
    public static List<String> query(String entity, String object, String property, KnowledgeAccessor knowledge) {
        String queryString = String.join("\n", "SELECT DISTINCT ?o WHERE {",
                String.format("?s rdfs:label '%s' .", entity),
                String.format("?has_object rdfs:subPropertyOf*/rdfs:label '%s' .", object),
                String.format("?has_datatype rdfs:subPropertyOf*/rdfs:label '%s' .", property),
                "?s ?has_object ?object . ?object ?has_datatype ?o .",
                "} LIMIT 5");
        logger.debug("SPARQL {}", queryString);
        List<String> result = knowledge.selectOneAsList(queryString, "o");
        logger.debug("SPARQL size: {}, query: {}", result.size(), queryString);
        return result;
    }

    /**
     * find datatype property x that
     * entity -> object -> x -> value
     *
     * @param entity
     * @param object
     * @param knowledge
     * @return
     */
    public static List<String> queryDatatypeOfObject(String entity, String object, KnowledgeAccessor knowledge) {
        String queryString = String.join("\n", "SELECT DISTINCT ?p WHERE {",
                String.format("?s rdfs:label '%s' .", entity),
                String.format("?super_object rdfs:label '%s' .", object),
                "?has_object rdfs:subPropertyOf* ?super_object .",
                "?has_datatype rdfs:label ?p .",
                "?s ?has_object ?object . ?object ?has_datatype ?property .",
                "} LIMIT 5");
        logger.debug("SPARQL {}", queryString);
        List<String> result = knowledge.selectOneAsList(queryString, "p");
        logger.debug("SPARQL size: {}, query: {}", result.size(), queryString);
        return result;
    }

    /**
     * find object property x that
     * entity -> x -> datatype -> value
     *
     * @param entity
     * @param datatype
     * @param knowledge
     * @return
     */
    public static List<String> queryObjectOfDatatype(String entity, String datatype, KnowledgeAccessor knowledge) {
        String queryString = String.join("\n", "SELECT DISTINCT ?p WHERE {",
                String.format("?s rdfs:label '%s' .", entity),
                String.format("?super_datatype rdfs:label '%s' .", datatype),
                "?has_datatype rdfs:subPropertyOf* ?super_datatype .",
                "?has_object rdfs:label ?p .",
                "?s ?has_object ?object . ?object ?has_datatype ?datatype .",
                "} LIMIT 5");
        logger.debug("SPARQL {}", queryString);
        List<String> result = knowledge.selectOneAsList(queryString, "p");
        logger.debug("SPARQL size: {}, query: {}", result.size(), queryString);
        return result;
    }

    /**
     * find object property x that
     * entity -> x -> o
     *
     * @param entity
     * @param knowledge
     * @return
     */
    public static List<String> queryObject(String entity, KnowledgeAccessor knowledge) {
        String queryString = String.join("\n", "SELECT DISTINCT ?p WHERE {",
                String.format("?s rdfs:label '%s' .", entity),
                "?s ?has_p ?o .",
                "?has_p rdf:type owl:ObjectProperty .",
                "?has_p rdfs:label ?p .",
                "} LIMIT 5");
        logger.debug("SPARQL {}", queryString);
        List<String> result = knowledge.selectOneAsList(queryString, "p");
        logger.debug("SPARQL size: {}, query: {}", result.size(), queryString);
        return result;
    }

    /**
     * find datatype property x that
     * entity -> x -> value
     *
     * @param entity
     * @param knowledge
     * @return
     */
    public static List<String> queryDatatype(String entity, KnowledgeAccessor knowledge) {
        String queryString = String.join("\n", "SELECT DISTINCT ?pLabel WHERE {",
                String.format("?s rdfs:label '%s' .", entity),
                "?s ?p ?o .",
                "?p rdf:type owl:DatatypeProperty .",
                // TODO after add SystemProperty to system graph, add:
                // "FILTER NOT EXISTS { ?p rdf:type lbrds:SystemProperty . }",
                "?p rdfs:label ?pLabel .",
                "} LIMIT 5");
        logger.debug("SPARQL {}", queryString);
        List<String> result = knowledge.selectOneAsList(queryString, "pLabel");
        logger.debug("SPARQL size: {}, query: {}", result.size(), queryString);
        return result;
    }

    /**
     * find sub property x that
     * entity -> x -> value where x is sub property of datatype
     *
     * @param entity
     * @param datatype
     * @param knowledge
     * @return
     */
    public static List<String> querySubProperty(String entity, String datatype, KnowledgeAccessor knowledge) {
        String queryString = String.join("\n", "SELECT DISTINCT ?pLabel WHERE {",
                String.format("?s rdfs:label '%s' .", entity),
                String.format("?p rdfs:subPropertyOf/rdfs:label '%s' .", datatype),
                "?s ?specific_p ?o .",
                "?specific_p rdf:type owl:DatatypeProperty .",
                "?specific_p rdfs:subPropertyOf* ?p .",
                // TODO after add SystemProperty to system graph, add:
                // "FILTER NOT EXISTS { ?p rdf:type lbrds:SystemProperty . }",
                "?p rdfs:label ?pLabel .",
                "} LIMIT 5");
        logger.debug("SPARQL {}", queryString);
        List<String> result = knowledge.selectOneAsList(queryString, "pLabel");
        logger.debug("SPARQL size: {}, query: {}", result.size(), queryString);
        return result;
    }

    /**
     * find entity x that
     * entity -> x -> object -> datatype -> value
     *
     * @param entity
     * @param datatype
     * @param knowledge
     * @return
     */
    public static List<String> queryRelatedEntity(String entity, String datatype, KnowledgeAccessor knowledge) {
        String queryString = String.join("\n", "SELECT DISTINCT ?next WHERE {",
                String.format("?s rdfs:label '%s' .", entity),
                String.format("?has_datatype rdfs:subPropertyOf*/rdfs:label '%s' .", datatype),
                "?s ?has_entity ?nextLink .",
                "?nextLink ?has_object ?object .",
                "?object ?has_datatype ?datatype .",
                "?nextLink rdfs:label ?next .",
                "} LIMIT 5");
        logger.debug("SPARQL {}", queryString);
        List<String> result = knowledge.selectOneAsList(queryString, "next");
        logger.debug("SPARQL size: {}, query: {}", result.size(), queryString);
        return result;
    }


    /**
     * find entity x that
     * entity -> x -> object -> datatype -> value
     *
     * @param entity
     * @param object
     * @param datatype
     * @param knowledge
     * @return
     */
    public static List<String> queryRelatedEntity(String entity, String object, String datatype, KnowledgeAccessor knowledge) {
        String queryString = String.join("\n", "SELECT DISTINCT ?next WHERE {",
                String.format("?s rdfs:label '%s' .", entity),
                String.format("?has_datatype rdfs:subPropertyOf*/rdfs:label '%s' .", datatype),
                String.format("?has_object rdfs:subPropertyOf*/rdfs:label '%s' .", object),
                "?s ?has_entity ?nextLink .",
                "?nextLink ?has_object ?object .",
                "?object ?has_datatype ?datatype .",
                "?nextLink rdfs:label ?next .",
                "} LIMIT 5");
        logger.debug("SPARQL {}", queryString);
        List<String> result = knowledge.selectOneAsList(queryString, "next");
        logger.debug("SPARQL size: {}, query: {}", result.size(), queryString);
        return result;
    }


    /**
     * find entity x that
     * x -> relationship -> value
     *
     * @param relationship
     * @param clazz
     * @param knowledge
     * @return
     */
    public static List<String> queryEntity(String relationship, String clazz, KnowledgeAccessor knowledge) {
        String queryString = String.join("\n", "SELECT DISTINCT ?s WHERE {",
                String.format("?p rdfs:subPropertyOf*/rdfs:label '%s' .", relationship),
                "?sURI ?p ?o .",
                "?sURI rdfs:label ?s .",
                clazz == null ? "" : String.format(
                        "{ ?sURI a/rdfs:label '%s' .} UNION { ?sURI a/rdfs:subClassOf*/rdfs:label '%s' .} .",
                        clazz, clazz),
                "} LIMIT 5");
        logger.debug("SPARQL {}", queryString);
        List<String> result = knowledge.selectOneAsList(queryString, "s");
        logger.debug("SPARQL size: {}, query: {}", result.size(), queryString);
        return result;
    }


    /**
     * find entity x that
     * x -> object -> datatype -> value
     *
     * @param object
     * @param datatype
     * @param clazz
     * @param knowledge
     * @return
     */
    public static List<String> queryEntity(String object, String datatype, String clazz, KnowledgeAccessor knowledge) {
        String queryString = String.join("\n", "SELECT DISTINCT ?s WHERE {",
                String.format("?has_object rdfs:subPropertyOf*/rdfs:label '%s' .", object),
                String.format("?has_datatype rdfs:subPropertyOf*/rdfs:label '%s' .", datatype),
                "?sURI ?has_object ?object . ?object ?has_datatype ?datatype .",
                "?sURI rdfs:label ?s.",
                clazz == null ? "" : String.format(
                        "{ ?sURI a/rdfs:label '%s' .} UNION { ?sURI a/rdfs:subClassOf*/rdfs:label '%s' .} .",
                        clazz, clazz),
                "} LIMIT 5");
        logger.debug("SPARQL {}", queryString);
        List<String> result = knowledge.selectOneAsList(queryString, "s");
        logger.debug("SPARQL size: {}, query: {}", result.size(), queryString);
        return result;
    }


    /**
     * find instances of a class
     *
     * @param clazz
     * @param knowledge
     * @return
     */
    public static List<String> queryInstances(String clazz, KnowledgeAccessor knowledge) {
        String queryString = String.join("\n", "SELECT DISTINCT ?sLabel WHERE {",
                String.format("?s rdf:type/rdfs:label '%s' .", clazz),
                "?s rdfs:label ?sLabel .",
                "}");
        logger.debug("SPARQL {}", queryString);
        return knowledge.selectOneAsList(queryString, "sLabel");
    }

    /**
     * find subclasses of a class
     *
     * @param clazz
     * @param knowledge
     * @return
     */
    public static List<String> querySubclasses(String clazz, KnowledgeAccessor knowledge) {
        String queryString = String.join("\n", "SELECT DISTINCT ?sLabel WHERE {",
                String.format("?s rdfs:subClassOf/rdfs:label '%s' .", clazz),
                "?s rdfs:label ?sLabel .",
                "}");
        logger.debug("SPARQL {}", queryString);
        return knowledge.selectOneAsList(queryString, "sLabel");
    }

    /**
     * find the type of an entity
     *
     * @param entity
     * @param knowledge
     * @return
     */
    public static List<String> queryClass(String entity, KnowledgeAccessor knowledge) {
        String queryString = String.join("\n", "SELECT DISTINCT ?oLabel WHERE {",
                String.format("?s rdfs:label '%s' .", entity),
                "?s rdf:type/rdfs:label ?oLabel .",
                "}");
        logger.debug("SPARQL {}", queryString);
        return knowledge.selectOneAsList(queryString, "oLabel");
    }

    /**
     * get all instances except BNnode
     *
     * @param knowledge
     * @return
     */
    public static List<String> getAllIntances(KnowledgeAccessor knowledge) {
        String queryString = String.join("\n", "SELECT DISTINCT (count(?s) as ?s) WHERE {",
                "?s a ?class.",
                "?class rdfs:subClassOf* owl:Thing.",
                "FILTER NOT EXISTS { ?class rdfs:subClassOf* <http://hual.ai/standard#BNclass> }",
                "}");
        logger.debug("SPARQL {}", queryString);
        return knowledge.selectOneAsList(queryString, "s");
    }

    /**
     * get all value of dataType Property
     *
     * @param knowledge
     * @return
     */
    public static List<String> getAllDataTypeValue(KnowledgeAccessor knowledge) {
        String queryString = String.join("\n", "SELECT distinct (count(?value) as ?v) WHERE {",
                "?s ?p ?value.",
                "?p a owl:DatatypeProperty.",
                "}");
        logger.debug("SPARQL {}", queryString);
        return knowledge.selectOneAsList(queryString, "v");
    }

    /**
     * get all value of object Property
     *
     * @param knowledge
     * @return
     */
    public static List<String> getAllObjectValue(KnowledgeAccessor knowledge) {
        String queryString = String.join("\n", "SELECT distinct (count(?value) as ?v) WHERE {",
                "?s ?p ?value.",
                "?p rdfs:subPropertyOf <http://hual.ai/standard#HualObjectProperty>.",
                "}");
        logger.debug("SPARQL {}", queryString);
        return knowledge.selectOneAsList(queryString, "v");
    }

    /**
     * find property with type
     *
     * @param clazziri
     * @param knowledge
     * @return
     */
    public static List<KnowledgeProperty> queryPropertyWithHualType(String clazziri, KnowledgeAccessor knowledge) {
        String queryString = String.join("\n", "SELECT DISTINCT ?p ?pLabel ?pType WHERE {",
                "VALUES ?pType { <http://hual.ai/standard#YshapeProperty> <http://hual.ai/standard#DiffusionProperty> <http://hual.ai/standard#ConditionProperty> <http://hual.ai/standard#HualDataTypeProperty> <http://hual.ai/standard#HualObjectProperty> }\n",
                String.format("<%s> rdfs:subClassOf* ?clazz.", clazziri),
                "?p rdfs:domain ?clazz. ",
                "OPTIONAL{?p rdfs:label ?pLabel.}",
                "?p rdfs:subPropertyOf* ?pType.",
                "} ");
        logger.debug("SPARQL {}", queryString);
        SelectResult result = knowledge.select(queryString);
        logger.debug("SPARQL size: {}, query: {}", result.getBindings().size(), queryString);
        return result.getBindings().stream().map(b -> new KnowledgeProperty(b.value("p"), b.value("pLabel"), b.value("pType")))
                .collect(Collectors.toList());
    }

    /**
     * find all Properties with Label and Type
     *
     * @param knowledge
     * @return
     */
    public static List<KnowledgeProperty> queryAllProperties(KnowledgeAccessor knowledge) {
        String queryString = String.join("\n", "SELECT DISTINCT ?p ?pLabel ?pType WHERE {",
                "VALUES ?pType { <http://hual.ai/standard#YshapeProperty> <http://hual.ai/standard#DiffusionProperty> <http://hual.ai/standard#ConditionProperty> <http://hual.ai/standard#HualDataTypeProperty> <http://hual.ai/standard#HualObjectProperty>}\n",
                "OPTIONAL{?p rdfs:label ?pLabel.}",
                "?p rdfs:subPropertyOf* ?pType.",
                "} ");
        logger.debug("SPARQL {}", queryString);
        SelectResult result = knowledge.select(queryString);
        logger.debug("SPARQL size: {}, query: {}", result.getBindings().size(), queryString);
        return result.getBindings().stream().map(b -> new KnowledgeProperty(b.value("p"), b.value("pLabel"), b.value("pType")))
                .collect(Collectors.toList());
    }

    /**
     * find all Properties with Label and Type and value and ObjectLabel and BNlabel
     *
     * @param clazziri
     * @param knowledge
     * @return
     */
    public static List<InstanceWithProperty> queryInstancesAndProperties(String clazziri, KnowledgeAccessor knowledge) {
        String queryString = String.join("\n", "SELECT DISTINCT ?instance ?instanceLabel ?p ?o ?objectLabel ?bnLabel WHERE {",
                String.format("<%s> a owl:Class.", clazziri),
                String.format("<%s> rdfs:subClassOf* ?clazz.", clazziri),
                "?instance a ?clazz.",
                "?instance rdfs:label ?instanceLabel.",
                "?instance ?p ?o.",
                "optional{?o rdfs:label ?objectLabel}",
                "optional{?o <http://hual.ai/special#bnlabel> ?bnLabel}",
                "} ");
        logger.debug("SPARQL {}", queryString);
        SelectResult result = knowledge.select(queryString);
        logger.debug("SPARQL size: {}, query: {}", result.getBindings().size(), queryString);
        return result.getBindings().stream().map(b -> new InstanceWithProperty(b.value("instance"), b.value("instanceLabel"), b.value("p"),
                b.value("o"), b.value("objectLabel"), b.value("bnLabel")))
                .collect(Collectors.toList());
    }

    /**
     * find all Property by a instance
     *
     * @param instance
     * @param knowledge
     * @return
     */
    public static List<KnowledgeProperty> queryPropertyByInstance(String instance, KnowledgeAccessor knowledge) {
        String queryString = String.join("\n", "SELECT DISTINCT ?p ?pLabel ?pType WHERE {",
                "VALUES ?pType { <http://hual.ai/standard#YshapeProperty> <http://hual.ai/standard#DiffusionProperty> <http://hual.ai/standard#ConditionProperty> <http://hual.ai/standard#HualDataTypeProperty> <http://hual.ai/standard#HualObjectProperty> }\n",
                String.format("<%s> ?p ?o.", instance),
                "OPTIONAL{?p rdfs:label ?pLabel.}",
                "?p rdfs:subPropertyOf* ?pType.",
                "} ");
        logger.debug("SPARQL {}", queryString);
        SelectResult result = knowledge.select(queryString);
        logger.debug("SPARQL size: {}, query: {}", result.getBindings().size(), queryString);
        return result.getBindings().stream().map(b -> new KnowledgeProperty(b.value("p"), b.value("pLabel"), b.value("pType")))
                .collect(Collectors.toList());
    }

    /**
     * Find all bn&bnlabel, dp&dplabel and value of an entity and a objectProperty
     *
     * @param entityiri
     * @param propertyiri
     * @param knowledge
     * @return
     */
    public static List<BnAndDps> queryBnAndDps(String entityiri, String propertyiri, KnowledgeAccessor knowledge) {
        String queryString = String.join("\n", "SELECT DISTINCT ?bn ?bnLabel ?dp ?dpLabel ?value ?valueLabel WHERE {",
                "VALUES ?after { <http://hual.ai/standard#HualDataTypeProperty> <http://hual.ai/standard#HualObjectProperty> }",
                String.format("<%s> <%s> ?bn.", entityiri, propertyiri),
                "?bn ?dp ?value.",
                "?dp rdfs:label ?dpLabel.",
                "OPTIONAL{?bn <http://hual.ai/special#bnlabel> ?bnLabel.}",
                "OPTIONAL{?value rdfs:label ?valueLabel }",
                "?dp rdfs:subPropertyOf* ?after.",
                "}");
        logger.debug("SPARQL {}", queryString);
        SelectResult result = knowledge.select(queryString);
        logger.debug("SPARQL size: {}, query: {}", result.getBindings().size(), queryString);
        return result.getBindings().stream().map(b -> new BnAndDps(b.value("bn"), b.value("bnLabel"), b.value("dp"), b.value("dpLabel"), b.value("value")))
                .collect(Collectors.toList());
    }

    /**
     * Find the antherinstance with same Yshapeproperty
     *
     * @param entityiri
     * @param propertyiri
     * @param knowledge
     * @return
     */
    public static List<YshapeAnotherAndBn> queryYshapeAnotherinstance(String entityiri, String propertyiri, KnowledgeAccessor knowledge) {
        String queryString = String.join("\n", "SELECT DISTINCT ?s ?sLabel ?bn WHERE {",
                String.format("<%s> <%s> ?bn .", entityiri, propertyiri),
                String.format("?s <%s> ?bn.", propertyiri),
                String.format("filter(?s!=<%s>)", entityiri),
                "?s rdfs:label ?sLabel.",
                "}");
        logger.debug("SPARQL {}", queryString);
        SelectResult result = knowledge.select(queryString);
        logger.debug("SPARQL size: {}, query: {}", result.getBindings().size(), queryString);
        return result.getBindings().stream().map(b -> new YshapeAnotherAndBn(b.value("s"), b.value("sLabel"), b.value("bn")))
                .collect(Collectors.toList());
    }

    /**
     * Find all underconditionProperty and conidtionentity with a ConditionProperty
     *
     * @param entityiri
     * @param propertyiri
     * @param knowledge
     * @return
     */
    public static List<ConditionAndUndercondition> conditionWithUndercondition(String entityiri, String propertyiri, KnowledgeAccessor knowledge) {
        String queryString = String.join("\n", "SELECT DISTINCT  ?conditionLabel ?undercondition ?underconditionLabel ?bn WHERE {",
                String.format("<%s> <%s> ?bn .", entityiri, propertyiri),
                "?bn ?undercondition ?condition.",
                "?undercondition rdfs:subPropertyOf <http://hual.ai/standard#Undercondition>.",
                "?undercondition rdfs:label ?underconditionLabel.",
                "?condition rdfs:label ?conditionLabel.",
                "}");
        logger.debug("SPARQL {}", queryString);
        SelectResult result = knowledge.select(queryString);
        logger.debug("SPARQL size: {}, query: {}", result.getBindings().size(), queryString);
        return result.getBindings().stream().map(b -> new ConditionAndUndercondition(b.value("conditionLabel"), b.value("undercondition"), b.value("underconditionLabel"), b.value("bn")))
                .collect(Collectors.toList());
    }

    /**
     * Find the object instance by a entity and a HualObjectProperty
     *
     * @param entityiri
     * @param propertyiri
     * @param knowledge
     * @return
     */
    public static List<ObjectInstance> queryObjectinstance(String entityiri, String propertyiri, KnowledgeAccessor knowledge) {
        String queryString = String.join("\n", "SELECT DISTINCT ?o ?oLabel WHERE {",
                String.format("<%s> <%s> ?o .", entityiri, propertyiri),
                "?o rdfs:label ?oLabel .",
                "}");
        logger.debug("SPARQL {}", queryString);
        SelectResult result = knowledge.select(queryString);
        logger.debug("SPARQL size: {}, query: {}", result.getBindings().size(), queryString);
        return result.getBindings().stream().map(b -> new ObjectInstance(b.value("o"), b.value("oLabel")))
                .collect(Collectors.toList());

    }

    /**
     * Find the instances in a property range class
     *
     * @param propertyiri
     * @param knowledge
     * @return
     */
    public static List<RangeInstance> queryRangeInstance(String propertyiri, KnowledgeAccessor knowledge) {
        String queryString = String.join("\n", "SELECT DISTINCT ?o ?oLabel ?bnLabel WHERE {",
                String.format("<%s> rdfs:range ?class.", propertyiri),
                "?o a ?class.",
                "optional{?o rdfs:label ?oLabel}",
                "optional{?o <http://hual.ai/special#bnlabel> ?bnLabel } .",
                "}");
        logger.debug("SPARQL {}", queryString);
        SelectResult result = knowledge.select(queryString);
        logger.debug("SPARQL size: {}, query: {}", result.getBindings().size(), queryString);
        return result.getBindings().stream().map(b -> new RangeInstance(b.value("o"), b.value("oLabel"), b.value("bnLabel")))
                .collect(Collectors.toList());

    }

    /**
     * From a instance to get simple combinations
     *
     * @param instanceiri
     * @param knowledge
     * @return
     */
    public static List<SimpleEntityAndProperty> querySimpleCombinations(String instanceiri, KnowledgeAccessor knowledge) {
        String queryString = String.join("\n", "SELECT DISTINCT  ?sLabel ?property ?pLabel WHERE {",
                String.format("<%s> rdfs:label ?sLabel.", instanceiri),
                String.format("<%s> ?property ?o.   ", instanceiri),
                "?property rdfs:label ?pLabel.",
                "?property rdfs:subPropertyOf <http://hual.ai/standard#HualDataTypeProperty>.",
                "}");
        logger.debug("SPARQL {}", queryString);
        SelectResult result = knowledge.select(queryString);
        logger.debug("SPARQL size: {}, query: {}", result.getBindings().size(), queryString);
        return result.getBindings().stream().map(b -> new SimpleEntityAndProperty(instanceiri, b.value("sLabel"), b.value("property"), b.value("pLabel")))
                .collect(Collectors.toList());

    }

    /**
     * From a instance to get complex combinations
     *
     * @param entityiri
     * @param knowledge
     * @return
     */
    public static List<ComplexEntityAndProperty> queryComplexCombinations(String entityiri, KnowledgeAccessor knowledge) {
        String queryString = String.join("\n", "SELECT DISTINCT  ?bn ?bnLabel ?dp ?dpLabel ?cLabel ?cClassLabel  WHERE {",
                "VALUES ?pType { <http://hual.ai/standard#YshapeProperty> <http://hual.ai/standard#DiffusionProperty> <http://hual.ai/standard#ConditionProperty> }\n",
                "VALUES ?after { <http://hual.ai/standard#HualDataTypeProperty> <http://hual.ai/standard#HualObjectProperty> }",
                String.format("<%s> ?op ?bn.", entityiri),
                "?op rdfs:subPropertyOf ?pType.",
                "?bn <http://hual.ai/special#bnlabel> ?bnLabel.",
                "?bn ?dp ?value.",
                "?dp rdfs:label ?dpLabel.",
                "?dp rdfs:subPropertyOf ?after.",
                "?dp rdfs:label ?dpLabel.",
                "optional{?bn ?undercondition ?condition.",
                "?undercondition rdfs:subPropertyOf <http://hual.ai/standard#Undercondition>.",
                "?condition rdfs:label ?cLabel.",
                "?condition rdf:type ?cClass.",
                "?cClass rdfs:subClassOf <http://hual.ai/standard#Conditionclass>.",
                "?cClass rdfs:label ?cClassLabel.",
                "}",
                "} ");
        logger.debug("SPARQL {}", queryString);
        SelectResult result = knowledge.select(queryString);
        logger.debug("SPARQL size: {}, query: {}", result.getBindings().size(), queryString);
        return result.getBindings().stream().map(b -> new ComplexEntityAndProperty(b.value("bn"), b.value("bnLabel"), b.value("dp"), b.value("dpLabel"), b.value("cLabel"), b.value("cClassLabel")))
                .collect(Collectors.toList());

    }

    @Nullable
    public static String queryLabel(String uri, KnowledgeAccessor knowledge) {
        String queryString = "SELECT DISTINCT ?sLabel WHERE {%n" +
                String.format("<%s> rdfs:label ?sLabel.%n", uri) +
                "}";
        logger.debug("SPARQL {}", queryString);
        List<String> result = knowledge.selectOneAsList(queryString, "sLabel");
        logger.debug("SPARQL size: {}, query: {}", result.size(), queryString);
        return result.isEmpty() ? null : result.get(0);
    }


}
