package ai.hual.labrador.nlu.ners.engines;

import ai.hual.labrador.exceptions.NLUException;
import ai.hual.labrador.nlu.Config;
import ai.hual.labrador.nlu.ners.NERResource;
import ai.hual.labrador.nlu.ners.NERResult;
import com.huaban.analysis.jieba.JiebaSegmenter;
import com.huaban.analysis.jieba.WordDictionary;
import com.rits.cloning.Cloner;
import org.apache.commons.lang3.StringUtils;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;


public class ProductNERbyRule {
    private static final String BELONG_TO_NER_NAME = "ProductNER";
    private static final String RESOURCES_DIR = "Resources_ProductNERbyRule(deprecated)";
    private static final String MAPPING_FILE = RESOURCES_DIR + "/" + "mapping/mapping.txt";
    private static final String AMBIGUOUS_PREFIX_FILE = RESOURCES_DIR + "/" + "detail/ambiguous_prefix.txt";
    private static final String AMBIGUOUS_SUFFIX_FILE = RESOURCES_DIR + "/" + "detail/ambiguous_suffix.txt";
    private static final String TAIL_MINUS_ONE_AND_TAIL_CUT_TAIL_PATTERNS_FILE = RESOURCES_DIR + "/" + "detail/tail_minus_one_and_tail_cut_tail_patterns.txt";
    private static final String TAIL_MINUS_ONE_AND_TAIL_STOP_PATTERNS_FILE = RESOURCES_DIR + "/" + "detail/tail_minus_one_and_tail_stop_patterns.txt";
    private static final String TAIL_PLUS_ONE_PATTERNS_FILE = RESOURCES_DIR + "/" + "detail/tail_plus_one_patterns.txt";
    private static final String KERNEL_FILE = RESOURCES_DIR + "/" + "kernel.txt";
    private static final String USED_LIMITED_FILE = RESOURCES_DIR + "/" + "limited-1.txt";
    private static final String USED_LIMITED_REGEX_FILE = RESOURCES_DIR + "/" + "regex-limited-1.txt";
    private static final String PREFIX_FILE = RESOURCES_DIR + "/" + "prefix.txt";
    private static final String SUFFIX_FILE = RESOURCES_DIR + "/" + "suffix.txt";
    private static final String PROPERTIES_FILE = RESOURCES_DIR + "/" + "properties.txt";
    private static final String STANDARD_ENTITY_FILE = RESOURCES_DIR + "/" + "standard-entity.txt";
    private static final String PLAN_FILE = RESOURCES_DIR + "/" + "plans.txt";
    private static final String SKIPS_FILE = RESOURCES_DIR + "/" + "skips.txt";



    private static final List<String> PRODUCTNER_WORDLIST = Arrays.asList("ambiguous_prefix","ambiguous_suffix","kernel","limited","prefix","suffix","properties","standard_entity","plans","skips");
    private static final List<String> PRODUCTNER_REGEXLIST = Arrays.asList("regex_limited","tail_minus_one_and_tail_cut_tail_patterns","tail_minus_one_and_tail_stop_patterns","tail_plus_one_patterns","adjust_segment_patterns");






    private static final String USER_DICT_FILE = RESOURCES_DIR + "/" + "userdict/userdict.txt";

    private static final Cloner cloner = new Cloner();

    private List<NERResult.Candidate> deps;
    private HashMap<String, List<String>> mapping;
    private HashMap<String, Object> resources;
    private List<NERResult.Candidate> extras;
    private JiebaSegmenter segmenter;


    public ProductNERbyRule(Map<String,NERResource> resources) {
        this.mapping = this.getMapping();
        this.resources = this.getResources(resources);
        this.segmenter = new JiebaSegmenter();
        if(this.resources != null)
            loadUserdict((List<String>)this.resources.get("prefix"),
                (List<String>)this.resources.get("kernel"),
                (List<String>)this.resources.get("limited"),
                (List<String>)this.resources.get("suffix"));
    }

    private void loadUserdict(List<String>... args){
        WordDictionary dictAdd = WordDictionary.getInstance();
        try {
            File file = File.createTempFile(UUID.randomUUID().toString().replaceAll("-",""),"");
            OutputStreamWriter fw = new OutputStreamWriter(new FileOutputStream(file), "UTF-8");

            for(List<String> arg:args){
                if(arg!=null){
                    for(String word : arg){
                        fw.write(String.format("%s 99999%n",word));
                    }
                }
            }
            fw.flush();
            fw.close();
            Path path = file.toPath();
            dictAdd.loadUserDict(path);
            if(!file.delete())
                file.deleteOnExit();
        } catch (IOException ex) {
            throw new NLUException("Could not create the file ");
        }


    }

