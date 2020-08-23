package ai.hual.labrador.nlu.annotators;

import ai.hual.labrador.utils.DateDurationUtils;
import ai.hual.labrador.utils.DateUtils;
import ai.hual.labrador.utils.DirectionalDateUtils;
import org.springframework.stereotype.Component;

/**
 * Implementation of the <tt>RegexAnnotator</tt> abstract class.
 * This annotator can annotate <Strong>directional date</Strong>
 * , which is either a ray (with start or end point) or a line
 * segment in time line. Before this annotator, the queryAct
 * should already be annotated with <tt>NumAnnotator</tt>,
 * <tt>DateAnnotator</tt> and <tt>DateDurationAnnotator</tt>.
 *
 * <p>During initialization, it will construct a map from function's name
 * to an lambda expression. Then, read the number regex file into memory.
 *
 * <p>Returned data structure is {@link DirectionalDateUtils.DirectionalDate},
 * where <tt>type</tt> should be check first to determine the date unit that
 * is being described. Then, check the start and end field, only one of them
 * is assigned a DirectionalDate object, the other is null. Finally, get length,
 * if it's 0, which means the length is not defined, so it's a time ray, with
 * only one end defined; if it's bigger than 0, then this is a time segment with
 * defined length.
 * For example, if type is DateType.DAY, start is Date( type: DateType.DAY,
 * year: 2017, month: 8, day: 1 ), and length is 3, it means this is a defined
 * date period of 3 days long, start at 2017-08-01.
 *
 * <p>
 * This is an example of the annotator's usage:
 * <pre>
 *     act.query = 之后五天或者8号后的机票
 *     act.pQuery = 之后{{日期段}}或者{{日期}}后的机票
 *     act.slots = { 日期段: [DateDurationUtils.Duration(type: DateType.DAY, length: 5],
 *                  日期: [DateUtils.Date(type: DateType.DAY, ..., day: 8 ...)]
 *                  }
 *     -> DirectionalDateAnnotator.annotate(act) ->
 *     act.pQuery == {{确定日期段}}或者{{日期起始}}的机票
 *     act.slots == {
 *      确定日期段:
 *      [DirectionalDateUtils.DirectionalDate(type: DateType.DAY, start: DateUtils.Date("today"), length: 5)],
 *      日期起始:
 *      [DirectionalDateUtils.DirectionalDate(type: DateType.DAY, start: DateUtils.Date(... day: 8 ...), length: 0)]
 *     }
 * </pre>
 *
 * @author Yuqi
 * @see RegexAnnotator
 * @since 1.8
 */
@Component("directionalDateAnnotator")
public class DirectionalDateAnnotator extends RegexAnnotator {

    public DirectionalDateAnnotator() {

        /* construct map from label to lambda function */
        labelMap.put("startDate(date)",
                valuePack -> DirectionalDateUtils.startDate((DateUtils.Date) valuePack.slotValues.get(0).matched));
        labelMap.put("endDate(date)",
                valuePack -> DirectionalDateUtils.endDate((DateUtils.Date) valuePack.slotValues.get(0).matched));
        labelMap.put("prevDateDuration(date_duration)",
                valuePack -> DirectionalDateUtils.prevDateDuration(
                        (DateDurationUtils.Duration) valuePack.slotValues.get(0).matched));
        labelMap.put("nextDateDuration(date_duration)",
                valuePack -> DirectionalDateUtils.nextDateDuration(
                        (DateDurationUtils.Duration) valuePack.slotValues.get(0).matched));
        labelMap.put("prevDateDuration(date,date_duration)",
                valuePack -> DirectionalDateUtils.prevDateDuration(
                        (DateUtils.Date) valuePack.slotValues.get(0).matched,
                        (DateDurationUtils.Duration) valuePack.slotValues.get(1).matched));
        labelMap.put("nextDateDuration(date,date_duration)",
                valuePack -> DirectionalDateUtils.nextDateDuration(
                        (DateUtils.Date) valuePack.slotValues.get(0).matched,
                        (DateDurationUtils.Duration) valuePack.slotValues.get(1).matched));

        /* get regex list */
        this.regexList = fetchRegexFile("directional_date_regex_file");
    }

}
