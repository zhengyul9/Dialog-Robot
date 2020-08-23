package ai.hual.labrador.dm;

import ai.hual.labrador.nlu.QueryAct;
import ai.hual.labrador.utils.DateUtils;
import ai.hual.labrador.utils.TimeUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static ai.hual.labrador.dm.java.DialogConfig.SYSTEM_TURNS_MAINTAIN_NAME;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ContextJsonDeserializerTest {

    private ObjectMapper mapper = new ObjectMapper();

    @Test
    @SuppressWarnings("unchecked")
    void deserializeContextTest() throws IOException {

        String json =
                "{\"slots\":{\"sys.query\":\"2018年2月3号下午3点半的机票多少钱，没有我就退票了\",\"订票价格查询结果\":null," +
                        "\"sys.turns\":{\"日期\":1,\"时刻\":1},\"订票日期查询结果\":null," +
                        "\"时刻\":{\"type\":\"MINUTE\",\"hour\":15,\"minute\":30,\"second\":0}," +
                        "\"sys.turn\":1,\"sys.intent\":null," +
                        "\"sys.faqAnswer\":{\"query\":null,\"score\":0,\"confidence\":0,\"standardQuestion\":null,\"answer\":null,\"hits\":[]}," +
                        "\"日期\":{\"type\":\"DAY\",\"century\":0,\"year\":2018,\"season\":0,\"month\":2,\"tendays\":0,\"week\":0,\"weekday\":0,\"day\":3}," +
                        "\"sys.hyps\":[" +
                        "{\"intent\":null,\"regex\":null,\"regexStart\":0,\"regexEnd\":0,\"regexRealStart\":0,\"regexRealEnd\":0,\"query\":\"2018年2月3号下午3点半的机票多少钱，没有我就退票了\",\"pQuery\":\"{{日期}}{{时刻}}的机票多少钱，没有我就退票了\",\"slots\":{\"日期\":[{\"matched\":{\"type\":\"DAY\",\"century\":0,\"year\":2018,\"season\":0,\"month\":2,\"tendays\":0,\"week\":0,\"weekday\":0,\"day\":3},\"type\":\"ai.hual.labrador.utils.DateUtils$Date\",\"label\":\"date(y,m,d)\",\"key\":\"日期\",\"realStart\":0,\"realEnd\":9,\"realLength\":9}],\"时刻\":[{\"matched\":{\"type\":\"MINUTE\",\"hour\":15,\"minute\":30,\"second\":0},\"type\":\"ai.hual.labrador.utils.TimeUtils$Time\",\"label\":\"timeHalfPm(h)\",\"key\":\"时刻\",\"realStart\":9,\"realEnd\":14,\"realLength\":5}]},\"score\":2.853116706110003}," +
                        "{\"intent\":null,\"regex\":null,\"regexStart\":0,\"regexEnd\":0,\"regexRealStart\":0,\"regexRealEnd\":0,\"query\":\"2018年2月3号下午3点半的机票多少钱，没有我就退票了\",\"pQuery\":\"{{日期}}3号{{时刻}}的机票多少钱，没有我就退票了\",\"slots\":{\"日期\":[{\"matched\":{\"type\":\"DAY\",\"century\":0,\"year\":2018,\"season\":0,\"month\":2,\"tendays\":0,\"week\":0,\"weekday\":0,\"day\":0},\"type\":\"ai.hual.labrador.utils.DateUtils$Date\",\"label\":\"date(y,m)\",\"key\":\"日期\",\"realStart\":0,\"realEnd\":7,\"realLength\":7}],\"时刻\":[{\"matched\":{\"type\":\"MINUTE\",\"hour\":15,\"minute\":30,\"second\":0},\"type\":\"ai.hual.labrador.utils.TimeUtils$Time\",\"label\":\"timeHalfPm(h)\",\"key\":\"时刻\",\"realStart\":9,\"realEnd\":14,\"realLength\":5}]},\"score\":2.3579476910000015}," +
                        "{\"intent\":null,\"regex\":null,\"regexStart\":0,\"regexEnd\":0,\"regexRealStart\":0,\"regexRealEnd\":0,\"query\":\"2018年2月3号下午3点半的机票多少钱，没有我就退票了\",\"pQuery\":\"{{日期}}下午{{数字}}点半的机票多少钱，没有我就退票了\",\"slots\":{\"日期\":[{\"matched\":{\"type\":\"DAY\",\"century\":0,\"year\":2018,\"season\":0,\"month\":2,\"tendays\":0,\"week\":0,\"weekday\":0,\"day\":3},\"type\":\"ai.hual.labrador.utils.DateUtils$Date\",\"label\":\"date(y,m,d)\",\"key\":\"日期\",\"realStart\":0,\"realEnd\":9,\"realLength\":9}],\"数字\":[{\"matched\":3.0,\"type\":\"java.lang.Double\",\"label\":\"getDigits(str)\",\"key\":\"数字\",\"realStart\":11,\"realEnd\":12,\"realLength\":1}]},\"score\":2.084708371801689}," +
                        "{\"intent\":null,\"regex\":null,\"regexStart\":0,\"regexEnd\":0,\"regexRealStart\":0,\"regexRealEnd\":0,\"query\":\"2018年2月3号下午3点半的机票多少钱，没有我就退票了\",\"pQuery\":\"{{日期}}2月{{日期}}{{时刻}}的机票多少钱，没有我就退票了\",\"slots\":{\"日期\":[{\"matched\":{\"type\":\"YEAR\",\"century\":0,\"year\":2018,\"season\":0,\"month\":0,\"tendays\":0,\"week\":0,\"weekday\":0,\"day\":0},\"type\":\"ai.hual.labrador.utils.DateUtils$Date\",\"label\":\"date(y)\",\"key\":\"日期\",\"realStart\":0,\"realEnd\":5,\"realLength\":5},{\"matched\":{\"type\":\"DAY\",\"century\":0,\"year\":0,\"season\":0,\"month\":0,\"tendays\":0,\"week\":0,\"weekday\":0,\"day\":3},\"type\":\"ai.hual.labrador.utils.DateUtils$Date\",\"label\":\"date(d)\",\"key\":\"日期\",\"realStart\":7,\"realEnd\":9,\"realLength\":2}],\"时刻\":[{\"matched\":{\"type\":\"MINUTE\",\"hour\":15,\"minute\":30,\"second\":0},\"type\":\"ai.hual.labrador.utils.TimeUtils$Time\",\"label\":\"timeHalfPm(h)\",\"key\":\"时刻\",\"realStart\":9,\"realEnd\":14,\"realLength\":5}]},\"score\":2.0438317370604793}," +
                        "{\"intent\":null,\"regex\":null,\"regexStart\":0,\"regexEnd\":0,\"regexRealStart\":0,\"regexRealEnd\":0,\"query\":\"2018年2月3号下午3点半的机票多少钱，没有我就退票了\",\"pQuery\":\"{{日期}}下午3点半的机票多少钱，没有我就退票了\",\"slots\":{\"日期\":[{\"matched\":{\"type\":\"DAY\",\"century\":0,\"year\":2018,\"season\":0,\"month\":2,\"tendays\":0,\"week\":0,\"weekday\":0,\"day\":3},\"type\":\"ai.hual.labrador.utils.DateUtils$Date\",\"label\":\"date(y,m,d)\",\"key\":\"日期\",\"realStart\":0,\"realEnd\":9,\"realLength\":9}]},\"score\":2.043831737060479}" +
                        "]," +
                        "\"乘客类别\":null,\"人数\":null}," +
                        "\"types\":{" +
                        "\"时刻\":\"ai.hual.labrador.utils.TimeUtils$Time\"," +
                        "\"sys.turn\":\"java.lang.Integer\"," +
                        "\"sys.turns\":\"java.util.HashMap\"," +
                        "\"sys.faqAnswer\":\"ai.hual.labrador.faq.FaqAnswer\"," +
                        "\"日期\":\"ai.hual.labrador.utils.DateUtils$Date\"" +
                        "}," +
                        "\"currentState\":{\"currentState\":\"咨询\",\"subStates\":{\"咨询\":{\"currentState\":\"问日期\",\"subStates\":{\"问日期\":{\"currentState\":null,\"subStates\":{}},\"问价格\":{\"currentState\":null,\"subStates\":{}}}},\"订票\":{\"currentState\":\"问日期\",\"subStates\":{\"问日期\":{\"currentState\":null,\"subStates\":{}},\"问价格\":{\"currentState\":null,\"subStates\":{}}}},\"退票\":{\"currentState\":\"问日期\",\"subStates\":{\"问日期\":{\"currentState\":null,\"subStates\":{}},\"问价格\":{\"currentState\":null,\"subStates\":{}}}}}}}";

        Context context = mapper.readValue(json, Context.class);

        Map<String, Object> slots = context.getSlots();
        assertEquals(12, slots.size());
        assertEquals(5, context.getTypes().size());
        List<QueryAct> hyps = (List<QueryAct>) context.slotContentByName("sys.hyps");
        assertEquals(5, hyps.size());
        assertTrue(hyps.get(0) != null);
        assertEquals("2018年2月3号下午3点半的机票多少钱，没有我就退票了", hyps.get(0).getQuery());
        assertEquals("{{日期}}{{时刻}}的机票多少钱，没有我就退票了", hyps.get(0).getPQuery());
        assertEquals(2, hyps.get(0).getSlots().values().size());
        assertEquals(1, hyps.get(4).getSlots().values().size());
        HashMap<String, Integer> turnsMap = (HashMap<String, Integer>) slots.get(SYSTEM_TURNS_MAINTAIN_NAME);
        assertEquals(2, turnsMap.keySet().size());
        assertEquals(1, (int) turnsMap.get("日期"));
        assertEquals(1, (int) turnsMap.get("时刻"));
        assertTrue(slots.get("日期") instanceof DateUtils.Date);
        assertTrue(slots.get("时刻") instanceof TimeUtils.Time);
        assertEquals("2018-02-03", slots.get("日期").toString());
        assertEquals("15:30:00", slots.get("时刻").toString());

        // serialized result will be different with json in some filed's order
        // assertEquals(json, mapper.writeValueAsString(context));
    }
}