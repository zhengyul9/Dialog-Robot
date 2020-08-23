package ai.hual.labrador.nlu.annotators;

import ai.hual.labrador.utils.DirectionalTimeUtils;
import ai.hual.labrador.utils.TimeDurationUtils;
import ai.hual.labrador.utils.TimeUtils;
import org.springframework.stereotype.Component;

/**
 * Implementation of the <tt>RegexAnnotator</tt> abstract class.
 * This annotator can annotate <Strong>directional time</Strong>
 * , which is either a ray (with start or end point) or a line
 * segment in time line. Before this annotator, the queryAct
 * should already be annotated with <tt>NumAnnotator</tt>,
 * <tt>TimeAnnotator</tt> and <tt>TimeDurationAnnotator</tt>.
 *
 * <p>During initialization, it will construct a map from function's name
 * to an lambda expression. Then, read the number regex file into memory.
 *
 * <p>Returned data structure is {@link DirectionalTimeUtils.DirectionalTime},
 * where <tt>type</tt> should be check first to determine the time unit that
 * is being described. Then, check the start and end field, only one of them
 * is assigned a DirectionalTime object, the other is null. Finally, get length,
 * if it's 0, which means the length is not defined, so it's a time ray, with
 * only one end defined; if it's bigger than 0, then this is a time segment with
 * defined length.
 * For example, if <tt>type</tt> is TimeType.HOUR, <tt>start</tt> is Date( type: TimeType.HOUR,
 * hour: 8, minute: 0, second: 0 ), and length is 3, it means this is a defined
 * time period of 3 hours long, start at 8:00:00.
 *
 * <p>Notice, if you also need the Date information, then <tt>DateAnnotator</tt>
 * should be applied before this annotator. So that you can look for slots
 * whose key is "日期" in resulting queryAct.
 *
 * <p>
 * This is an example of the annotator's usage:
 * <pre>
 *     act.query = 从8点10分开始到10点左右的电影
 *     act.pQuery = 从{{时刻}}开始到{{时刻}}左右的电影
 *     act.slots = { 时刻: [TimeUtils.Time(type: TimeType.MINUTE, hour: 8, minute: 10),
 *                  TimeUtils.Time(type: TimeType.HOUR, hour: 10)] }
 *     -> DirectionalTimeAnnotator.annotate(act) ->
 *     act.pQuery == {{时刻起始}}{{时刻结束}}左右的电影
 *     act.slots == {
 *      时刻起始:
 *      [DirectionalTimeUtils.DirectionalTime(type: TimeType.MINUTE, start: TimeUtils.Time(... hour: 8 ...), length: 0)],
 *      时刻结束:
 *      [DirectionalTimeUtils.DirectionalTime(type: TimeType.HOUR, start: TimeUtils.Time(... hour: 10 ...), length: 0]
 *     }
 * </pre>
 *
 * @author Yuqi
 * @see RegexAnnotator
 * @since 1.8
 */
@Component("directionalTimeAnnotator")
public class DirectionalTimeAnnotator extends RegexAnnotator {

    public DirectionalTimeAnnotator() {

        /* construct map from label to lambda function */
        labelMap.put("startTime(time)",
                valuePack -> DirectionalTimeUtils.afterTime((TimeUtils.Time) valuePack.slotValues.get(0).matched));
        labelMap.put("endTime(time)",
                valuePack -> DirectionalTimeUtils.beforeTime((TimeUtils.Time) valuePack.slotValues.get(0).matched));
        labelMap.put("prevTimeDuration(time_duration)",
                valuePack -> DirectionalTimeUtils.prevTimeDuration(
                        (TimeDurationUtils.Duration) valuePack.slotValues.get(0).matched));
        labelMap.put("nextTimeDuration(time_duration)",
                valuePack -> DirectionalTimeUtils.nextTimeDuration(
                        (TimeDurationUtils.Duration) valuePack.slotValues.get(0).matched));
        labelMap.put("prevTimeDuration(time,time_duration)",
                valuePack -> DirectionalTimeUtils.prevTimeDuration(
                        (TimeUtils.Time) valuePack.slotValues.get(0).matched,
                        (TimeDurationUtils.Duration) valuePack.slotValues.get(1).matched));
        labelMap.put("nextTimeDuration(time,time_duration)",
                valuePack -> DirectionalTimeUtils.nextTimeDuration(
                        (TimeUtils.Time) valuePack.slotValues.get(0).matched,
                        (TimeDurationUtils.Duration) valuePack.slotValues.get(1).matched));

        /* get regex list */
        this.regexList = fetchRegexFile("directional_time_regex_file");
    }

}
