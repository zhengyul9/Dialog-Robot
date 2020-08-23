package ai.hual.labrador.nlu.annotators;

import ai.hual.labrador.utils.TimeDurationUtils;
import org.springframework.stereotype.Component;

/**
 * Implementation of the <tt>RegexAnnotator</tt> abstract class.
 * This annotator can annotate <Strong>time duration</Strong> in queryAct,
 * before witch, the queryAct should already be annotated with
 * <tt>NumAnnotator</tt>.
 *
 * <p>During initialization, it will construct a map from function's name
 * to an lambda expression. Then, read the number regex file into memory.
 *
 * <p>Returned data structure is {@link TimeDurationUtils.Duration}, where <tt>type</tt>
 * should be check first to determine the time unit that is being described. Then,
 * acquire Duration.length to get the duration period.
 * Notice, to avoid repeating, type here is using TimeType.
 * For example, if type is TimeType.HOUR, and length is 5, it means this duration
 * is 5 hours long.
 *
 * <p>
 * This is an example of the annotator's usage:
 * <pre>
 *     act.query = 两小时的电影
 *     act.pQuery = {{数字}}小时的电影
 *     act.slots = { 数字: [2] }
 *     -> TimeDurationAnnotator.annotate(act) ->
 *     act.pQuery == {{时刻段}}的电影
 *     act.slots == { 时刻段: [TimeDurationUtils.Duration(type: TimeType.HOUR, length: 2) }
 * </pre>
 *
 * @author Yuqi
 * @see RegexAnnotator
 * @since 1.8
 */
@Component("timeDurationAnnotator")
public class TimeDurationAnnotator extends RegexAnnotator {

    public TimeDurationAnnotator() {

        /* construct map from label to lambda function */
        labelMap.put("durationHalf(d)",
                valuePack -> TimeDurationUtils.dDurationHalf(((Double) valuePack.slotValues.get(0).matched).intValue()));
        labelMap.put("durationDayHalf()",
                valuePack -> TimeDurationUtils.dDurationHalf());
        labelMap.put("duration(h,m,s)",
                valuePack -> TimeDurationUtils.hmsDuration(((Double) valuePack.slotValues.get(0).matched).intValue(),
                        ((Double) valuePack.slotValues.get(1).matched).intValue(), ((Double) valuePack.slotValues.get(2).matched).intValue()));
        labelMap.put("duration(h,m)",
                valuePack -> TimeDurationUtils.hmDuration(((Double) valuePack.slotValues.get(0).matched).intValue(),
                        ((Double) valuePack.slotValues.get(1).matched).intValue()));
        labelMap.put("duration(m,s)",
                valuePack -> TimeDurationUtils.msDuration(((Double) valuePack.slotValues.get(0).matched).intValue(),
                        ((Double) valuePack.slotValues.get(1).matched).intValue()));
        labelMap.put("duration(h,s)",
                valuePack -> TimeDurationUtils.hsDuration(((Double) valuePack.slotValues.get(0).matched).intValue(),
                        ((Double) valuePack.slotValues.get(1).matched).intValue()));
        labelMap.put("duration(h)",
                valuePack -> TimeDurationUtils.hDuration(((Double) valuePack.slotValues.get(0).matched).intValue()));
        labelMap.put("durationHalf(h)",
                valuePack -> TimeDurationUtils.hDurationHalf(((Double) valuePack.slotValues.get(0).matched).intValue()));
        labelMap.put("durationHourHalf()",
                valuePack -> TimeDurationUtils.hDurationHalf());
        labelMap.put("duration(q)",
                valuePack -> TimeDurationUtils.qDuration(((Double) valuePack.slotValues.get(0).matched).intValue()));
        labelMap.put("duration(m)",
                valuePack -> TimeDurationUtils.mDuration(((Double) valuePack.slotValues.get(0).matched).intValue()));
        labelMap.put("durationHalf(m)",
                valuePack -> TimeDurationUtils.mDurationHalf(((Double) valuePack.slotValues.get(0).matched).intValue()));
        labelMap.put("durationMinHalf()",
                valuePack -> TimeDurationUtils.mDurationHalf());
        labelMap.put("duration(s)",
                valuePack -> TimeDurationUtils.sDuration(((Double) valuePack.slotValues.get(0).matched).intValue()));
        labelMap.put("durationHalf(s)",
                valuePack -> TimeDurationUtils.sDurationHalf(((Double) valuePack.slotValues.get(0).matched).intValue()));
        labelMap.put("durationSecHalf()",
                valuePack -> TimeDurationUtils.sDurationHalf());
        labelMap.put("duration(ms)",
                valuePack -> TimeDurationUtils.msDuration(((Double) valuePack.slotValues.get(0).matched).intValue()));

        /* get regex list */
        this.regexList = fetchRegexFile("time_duration_regex_file");
    }

}
