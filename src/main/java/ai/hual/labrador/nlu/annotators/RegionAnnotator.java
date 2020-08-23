package ai.hual.labrador.nlu.annotators;

import ai.hual.labrador.exceptions.NLUException;
import ai.hual.labrador.nlu.Config;
import ai.hual.labrador.nlu.Dict;
import ai.hual.labrador.nlu.DictModel;
import ai.hual.labrador.nlu.QueryAct;
import ai.hual.labrador.nlu.annotators.dict.VanillaCombinationBFS;
import ai.hual.labrador.utils.RegionUtils;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 * Implementation of the <tt>RegexAnnotator</tt> abstract class.
 * This annotator can annotate <Strong>region</Strong> in queryAct.
 *
 * <p>During initialization, it will construct a map from function's name
 * to an lambda expression. Then, read the number regex file into memory.
 * As the process of annotate is mainly extracting region words from dict,
 * a dictAnnotator is used precedingly.
 *
 * <p>Annotated data structure is {@link RegionUtils.Region}, where <tt>type</tt>
 * should be check first to determine the specific type of region. The name field
 * contains the name of region. NOTICE, region does not need to be understood.
 *
 * <p>
 * This is an example of the annotator's usage:
 * <pre>
 *     act.query = 在深圳
 *     act.pQuery = 在深圳
 *     -> RegionAnnotator.annotate(act) ->
 *     act.pQuery == 在{{市}}
 *     act.slots == { 市: [RegionUtils.Region(type: RegionType.CITY, name: "深圳")] }
 * </pre>
 *
 * @author Yuqi
 * @see RegexAnnotator
 * @since 1.8
 */
@Component("regionAnnotator")
public class RegionAnnotator extends RegexAnnotator {

    private DictAnnotator regionDA;
    private static Properties properties = new Properties();

    public RegionAnnotator() {

        String dictFile = "region_file";
        DictModel regionDictModel = getRegionDictModel(dictFile);
        properties.setProperty("nlu.dictAnnotator.usePinyinRobust", "false");
        regionDA = new DictAnnotator(regionDictModel, properties);

        /* get regex list */
        this.regexList = fetchRegexFile("region_regex_file");

        /* construct map from label to lambda function */
        labelMap.put("province(province)",
                valuePack -> RegionUtils.province((String) valuePack.slotValues.get(0).matched));
        labelMap.put("city(city)",
                valuePack -> RegionUtils.city((String) valuePack.slotValues.get(0).matched));
        labelMap.put("county(county)",
                valuePack -> RegionUtils.county((String) valuePack.slotValues.get(0).matched));
        labelMap.put("district(district)",
                valuePack -> RegionUtils.district((String) valuePack.slotValues.get(0).matched));
    }

    /**
     * Create DictModel from dictionary file, where each line is formatted as
     * label=key.
     *
     * @param file region dictionary file
     * @return DictModel of region
     */
    private DictModel getRegionDictModel(String file) {

        // Initialize configurations
        new Config();
        String LOCATION_FILE = Config.get(file);

        List<Dict> dictList = new ArrayList<>();

        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(
                    Config.getLoader().getResourceAsStream(LOCATION_FILE), StandardCharsets.UTF_8));
            br.lines().forEach(line -> {
                if (line.charAt(0) == '#')  // ignore #
                    return;
                String[] split1 = line.split("=");   // separated by tab
                assert (split1.length == 2);
                String label = split1[0].trim();  // get label
                String word = split1[1].trim();  // get key

                dictList.add(new Dict(label, word));
            });
            br.close();
        } catch (IOException ex) {
            throw new NLUException("Could not find region dict file " + LOCATION_FILE);
        }

        return new DictModel(dictList);
    }

    /**
     * Use DictAnnotator first, then annotate using regex.
     *
     * @param queryAct The query act to be annotated.
     * @return list of queryAct
     */
    @Override
    public List<QueryAct> annotate(QueryAct queryAct) {

        // annotate with dictionary first
        List<QueryAct> resultListMax = regionDA.annotate(queryAct);
        List<QueryAct> resultList = new VanillaCombinationBFS().combinationBFS(resultListMax);

        List<QueryAct> queryActList = new ArrayList<>();    // return result
        // get regex list
        List<LabeledRegex<String>> regexList = getRegex();
        if (regexList.size() == 0) {
            return queryActList;
        }

        resultList.forEach(act -> {
            QueryAct annotated = greedyMatchAnnotate(act);
            queryActList.add(annotated);
        });

        return queryActList;
    }
}
