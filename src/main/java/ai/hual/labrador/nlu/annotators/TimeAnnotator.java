package ai.hual.labrador.nlu.annotators;

import ai.hual.labrador.utils.TimeUtils;
import org.springframework.stereotype.Component;

/**
 * Implementation of the <tt>RegexAnnotator</tt> abstract class.
 * This annotator can annotate <Strong>time point in 24-Hour format</Strong>
 * in queryAct, before witch, the queryAct should already be annotated with
 * <tt>NumAnnotator</tt>.
 *
 * <p>During initialization, it will construct a map from function's name
 * to an lambda expression. Then, read the number regex file into memory.
 *
 * <p>Annotated data structure is {@link TimeUtils.Time}, where <tt>type</tt>
 * should be check first to determine the lowest time unit level available. Then,
 * programmer can acquire the upper level time units in need.
 * For example, if type is TimeType.SECOND, this implies that Time.hour, Time.minute,
 * and Time.second is all set meaningfully. In contrast, if type is TimeType.HOUR,
 * then only Time.hour is set, others all 0.
 *
 * <p>
 * This is an example of the annotator's usage:
 * <pre>
 *     act.query = 12点2两刻10秒,和9:10Pm
 *     act.pQuery = {{数字}}点{{数字}}刻{{数字}}秒,和{{数字}}:{{数字}}Pm
 *     act.slots = { 数字: [12, 2, 10, 9, 10] }
 *     -> DateAnnotator.annotate(act) ->
 *     act.pQuery == {{时刻}},和{{时刻}}
 *     act.slots == { 时刻: [TimeUtils.Time(type: TimeType.SECOND, hour: 12, minute: 30, second: 10),
 *                          TimeUtils.Time(type: TimeType.MINUTE, hour: 21, minute: 10, second: 0] }
 * </pre>
 *
 * @author Yuqi
 * @see RegexAnnotator
 * @since 1.8
 */
@Component("timeAnnotator")
public class TimeAnnotator extends RegexAnnotator {


