package ai.hual.labrador.nlu.annotators;

import ai.hual.labrador.utils.DirectionalRegionUtils;
import ai.hual.labrador.utils.RegionUtils.Region;
import org.springframework.stereotype.Component;

/**
 * Implementation of the <tt>RegexAnnotator</tt> abstract class.
 * This annotator can annotate <Strong>directional region</Strong>
 * in queryAct, before witch, the queryAct should already be annotated
 * with <tt>RegionAnnotator</tt>.
 *
 * <p>During initialization, it will construct a map from function's name
 * to an lambda expression. Then, read the number regex file into memory.
 *
 * <p>Annotated data structure is {@link DirectionalRegionUtils.DirectionalRegion},
 * where one of the field, <tt>start</tt> and <tt>end</tt> is not null.
 *
 * <p>
 * This is an example of the annotator's usage:
 * <pre>
 *     act.query = 从北京到上海的机票
 *     act.pQuery = 从{{市}}到{{市}}的机票
 *     act.slots = { 市: [Region(type: RegionType.CITY, name: "北京"),
 *                      Region(type: RegionType:CITY, name: "上海"] }
 *     -> DirectionalRegionAnnotator.annotate(act) ->
 *     act.pQuery == {{起始地}}{{到达地}}
 *     act.slots == { 起始地: [DirectionalRegionUtils.DirectionalRegion(
 *                              start: Region(type: RegionType.CITY, name: "北京")),
 *                  到达地: [DirectionalRegionUtils.DirectionalRegion(
 *                              end: Region(type: RegionType.CITY, name: "上海")),
 * </pre>
 *
 * @author Yuqi
 * @see RegexAnnotator
 * @since 1.8
 */
@Component("directionalRegionAnnotator")
public class DirectionalRegionAnnotator extends RegexAnnotator {

    public DirectionalRegionAnnotator() {

        /* construct map from label to lambda function */
        labelMap.put("from(region)",
                valuePack -> DirectionalRegionUtils.from((Region) valuePack.slotValues.get(0).matched));
        labelMap.put("to(region)",
                valuePack -> DirectionalRegionUtils.to((Region) valuePack.slotValues.get(0).matched));

        /* get regex list */
        this.regexList = fetchRegexFile("directional_region_regex_file");
    }
}
