package ai.hual.labrador.nlu.ners;

import ai.hual.labrador.exceptions.NLUException;
import ai.hual.labrador.utils.ComponentScanUtils;
import com.rits.cloning.Cloner;
import org.apache.commons.lang3.tuple.Pair;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;


public class NERModInitiator {

    private static final String SUBNERS_PACKAGE = "ai.hual.labrador.nlu.ners.subners";
    private static final String SUBNERS_PROP_NAME = "nlu.ners";
    private static final String SUBNERS_SPLITTER = ",";
    private Map<String, NERNode> nerNodes;
    private List<NERNode> sources;
    private String subners;
    private NERResources resources;



    /**
     * Construct a NERModInitiator with text and properties.
     *
     *  @param subners specifies the recognized results that the user want eventually
     */
    public NERModInitiator(String subners,NERResources resources) {
        this.resources = resources;
        this.subners = subners;
        this.nerNodes = genNERNodes();
        this.genDAG();
        this.sources = findSources();
    }

    private void genDAG() {
        String[] specifiedSubNERS = this.subners.split(SUBNERS_SPLITTER);

        for (String name : specifiedSubNERS) {
            NERNode element = nerNodes.get(name);
            if (!element.isActive()) {
                element.enable();
            }
        }

        Set<List<String>> trace = new HashSet<>();
        List<String> queue = new ArrayList<>(Arrays.asList(specifiedSubNERS));
        while (!queue.isEmpty()) {
            trace.add(new ArrayList<>(queue));
            String top = queue.remove(0);
            NERNode topNERNode = nerNodes.get(top);
            for (String dep : topNERNode.getNerModule().getClass().getAnnotation(NER.class).dependencies()) {
                if (!nerNodes.containsKey(dep)) {
                    throw new NLUException(String.format("NER module %s not exist", dep));
                }
                NERNode element = nerNodes.get(dep);
                if (!element.isActive()) {
                    element.enable();
                }
                element.addEdge(topNERNode);
                queue.add(element.getNerName());
            }
            if (trace.contains(queue)) {
                throw new NLUException("A cycle has been found in NER module building.");
            }
            trace.add(new ArrayList<>(queue));
        }

        // remove inactive NERNodes
        nerNodes.values().removeIf(node -> !node.isActive());
    }

    private Map<String, NERNode> genNERNodes() {
        List<NERModule> nerModules = ComponentScanUtils
                .filterAnnotation(NER.class).withBean("resources",this.resources)
                .scan(SUBNERS_PACKAGE, NERModule.class);

        Map<String, NERNode> nerNodes = new HashMap<>();
        for (NERModule nerModule : nerModules) {
            String name = nerModule.getClass().getAnnotation(NER.class).name();
            nerNodes.put(name, new NERNode(name, nerModule));
        }
        return nerNodes;
    }

    public List<NERNode> getSources() {
        return this.sources;
    }

    private List<NERNode> findSources() {
        List<NERNode> LocalSources = new ArrayList<>();
        for (Map.Entry<String, NERNode> entry : this.nerNodes.entrySet()) {
            if (0 == entry.getValue().getInDegree() && entry.getValue().isActive()) {
                LocalSources.add(entry.getValue());
            }
        }
        return LocalSources;
    }

    // return ner module name and its level
    // level == 0 means source nodes
    public List<Pair<String, Integer>> topological() {
        List<Pair<String, Integer>> topo = new ArrayList<>();

        Set<String> marked = new HashSet<>();
        int level = -1;
        for (NERNode source : sources) {
            if (!marked.contains(source.getNerName())) {
                this.dfs(source.getNerName(), marked, topo, level + 1);
            }
        }
        return topo.stream().sorted(Comparator.comparing(x -> x.getKey())).sorted(Comparator.comparing(x -> x.getValue())).collect(Collectors.toList());
    }

    private void dfs(String cur, Set<String> marked, List<Pair<String, Integer>> topo, int level) {
        marked.add(cur);
        for (NERNode adjoin : nerNodes.get(cur).getAdjoin()) {
            if (!marked.contains(adjoin.getNerName())) {
                dfs(adjoin.getNerName(), marked, topo, level + 1);
            }
        }
        topo.add(0, Pair.of(cur, level));
    }

    public List<NERResult> process(String text) {
        List<Pair<String, Integer>> priorities = topological();
        HashMap<String, NERResult> calculated = new HashMap<>();
        for (Pair<String, Integer> priority : priorities) {
            String nerName = priority.getLeft();
            int level = priority.getRight();
            List<NERResult> depNerResults = level == 0 ? null : Arrays.stream(nerNodes.get(nerName)
                    .getNerModule().getClass().getAnnotation(NER.class).dependencies())
                    .map(calculated::get).filter(x -> x != null).collect(Collectors.toList());
            calculated.put(nerName, nerNodes.get(nerName).getNerModule().recognize(text, depNerResults));
        }
        List<NERResult> nerResults = new ArrayList<>();
        String[] destNERS = this.subners.split(SUBNERS_SPLITTER);
        for (String dest : destNERS) {
            nerResults.add(calculated.get(dest));
        }
        return nerResults;
    }

}
