package ai.hual.labrador.nlu;

import ai.hual.labrador.dialog.AccessorRepository;
import ai.hual.labrador.nlu.preprocessors.Preprocessors;
import ai.hual.labrador.utils.ComponentScanUtils;
import com.google.common.collect.ImmutableList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static ai.hual.labrador.nlu.HandsUpIntentIdentifier.MATCHER_PROP_NAME;

/**
 * An implementation of NLU that utilize {@link Annotator} to rewrite query and slots.
 * Created by Dai Wentao on 2017/6/26.
 */
public class NLUImpl implements NLU {

    private static final Logger logger = LoggerFactory.getLogger(NLUImpl.class);

    private static final String NO_INTENT_MATCHER_MARK = "NO_INTENT_MATCHER";

    public static final int MAX_LENGTH = 64;

    public static final List<String> DEFAULT_ANNOTATORS = ImmutableList.of(
            "dictAnnotator",
            "numAnnotator",
            "timeAnnotator",
            "timeDurationAnnotator",
            "dateAnnotator",
            "dateDurationAnnotator",
            "phraseAnnotator");

    public static final List<String> DEFAULT_PREPROCESSORS = ImmutableList.of(
            "deleteSpacePreprocessor",
            "bracePreprocessor",
            "traditionalToSimplifiedChinesePreprocessor",
            "dbcCasePreprocessor");

    public static final String ANNOTATOR_SPLITTER = ",";
    public static final String PREPROCESSOR_SPLITTER = ",";

    public static final String ANNOTATOR_PACKAGE = "ai.hual.labrador.nlu.annotators";
    public static final String PREPROCESSOR_PACKAGE = "ai.hual.labrador.nlu.preprocessors";
    public static final String ANNOTATOR_PROP_NAME = "nlu.annotators";
    public static final String PREPROCESSOR_PROP_NAME = "nlu.preprocessors";

    private List<Annotator> annotators;
    private Preprocessors preprocessors;
    private IntentIdentifier intentIdentifier;

    public NLUImpl(DictModel dictmodel, GrammarModel grammarModel, AccessorRepository accessorRepository,
                   Properties properties) {
        this(dictmodel, grammarModel, accessorRepository, DEFAULT_ANNOTATORS, properties);
    }

    public NLUImpl(DictModel dictmodel, GrammarModel grammarModel, AccessorRepository accessorRepository,
                   List<String> annotators, Properties properties) {
        this(dictmodel, grammarModel, accessorRepository,
                annotators, new String[]{ANNOTATOR_PACKAGE},
                new String[]{PREPROCESSOR_PACKAGE}, properties);
    }

    /**
     * Construct a NLUImpl with {@link DictModel}, {@link GrammarModel}, annotators and preprocessors.
     *
     * @param dictModel            The dict model
     * @param grammarModel         The grammar model
     * @param accessorRepository   The accessor repository providing access to faq/kg/intent classifier/...
     * @param annotators           annotators used in NLUImpl
     * @param annotatorPackages    packages to be scanned to find annotator beans
     * @param preprocessorPackages packages to be scanned to find preprocessor beans
     * @param properties           properties
     */
    public NLUImpl(DictModel dictModel, GrammarModel grammarModel, AccessorRepository accessorRepository,
                   List<String> annotators, String[] annotatorPackages, String[] preprocessorPackages,
                   Properties properties) {
        Object matcherProp = properties.getProperty(MATCHER_PROP_NAME);
        if (matcherProp == null || !matcherProp.equals(""))
            intentIdentifier = new HandsUpIntentIdentifier(grammarModel, accessorRepository, properties);
        else    // no intent matcher specified, only empty string, means nlu stop before intentIdentifier
            intentIdentifier = null;
        if (properties.containsKey(ANNOTATOR_PROP_NAME)) {
            annotators = Arrays.asList(properties.getProperty(ANNOTATOR_PROP_NAME).split(ANNOTATOR_SPLITTER));
            logger.debug("Used annotators: {}", annotators);
        }

        // construct annotators and preprocessors
        this.annotators = ComponentScanUtils
                .withBean("dictModel", dictModel)
                .withBean("grammarModel", grammarModel)
                .withBean("properties", properties)
                .scan(annotators, annotatorPackages, Annotator.class);
        this.preprocessors = new Preprocessors(properties);
    }

    public NLUResult understand(String input) {
        // preprocess
        QueryAct queryAct = new QueryAct(preprocessors.preprocess(input));

        // go through annotators
        Stream<QueryAct> queryActStream = Stream.of(queryAct);
        for (Annotator annotator : annotators) {
            queryActStream = queryActStream
                    .flatMap(act -> annotator.annotate(act).stream())
                    .map(QueryActDistinctWrapper::new)
                    .distinct()
                    .sorted()
                    .limit(MAX_LENGTH)
                    .map(QueryActDistinctWrapper::getQueryAct);
        }
        List<QueryAct> queryActs = queryActStream.collect(Collectors.toList());

        if (intentIdentifier == null) {
            Map<String, List<QueryAct>> noIntentResultMap = new HashMap<>();
            noIntentResultMap.put(NO_INTENT_MATCHER_MARK, queryActs);
            return new NLUResult(NO_INTENT_MATCHER_MARK, 0, noIntentResultMap, queryActs);
        } else
            return intentIdentifier.identifyIntent(queryActs);
    }


}