    public List<NERResult.Candidate> recognizer(List<NERResult.Candidate> deps,String text) {
        if(this.resources == null)
            return null;
        this.deps = deps;
        this.extras = cloner.deepClone(deps);
        if (this.extras != null)
            this.extras = this.extras.stream().sorted(Comparator.comparing(NERResult.Candidate::getRealStart)).collect(Collectors.toList());
        HashMap<String, Object> localresources = this.resources;
        if (this.deps != null) {
            localresources = cloner.deepClone(this.resources);
            for (NERResult.Candidate dep : deps) {
                String word = text.substring(dep.getRealStart(), dep.getRealEnd());
                if(this.mapping.containsKey(dep.getPos())){
                    for (String WordSetName : this.mapping.get(dep.getPos())) {
                        ((List<String>) this.resources.get(WordSetName)).add(word);
                    }
                }
            }
        }
        List<String> segments = this.cut(text);
        this.adjustSegments(text,segments, 0, 0, false);
        int start = 0;
        int end = segments.size();
        Integer max_tails = null;
        List<String> tmp = new ArrayList<>();
        List<HashMap<String, HashMap<String, Object>>> entity_link_info = new ArrayList<>();
        List<HashMap<String, Object>> guess = new ArrayList<>();
        HashMap<String, Object> BoundaryInfo = null;
        // 基于核心词和非核心词搜索
        do {
            BoundaryInfo = this.search(text,segments, start, end);
            if((Boolean) BoundaryInfo.get("flag"))
                guess.add(cloner.deepClone(BoundaryInfo));
            postProcessing(text,segments, (List<Integer>) BoundaryInfo.get("heads"), (List<Integer>) BoundaryInfo.get("tails")
                    , (boolean) BoundaryInfo.get("flag"), false, (Integer) BoundaryInfo.get("kernel_idx")
                    , (List<String>) this.resources.get("ambiguous_suffix"), (List<Pattern>) this.resources.get("tail_plus_one_patterns")
                    , tmp, entity_link_info, (List<String>) this.resources.get("invalid_single_word_as_entity")
                    , (List<String>) this.resources.get("skips"), (List<Pattern>) this.resources.get("tail_minus_one_and_tail_stop_patterns")
                    , (List<Pattern>) this.resources.get("tail_minus_one_and_tail_cut_tail_patterns"), (List<String>) this.resources.get("ambiguous_prefix"));
            if (((List<Integer>) BoundaryInfo.get("tails")).size() > 0) {
                max_tails = ((List<Integer>) BoundaryInfo.get("tails")).stream().sorted(Comparator.comparing(x -> -1 * x)).collect(Collectors.toList()).get(0);
                start = max_tails + 1;
            }

        }
        while (BoundaryInfo != null && ((List<Integer>) BoundaryInfo.get("tails")).size() > 0 && max_tails != null && max_tails < end);

        // 基于prefix和suffix搜索
        if(entity_link_info.size() == 0){
            Integer idx_prefix = null;
            Integer idx_suffix = null;
            List<String> prefix = (List<String>) this.resources.get("prefix");
            List<String> suffix = (List<String>) this.resources.get("suffix");
            for(int i = 0 ; i < segments.size(); ++i){
                if(prefix.contains(segments.get(i))){
                    idx_prefix = i;
                    break;
                }
            }

            for(int j = segments.size() - 1 ; j > 0; --j){
                if(suffix.contains(segments.get(j))){
                    idx_suffix = j;
                    break;
                }
            }

            if(idx_prefix != null && idx_suffix != null && idx_prefix < idx_suffix){

                List<Integer> tmpheads = new ArrayList<Integer>();
                tmpheads.add(idx_prefix);
                List<Integer> tmptails = new ArrayList<Integer>();
                tmptails.add(idx_suffix);
                postProcessing(text,segments, tmpheads, tmptails
                        , false, true, null
                        , (List<String>) this.resources.get("ambiguous_suffix"), (List<Pattern>) this.resources.get("tail_plus_one_patterns")
                        , tmp, entity_link_info, (List<String>) this.resources.get("invalid_single_word_as_entity")
                        , (List<String>) this.resources.get("skips"), (List<Pattern>) this.resources.get("tail_minus_one_and_tail_stop_patterns")
                        , (List<Pattern>) this.resources.get("tail_minus_one_and_tail_cut_tail_patterns"), (List<String>) this.resources.get("ambiguous_prefix"));
            }
        }


        if (guess.size() > 1) {
            guess = guess.stream().sorted(Comparator.comparing(x -> {
                        if ((boolean) x.get("flag"))
                            return (Integer) x.get("kernel_idx");
                        else
                            return 99999999;
                    }
            )).collect(Collectors.toList());
//            guess = guess.stream().sorted(Comparator.comparing(x-> {
//                        if((boolean)x.get("flag"))
//                            return 0;
//                        else
//                            return 1;
//                    }
//            )).collect(Collectors.toList());
            List<String> IdxList = new ArrayList<>();
            for (Integer i = 0; i < segments.size(); ++i) {
                IdxList.add(i.toString());
            }
            String AllString = "/" + String.join("/", IdxList) + "/";
            StringBuilder partBuilder = new StringBuilder();
            partBuilder.append("/");
            Integer idx_guess = 0;
            while (idx_guess < guess.size() && (boolean) guess.get(idx_guess).get("flag")) {
                StringBuilder tmpBuilder = new StringBuilder();
                tmpBuilder.append(Integer.toString((int) guess.get(idx_guess).get("kernel_idx")));
                tmpBuilder.append("/");
                partBuilder.append(tmpBuilder);
                ++idx_guess;
            }
            String part = partBuilder.toString();
            if (idx_guess >= 2 && AllString.contains(part)) {
                List<Integer> GuessHeads = (List<Integer>) guess.get(0).get("heads");
                List<Integer> GuessTails = (List<Integer>) guess.get(idx_guess - 1).get("tails");
                List<Integer> GuessKernelIdx = new ArrayList<>();
                GuessKernelIdx.add((Integer) guess.get(0).get("kernel_idx"));
                GuessKernelIdx.add((Integer) guess.get(idx_guess - 1).get("kernel_idx"));
                postProcessing(text,segments, GuessHeads, GuessTails
                        , false, true, GuessKernelIdx
                        , (List<String>) this.resources.get("ambiguous_suffix"), (List<Pattern>) this.resources.get("tail_plus_one_patterns")
                        , tmp, entity_link_info, (List<String>) this.resources.get("invalid_single_word_as_entity")
                        , (List<String>) this.resources.get("skips"), (List<Pattern>) this.resources.get("tail_minus_one_and_tail_stop_patterns")
                        , (List<Pattern>) this.resources.get("tail_minus_one_and_tail_cut_tail_patterns"), (List<String>) this.resources.get("ambiguous_prefix"));
            }
        }
        Map<String, NERResult.Candidate> candidates = new HashMap<>();
        for (HashMap<String, HashMap<String, Object>> one : entity_link_info) {
            //for(String entity:one.keySet()){
            for (Map.Entry<String, HashMap<String, Object>> entry : one.entrySet()) {
                String entity = entry.getKey();
                HashMap<String, Object> info = entry.getValue();
                if (candidates.containsKey(((Integer) info.get("realStart")).toString() + "-" + ((Integer) info.get("realEnd")).toString()))
                    continue;
                List<String> cur_set = (List<String>) this.resources.get("standard_entity");
                String kernel_word = (String) info.get("kernel_word");
                String cur_filter = null;
                List<String> tmpList = null;
                if (kernel_word.length() != 0) {
                    cur_filter = kernel_word;
                    tmpList = new ArrayList<>();
                    for (String word : cur_set) {
                        if (word.contains(cur_filter))
                            tmpList.add(word);
                    }
                    cur_set = tmpList;
                }
                List<String> entity_segments = (List<String>) info.get("segments");
                for (String segment : entity_segments) {
                    if (((List<String>) this.resources.get("prefix")).contains(segment)
                            || ((List<String>) this.resources.get("suffix")).contains(segment)
                            || ((List<String>) this.resources.get("skips")).contains(segment)
                            || segment.equals(kernel_word)
                            )
                        continue;
                    tmpList = new ArrayList<>();
                    cur_filter = segment;
                    for (String word : cur_set) {
                        if (word.contains(cur_filter))
                            tmpList.add(word);
                    }
                    cur_set = tmpList;
                }

                String standard = cur_set.size() != 0 ? String.join("/", cur_set) : "Not found";
                NERResult.Candidate candidate = new NERResult.Candidate(
                        (Integer) info.get("realStart"), (Integer) info.get("realEnd"), "人寿保险_产品",
                        BELONG_TO_NER_NAME, entity, standard, String.join("/", segments));
                String key = candidate.getRealStart() + "-" + candidate.getRealEnd();
                candidates.put(key, candidate);
            }
        }

        this.resources = localresources;
        return new ArrayList<>(candidates.values());
    }

