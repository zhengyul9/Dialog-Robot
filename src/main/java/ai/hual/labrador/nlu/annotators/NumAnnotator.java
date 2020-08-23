package ai.hual.labrador.nlu.annotators;

import ai.hual.labrador.nlu.QueryAct;
import ai.hual.labrador.nlu.SlotValue;
import ai.hual.labrador.utils.DigitUtils;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;


/**
 * Implementation of the <tt>RegexAnnotator</tt> abstract class.  This
 * annotator can annotate <strong>integer numbers</strong> in queryAct,
 * Arabic and Chinese.
 *
 * <p>During initialization, it will construct a map from function's name
 * to an lambda expression. Then, read the number regex file into memory.
 *
 * <p><strong>Note that this annotator will left Chinese special expression
 * of year number un-annotated, which should be annotated in
 * <tt>DateAnnotator</tt></strong>
 * Some example for such expression are: 二零零五, 一七, 零二, 九八
 *
 * <p>
 * This is an example of this annotator's usage:
 * <pre>
 *     act.pQuery = 2017年八月1日,    act.slots = { }
 *     -> NumAnnotator.annotate(act) ->
 *     act.pQuery == {{数字}}年{{数字}}月{{数字}}日,
 *     act.slots == { 数字: [2017, 8, 1] }
 * </pre>
 *
 * @author Yuqi
 * @see RegexAnnotator
 * @since 1.8
 */
@Component("numAnnotator")
public class NumAnnotator extends RegexAnnotator {


    private List<LabeledRegex<String>> allRegex;
    private List<LabeledRegex<String>> firstRoundRegex;
    private List<LabeledRegex<String>> secondRoundRegex;

    public NumAnnotator() {

        // construct map from label to lambda function
        labelMap.put("getDigits(str)", valuePack -> DigitUtils.getDigits(valuePack.matched));
        labelMap.put("consecutiveDigits(str)", valuePack -> DigitUtils.consecutiveDigits(valuePack.matchedQuery));
        labelMap.put("getMixedDigits(str)", valuePack -> DigitUtils.getDigits(valuePack.matchedQuery));
        labelMap.put("getFractionDigits(str,slot)", valuePack ->
                DigitUtils.getFractionalDigits(valuePack.matchedQuery, valuePack.slotValues));
        labelMap.put("getDecimalDigits(slot)", valuePack ->
                DigitUtils.getDecimalDigits(valuePack.matchedQuery, valuePack.slotValues));
        labelMap.put("combineDigits(slot)", valuePack -> DigitUtils.combineDigits(valuePack.slotValues));
        labelMap.put("percent(digit)", valuePack -> DigitUtils.getPercentDigit(valuePack.slotValues));

        // get regex list
        this.regexList = fetchRegexFile("number_regex_file");

        this.allRegex = this.regexList;
        this.firstRoundRegex = this.regexList.stream()
                .filter(r -> !r.getRegex().pattern().contains("\\{\\{"))
                .collect(Collectors.toList());

        this.secondRoundRegex = this.regexList.stream()
                .filter(r -> r.getRegex().pattern().contains("\\{\\{"))
                .collect(Collectors.toList());
    }

    /**
     * Annotate twice to deal with mixed number expression.
     *
     * @param queryAct The query act to be annotated.
     * @return list of queryAct
     */
    @Override
    public List<QueryAct> annotate(QueryAct queryAct) {
        super.regexList = this.firstRoundRegex;
        List<QueryAct> firstRound = super.annotate(queryAct);
        // the second round don't need 2^n cause it is the unfinished phase of round one,
        // which means all matched part should be transformed.
        super.regexList = this.secondRoundRegex;
        List<QueryAct> result = firstRound.stream()
                .map(super::greedyMatchAnnotate)
                .distinct()
                .collect(Collectors.toList());
        super.regexList = this.allRegex; // reset
        return result;
    }

    @Override
    public boolean matchedValueFilter(QueryAct queryAct, SlotValue matchedValue) {
        List<Character> filterList = Arrays.asList('天', '个');
        List<String> matchedQueryPartList;
        String query = queryAct.getQuery();
        String pQuery = queryAct.getPQuery();
        int pQueryStart = matchedValue.start;
        int pQueryEnd = matchedValue.start + matchedValue.matched.toString().length();
        if (queryAct.getQuery().equals(pQuery)) {
            return true;
        } else {
            matchedQueryPartList = queryAct.getSortedSlotAsList().stream()
                    .filter(slot -> slot.start >= pQueryStart && slot.end <= pQueryEnd)
                    .map(slot -> query.substring(slot.realStart, slot.realEnd))
                    .collect(Collectors.toList());
        }
        int slotCount = matchedQueryPartList.size();
        if (slotCount == 2) {
            if (pQueryEnd != pQuery.length() && filterList.contains(pQuery.charAt(pQueryEnd)))
                return false;
            else if (matchedQueryPartList.get(0).equals("一") && matchedQueryPartList.get(1).equals("两"))
                return false;
        }
        return true;
    }
}
