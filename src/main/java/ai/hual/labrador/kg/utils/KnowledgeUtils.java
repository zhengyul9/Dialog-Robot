package ai.hual.labrador.kg.utils;

import ai.hual.labrador.exceptions.KnowledgeException;
import ai.hual.labrador.kg.Binding;
import ai.hual.labrador.kg.KnowledgeAccessor;
import ai.hual.labrador.kg.SelectResult;
import com.google.common.collect.ImmutableSet;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

public class KnowledgeUtils {

    /**
     * check if str is a word that has the meaning of "none"
     *
     * @param str The string
     * @return true if the word says "none"
     */
    public static boolean isNone(String str) {
        return ImmutableSet.of("无", "没有").contains(str);
    }

    /**
     * Find a property with a specified label.
     *
     * @param knowledge The model where the property is found
     * @param label     The label of the property
     * @return A property uri with the specified label.
     */
    public static String findPropertyWithLabel(KnowledgeAccessor knowledge, String label) {
        String queryString = String.join("\n", "SELECT DISTINCT ?p WHERE {",
                String.format("?p rdfs:label '%s' .", label),
                "{ ?p rdf:type ?type . } UNION { ?p rdf:type/rdfs:subClassOf* ?type . }",
                "VALUES ?type { rdf:Property owl:DatatypeProperty owl:ObjectProperty }",
                "}");
        List<String> properties = knowledge.selectOneAsList(queryString, "p");
        return properties.isEmpty() ? null : properties.get(0);
    }

    /**
     * Find a class with specified label
     *
     * @param knowledge The model where the class is found
     * @param label     The label of the class
     * @return A class uri with the specified label
     */
    public static String findClassWithLabel(KnowledgeAccessor knowledge, String label) {
        String queryString = String.join("\n", "SELECT DISTINCT ?s WHERE {",
                String.format("?s rdfs:label '%s' .", label),
                "{ ?s rdf:type rdfs:Class . } UNION { ?s rdf:type owl:Class . }",
                "}");
        List<String> classes = knowledge.selectOneAsList(queryString, "s");
        return classes.isEmpty() ? null : classes.get(0);
    }

    /**
     * Convert select results to object list.
     *
     * @param selectResult
     * @param clazz
     * @param <T>
     * @return
     */
    public static <T> List<T> convertToObjects(SelectResult selectResult, Class<T> clazz) {
        List<T> objectList = new ArrayList<>();
        List<String> vars = selectResult.getVars();
        for (Binding binding : selectResult.getBindings()) {    // each binding is an object
            List<String> paramList = new ArrayList<>();
            for (String var : vars)
                paramList.add(binding.value(var));
            try {
                objectList.add(clazz.getConstructor(List.class).newInstance(paramList));
            } catch (InstantiationException | InvocationTargetException | IllegalAccessException | NoSuchMethodException e) {
                throw new KnowledgeException("Can not instantiate object type: " + clazz.getSimpleName() +
                        "giving param list: " + paramList + ". Check constructor", e);
            }
        }
        return objectList;
    }
}