    public void postProcessing(String query,List<String> segments, List<Integer> heads, List<Integer> tails,
                               boolean flag, boolean isGuessed, Object kernel_idx, List<String> ambiguous_suffix,
                               List<Pattern> tail_plus_one_patterns, List<String> tmp,
                               List<HashMap<String, HashMap<String, Object>>> entity_link_info,
                               List<String> invalid_single_word_as_entity,
                               List<String> skips,
                               List<Pattern> tail_minus_one_and_tail_stop_patterns,
                               List<Pattern> tail_minus_one_and_tail_cut_tail_patterns,
                               List<String> ambiguous_prefix) {
        String kernel_word = "";
        if (flag)
            kernel_word = segments.get((Integer) kernel_idx);
        if (isGuessed) {
            kernel_word = "";
            if(kernel_idx != null)
                kernel_word = getOriginTextFromSegments(query,segments,((List<Integer>) kernel_idx).get(0),((List<Integer>) kernel_idx).get(1));
                //kernel_word = String.join("", segments.subList(((List<Integer>) kernel_idx).get(0), ((List<Integer>) kernel_idx).get(1)));

        }
        HashMap<String, HashMap<String, Object>> one = new HashMap<>();
        while (heads.size() != 0) {
            Integer head = heads.remove(0);
            for (Integer tail : tails) {
                if (tail < head)
                    continue;
                List<List<Integer>> pairs = new ArrayList<>();
                boolean ending_label = false;
                while (!ending_label) {
                    int n = 0;
                    if (tail - head >= 1 && Pattern.compile("^"+makeQueryStringAllRegExp(segments.get(head))+".*$").matcher(segments.get(head + 1)).matches()) {
                        ++head;
                        ++n;
                    }
                    if (tail - head >= 1 && Pattern.compile("^.*"+makeQueryStringAllRegExp(segments.get(tail))+"$").matcher(segments.get(tail - 1)).matches()) {
                        --tail;
                        ++n;
                    }
                    if (tail - head >= 1
                            && (ambiguous_prefix.contains(segments.get(head)) || skips.contains(segments.get(head)))
                            && !heads.contains(head + 1)) {
                        heads.add(head + 1);
                        ++n;
                    }

                    if (tail - head >= 1
                            && (ambiguous_suffix.contains(segments.get(tail)) || skips.contains(segments.get(tail)))
                            ) {
                        List<Integer> tmpPair = new ArrayList<>();
                        tmpPair.add(head);
                        tmpPair.add(tail);
                        pairs.add(tmpPair);
                        --tail;
                        ++n;
                    }
                    int tmpSegStartIdx = tail - 1;
                    if (tmpSegStartIdx < 0)
                        tmpSegStartIdx = 0;
                    String tmpSeg = getOriginTextFromSegments(query,segments,tmpSegStartIdx,tail + 1);
                    //String tmpSeg = String.join("", segments.subList(tmpSegStartIdx, tail + 1));
                    if (tail - head >= 2
                            && tail_minus_one_and_tail_stop_patterns.stream().filter(x -> x.matcher(tmpSeg).find()).collect(Collectors.toList()).size() > 0
                            ) {
                        tail -= 2;
                        ++n;
                    }

                    if (tail - head >= 2
                            && tail_minus_one_and_tail_cut_tail_patterns.stream().filter(x -> x.matcher(tmpSeg).find()).collect(Collectors.toList()).size() > 0
                            ) {
                        --tail;
                        ++n;
                    }
                    int tmpSeg1StartIndex = tail;
                    if (tmpSeg1StartIndex < 0)
                        tmpSeg1StartIndex = 0;
                    int tmpSeg1EndIndex = tail + 2;
                    if (tail == segments.size() - 1)
                        tmpSeg1EndIndex = tail + 1;
                    String tmpSeg1 = getOriginTextFromSegments(query,segments,tmpSeg1StartIndex,tmpSeg1EndIndex);
                    //String tmpSeg1 = String.join("", segments.subList(tmpSeg1StartIndex, tmpSeg1EndIndex));
                    if (tail < segments.size() - 1
                            && tail_plus_one_patterns.stream()
                            .filter(x -> x.matcher(tmpSeg1).find())
                            .collect(Collectors.toList()).size() > 0
                            ) {
                        --tail;
                        ++n;
                    }
                    if (0 == n) {
                        ending_label = true;
                    }
                }
                List<Integer> tmpPair1 = new ArrayList<>();
                tmpPair1.add(head);
                tmpPair1.add(tail);
                pairs.add(tmpPair1);
                for (List<Integer> pair : pairs) {
                    Integer iter_head = pair.get(0);
                    Integer iter_tail = pair.get(1);
                    Integer real_head = cloner.deepClone(iter_head);
                    Integer real_tail = cloner.deepClone(iter_tail);
                    for (int it = iter_head; it < iter_tail + 1; ++it) {
                        if (Pattern.compile("^[,，.。?？!！、]$").matcher(segments.get(it)).find()) {
                            if (flag) {
                                if (it > (Integer) kernel_idx && it <= real_tail)
                                    real_tail = it - 1;
                                else if (it < (Integer) kernel_idx && it >= real_head)
                                    real_head = it + 1;
                            } else if (isGuessed) {
                                if (it > ((List<Integer>) kernel_idx).get(1) && it <= real_tail)
                                    real_tail = it - 1;
                                else if (it < ((List<Integer>) kernel_idx).get(0) && it >= real_head)
                                    real_head = it + 1;
                            } else {
                                if (it <= real_tail)
                                    real_tail = it - 1;
                            }
                        }
                    }
                    if (real_head.equals(real_tail) && invalid_single_word_as_entity.contains(segments.get(real_head)))
                        continue;

                    if (real_tail >= real_head) {
                        List<Integer> pairs_removed_brackets = this.removeInvalidBrackets(segments, real_head, real_tail);
                        Integer ultimate_real_head = pairs_removed_brackets.get(0);
                        Integer ultimate_real_tail = pairs_removed_brackets.get(1);
                        String entity = getOriginTextFromSegments(query,segments,ultimate_real_head,ultimate_real_tail + 1);
                        if (!one.containsKey(entity)) {
                            List<Integer> accumulate = new ArrayList<>();
                            int tmp_accumulate = 0;
                            for (String segment : segments) {
                                tmp_accumulate += segment.length();
                                accumulate.add(tmp_accumulate);
                            }
                            HashMap<String, Object> TmpInfo = new HashMap<>();
                            TmpInfo.put("kernel_word", kernel_word);
                            TmpInfo.put("segments", segments.subList(ultimate_real_head, ultimate_real_tail + 1));
                            TmpInfo.put("realStart", accumulate.get(ultimate_real_head) - segments.get(ultimate_real_head).length());
                            TmpInfo.put("realEnd", accumulate.get(ultimate_real_tail));
                            one.put(entity, TmpInfo);
                        }
                    }
                }
            }
        }
        if (one.size() != 0) {
            tmp.add(String.join("^", one.keySet()));
            entity_link_info.add(one);
        }
    }