    public TimeAnnotator() {

        /* construct map from label to lambda function */
        labelMap.put("time(h)",
                valuePack -> TimeUtils.hTime(((Double) valuePack.slotValues.get(0).matched).intValue()));
        labelMap.put("time(m)",
                valuePack -> TimeUtils.mTime(((Double) valuePack.slotValues.get(0).matched).intValue()));
        labelMap.put("time(s)",
                valuePack -> TimeUtils.sTime(((Double) valuePack.slotValues.get(0).matched).intValue()));
        labelMap.put("timeHalf(h)",
                valuePack -> TimeUtils.hHalfHour(((Double) valuePack.slotValues.get(0).matched).intValue()));
        labelMap.put("time(h,m)",
                valuePack -> TimeUtils.hmTime(((Double) valuePack.slotValues.get(0).matched).intValue(),
                        ((Double) valuePack.slotValues.get(1).matched).intValue()));
        labelMap.put("time(h,m,s)",
                valuePack -> TimeUtils.hmsTime(((Double) valuePack.slotValues.get(0).matched).intValue(),
                        ((Double) valuePack.slotValues.get(1).matched).intValue(),
                        ((Double) valuePack.slotValues.get(2).matched).intValue()));
        labelMap.put("timeAm(h)",
                valuePack -> TimeUtils.hTimeAm(((Double) valuePack.slotValues.get(0).matched).intValue()));
        labelMap.put("timePm(h)",
                valuePack -> TimeUtils.hTimePm(((Double) valuePack.slotValues.get(0).matched).intValue()));
        labelMap.put("timeNight(h)",
                valuePack -> TimeUtils.hTimeNight(((Double) valuePack.slotValues.get(0).matched).intValue()));
        labelMap.put("timeNight(h,m)",
                valuePack -> TimeUtils.hmTimeNight(((Double) valuePack.slotValues.get(0).matched).intValue(),
                        ((Double) valuePack.slotValues.get(1).matched).intValue()));
        labelMap.put("timeNight(h,q)",
                valuePack -> TimeUtils.hqTimeNight(((Double) valuePack.slotValues.get(0).matched).intValue(),
                        ((Double) valuePack.slotValues.get(1).matched).intValue()));
        labelMap.put("time(h,q)",
                valuePack -> TimeUtils.hqTime(((Double) valuePack.slotValues.get(0).matched).intValue(),
                        ((Double) valuePack.slotValues.get(1).matched).intValue()));
        labelMap.put("timeHalfAm(h)",
                valuePack -> TimeUtils.hHalfHourAm(((Double) valuePack.slotValues.get(0).matched).intValue()));
        labelMap.put("timeHalfPm(h)",
                valuePack -> TimeUtils.hHalfHourPm(((Double) valuePack.slotValues.get(0).matched).intValue()));
        labelMap.put("timeAm(h,m)",
                valuePack -> TimeUtils.hmTimeAm(((Double) valuePack.slotValues.get(0).matched).intValue(),
                        ((Double) valuePack.slotValues.get(1).matched).intValue()));
        labelMap.put("timePm(h,m)",
                valuePack -> TimeUtils.hmTimePm(((Double) valuePack.slotValues.get(0).matched).intValue(),
                        ((Double) valuePack.slotValues.get(1).matched).intValue()));
        labelMap.put("timeAm(h,q)",
                valuePack -> TimeUtils.hqTimeAm(((Double) valuePack.slotValues.get(0).matched).intValue(),
                        ((Double) valuePack.slotValues.get(1).matched).intValue()));
        labelMap.put("timePm(h,q)",
                valuePack -> TimeUtils.hqTimePm(((Double) valuePack.slotValues.get(0).matched).intValue(),
                        ((Double) valuePack.slotValues.get(1).matched).intValue()));
        labelMap.put("timeAm(h,m,s)",
                valuePack -> TimeUtils.hmsTimeAm(((Double) valuePack.slotValues.get(0).matched).intValue(),
                        ((Double) valuePack.slotValues.get(1).matched).intValue(),
                        ((Double) valuePack.slotValues.get(2).matched).intValue()));
        labelMap.put("timePm(h,m,s)",
                valuePack -> TimeUtils.hmsTimePm(((Double) valuePack.slotValues.get(0).matched).intValue(),
                        ((Double) valuePack.slotValues.get(1).matched).intValue(),
                        ((Double) valuePack.slotValues.get(2).matched).intValue()));
        labelMap.put("timeNight(h,m,s)",
                valuePack -> TimeUtils.hmsTimeNight(((Double) valuePack.slotValues.get(0).matched).intValue(),
                        ((Double) valuePack.slotValues.get(1).matched).intValue(),
                        ((Double) valuePack.slotValues.get(2).matched).intValue()));
        labelMap.put("thisHour()",
                valuePack -> TimeUtils.thisHour());
        labelMap.put("thisTime()",
                valuePack -> TimeUtils.thisTime());
        labelMap.put("prevHour(1)",
                valuePack -> TimeUtils.prevHourOne());
        labelMap.put("nextHour(1)",
                valuePack -> TimeUtils.nextHourOne());
        labelMap.put("beforeDayHalf()",
                valuePack -> TimeUtils.beforeDayHalf());
        labelMap.put("beforeDayHalf(d)",
                valuePack -> TimeUtils.beforeDayHalf(((Double) valuePack.slotValues.get(0).matched).intValue()));
        labelMap.put("beforeHour(h)",
                valuePack -> TimeUtils.beforeHour(((Double) valuePack.slotValues.get(0).matched).intValue()));
        labelMap.put("beforeHourHalf()",
                valuePack -> TimeUtils.beforeHourHalf());
        labelMap.put("beforeHourHalf(h)",
                valuePack -> TimeUtils.beforeHourHalf(((Double) valuePack.slotValues.get(0).matched).intValue()));
        labelMap.put("beforeQuarter(q)",
                valuePack -> TimeUtils.beforeQuarter(((Double) valuePack.slotValues.get(0).matched).intValue()));
        labelMap.put("beforeMin(h)",
                valuePack -> TimeUtils.beforeMin(((Double) valuePack.slotValues.get(0).matched).intValue()));
        labelMap.put("beforeMinHalf()",
                valuePack -> TimeUtils.beforeMinHalf());
        labelMap.put("beforeMinHalf(m)",
                valuePack -> TimeUtils.beforeMinHalf(((Double) valuePack.slotValues.get(0).matched).intValue()));
        labelMap.put("beforeSec(s)",
                valuePack -> TimeUtils.beforeSec(((Double) valuePack.slotValues.get(0).matched).intValue()));
        labelMap.put("afterDayHalf()",
                valuePack -> TimeUtils.afterDayHalf());
        labelMap.put("afterDayHalf(d)",
                valuePack -> TimeUtils.afterDayHalf(((Double) valuePack.slotValues.get(0).matched).intValue()));
        labelMap.put("afterHour(h)",
                valuePack -> TimeUtils.afterHour(((Double) valuePack.slotValues.get(0).matched).intValue()));
        labelMap.put("afterHourHalf()",
                valuePack -> TimeUtils.afterHourHalf());
        labelMap.put("afterHourHalf(d)",
                valuePack -> TimeUtils.afterHourHalf(((Double) valuePack.slotValues.get(0).matched).intValue()));
        labelMap.put("afterQuarter(q)",
                valuePack -> TimeUtils.afterQuarter(((Double) valuePack.slotValues.get(0).matched).intValue()));
        labelMap.put("afterMin(m)",
                valuePack -> TimeUtils.afterMin(((Double) valuePack.slotValues.get(0).matched).intValue()));
        labelMap.put("afterMinHalf()",
                valuePack -> TimeUtils.afterMinHalf());
        labelMap.put("afterMinHalf(m)",
                valuePack -> TimeUtils.afterMinHalf(((Double) valuePack.slotValues.get(0).matched).intValue()));
        labelMap.put("afterSec(s)",
                valuePack -> TimeUtils.afterSec(((Double) valuePack.slotValues.get(0).matched).intValue()));

        /* get regex list */
        this.regexList = fetchRegexFile("time_regex_file");
    }

}
