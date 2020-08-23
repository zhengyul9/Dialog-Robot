package ai.hual.labrador.nlu.annotators;

import ai.hual.labrador.utils.DateDurationUtils;
import org.springframework.stereotype.Component;

/**
 * Implementation of the <tt>RegexAnnotator</tt> abstract class.
 * This annotator can annotate <Strong>date duration</Strong> in queryAct,
 * before witch, the queryAct should already be annotated with
 * <tt>NumAnnotator</tt>.
 *
 * <p>During initialization, it will construct a map from function's name
 * to an lambda expression. Then, read the number regex file into memory.
 *
 * <p>Returned data structure is {@link DateDurationUtils.Duration}, where <tt>type</tt>
 * should be check first to determine the date unit that is being described. Then,
 * acquire Duration.length to get the duration period.
 * Notice, to avoid repeating, type here is using DateType.
 * For example, if type is DateType.DAY, and length is 5, it means this duration
 * is 5 days long.
 *
 * <p>
 * This is an example of the annotator's usage:
 * <pre>
 *     act.query = 3周的会议
 *     act.pQuery = {{数字}}周的会议
 *     act.slots = { 数字: [3] }
 *     -> DateDurationAnnotator.annotate(act) ->
 *     act.pQuery == {{日期段}}的会议
 *     act.slots == { 日期段: [DateDurationUtils.Duration(type: DateType.WEEK, length: 3) }
 * </pre>
 *
 * @author Yuqi
 * @see RegexAnnotator
 * @since 1.8
 */
@Component("dateDurationAnnotator")
public class DateDurationAnnotator extends RegexAnnotator {


    public DateDurationAnnotator() {

        // construct map from label to lambda function
        labelMap.put("duration(c)",
                valuePack -> DateDurationUtils.cDuration(((Double) valuePack.slotValues.get(0).matched).intValue()));
        labelMap.put("durationHalf(c)",
                valuePack -> DateDurationUtils.cDurationHalf(((Double) valuePack.slotValues.get(0).matched).intValue()));
        labelMap.put("durationCenturyHalf()",
                valuePack -> DateDurationUtils.cDurationHalf());
        labelMap.put("duration(y)",
                valuePack -> DateDurationUtils.yDuration(((Double) valuePack.slotValues.get(0).matched).intValue()));
        labelMap.put("durationHalf(y)",
                valuePack -> DateDurationUtils.yDurationHalf(((Double) valuePack.slotValues.get(0).matched).intValue()));
        labelMap.put("durationYearHalf()",
                valuePack -> DateDurationUtils.yDurationHalf());
        labelMap.put("duration(s)",
                valuePack -> DateDurationUtils.sDuration(((Double) valuePack.slotValues.get(0).matched).intValue()));
        labelMap.put("duration(m)",
                valuePack -> DateDurationUtils.mDuration(((Double) valuePack.slotValues.get(0).matched).intValue()));
        labelMap.put("durationHalf(m)",
                valuePack -> DateDurationUtils.mDurationHalf(((Double) valuePack.slotValues.get(0).matched).intValue()));
        labelMap.put("durationMonthHalf()",
                valuePack -> DateDurationUtils.mDurationHalf());
        labelMap.put("duration(td)",
                valuePack -> DateDurationUtils.tdDuration(((Double) valuePack.slotValues.get(0).matched).intValue()));
        labelMap.put("duration(w)",
                valuePack -> DateDurationUtils.wDuration(((Double) valuePack.slotValues.get(0).matched).intValue()));
        labelMap.put("duration(d)",
                valuePack -> DateDurationUtils.dDuration(((Double) valuePack.slotValues.get(0).matched).intValue()));

        /* get regex list */
        this.regexList = fetchRegexFile("date_duration_regex_file");
    }

}