    public List<Integer> removeInvalidBrackets(List<String> segments, int real_head, int real_tail) {
        HashMap<String, HashMap<String, String>> match = new HashMap<String, HashMap<String, String>>() {{
            put("(", new HashMap<String, String>() {{
                put("direct", "left");
                put("value", "1");
            }});
            put(")", new HashMap<String, String>() {{
                put("direct", "right");
                put("value", "1");
            }});
            put("（", new HashMap<String, String>() {{
                put("direct", "left");
                put("value", "1");
            }});
            put("）", new HashMap<String, String>() {{
                put("direct", "right");
                put("value", "1");
            }});
            put("[", new HashMap<String, String>() {{
                put("direct", "left");
                put("value", "2");
            }});
            put("]", new HashMap<String, String>() {{
                put("direct", "right");
                put("value", "2");
            }});
            put("【", new HashMap<String, String>() {{
                put("direct", "left");
                put("value", "2");
            }});
            put("】", new HashMap<String, String>() {{
                put("direct", "right");
                put("value", "2");
            }});
            put("{", new HashMap<String, String>() {{
                put("direct", "left");
                put("value", "3");
            }});
            put("}", new HashMap<String, String>() {{
                put("direct", "right");
                put("value", "3");
            }});
            put("『", new HashMap<String, String>() {{
                put("direct", "left");
                put("value", "3");
            }});
            put("』", new HashMap<String, String>() {{
                put("direct", "right");
                put("value", "3");
            }});
            put("<", new HashMap<String, String>() {{
                put("direct", "left");
                put("value", "4");
            }});
            put(">", new HashMap<String, String>() {{
                put("direct", "right");
                put("value", "4");
            }});
            put("《", new HashMap<String, String>() {{
                put("direct", "left");
                put("value", "4");
            }});
            put("》", new HashMap<String, String>() {{
                put("direct", "right");
                put("value", "4");
            }});
        }};
        List<List<Object>> drops = null;
        while (drops == null || drops.size() != 0) {
            List<List<Object>> stack = new ArrayList<>();
            drops = new ArrayList<>();
            for (int i = real_head; i < real_tail + 1; ++i) {
                String seg = segments.get(i);
                if (match.containsKey(seg)) {
                    List<Object> node = new ArrayList<>();
                    node.add(i);
                    node.add(seg);
                    node.add(match.get(seg));

                    if (((HashMap<String, String>) node.get(2)).get("direct").equals("left")) {
                        stack.add(0, node);
                    }
                    if (((HashMap<String, String>) node.get(2)).get("direct").equals("right")) {
                        if (stack.size() != 0) {
                            List<Object> pop_node = stack.remove(0);
                            if (!((HashMap<String, String>) pop_node.get(2)).get("value").equals(((HashMap<String, String>) node.get(2)).get("value"))) {
                                drops.add(pop_node);
                                drops.add(node);
                            }
                        } else {
                            drops.add(node);
                        }

                    }
                }
            }
            while (stack.size() != 0)
                drops.add(stack.remove(0));

            if (drops.size() != 0) {
                List<Object> last_tail = null;
                // descend
                drops = drops.stream().sorted(Comparator.comparing(x -> -1 * (Integer) x.get(0))).collect(Collectors.toList());
                if (real_tail == (Integer) drops.get(0).get(0)) {
                    last_tail = drops.get(0);
                    for (int i = 1; i < drops.size(); ++i) {
                        if ((Integer) last_tail.get(0) - (Integer) drops.get(i).get(0) == 1) {
                            last_tail = drops.get(i);
                        } else
                            break;
                    }
                }
                if (last_tail != null) {
                    real_tail = (Integer) last_tail.get(0) - 1;
                }

                List<Object> last_head = null;
                // ascend
                drops = drops.stream().sorted(Comparator.comparing(x -> (Integer) x.get(0))).collect(Collectors.toList());
                if (real_head == (Integer) drops.get(0).get(0)) {
                    last_head = drops.get(0);
                    for (int i = 1; i < drops.size(); ++i) {
                        if ((Integer) drops.get(i).get(0) - (Integer) last_head.get(0) == 1) {
                            last_head = drops.get(i);
                        } else
                            break;
                    }
                }
                if (last_head != null)
                    real_head = (Integer) last_head.get(0) + 1;
            }
            stack.clear();
            drops.clear();
            for (int i = real_head; i < real_tail + 1; ++i) {
                String seg = segments.get(i);
                if (match.containsKey(seg)) {
                    List<Object> node = new ArrayList<>();
                    node.add(i);
                    node.add(seg);
                    node.add(match.get(seg));

                    if (((HashMap<String, String>) node.get(2)).get("direct").equals("left")) {
                        stack.add(0, node);
                    }
                    if (((HashMap<String, String>) node.get(2)).get("direct").equals("right")) {
                        if (stack.size() != 0) {
                            List<Object> pop_node = stack.remove(0);
                            if (
                                    (Math.abs((Integer) node.get(0) - (Integer) pop_node.get(0)) == 1
                                            && ((Integer) node.get(0) == real_tail || (Integer) pop_node.get(0) == real_head))
                                            ||
                                            ((((HashMap<String, String>) node.get(2)).get("value").equals(((HashMap<String, String>) pop_node.get(2)).get("value")))
                                                    && ((Integer) node.get(0) == real_tail
                                                    && (Integer) pop_node.get(0) == real_head))
                                    ) {
                                drops.add(pop_node);
                                drops.add(node);
                            }
                        }

                    }
                }
            }
                if (drops.size() != 0) {
                    List<Object> last_tail = null;
                    drops = drops.stream().sorted(Comparator.comparing(x -> (Integer) x.get(0))).collect(Collectors.toList());
                    if (real_tail == (Integer) drops.get(drops.size() - 1).get(0)) {
                        last_tail = drops.get(drops.size() - 1);
                        for (int it = drops.size() - 2; it > -1; --it) {
                            if ((Integer) last_tail.get(0) - (Integer) drops.get(it).get(0) == 1) {
                                last_tail = drops.get(it);
                            } else
                                break;
                        }
                    }

                    if (last_tail != null)
                        real_tail = (Integer) last_tail.get(0) - 1;

                    List<Object> last_head = null;
                    if (real_head == (Integer) drops.get(0).get(0)) {
                        last_head = drops.get(0);
                        for (int it = 1; it < drops.size(); ++it) {
                            if ((Integer) drops.get(it).get(0) - (Integer) last_head.get(0) == 1)
                                last_head = drops.get(it);
                            else
                                break;
                        }
                    }

                    if (last_head != null)
                        real_head = (Integer) last_head.get(0) + 1;
                }
        }
        List<Integer> result = new ArrayList<>();
        result.add(real_head);
        result.add(real_tail);
        return result;
    }

