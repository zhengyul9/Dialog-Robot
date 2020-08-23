package ai.hual.labrador.nlu.annotators;

import ai.hual.labrador.nlu.QueryAct;
import ai.hual.labrador.utils.DirectionalRegionUtils.DirectionalRegion;
import ai.hual.labrador.utils.RegionType;
import ai.hual.labrador.utils.RegionUtils.Region;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class DirectionalRegionAnnotatorTest {

    private static RegionAnnotator RA;
    private static DirectionalRegionAnnotator DRA;

    @BeforeAll
    static void setup() {
        RA = new RegionAnnotator();
        DRA = new DirectionalRegionAnnotator();
    }

    @Test
    public void fromToTest() {

        // construct inputs
        String query = "给我来张从北京到上海的票吧";
        QueryAct queryAct = new QueryAct(query);

        // output
        // use only first outcome of RegionAnnotator
        List<QueryAct> resultList = DRA.annotate(RA.annotate(queryAct).get(0));
        QueryAct result = resultList.get(0);

        assertEquals(4, resultList.size());
        assertEquals("给我来张{{起始地}}{{到达地}}的票吧", result.getPQuery());
        assertEquals(1, result.getSlots().get("起始地").size());
        DirectionalRegion start = new DirectionalRegion(
                new Region(RegionType.CITY, "北京"), null);
        DirectionalRegion end = new DirectionalRegion(null,
                new Region(RegionType.CITY, "上海"));

        assertEquals(start.toString(), result.getSlots().get("起始地").get(0).matched.toString());
        assertEquals(4, result.getSlots().get("起始地").get(0).start);
        assertEquals(11, result.getSlots().get("起始地").get(0).end);
        assertEquals(4, result.getSlots().get("起始地").get(0).realStart);
        assertEquals(7, result.getSlots().get("起始地").get(0).realEnd);

        assertEquals(end.toString(), result.getSlots().get("到达地").get(0).matched.toString());
        assertEquals(11, result.getSlots().get("到达地").get(0).start);
        assertEquals(18, result.getSlots().get("到达地").get(0).end);
        assertEquals(7, result.getSlots().get("到达地").get(0).realStart);
        assertEquals(10, result.getSlots().get("到达地").get(0).realEnd);
    }
}