    public HashMap<String, Object> search(String query,List<String> segments, int start, int end) {
        List<String> forward = (List<String>) this.resources.get("forward");
        List<String> backward = (List<String>) this.resources.get("backward");
        List<String> whole_without_kernel = (List<String>) this.resources.get("whole_without_kernel");
        List<String> skips = (List<String>) this.resources.get("skips");
        List<Pattern> limited_regex = (List<Pattern>) this.resources.get("regex_limited");
        // List<String> mytrie = (List<String>)this.resources.get("mytrie");
        List<String> kernel = (List<String>) this.resources.get("kernel");
        List<String> properties = (List<String>) this.resources.get("properties");

        if (start >= end || start < 0) {
            return setBoundaryInfo(new ArrayList<Integer>(), new ArrayList<Integer>(), false, 0);
        }
        boolean flag = false;
        Integer idx = null;
        for (int i = start; i < end; ++i) {
            String seg = segments.get(i);
            if (kernel.contains(seg)) {
                flag = true;
                idx = i;
                break;
            }
        }
        List<Integer> heads = new ArrayList<>();
        List<Integer> tails = new ArrayList<>();
        if (flag) {
            this.forwardsearch(query,segments, idx + 1, forward, tails, start, end, skips, limited_regex, properties);
            this.backwardsearch(query,segments, idx - 1, backward, heads, start, end, skips, limited_regex, properties);
        } else {
            idx = null;
            for (int i = start; i < end; ++i) {
                if (whole_without_kernel.contains(segments.get(i))) {
                    idx = i;
                    break;
                }
            }
            if (idx != null) {
                heads.add(idx);
                this.forwardsearch(query,segments, idx + 1, forward, tails, start, end, skips, limited_regex, properties);
            }
        }
        return setBoundaryInfo(heads, tails, flag, idx);
    }

    public void backwardsearch(String query,List<String> segments, int i, List<String> backward, List<Integer> heads, int start, int end, List<String> skips, List<Pattern> limited_regex, List<String> properties) {
        if (i < start) {
            heads.add(start);
            return;
        }
        if (backward.contains(segments.get(i)) || skips.contains(segments.get(i)) ||
                limited_regex.stream().filter(pattern -> pattern.matcher(segments.get(i)).find()).collect(Collectors.toList()).size() >= 1) {
            this.backwardsearch(query,segments, i - 1, backward, heads, start, end, skips, limited_regex, properties);
        } else {
            heads.add(i + 1);

            //if (i < end - 2 && properties.contains(String.join("", segments.subList(i, i + 2)))) {
            if (i < end - 2 && properties.contains(getOriginTextFromSegments(query,segments,i,i+2))) {
                heads.add(i + 2);
            }
        }
        return;
    }


    public void forwardsearch(String query,List<String> segments, int i, List<String> forward, List<Integer> tails, int start, int end, List<String> skips, List<Pattern> limited_regex, List<String> properties) {
        if (i >= end) {
            tails.add(i - 1);
            return;
        }
        String seg = segments.get(i);
        if (forward.contains(segments.get(i)) || skips.contains(segments.get(i)) ||
                limited_regex.stream().filter(pattern -> pattern.matcher(seg).find()).collect(Collectors.toList()).size() >= 1) {
            this.forwardsearch(query,segments, i + 1, forward, tails, start, end, skips, limited_regex, properties);
        } else {
            tails.add(i - 1);
//            if (segments.get(i).length() > 1) {
//                if (segments.get(i).startsWith("险")) {
//                    String split_a = segments.get(i).substring(0, 1);
//                    String split_b = segments.get(i).substring(1);
//                    segments.set(i, split_a);
//                    segments.add(i + 1, split_b);
//                    tails.set(tails.size() - 1, i);
//                    ++i;
//                }
//            }
            //if (i > start + 1 && properties.contains(String.join("", segments.subList(i - 1, i + 1))))
            if (i > start + 1 && properties.contains(getOriginTextFromSegments(query,segments,i - 1, i + 1)))
                tails.add(i - 2);
        }
        return;
    }

    private HashMap<String, Object> setBoundaryInfo(List<Integer> heads, List<Integer> tails, boolean flag, Integer kernel_idx) {
        HashMap<String, Object> BoundaryInfo = new HashMap<>();
        BoundaryInfo.put("heads", heads);
        BoundaryInfo.put("tails", tails);
        BoundaryInfo.put("flag", flag);
        BoundaryInfo.put("kernel_idx", kernel_idx);
        return BoundaryInfo;
    }

    public void adjustSegments(String query,List<String> segments, int idx, int start, boolean flag) {
        if (idx >= segments.size()) {
            if (flag) {
                if (idx - start > 1) {
                    List<String> word = new ArrayList<>();
                    for (int i = start; i < idx; ++i) {
                        word.add(segments.remove(start));
                    }
                    segments.add(start, String.join("", word));
                    //idx = start + 1;
                }
            }
            return;
        }

        if (!flag) {
            boolean flag1 = false;
            List<Pattern> patterns = (List<Pattern>) this.resources.get("patterns");
            for (Pattern pattern : patterns) {
                if (pattern.matcher(segments.get(idx)).find()) {
                    flag1 = true;
                    break;
                }
            }
            if (flag1) {
                start = idx;
                flag = true;
            }
            this.adjustSegments(query,segments, idx + 1, start, flag);
        } else {
            boolean flag1 = false;
            List<Pattern> patterns = (List<Pattern>) this.resources.get("patterns");
            for (Pattern pattern : patterns) {
                //if (pattern.matcher(String.join("", segments.subList(start, idx + 1))).find()) {
                if (pattern.matcher(getOriginTextFromSegments(query,segments,start, idx + 1)).find()) {
                    flag1 = true;
                    break;
                }
            }
            if (flag1) {
                this.adjustSegments(query,segments, idx + 1, start, flag);
            } else {
                //Pattern.compile("^年金").matcher(segments.get(idx)).find()||Pattern.compile("^款").matcher(segments.get(idx)).find())
                Integer loc = null;
                final String cur_seg = segments.get(idx);
                if (segments.get(idx).length() > 1 && ((List<Pattern>) this.resources.get("adjust_segment_patterns")).stream().filter(x -> x.matcher(cur_seg).find()).collect(Collectors.toList()).size() > 0) {
                    List<String> tmp = new ArrayList<>();
                    tmp.add(String.join("", segments.subList(start, idx)));
                    for (int i = 0; i < segments.get(idx).length(); ++i) {
                        String s = segments.get(idx).substring(i, i + 1);
                        tmp.add(s);
                        List<Pattern> patterns1 = (List<Pattern>) this.resources.get("patterns");
                        final String tmpstr = String.join("", tmp);
                        if(patterns1.stream().filter(pattern -> pattern.matcher(tmpstr).find()).collect(Collectors.toList()).size() == 0)
                        {
                            loc = i;
                            break;
                            }
                        }
                }
                if (loc != null && loc != 0) {
                    String seg = segments.get(idx);
                    segments.set(idx, seg.substring(0, loc));
                    idx += 1;
                    segments.add(idx, seg.substring(loc));
                }

                if (idx - start > 1) {
                    List<String> word = new ArrayList<>();
                    for (int i = start; i < idx; ++i) {
                        word.add(segments.remove(start));
                    }
                    segments.add(start, String.join("", word));
                    idx = start + 1;
                }

                flag = false;
                this.adjustSegments(query,segments, idx, start + 1, flag);
            }
        }
    }

    public List<String> cut(String text) {
        List<List<String>> info = new ArrayList<>();
        int left = 0;
        if (this.extras != null) {
            for (NERResult.Candidate extra : extras) {
                if (left != extra.getRealStart()) {
                    //1: 需要分词
                    //0: 不需要分词
                    List<String> tmp = new ArrayList<>();
                    tmp.add(text.substring(left, extra.getRealStart()));
                    tmp.add("1");
                    info.add(tmp);

                    List<String> tmp1 = new ArrayList<>();
                    tmp1.add(text.substring(extra.getRealStart(), extra.getRealEnd()));
                    tmp1.add("0");
                    info.add(tmp1);
                } else {
                    List<String> tmp1 = new ArrayList<>();
                    tmp1.add(text.substring(extra.getRealStart(), extra.getRealEnd()));
                    tmp1.add("0");
                    info.add(tmp1);
                }
                left = extra.getRealEnd();
            }
        }

        if (left < text.length()) {
            List<String> tmp = new ArrayList<>();
            tmp.add(text.substring(left, text.length()));
            tmp.add("1");
            info.add(tmp);
        }
        List<String> segments = new ArrayList<>();
        for (List<String> ele : info) {
            if (ele.get(1).equals("1")) {
                this.segmenter.process(ele.get(0), JiebaSegmenter.SegMode.SEARCH).stream().forEach(x -> segments.add(x.word));
            } else {
                segments.add(ele.get(0));
            }
        }

        return segments;
    }

    public HashMap<String, Object> getResources() {
        HashMap<String, Object> resources = new HashMap<>();
        List<String> kernel = this.getWordList(KERNEL_FILE);
        resources.put("kernel", kernel);
        List<String> limited = this.getWordList(USED_LIMITED_FILE);
        resources.put("limited", limited);
        List<String> prefix = this.getWordList(PREFIX_FILE);
        resources.put("prefix", prefix);
        List<String> suffix = this.getWordList(SUFFIX_FILE);
        resources.put("suffix", suffix);
        List<String> properties = this.getWordList(PROPERTIES_FILE);
        resources.put("properties", properties);
        List<String> standard_entity = this.getWordList(STANDARD_ENTITY_FILE);
        resources.put("standard_entity", standard_entity);
        List<String> plans = this.getWordList(PLAN_FILE);
        resources.put("plans", plans);
        List<String> ambiguous_prefix = this.getWordList(AMBIGUOUS_PREFIX_FILE);
        resources.put("ambiguous_prefix", ambiguous_prefix);
        List<String> ambiguous_suffix = this.getWordList(AMBIGUOUS_SUFFIX_FILE);
        resources.put("ambiguous_suffix", ambiguous_suffix);


        List<Pattern> limited_regex = this.getRegexList(USED_LIMITED_REGEX_FILE);
        resources.put("limited_regex", limited_regex);
        List<Pattern> tail_plus_one_patterns = this.getRegexList(TAIL_PLUS_ONE_PATTERNS_FILE);
        resources.put("tail_plus_one_patterns", tail_plus_one_patterns);
        List<Pattern> tail_minus_one_and_tail_cut_tail_patterns = this.getRegexList(TAIL_MINUS_ONE_AND_TAIL_CUT_TAIL_PATTERNS_FILE);
        resources.put("tail_minus_one_and_tail_cut_tail_patterns", tail_minus_one_and_tail_cut_tail_patterns);
        List<Pattern> tail_minus_one_and_tail_stop_patterns = this.getRegexList(TAIL_MINUS_ONE_AND_TAIL_STOP_PATTERNS_FILE);
        resources.put("tail_minus_one_and_tail_stop_patterns", tail_minus_one_and_tail_stop_patterns);


        List<String> forward = cloner.deepClone(suffix);
        List<String> CopyLimitted = cloner.deepClone(limited);
        List<String> CopyPlan = cloner.deepClone(plans);
        forward.addAll(CopyLimitted);
        forward.addAll(CopyPlan);

        resources.put("forward", forward);


        List<String> backward = cloner.deepClone(prefix);
        List<String> CopyLimitted1 = cloner.deepClone(limited);
        List<String> CopyPlan1 = cloner.deepClone(plans);
        forward.addAll(CopyLimitted1);
        forward.addAll(CopyPlan1);

        resources.put("backward", backward);

        List<String> whole_without_kernel = cloner.deepClone(prefix);
        List<String> CopyLimitted2 = cloner.deepClone(limited);
        List<String> CopyPlan2 = cloner.deepClone(plans);
        List<String> CopySuffix = cloner.deepClone(suffix);
        whole_without_kernel.addAll(CopyLimitted2);
        whole_without_kernel.addAll(CopyPlan2);
        whole_without_kernel.addAll(CopySuffix);
        whole_without_kernel = whole_without_kernel.stream().distinct().collect(Collectors.toList());

        resources.put("whole_without_kernel", whole_without_kernel);

        List<String> invalid_single_word_as_entity = cloner.deepClone(prefix);
        List<String> CopyLimitted3 = cloner.deepClone(limited);
        List<String> CopySuffix1 = cloner.deepClone(suffix);
        invalid_single_word_as_entity.addAll(CopyLimitted3);
        invalid_single_word_as_entity.addAll(CopySuffix1);
        invalid_single_word_as_entity = invalid_single_word_as_entity.stream().distinct().collect(Collectors.toList());

        resources.put("invalid_single_word_as_entity", invalid_single_word_as_entity);

        List<String> skips = this.getPunctuationList(SKIPS_FILE);
        resources.put("skips", skips);

        List<Pattern> patterns = new ArrayList<>();
        patterns.add(Pattern.compile("^[一二三四五六七八九零〇]+\\s*号{0,1}$"));
        patterns.add(Pattern.compile("^[0-9]+\\s*号{0,1}$"));
        patterns.add(Pattern.compile("^[0-9]+\\s*(年(?<!金))?(\\d+月?)?(\\d+(日(?<!额)|号)?)?((以|之)?(前|后))?$"));
        patterns.add(Pattern.compile("^[0-9]+\\s*(年(领)?)?$"));
        resources.put("patterns", patterns);
        return resources;
    }

    public List<String> getWordList(String file) {
        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(
                    Config.getLoader().getResourceAsStream(file), StandardCharsets.UTF_8));
            List<String> result = new ArrayList<>();
            br.lines().forEach(line -> {
                line = line.trim();
                if (line.length() == 0) //ignore blank line
                    return;
                if (line.charAt(0) == '#')  // ignore #
                    return;
                result.add(line);
            });
            br.close();
            return result;
        } catch (IOException ex) {
            throw new NLUException("Could not find region dict file " + file);
        }
    }

    public List<String> getPunctuationList(String file) {
        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(
                    Config.getLoader().getResourceAsStream(file), StandardCharsets.UTF_8));
            List<String> result = new ArrayList<>();
            br.lines().forEach(line -> {
                line = line.trim();
                if (line.length() == 0) //ignore blank line
                    return;
                result.add(line);
            });
            br.close();
            return result;
        } catch (IOException ex) {
            throw new NLUException("Could not find region dict file " + file);
        }
    }

    public List<Pattern> getRegexList(String file) {
        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(
                    Config.getLoader().getResourceAsStream(file), StandardCharsets.UTF_8));
            List<Pattern> result = new ArrayList<>();
            br.lines().forEach(line -> {
                line = line.trim();
                if (line.length() == 0) //ignore blank line
                    return;
                if (line.charAt(0) == '#')  // ignore #
                    return;
                result.add(Pattern.compile(line));
            });
            br.close();
            return result;
        } catch (IOException ex) {
            throw new NLUException("Could not find region dict file " + file);
        }
    }

    public HashMap<String, List<String>> getMapping() {
        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(
                    Config.getLoader().getResourceAsStream(MAPPING_FILE), StandardCharsets.UTF_8));
            HashMap<String, List<String>> result = new HashMap<>();
            br.lines().forEach(line -> {
                line = line.trim();
                if (line.length() == 0) //ignore blank line
                    return;
                if (line.charAt(0) == '#')  // ignore #
                    return;
                String[] split1 = line.split(" {4}");   // separated by space
                assert (split1.length == 2);
                String key = split1[0].trim();
                List<String> wordsets = Arrays.stream(split1[1].trim().split(",")).collect(Collectors.toList());
                result.put(key, wordsets);
            });
            br.close();
            return result;
        } catch (IOException ex) {
            throw new NLUException("Could not find region dict file " + MAPPING_FILE);
        }
    }

    public HashMap<String, Object> getResources(Map<String,NERResource> initial) {

        HashMap<String, Object> resources = new HashMap<>();

        for(String wordlist:PRODUCTNER_WORDLIST){
            NERResource tmp = initial.get(wordlist);
            if(tmp != null){
                resources.put(wordlist,tmp.getContents());
            }else
                return null;
        }

        for(String regexlist:PRODUCTNER_REGEXLIST){
            NERResource tmp = initial.get(regexlist);
            if(tmp != null){
                resources.put(regexlist,tmp.getContents());
            }else
                return null;
        }

        List<String> suffix = (List<String>)resources.get("suffix");
        List<String> limited = (List<String>)resources.get("limited");
        List<String> plans = (List<String>)resources.get("plans");
        List<String> prefix = (List<String>)resources.get("prefix");

        List<String> forward = cloner.deepClone(suffix);
        List<String> CopyLimitted = cloner.deepClone(limited);
        List<String> CopyPlan = cloner.deepClone(plans);
        forward.addAll(CopyLimitted);
        forward.addAll(CopyPlan);

        resources.put("forward", forward);


        List<String> backward = cloner.deepClone(prefix);
        List<String> CopyLimitted1 = cloner.deepClone(limited);
        List<String> CopyPlan1 = cloner.deepClone(plans);
        backward.addAll(CopyLimitted1);
        backward.addAll(CopyPlan1);

        resources.put("backward", backward);

        List<String> whole_without_kernel = cloner.deepClone(prefix);
        List<String> CopyLimitted2 = cloner.deepClone(limited);
        List<String> CopyPlan2 = cloner.deepClone(plans);
        List<String> CopySuffix = cloner.deepClone(suffix);
        whole_without_kernel.addAll(CopyLimitted2);
        whole_without_kernel.addAll(CopyPlan2);
        whole_without_kernel.addAll(CopySuffix);
        whole_without_kernel = whole_without_kernel.stream().distinct().collect(Collectors.toList());

        resources.put("whole_without_kernel", whole_without_kernel);

        List<String> invalid_single_word_as_entity = cloner.deepClone(prefix);
        List<String> CopyLimitted3 = cloner.deepClone(limited);
        List<String> CopySuffix1 = cloner.deepClone(suffix);
        invalid_single_word_as_entity.addAll(CopyLimitted3);
        invalid_single_word_as_entity.addAll(CopySuffix1);
        invalid_single_word_as_entity = invalid_single_word_as_entity.stream().distinct().collect(Collectors.toList());

        resources.put("invalid_single_word_as_entity", invalid_single_word_as_entity);

        List<String> skips = this.getPunctuationList(SKIPS_FILE);
        resources.put("skips", skips);

        List<Pattern> patterns = new ArrayList<>();
        patterns.add(Pattern.compile("^[A-Za-z]+\\+{0,1}\\s*款{0,1}$"));
        patterns.add(Pattern.compile("^[一二三四五六七八九零〇]+\\s*号{0,1}$"));
        patterns.add(Pattern.compile("^[0-9]+\\s*号{0,1}$"));
        patterns.add(Pattern.compile("^[0-9]+\\s*(年(?<!金))?(\\d+月?)?(\\d+(日(?<!额)|号)?)?((以|之)?(前|后))?$"));
        patterns.add(Pattern.compile("^[0-9]+\\s*(年(领)?)?$"));
        resources.put("patterns", patterns);

        return resources;
    }

    public static String makeQueryStringAllRegExp(String str) {
        if(StringUtils.isBlank(str)){
            return str;
           }
        return str.replace("\\", "\\\\").replace("*", "\\*")
                .replace("+", "\\+").replace("|", "\\|")
                .replace("{", "\\{").replace("}", "\\}")
                .replace("(", "\\(").replace(")", "\\)")
                .replace("^", "\\^").replace("$", "\\$")
                .replace("[", "\\[").replace("]", "\\]")
                .replace("?", "\\?").replace(",", "\\,")
                .replace(".", "\\.").replace("&", "\\&");
    }

    private String getOriginTextFromSegments(String text,List<String> segments,int idxsegstart,int idxsegend)
    {
        Integer begin = 0;
        for(String seg : segments.subList(0,idxsegstart)){
            begin += seg.length();

        }
        Integer size = 0;
        for(String seg : segments.subList(idxsegstart, idxsegend)){
            size += seg.length();
        }
        String entity = text.substring(begin,begin + size);

        return entity;
    }

}
